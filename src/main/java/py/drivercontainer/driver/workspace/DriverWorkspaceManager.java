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

import java.io.IOException;
import java.util.List;
import py.drivercontainer.driver.version.Version;
import py.icshare.DriverKey;

/**
 * This interface declares some common used methods to manage driver workspace.
 *
 */
public interface DriverWorkspaceManager {

  /**
   * Get a workspace from the given version and the given key.
   *
   * <p>Version and driver key are both parts of workspace.
   *
   * @param version driver version
   * @param key     driver key
   * @return driver workspace composed of the given version and the given key.
   */
  public DriverWorkspace getWorkspace(Version version, DriverKey key);

  /**
   * Create workspace in file system for driver running.
   *
   * @param version driver version
   * @param key     driver key
   * @return driver workspace composed of the given version and the given key.
   * @throws IOException if something wrong when creating driver workspace in file system.
   */
  public DriverWorkspace createWorkspace(Version version, DriverKey key)
      throws IOException, InterruptedException;

  /**
   * Delete workspace in file system after removing driver.
   *
   * @param version driver version
   * @param key     driver key
   * @return driver workspace composed of the given version and the given key.
   * @throws IOException if something wrong when deleting driver workspace in file system.
   */
  public DriverWorkspace deleteWorkspace(Version version, DriverKey key) throws IOException;

  /**
   * List all drivers' workspaces existing in file system.
   *
   * @return a list of driver workspaces.
   */
  public List<DriverWorkspace> listWorkspaces() throws IOException;
}
