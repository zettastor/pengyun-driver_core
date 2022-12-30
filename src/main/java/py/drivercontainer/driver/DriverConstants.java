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


/**
 * xx.
 */
public class DriverConstants {

  /**
   * File 'SPID' is where meta-data of driver persist in.
   */
  public static final String SPID_FILE_NAME = "SPID";

  public static final String LIB_DIR_NAME = "lib";

  public static final String CONFIG_DIR_NAME = "config";

  public static final String HIBERNATE_CONFIG_DIR_NAME = "hibernate-config";

  public static final String SPRING_CONFIG_DIR_NAME = "spring-config";

  /**
   * Begin from root path, e.g. 'var/123l/0/ISCSI/'
   */
  public static final int DRIVER_WORKSPACE_DEPTH = 4;
}
