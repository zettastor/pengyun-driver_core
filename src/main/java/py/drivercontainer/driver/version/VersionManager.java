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

package py.drivercontainer.driver.version;

import java.util.NoSuchElementException;
import py.driver.DriverType;

/**
 * This class declares some interfaces to manage version of specified type of driver.
 *
 */
public interface VersionManager {

  /**
   * It is possible that multiple processes(e.g. driver-container, deployment-daemon) access driver
   * version simultaneously. To synchronize driver version access, it is necessary to lock driver
   * version before accessing it. After driver version being locked, other processes could not
   * access it until the lock being released.
   *
   * @param driverType Since each driver has its own version, a driver type should be given for
   *                   version access.
   * @throws VersionException if something wrong when lock version of the given type of version
   */
  public void lockVersion(DriverType driverType) throws VersionException;

  /**
   * Release lock on driver version.
   *
   * @param driverType Since each driver has its own version, a driver type should be given for
   *                   version access.
   * @throws NoSuchElementException if version for the given type of driver hasn't been locked yet
   * @throws VersionException       if something wrong when unlock version of the given type of
   *                                version
   */
  public void unlockVersion(DriverType driverType) throws NoSuchElementException, VersionException;

  /**
   * Get current version of the given type of driver.
   *
   * @param driverType Since each driver has its own version, a driver type should be given for
   *                   version access.
   * @return Current version of the given type of driver; or null if there is no such driver version
   *          info in driver-container runtime environment.
   * @throws VersionException if something wrong when get current version of the given type of
   *                          version
   */
  public Version getCurrentVersion(DriverType driverType) throws VersionException;

  /**
   * Set current version of the given type of driver.
   *
   * @param driverType Since each driver has its own version, a driver type should be given for
   *                   version access.
   * @param version    Current version to be set.
   * @return Old current version of the given type of driver; or null if there is no such driver
   *          version info in driver-container runtime environment.
   * @throws VersionException if something wrong when set current version of the given type of
   *                          version
   */
  public Version setCurrentVersion(DriverType driverType, Version version) throws VersionException;

  /**
   * Get latest version of the given type of driver.
   *
   * <p>If latest driver version is different from current driver version, that means the type of
   * driver is required to be upgraded to the latest version.
   *
   * @param driverType Since each driver has its own version, a driver type should be given for
   *                   version access.
   * @return Latest version of the given type of driver; or null if there is no such driver version
   *          info in driver-container runtime environment.
   * @throws VersionException if something wrong when get latest version of the given type of
   *                          version
   */
  public Version getLatestVersion(DriverType driverType) throws VersionException;

  /**
   * Set latest version of the given type of driver.
   *
   * @param driverType Since each driver has its own version, a driver type should be given for
   *                   version access.
   * @param version    Latest version to be set.
   * @return Old latest version of the given type of driver; or null if there is no such driver
   *          version info in driver-container runtime environment.
   * @throws VersionException if something wrong when set latest version of the given type of
   *                          version
   */
  public Version setLatestVersion(DriverType driverType, Version version) throws VersionException;

  /**
   * Check if the given type of driver is on migration.
   *
   * @param driverType Since each driver has its own version, a driver type should be given for
   *                   version access.
   * @return true if the given type of driver is on migration; or false if it is not.
   * @throws VersionException if something wrong when check migration flag of the given type of
   *                          version
   */
  public boolean isOnMigration(DriverType driverType) throws VersionException;

  /**
   * Set new migration flag to the given type of driver.
   *
   * @param driverType    Since each driver has its own version, a driver type should be given for
   *                      version access.
   * @param isOnMigration New migration flag.
   * @throws VersionException if something wrong when set migration flag of the given type of
   *                          version
   */
  public void setOnMigration(DriverType driverType, boolean isOnMigration) throws VersionException;
}
