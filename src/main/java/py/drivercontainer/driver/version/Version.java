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
