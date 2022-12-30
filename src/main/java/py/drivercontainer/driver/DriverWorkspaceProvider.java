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

package py.drivercontainer.driver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.PyService;
import py.driver.DriverType;
import py.icshare.DriverKey;

/**
 * create or delete workingspace for driver.
 *
 */
public class DriverWorkspaceProvider {

  public static final String FILE_SEPARATOR = File.separator;
  private static final Logger logger = LoggerFactory.getLogger(DriverWorkspaceProvider.class);
  private static final String CONFIG = "config"; //FILE_SEPARATOR

  private static final String LIB = "lib";


  /**
   * xx.
   */
  public static String getWorkspace(String rootPath, DriverKey driverKey) {

    while (rootPath.endsWith(File.separator)) {
      rootPath = rootPath.substring(0, rootPath.length() - 1);
    }

    List<String> pathEntries;

    pathEntries = new ArrayList<String>();
    pathEntries.add(rootPath);
    pathEntries.add(Long.toString(driverKey.getVolumeId()));
    pathEntries.add(Integer.toString(driverKey.getSnapshotId()));
    pathEntries.add(driverKey.getDriverType().name());

    StringBuilder workspaceBuilder;

    workspaceBuilder = new StringBuilder();
    for (String pathEntry : pathEntries) {
      workspaceBuilder.append(pathEntry);
      workspaceBuilder.append(File.separator);
    }

    return workspaceBuilder.toString();
  }


  /**
   * xx.
   */
  public String createWorkspace(String rootPath, DriverKey driverKey) {
    String workingPath = getWorkspace(rootPath, driverKey);
    File workdir = new File(workingPath);
    if (!workdir.exists()) {
      try {
        workdir.mkdirs();
      } catch (Exception e) {
        logger.error("fail to mkdirs {}, exception {}", workingPath, e);
      }
    }
    return workingPath;
  }


  /**
   * xx.
   */
  public void linkServerConfigandLib(DriverType driverType, String workingPath)
      throws IOException, InterruptedException {
    String serverPath = getServerPath(driverType);
    String configPath = serverPath + FILE_SEPARATOR + CONFIG;
    String libPath = serverPath + FILE_SEPARATOR + LIB;
    createFileLink(configPath, workingPath + CONFIG);
    createFileLink(libPath, workingPath + LIB);
  }


  /**
   * xx.
   */
  public void deleteWorkspace(String path) {
    if (path == null) {
      return;
    }
    String command = String.format("rm -rf %s ", path);
    Process process;
    try {
      process = Runtime.getRuntime().exec(command);
      process.waitFor();
    } catch (IOException e) {
      logger.error("caught exception", e);
    } catch (InterruptedException e) {
      logger.error("caught exception", e);
    }
    if (Paths.get(path) != null) {
      Path storeTypePath = Paths.get(path).getParent();
      if (storeTypePath != null && storeTypePath.toFile() != null) {
        File[] fileStoreTypePath = storeTypePath.toFile().listFiles();
        if (fileStoreTypePath == null || fileStoreTypePath.length == 0) {
          try {
            storeTypePath.toFile().delete();
          } catch (Exception e) {
            logger
                .error("fail to delete file {} exception {}", storeTypePath.toFile().toString(), e);
          }
        }
      }
      if (storeTypePath == null) {
        logger.error("parent path is null.");
      }
    } else {
      logger.error("path is null on current path.");
    }
    if (Paths.get(path) != null) {
      Path storeTypePath = Paths.get(path).getParent();
      if (storeTypePath != null) {
        Path storeTypeParentPath = storeTypePath.getParent();
        if (storeTypeParentPath != null) {
          File[] fileStoreVolumeIdPath = storeTypeParentPath.toFile().listFiles();
          if (fileStoreVolumeIdPath == null || fileStoreVolumeIdPath.length == 0) {
            try {
              storeTypeParentPath.toFile().delete();
            } catch (Exception e) {
              logger.error("fail to delete file {} exception {}",
                  storeTypeParentPath.toFile().toString(), e);
            }
          }
        } else {
          logger.error("parent parent path is null for current path.");
        }
      } else {
        logger.error("parent path is null.");
      }
    } else {
      logger.error("path is null on current path.");
    }
  }


  /**
   * xx.
   */
  public String getServerPath(DriverType driverType) throws IOException {
    PyService serverName = PyService.COORDINATOR;
    File dir = new File(".");
    String serverPath = dir.getCanonicalPath();
    serverPath = serverPath.substring(0, serverPath.lastIndexOf(FILE_SEPARATOR) + 1);
    serverPath += serverName.getServiceProjectKeyName();
    return serverPath;
  }


  /**
   * xx.
   */
  public void createFileLink(String srcPath, String destPath)
      throws InterruptedException, IOException {
    String deleteCommand = String.format("rm -f %s", destPath);
    Process deleteProcess = Runtime.getRuntime().exec(deleteCommand);
    deleteProcess.waitFor();
    String createCommand = String.format("ln -s %s %s", srcPath, destPath);
    Process creatProcess = Runtime.getRuntime().exec(createCommand);
    creatProcess.waitFor();

  }

}
