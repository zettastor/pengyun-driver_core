/**
* Copyright (C) 2013-2024 Nanjing Pengyun Network Technology Co., Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/ 

package py.drivercontainer.driver.workspace;


import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.driver.DriverType;
import py.drivercontainer.driver.version.Version;
import py.drivercontainer.driver.version.file.VersionImpl;
import py.icshare.DriverKey;
import py.instance.InstanceId;


/**
 * The hierarchy of workspace for drivers in this implementation is as following.
 * <li>[version]/[volume id]/[snapshot id]/[driver type]</li>
 *
 */
public class DriverWorkspaceImpl implements DriverWorkspace {

  public static final int DRIVER_WORKSPACE_DEPTH = 4;
  private static final Logger logger = LoggerFactory.getLogger(DriverWorkspaceImpl.class);
  private String path;

  private Version version;

  private DriverKey driverKey;


  /**
   * xx.
   */
  public DriverWorkspaceImpl(String rootDirPath, Version version, DriverKey driverKey) {
    this.version = version;
    this.driverKey = driverKey;

    File pathBuilder = new File(rootDirPath);

    pathBuilder = new File(pathBuilder, version.format());
    pathBuilder = new File(pathBuilder, Long.toString(driverKey.getVolumeId()));
    pathBuilder = new File(pathBuilder, Integer.toString(driverKey.getSnapshotId()));
    pathBuilder = new File(pathBuilder, driverKey.getDriverType().name().toLowerCase());

    this.path = pathBuilder.getPath();
  }


  /**
   * xx.
   */
  public DriverWorkspaceImpl(InstanceId driverContainerId, String workspacePath) {
    File file;
    DriverType driverType;

    file = new File(workspacePath);
    try {
      driverType = DriverType.valueOf(file.getName().toUpperCase());
    } catch (IllegalArgumentException e) {
      logger.error("Unable to parse workspace from {}", workspacePath);
      throw e;
    }

    file = file.getParentFile();
    int snapshotId;
    snapshotId = Integer.parseInt(file.getName());

    file = file.getParentFile();
    long volumeId;
    volumeId = Long.parseLong(file.getName());

    this.driverKey = new DriverKey(driverContainerId.getId(), volumeId, snapshotId, driverType);

    file = file.getParentFile();
    this.version = VersionImpl.get(file.getName());
    if (this.version == null) {
      throw new IllegalArgumentException(
          "Unable to parse version from given workspace path: " + workspacePath);
    }

    this.path = workspacePath;
  }

  @Override
  public String getPath() {
    return this.path;
  }

  @Override
  public File getDir() {
    return new File(this.path);
  }

  @Override
  public Version getVersion() {
    return this.version;
  }

  @Override
  public DriverKey getKey() {
    return this.driverKey;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((driverKey == null) ? 0 : driverKey.hashCode());
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DriverWorkspaceImpl other = (DriverWorkspaceImpl) obj;
    if (driverKey == null) {
      if (other.driverKey != null) {
        return false;
      }
    } else if (!driverKey.equals(other.driverKey)) {
      return false;
    }
    if (path == null) {
      if (other.path != null) {
        return false;
      }
    } else if (!path.equals(other.path)) {
      return false;
    }
    if (version == null) {
      if (other.version != null) {
        return false;
      }
    } else if (!version.equals(other.version)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "DriverWorkspaceImpl [path=" + path + "]";
  }
}
