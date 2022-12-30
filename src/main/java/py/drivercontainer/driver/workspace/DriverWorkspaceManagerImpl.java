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

package py.drivercontainer.driver.workspace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.PyService;
import py.common.Utils;
import py.coordinator.lio.LioCommandManager;
import py.driver.DriverType;
import py.drivercontainer.driver.version.Version;
import py.drivercontainer.driver.version.file.VersionImpl;
import py.icshare.DriverKey;
import py.instance.InstanceId;

/**
 * An implementation for {@link DriverWorkspaceManager}.
 *
 */
public class DriverWorkspaceManagerImpl implements DriverWorkspaceManager {

  public static final String LINK_SRC_CONFIG_NAME = "config";
  public static final String LINK_SRC_LIB_NAME = "lib";
  private static final Logger logger = LoggerFactory.getLogger(DriverWorkspaceManagerImpl.class);
  /**
   * parent directory of all driver workspaces, usually is "[user.dir]/var".
   */
  private final String rootDirPath;

  /**
   * root path for various version of driver server library, usually is "_packages/".
   */
  private final String libraryRootPath;

  /**
   * instance id for service driver-container.
   */
  private final InstanceId driverContainerId;

  /**
   * Constructor.
   *
   * @param driverContainerId instance id for service driver-container.
   * @param rootDirPath       parent directory of all driver workspaces, usually is
   *                          "[user.dir]/var".
   * @param libraryRootPath   root path for various version of driver server library, usually is
   *                          "_packages/".
   */
  public DriverWorkspaceManagerImpl(InstanceId driverContainerId, String rootDirPath,
      String libraryRootPath) {
    super();
    this.driverContainerId = driverContainerId;
    this.rootDirPath = rootDirPath;
    this.libraryRootPath = libraryRootPath;
  }

  @Override
  public DriverWorkspace getWorkspace(Version version, DriverKey key) {
    DriverWorkspaceImpl workspace;

    workspace = new DriverWorkspaceImpl(rootDirPath, version, key);

    return workspace;
  }

  @Override
  public DriverWorkspace createWorkspace(Version version, DriverKey key) throws IOException {
    DriverWorkspace workspace;
    File workspaceDir;
    workspace = getWorkspace(version, key);
    workspaceDir = workspace.getDir();
    if (!workspaceDir.exists() && !workspaceDir.mkdirs()) {
      String errMsg = "Unable to create workspace " + workspace.getPath();
      logger.error("{}", errMsg);
      throw new IOException(errMsg);
    }

    PyService driverService;
    switch (key.getDriverType()) {
      case ISCSI:
      case NBD:
        driverService = PyService.COORDINATOR;
        break;
      default:
        throw new IllegalStateException(
            "Unable to handle driver with type " + key.getDriverType().name());
    }

    File libraryRoot;
    File libraryDir = null;
    libraryRoot = new File(libraryRootPath);
    if (!libraryRoot.exists()) {
      String errMsg = "No such directory " + libraryRootPath;
      logger.error("{}", errMsg);
      throw new IOException(errMsg);
    }
    if (!libraryRoot.isDirectory()) {
      String errMsg = "Not a directory: " + libraryRootPath;
      logger.error("{}", errMsg);
      throw new IOException(errMsg);
    }
    File[] fileArray = libraryRoot.listFiles();
    if (fileArray != null) {
      for (File libraryItem : fileArray) {
        logger.debug("Checking server package item {} ...", libraryItem);
        logger.warn("libraryItemName:{}, driverServiceProjectName:{}, versionAppendix:{}",
            libraryItem.getName(), driverService.getServiceProjectKeyName(),
            version.formatInstallationAppendix());
        boolean containServiceProjectName = libraryItem.getName()
            .contains(driverService.getServiceProjectKeyName());
        boolean containTimestamp = libraryItem.getName()
            .contains(version.formatInstallationAppendix());
        logger.warn("containServiceProjectName:{}, containTimestamp:{}", containServiceProjectName,
            containTimestamp);
        if (containServiceProjectName && containTimestamp) {
          libraryDir = libraryItem;
          break;
        }
      }
    }
    if (libraryDir == null) {
      String errMsg = "No such driver " + key + " with version " + version;
      logger.error("{}", errMsg);
      throw new IOException(errMsg);
    }

    File libLinkDest;
    File libLinkSrc;
    libLinkDest = new File(workspaceDir, LINK_SRC_LIB_NAME);
    logger.warn("iscsi dir is :{}", libLinkDest.toString());
    libLinkSrc = new File(libraryDir, LINK_SRC_LIB_NAME);
    try {
      if (libLinkDest.exists()) {
        Files.delete(Paths.get(libLinkDest.getPath()));
      }
      Files.createSymbolicLink(Paths.get(libLinkDest.getPath()), Paths.get(libLinkSrc.getPath()));
    } catch (IOException e) {
      logger.error("Failed to link {} to {}", libLinkSrc, libLinkDest);
      throw e;
    }

    File configLinkDest;
    configLinkDest = new File(workspaceDir, LINK_SRC_CONFIG_NAME);
    File configLinkSrc;
    configLinkSrc = new File(libraryDir, LINK_SRC_CONFIG_NAME);
    try {
      if (configLinkDest.exists()) {
        Files.delete(Paths.get(configLinkDest.getPath()));
      }
      Files.createSymbolicLink(Paths.get(configLinkDest.getPath()),
          Paths.get(configLinkSrc.getPath()));
    } catch (IOException e) {
      logger.error("Failed to link {} to {}", configLinkSrc, configLinkDest);
      throw e;
    }

    /* send configLinkDest to LioCommandManager.java for setting io.depth only for iscsi */
    if (key.getDriverType().equals(DriverType.ISCSI)) {
      LioCommandManager.setDefaultCmdsnDepthPath(configLinkDest.toString());
    }
    return workspace;
  }



  /**
   * xx.
   */
  public DriverWorkspace deleteWorkspace(Version version, DriverKey key) throws IOException {
    DriverWorkspace workspace;

    workspace = getWorkspace(version, key);
    Utils.deleteFileOrDirectory(workspace.getDir());

    return workspace;
  }

  @Override
  public List<DriverWorkspace> listWorkspaces() throws IOException {
    final File levelSeparator = null;

    File file;
    int curDepth = 0;
    Queue<File> fileQueue = new LinkedList<File>();
    List<DriverWorkspace> workspaces = new ArrayList<>();

    fileQueue.offer(new File(rootDirPath));
    fileQueue.offer(levelSeparator);

    while (fileQueue.size() > 0) {
      file = fileQueue.poll();
      if (file == levelSeparator) {
        if (curDepth == DriverWorkspaceImpl.DRIVER_WORKSPACE_DEPTH - 1) {
          // complete finding all workspace
          break;
        } else {
          ++curDepth;
          fileQueue.offer(levelSeparator);
          continue;
        }
      }

      File[] fileArray = file.listFiles();
      if (fileArray != null) {
        for (File subEntry : fileArray) {
          if (curDepth == 0) {
            Version version;
            version = VersionImpl.get(subEntry.getName());
            if (version == null) {
              // not a workspace
              continue;
            }
          }

          if (subEntry.isDirectory()) {
            fileQueue.offer(subEntry);
          }
        }
      }
    }

    for (File workspaceDir : fileQueue) {
      if (workspaceDir == levelSeparator) {
        break;
      }

      DriverWorkspaceImpl workspace;

      workspace = new DriverWorkspaceImpl(driverContainerId, workspaceDir.getPath());
      workspaces.add(workspace);
    }

    return workspaces;
  }

  /**
   * Link all entries under the given target directory to the given link directory.
   *
   * @param linkDir   directory having all link(destination) entries
   * @param targetDir directory having all target(source) entries
   * @throws IOException if something wrong when linking entries
   */
  void linkDirectory(File linkDir, File targetDir) throws IOException {
    if (!linkDir.isDirectory()) {
      throw new IllegalArgumentException("No a directory: " + linkDir.getPath());
    }
    if (!targetDir.isDirectory()) {
      throw new IllegalArgumentException("No a directory: " + targetDir.getPath());
    }

    logger.debug("Linking sub-entries under {} to directory {} ...", targetDir, linkDir);

    String[] strArray = targetDir.list();
    if (strArray != null) {
      for (String itemUnderTarget : strArray) {
        Path linkPath;
        Path targetPath;

        linkPath = Paths.get(linkDir.getPath(), itemUnderTarget);
        targetPath = Paths.get(targetDir.getPath(), itemUnderTarget);

        try {
          Files.createSymbolicLink(linkPath, targetPath);
        } catch (IOException e) {
          logger.error("Unable to link entry {} to path {}.", targetPath, linkPath);
          throw e;
        }
      }
    }
  }
}
