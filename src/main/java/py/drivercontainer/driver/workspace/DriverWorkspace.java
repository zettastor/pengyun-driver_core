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
