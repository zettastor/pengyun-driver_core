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
