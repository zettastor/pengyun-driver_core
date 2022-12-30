/*
 * Copyright (c) 2022. PengYunNetWork
 *
 * This program is free software: you can use, redistribute, and/or modify it
 * under the terms of the GNU Affero General Public License, version 3 or later ("AGPL"),
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  You should have received a copy of the GNU Affero General Public License along with
 *  this program. If not, see <http://www.gnu.org/licenses/>.
 */

package py.drivercontainer.driver.version.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.driver.DriverType;
import py.drivercontainer.driver.version.Version;
import py.drivercontainer.driver.version.VersionException;
import py.drivercontainer.driver.version.VersionManager;

/**
 * This class is an implementation of {@link VersionManager} based on version and migration files.
 *
 */
public class VersionManagerImpl implements VersionManager {

  public static final String CURRENT_VERSION_FILE_POSTFIX = "_cur_ver";

  public static final String LATEST_VERSION_FILE_POSTFIX = "_latest_ver";

  public static final String MIGRATION_FILE_POSTFIX = "_migrat_ver";

  public static final String BAK_FILE_POSTFIX = "_bak";

  private static final Logger logger = LoggerFactory.getLogger(VersionManagerImpl.class);
  private final String versionDir;
  /**
   * This table manages version lock of some specific driver in one process which is different from
   * file lock table used for synchronization between two processes.
   *
   * <p>Acquire file lock multiple times in one process causes exception being thrown out. To
   * prevent it, acquire in process lock first and then file lock.
   */
  private final Map<DriverType, Lock> inProccessLockTable = new ConcurrentHashMap<>();
  private final Map<DriverType, Pair<FileChannel, FileLock>> versionLockTable = new HashMap<>();
  private final VersionLineParser versionLineParser = new VersionLineParser();
  private final MigrationFlagLineParser migrationFlagLineParser = new MigrationFlagLineParser();

  /**
   * In constructor, it checks whether version files exist and create it if doesn't exist.
   *
   * @param versionDir version files' root directory
   * @throws IOException if create version files fails
   */
  public VersionManagerImpl(String versionDir) throws IOException {

    this.versionDir = versionDir;

    for (DriverType driverType : DriverType.values()) {
      if (driverType == DriverType.ISCSI) {
        continue;
      }

      inProccessLockTable.put(driverType, new ReentrantLock());

      String currentVersionFileName = getCurVersionFileName(driverType);
      String latestVersionFileName = getLatestVersionFileName(driverType);

      File currentVersionFile = new File(versionDir, currentVersionFileName);
      File latestVersionFile = new File(versionDir, latestVersionFileName);

      if (!currentVersionFile.getParentFile().exists()) {
        currentVersionFile.getParentFile().mkdirs();
      }
      if (!currentVersionFile.exists()) {
        currentVersionFile.createNewFile();
      }

      if (!latestVersionFile.exists()) {
        latestVersionFile.createNewFile();
      }

      String migrationFileName = getMigrationFileName(driverType);
      File migrationFile = new File(versionDir, migrationFileName);
      if (!migrationFile.exists()) {
        migrationFile.createNewFile();
      }
    }

    Runtime.getRuntime().addShutdownHook(new JvmShutdownHook(this));
  }

  @Override
  @SuppressWarnings("resource")
  public void lockVersion(DriverType driverType) throws VersionException {
    if (driverType == DriverType.ISCSI) {
      driverType = DriverType.NBD;
    }
    inProccessLockTable.get(driverType).lock();
    final String latestVersionFileName = getLatestVersionFileName(driverType);

    File latestVersionFile;
    FileChannel fileChannel;
    FileLock fileLock;
    latestVersionFile = new File(versionDir, latestVersionFileName);
    try {
      fileChannel = new RandomAccessFile(latestVersionFile, "rw").getChannel();
    } catch (IOException e) {
      logger.error("Something wrong when opening latest version file {}", latestVersionFile, e);
      throw new VersionException(e);
    }
    try {
      fileLock = fileChannel.lock();
    } catch (IOException e) {
      logger.error("Something wrong when locking latest version file {}", latestVersionFile, e);
      try {
        fileChannel.close();
      } catch (IOException closeExp) {
        logger.warn("Something wrong when closing file {}", latestVersionFile, e);
      }
      throw new VersionException(e);
    }
    Pair<FileChannel, FileLock> fileLockPair;

    fileLockPair = new ImmutablePair<FileChannel, FileLock>(fileChannel, fileLock);
    versionLockTable.put(driverType, fileLockPair);
  }

  @Override
  public void unlockVersion(DriverType driverType) throws NoSuchElementException, VersionException {
    if (driverType == DriverType.ISCSI) {
      driverType = DriverType.NBD;
    }

    Pair<FileChannel, FileLock> fileLockPair;

    fileLockPair = versionLockTable.get(driverType);
    if (fileLockPair == null) {
      logger.error("No such version lock for driver {}", driverType.name());
      throw new NoSuchElementException();
    }

    try {
      fileLockPair.getRight().release();
    } catch (IOException e) {
      logger.warn("Something wrong when releasing version lock for driver {}.", driverType.name());
      throw new VersionException(e);
    }

    try {
      fileLockPair.getLeft().close();
    } catch (IOException e) {
      logger.warn("Something wrong when closing version file for driver {}.", driverType.name());
    }

    versionLockTable.remove(driverType);

    inProccessLockTable.get(driverType).unlock();
  }

  @Override
  public Version getCurrentVersion(DriverType driverType) throws VersionException {
    final String currentVersionFileName = getCurVersionFileName(driverType);
    final String currentVersionBakFileName = getCurVersionBakFileName(driverType);

    return this
        .<Version>getContent(currentVersionFileName, currentVersionBakFileName, versionLineParser);
  }

  @Override
  public Version setCurrentVersion(DriverType driverType, Version version) throws VersionException {
    final String currentVersionFileName = getCurVersionFileName(driverType);
    final String currentVersionBakFileName = getCurVersionBakFileName(driverType);

    return this.<Version>setContent(currentVersionFileName, currentVersionBakFileName, version,
        versionLineParser);
  }

  @Override
  public Version getLatestVersion(DriverType driverType) throws VersionException {
    final String latestVersionFileName = getLatestVersionFileName(driverType);
    final String latestVersionBakFileName = getLatestVersionBakFileName(driverType);

    return this
        .<Version>getContent(latestVersionFileName, latestVersionBakFileName, versionLineParser);
  }

  @Override
  public Version setLatestVersion(DriverType driverType, Version version) throws VersionException {
    final String latestVersionFileName = getLatestVersionFileName(driverType);
    final String latestVersionBakFileName = getLatestVersionBakFileName(driverType);

    return this.<Version>setContent(latestVersionFileName, latestVersionBakFileName, version,
        versionLineParser);
  }

  @Override
  public boolean isOnMigration(DriverType driverType) throws VersionException {
    final String migrationFileName = getMigrationFileName(driverType);
    final String migrationBakFileName = getMigrationBakFileName(driverType);

    Integer migrationFlag;

    migrationFlag = this
        .<Integer>getContent(migrationFileName, migrationBakFileName, migrationFlagLineParser);
    return migrationFlag != null && migrationFlag == 1;
  }

  @Override
  public void setOnMigration(DriverType driverType, boolean isOnMigration) throws VersionException {
    final String migrationFileName = getMigrationFileName(driverType);
    final String migrationBakFileName = getMigrationBakFileName(driverType);

    this.<Integer>setContent(migrationFileName, migrationBakFileName, isOnMigration ? 1 : 0,
        migrationFlagLineParser);
  }

  /**
   * Get content from file with the given file name. If no content got from it, then get content
   * from file with the given backup file name.
   *
   * @param fileName    name of file
   * @param bakFileName name of backup file
   * @param lineParser  parser which can parse a line from file to the given type of value
   */
  <T> T getContent(String fileName, String bakFileName, FileLineParser<T> lineParser)
      throws VersionException {
    T content;

    try {
      content = this.<T>getAndCopyContent(fileName, null, lineParser, true);
    } catch (IOException e) {
      throw new VersionException(e);
    }

    if (content == null) {
      try {
        // Get content from backup file and restore content in it to source file. It is possible
        // that reads from
        // backup file successfully but fails to restore content. In that case, content can also
        // be got.
        content = this.<T>getAndCopyContent(fileName, bakFileName, lineParser, true);
      } catch (IOException e) {
        throw new VersionException(e);
      }
    }

    return content;
  }

  /**
   * Set the given content to file with the given file name. To prevent data corruption on
   * persistence to file, it is necessary to backup data to file with the given backup file name.
   *
   * @param fileName    name of file
   * @param bakFileName name of backup file
   * @param content     content to be set to file
   * @param lineParser  parser which can parse a line from file to the given type of value
   */
  <T> T setContent(String fileName, String bakFileName, T content, FileLineParser<T> lineParser)
      throws VersionException {
    T oldContent;

    try {
      oldContent = this.<T>getAndCopyContent(bakFileName, fileName, lineParser, false);
    } catch (IOException e) {
      throw new VersionException(e);
    }

    File file;

    file = new File(versionDir, fileName);

    BufferedWriter bw = null;
    String versionStr;

    versionStr = lineParser.fomat(content);
    try {
      bw = new BufferedWriter(new FileWriter(file));
      bw.write(versionStr);
      bw.flush();
    } catch (IOException e) {
      logger.error("Something wrong when persisting version to file {}", file, e);
      throw new VersionException(e);
    } finally {
      if (bw != null) {
        try {
          bw.close();
        } catch (IOException e) {
          logger.warn("Something wrong when closing file {}", file);
        }
      }
    }

    return oldContent;

  }

  /**
   * If the backup file name is not given, then get content from file with the given file name.
   * Otherwise, get content from file with the given backup file name and copy its content to file
   * with the given file name.
   *
   * @param fileName      name of file
   * @param bakFileName   name of backup file
   * @param lineParser    parser which can parse a line from file to the given type of value
   * @param ignoreCopyErr a flag to tell this method whether to ignore error present on copy
   *                      procedure
   * @return the given type of value if exist content in file and its format is valid, or null.
   * @throws IOException something wrong when accessing the given files
   */
  <T> T getAndCopyContent(String fileName, String bakFileName, FileLineParser<T> lineParser,
      boolean ignoreCopyErr)
      throws IOException {
    File file;
    File bakFile;
    File readFile;
    File writeFile = null;
    T content;
    BufferedReader br = null;
    BufferedWriter bw = null;
    String line;

    file = new File(versionDir, fileName);

    if (bakFileName == null) {
      readFile = file;
    } else { // read from backup file and copy its content to file
      bakFile = new File(versionDir, bakFileName);
      if (!bakFile.exists()) {
        return null;
      }
      readFile = bakFile;
      writeFile = file;
    }

    try {
      br = new BufferedReader(new FileReader(readFile));

      line = br.readLine();
      if (line == null) {
        //logger.info("Version file {} is empty", readFile);
        return null;
      }

      content = lineParser.parse(line);
    } catch (IOException e) {
      logger.error("Something wrong when accessing version file {}", readFile, e);
      throw e;
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          logger.warn("Something wrong when closing version file {}", readFile, e);
        }
        br = null;
      }
    }

    if (writeFile != null) {
      try {
        bw = new BufferedWriter(new FileWriter(writeFile));
        bw.write(line);
        bw.flush();
      } catch (IOException e) {
        logger.error("Something wrong when restoring version file {}", writeFile, e);
        if (!ignoreCopyErr) {
          throw e;
        }
      } finally {
        if (bw != null) {
          try {
            bw.close();
          } catch (IOException e) {
            logger.warn("Something wrong when closing file {}", writeFile, e);
          }
        }
      }
    }

    return content;
  }

  String getCurVersionFileName(DriverType driverType) {
    return driverType.name().toLowerCase() + CURRENT_VERSION_FILE_POSTFIX;
  }

  String getCurVersionBakFileName(DriverType driverType) {
    return getCurVersionFileName(driverType) + BAK_FILE_POSTFIX;
  }

  String getLatestVersionFileName(DriverType driverType) {
    return driverType.name().toLowerCase() + LATEST_VERSION_FILE_POSTFIX;
  }

  String getLatestVersionBakFileName(DriverType driverType) {
    return getLatestVersionFileName(driverType) + BAK_FILE_POSTFIX;
  }

  String getMigrationFileName(DriverType driverType) {
    return driverType.name().toLowerCase() + MIGRATION_FILE_POSTFIX;
  }

  String getMigrationBakFileName(DriverType driverType) {
    return getMigrationFileName(driverType) + BAK_FILE_POSTFIX;
  }

  private static class JvmShutdownHook extends Thread {

    private final VersionManagerImpl versionManager;

    public JvmShutdownHook(VersionManagerImpl versionManager) {
      this.versionManager = versionManager;
    }

    @Override
    public void run() {
      logger.warn("JVM is shutting down, let's release file locks being held by version-manager.");

      for (DriverType driverType : versionManager.versionLockTable.keySet()) {
        Pair<FileChannel, FileLock> fileLockPair;

        fileLockPair = versionManager.versionLockTable.get(driverType);
        try {
          fileLockPair.getRight().release();
        } catch (IOException e) {
          logger.warn("Something wrong when releasing version lock for driver {} on JVM shutdown.",
              driverType.name());
        }

        try {
          fileLockPair.getLeft().close();
        } catch (IOException e) {
          logger.warn("Something wrong when closing open file for driver {} on JVM shutdown.",
              driverType.name());
        }
      }
    }
  }

  private static class VersionLineParser implements FileLineParser<Version> {

    @Override
    public Version parse(String line) {
      return VersionImpl.get(line.trim());
    }

    @Override
    public String fomat(Version content) {
      return content.format();
    }
  }

  private static class MigrationFlagLineParser implements FileLineParser<Integer> {

    @Override
    public Integer parse(String line) {
      return Integer.valueOf(line.trim());
    }

    @Override
    public String fomat(Integer content) {
      return content.toString();
    }

  }
}
