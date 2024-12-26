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
import py.drivercontainer.driver.version.Version;
import py.icshare.DriverKey;

/**
 * Driver workspace is composed of items in {@link DriverKey} and {@link Version}. This interface
 * declares some common used methods to access driver workspace.
 *
 */
public interface DriverWorkspace {

  /**
   * Get path string of workspace.
   *
   * @return relative path to current workspace or absolute path.
   */
  public String getPath();

  /**
   * Get directory representing the driver workspace.
   *
   * @return directory representing the driver workspace.
   */
  public File getDir();

  /**
   * Get version of driver under this workspace.
   *
   * @return version of driver under this workspace
   */
  public Version getVersion();

  /**
   * Get key of driver under this workspace.
   *
   * @return key of driver under this workspace
   */
  public DriverKey getKey();
}
