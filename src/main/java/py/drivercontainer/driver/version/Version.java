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

/**
 * It is supposed that version is composed of some parts, such as primary version, production and
 * timestamp.
 *
 */
public interface Version {

  /**
   * Get primary version (e.g. 1.0.0; 2.3.0...).
   *
   * @return primary version.
   */
  public String getPrimaryVersion();

  /**
   * Get production in version.
   *
   * @return production in version.
   */
  public Production getProduction();

  /**
   * Get timestamp in version, and it is composed of year, month, day of month, hour of day, minute
   * and second (e.g.201709081011 ...).
   *
   * @return timestamp in version.
   */
  public Timestamp getTimestamp();

  /**
   * Format all parts of version in version format.
   *
   * @return Version string in version format.
   */
  public String format();


  /**
   * Get service install path from version. refer to buildServiceInstallationPath not containing
   * 'internal' or 'release'
   *
   * @return Path string in version format.
   */
  public String formatInstallationAppendix();
}
