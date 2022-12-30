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

package py.drivercontainer;

public interface JvmConfiguration {

  /**
   * Get main class full name for some driver.
   *
   * @return main class for some driver.
   */
  public String getMainClass();

  /**
   * Get initial memory pool size for java virtual machine running some driver.
   *
   * @return initial memory pool size for java virtual machine running some driver.
   */
  public String getInitialMemPoolSize();

  /**
   * Get minimum memory pool size for java virtual machine running some driver.
   *
   * @return minimum memory pool size for java virtual machine running some driver.
   */
  public String getMinMemPoolSize();

  /**
   * Get max memory pool size for java virtual machine running some driver.
   *
   * @return max memory pool size for java virtual machine running some driver.
   */
  public String getMaxMemPoolSize();

  /**
   * Get max direct memory size for java virtual machine running some driver. Direct memory is the
   * memory application process allocated from operation system directly.
   *
   * @return max direct memory size for java virtual machine running some driver.
   */
  public String getMaxDirectMemorySize();

  /**
   * Get max GC pause for java virtual machine running some driver.
   *
   * @return max GC pause for java virtual machine running some driver.
   */
  public long getMaxGcPauseMillis();

  /**
   * Get max GC pause interval for java virtual machine running some driver.
   *
   * @return max GC pause interval for java virtual machine running some driver.
   */
  public long getGcPauseIntervalMillis();

  /**
   * Get number of parallel GC threads.
   *
   * @return number of parallel GC threads.
   */
  public int getParallelGcThreads();

  public String getYourkitAgentPath();

  public int getJmxBasePort();

  public boolean isJmxEnable();

  public String getInitMetaSpaceSize();

  public int getG1rSetUpdatingPauseTimePercent();

  public int getConcGcThreads();

  public String getNettyLeakDetectionLevel();

  public String getNettyLeakDetectionTargetRecords();

  public int getNettyAllocatorMaxOrder();
}
