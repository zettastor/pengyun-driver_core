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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({"classpath:config/coordinator-jvm.properties"})
public class JvmConfigurationForDriver implements JvmConfiguration {

  @Value("${main.class:py.coordinator.driver.DriverLauncher}")
  private String mainClass = "py.coordinator.driver.DriverLauncher";

  @Value("${initial.mem.pool.size:1024m}")
  private String initialMemPoolSize = "1024m";

  @Value("${min.mem.pool.size:1024m}")
  private String minMemPoolSize = "1024m";

  @Value("${max.mem.pool.size:1024m}")
  private String maxMemPoolSize = "1024m";

  @Value("${max.direct.memory.size:1G}")
  private String maxDirectMemorySize = "1G";

  @Value("${max.gc.pause.ms:200}")
  private long maxGcPauseMillis = 200;

  @Value("${gc.pause.internal.ms:1000}")
  private long gcPauseIntervalMillis = 1000;

  @Value("${parallel.gc.threads:10}")
  private int parallelGcThreads = 10;

  @Value("${yourkit.agent.path:@null}")
  private String yourkitAgentPath = "";

  @Value("${jmx.base.port}")
  private int jmxBasePort;

  @Value("${jmx.enable:false}")
  private boolean jmxEnable = false;

  @Value("${initial.metaspace.size:20m}")
  private String initMetaSpaceSize = "20m";

  @Value("${g1.rs.updating.pause.time.percent:10}")
  private int g1rSetUpdatingPauseTimePercent = 10;

  @Value("${conc.gc.threads:0}")
  private int concGcThreads = 0;

  @Value("${netty.leak.detection.level:disabled}")
  private String nettyLeakDetectionLevel = "disabled";

  @Value("${netty.leak.detection.target.records:20}")
  private String nettyLeakDetectionTargetRecords = "20";

  @Value("${netty.allocator.maxOrder:11}")
  private int nettyAllocatorMaxOrder = 11;

  public String getMainClass() {
    return mainClass;
  }

  public void setMainClass(String mainClass) {
    this.mainClass = mainClass;
  }

  public String getInitialMemPoolSize() {
    return initialMemPoolSize;
  }

  public void setInitialMemPoolSize(String initialMemPoolSize) {
    this.initialMemPoolSize = initialMemPoolSize;
  }

  public String getMinMemPoolSize() {
    return minMemPoolSize;
  }

  public void setMinMemPoolSize(String minMemPoolSize) {
    this.minMemPoolSize = minMemPoolSize;
  }

  public String getMaxMemPoolSize() {
    return maxMemPoolSize;
  }

  public void setMaxMemPoolSize(String maxMemPoolSize) {
    this.maxMemPoolSize = maxMemPoolSize;
  }

  public long getMaxGcPauseMillis() {
    return maxGcPauseMillis;
  }

  public void setMaxGcPauseMillis(long maxGcPauseMillis) {
    this.maxGcPauseMillis = maxGcPauseMillis;
  }

  public long getGcPauseIntervalMillis() {
    return gcPauseIntervalMillis;
  }

  public void setGcPauseIntervalMillis(long gcPauseIntervalMillis) {
    this.gcPauseIntervalMillis = gcPauseIntervalMillis;
  }

  public int getParallelGcThreads() {
    return parallelGcThreads;
  }

  public void setParallelGcThreads(int parallelGcThreads) {
    this.parallelGcThreads = parallelGcThreads;
  }

  public int getJmxBasePort() {
    return jmxBasePort;
  }

  public void setJmxBasePort(int jmxBasePort) {
    this.jmxBasePort = jmxBasePort;
  }

  public String getYourkitAgentPath() {
    return yourkitAgentPath;
  }

  public void setYourkitAgentPath(String yourkitAgentPath) {
    this.yourkitAgentPath = yourkitAgentPath;
  }

  public String getMaxDirectMemorySize() {
    return maxDirectMemorySize;
  }

  public void setMaxDirectMemorySize(String maxDirectMemorySize) {
    this.maxDirectMemorySize = maxDirectMemorySize;
  }

  public boolean isJmxEnable() {
    return jmxEnable;
  }

  public void setJmxEnable(boolean jmxEnable) {
    this.jmxEnable = jmxEnable;
  }

  @Override
  public String getInitMetaSpaceSize() {
    return initMetaSpaceSize;
  }

  @Override
  public int getG1rSetUpdatingPauseTimePercent() {
    return g1rSetUpdatingPauseTimePercent;
  }

  @Override
  public int getConcGcThreads() {
    return concGcThreads;
  }

  @Override
  public String getNettyLeakDetectionLevel() {
    return nettyLeakDetectionLevel;
  }

  public void setNettyLeakDetectionLevel(String nettyLeakDetectionLevel) {
    this.nettyLeakDetectionLevel = nettyLeakDetectionLevel;
  }

  @Override
  public String getNettyLeakDetectionTargetRecords() {
    return nettyLeakDetectionTargetRecords;
  }

  public void setNettyLeakDetectionTargetRecords(String nettyLeakDetectionTargetRecords) {
    this.nettyLeakDetectionTargetRecords = nettyLeakDetectionTargetRecords;
  }

  @Override
  public int getNettyAllocatorMaxOrder() {
    return nettyAllocatorMaxOrder;
  }
}
