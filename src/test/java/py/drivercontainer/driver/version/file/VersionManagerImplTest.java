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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.driver.DriverType;
import py.drivercontainer.driver.version.Version;
import py.test.TestBase;

/**
 * A class contains some tests for {@link VersionManagerImpl}.
 *
 */
public class VersionManagerImplTest extends TestBase {

  private static final Logger logger = LoggerFactory.getLogger(VersionManagerImplTest.class);

  private String versionDirPath = "/tmp/ver_dir";
  private VersionManagerImpl versionManager;

  @Override
  public void init() throws Exception {
    super.init();

    cleanup();

    File versionDir = new File(versionDirPath);
    if (!versionDir.exists()) {
      versionDir.mkdirs();
    }

    versionManager = new VersionManagerImpl(versionDirPath);
  }

  @Test
  public void testSetAndGetCurrentVersion() throws Exception {
    final int times = 10;

    for (int i = 0; i < times; i++) {
      Version expectedVersion = VersionImpl
          .get("2.3.0-internal-201709081731" + String.format("%02d", (i % 60)));
      Version realVersion;

      versionManager.setCurrentVersion(DriverType.NBD, expectedVersion);

      realVersion = versionManager.getCurrentVersion(DriverType.NBD);

      Assert.assertEquals(expectedVersion, realVersion);
    }
  }

  /**
   * In this test plan, it tries to get current version of some specified driver in case that
   * current version file is broken. It is supposed that a proper current version could be got due
   * to existing a backup current version file.
   */
  @Test
  public void testGetBrokenCurrentVersion() throws Exception {
    final int times = 10;

    for (int i = 0; i < times; i++) {
      Version expectedVersion = VersionImpl
          .get("2.3.0-internal-201709081731" + String.format("%02d", (i % 60)));
      final Version realVersion;
      File currentVersionFile;

      versionManager.setCurrentVersion(DriverType.NBD, expectedVersion);
      versionManager.setCurrentVersion(DriverType.NBD, expectedVersion);

      currentVersionFile = new File(versionDirPath,
          versionManager.getCurVersionFileName(DriverType.NBD));
      breakFile(currentVersionFile);

      realVersion = versionManager.getCurrentVersion(DriverType.NBD);

      Assert.assertEquals(expectedVersion, realVersion);
    }
  }

  /**
   * In this test plan, it tries to get current version of some specified driver in case that
   * current version file is empty. It is supposed that null current version will be got.
   */
  @Test
  public void testGetNoCurrentVersion() throws Exception {
    Assert.assertNull(versionManager.getCurrentVersion(DriverType.NBD));
  }

  @Test
  public void testSetAndGetLatestVersion() throws Exception {
    final int times = 10;

    for (int i = 0; i < times; i++) {
      Version expectedVersion = VersionImpl
          .get("2.3.0-internal-201709081731" + String.format("%02d", (i % 60)));
      Version realVersion;

      versionManager.setLatestVersion(DriverType.NBD, expectedVersion);

      realVersion = versionManager.getLatestVersion(DriverType.NBD);

      Assert.assertEquals(expectedVersion, realVersion);
    }
  }

  @Test
  public void testSetAndCheckMigrationFlag() throws Exception {
    final int times = 10;

    for (int i = 0; i < times; i++) {
      boolean isOnMigration = (i % 2 == 0);
      versionManager.setOnMigration(DriverType.NBD, isOnMigration);
      if (isOnMigration) {
        Assert.assertTrue(versionManager.isOnMigration(DriverType.NBD));
      } else {
        Assert.assertFalse(versionManager.isOnMigration(DriverType.NBD));
      }
    }
  }

  @Test
  public void testLockAndUnlockVersion() throws Exception {
    try {
      versionManager.lockVersion(DriverType.NBD);
      versionManager.lockVersion(DriverType.FSD);
      versionManager.unlockVersion(DriverType.FSD);
      versionManager.unlockVersion(DriverType.NBD);
    } catch (Exception e) {
      logger.error("Caught an exception", e);
      Assert.fail();
    }
  }

  @Test
  public void testLockMultipleTimes() throws Exception {
    versionManager.lockVersion(DriverType.NBD);
    try {
      versionManager.lockVersion(DriverType.NBD);
      Assert.fail();
    } catch (IllegalStateException e) {
      logger.error("caught exception", e);
    }
    try {
      versionManager.lockVersion(DriverType.ISCSI);
      Assert.fail();
    } catch (IllegalStateException e) {
      logger.error("caught exception", e);
    }

    versionManager.unlockVersion(DriverType.NBD);
  }

  @Test
  public void testUnlockNoneLockedVersion() throws Exception {
    try {
      versionManager.unlockVersion(DriverType.NBD);
      Assert.fail();
    } catch (NoSuchElementException e) {
      logger.error("caught exception", e);
    }

    try {
      versionManager.unlockVersion(DriverType.FSD);
      Assert.fail();
    } catch (NoSuchElementException e) {
      logger.error("caught exception", e);
    }
  }


  /**
   * xx.
   */
  @After
  public void cleanup() throws Exception {
    File versionDir = new File(versionDirPath);
    if (versionDir.exists()) {
      for (File versionFile : versionDir.listFiles()) {
        versionFile.delete();
      }

      versionDir.delete();
    }
  }

  private void breakFile(File file) throws IOException, InterruptedException {
    FileOutputStream fos = new FileOutputStream(file, false);
    fos.write("".getBytes());
    fos.flush();
    fos.getFD().sync();
    fos.close();
  }
}
