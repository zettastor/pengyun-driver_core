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
