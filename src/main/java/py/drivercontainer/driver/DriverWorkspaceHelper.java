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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.driver.DriverMetadata;
import py.driver.DriverType;
import py.icshare.DriverKey;


/**
 * xx.
 */
public class DriverWorkspaceHelper {

  private static final Logger logger = LoggerFactory.getLogger(DriverWorkspaceHelper.class);


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
  public static DriverKey getDriverKey(long driverContainerId, String rootPath, String workspace) {
    if (workspace.startsWith(rootPath)) {
      workspace = workspace.substring(rootPath.length());
    }

    List<String> driverKeyElements;
    String[] splits;
    String separator = (File.separatorChar == '\\' ? "\\\\" : File.separator);
    splits = workspace.split(separator);
    driverKeyElements = new ArrayList<>();
    for (String split : splits) {
      if (!split.isEmpty()) {
        driverKeyElements.add(split);
      }
    }

    long volumeId;
    int snapshotId;
    DriverType driverType;

    volumeId = Long.parseLong(driverKeyElements.get(0));
    snapshotId = Integer.parseInt(driverKeyElements.get(1));
    driverType = DriverType.valueOf(driverKeyElements.get(2));

    return new DriverKey(driverContainerId, volumeId, snapshotId, driverType);
  }


  /**
   * xx.
   */
  public static List<DriverMetadata> scanDrivers(String rootPath) {
    File rootDir;

    rootDir = new File(rootPath);
    if (!rootDir.exists()) {
      throw new IllegalArgumentException("No such path " + rootPath);
    }
    if (!rootDir.isDirectory()) {
      throw new IllegalArgumentException("Not a directory " + rootPath);
    }

    List<DriverMetadata> drivers = new ArrayList<>();
    Queue<File> directories = new LinkedList<>();
    // To prevent dead loop(soft link to current directory), limit searching depth with workspace
    // depth of driver.
    int depthRemaining = DriverConstants.DRIVER_WORKSPACE_DEPTH;

    /*
     * Use 'breadth first search' algorithm to find out all driver meta-data file and parse driver
     * meta-data from
     * it.
     */
    directories.offer(rootDir);
    // Use 'null' as separator of two levels in directories tree.
    directories.offer(null);
    while (directories.size() > 0 && depthRemaining > 0) {
      File directory;

      directory = directories.poll();
      if (directory == null) {
        --depthRemaining;
        directories.offer(null);
        continue;
      }

      File[] subEntries = directory.listFiles();
      if (subEntries != null) {
        for (File subEntry : subEntries) {
          if (subEntry.isDirectory()) {
            directories.offer(subEntry);
          } else if (subEntry.getName().equals(DriverConstants.SPID_FILE_NAME)) {
            DriverMetadata driver;

            driver = DriverMetadata.buildFromFile(subEntry.toPath());
            if (driver == null) {
              logger.warn("Unable to parse driver metadata from file {}", subEntry);
            } else {
              drivers.add(driver);
            }
          }
        }
      }
    }

    return drivers;
  }

}
