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

package py.drivercontainer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.drivercontainer.JvmConfiguration;
import py.drivercontainer.driver.DriverConstants;

public class JavaProcessBuilder {

  /**
   * metrics relative properties file name.
   */
  public static final String PROP_FILE_NAME_METRICS = "metric.properties";
  /**
   * key of property "metric.enable" in metrics relative properties file.
   */
  public static final String PROP_KEY_METRIC_ENABLE = "metric.enable";
  public static final String PROP_KEY_METRIC_PROFILE = "metric.enable.profiles";
  private static final Logger logger = LoggerFactory.getLogger(JavaProcessBuilder.class);
  private JvmConfiguration jvmConfig;
  private String workingDirectory;
  private List<String> classpathEntries = new ArrayList<String>();
  private List<String> mainClassArguments = new ArrayList<String>();
  private String jvmRuntime;
  private int metricPort = 0;
  private long volumeId;
  private int snapshotId;

  public JavaProcessBuilder(JvmConfiguration jvmConfig) {
    this.jvmConfig = jvmConfig;
    this.jvmRuntime = defaultJavaPath();
  }


  /**
   * xx.
   */
  public static String defaultJavaPath() {
    StringBuilder sb = new StringBuilder();
    sb.append(System.getProperty("java.home"));
    sb.append(File.separator);
    sb.append("bin");
    sb.append(File.separator);
    sb.append("java");

    return sb.toString();
  }

  /**
   * for ARM Architecture, the OpenJDK 11 output is different from x86_64.
   * $ java11 -version
   * openjdk version "11-ea" 2018-09-25
   * OpenJDK Runtime Environment (build 11-ea+28)
   * OpenJDK 64-Bit Server VM (build 11-ea+28, mixed mode, sharing)
   * $ uname -a
   * Linux server220 4.14.0-115.5.1.el7a.06.aarch64 #1 SMP Tue Jun 18 10:34:55 CST 2019 aarch64
   * aarch64 aarch64 GNU/Linux
   * */
  public static double getJavaVersion() {
    String version = System.getProperty("java.version");
    if (version.contains("11-ea")) {
      logger.warn("version {} is ARM Arch.", version);
      return 11.0;
    }
    int pos = version.indexOf('.');
    if (pos == -1) {
      logger.warn("version {} has no comma symbol.", version);
      return 11.0;
    }
    pos = version.indexOf('.', pos + 1);
    double javaVersion = Double.parseDouble(version.substring(0, pos));
    logger.warn("java version is {}, parsed to double is {}", version, javaVersion);
    return javaVersion;
  }

  private static String createSubDirectory(String parent, String sub) {
    File parentFile = new File(parent);
    if (!parentFile.isDirectory()) {
      logger.error("not a directory {}", parent);
      throw new IllegalArgumentException();
    }
    Path parentPath = new File(parent).toPath();
    Path subPath = parentPath.resolve(sub);
    File subFile = subPath.toFile();
    if (subFile.exists()) {
      if (subFile.isDirectory()) {
        logger.info("directory already exists");
      } else {
        logger.info("path already exists, but not a directory {}", subPath);
        throw new IllegalArgumentException();
      }
    } else {
      boolean success = subFile.mkdirs();
      if (!success) {
        logger.error("create directory failed {}", subPath);
        throw new IllegalArgumentException();
      }
    }
    return subFile.getAbsolutePath();
  }


  /**
   * xx.
   */
  public static void fillClasspathEntries(List<String> entries, String workingDir) {
    entries.add(workingDir);

    File libDir = new File(workingDir, DriverConstants.LIB_DIR_NAME);
    if (libDir.exists() && libDir.isDirectory()) {
      File[] fileArray = libDir.listFiles();
      if (fileArray != null) {
        for (File jarFile : fileArray) {
          if (jarFile.isFile()) {
            entries.add(jarFile.getAbsolutePath());
          }
        }
      }
    }

    File configDir = new File(workingDir, DriverConstants.CONFIG_DIR_NAME);
    if (configDir.exists() && configDir.isDirectory()) {
      entries.add(configDir.getAbsolutePath());
    }

    File hibernateConfigDir = new File(workingDir, DriverConstants.HIBERNATE_CONFIG_DIR_NAME);
    if (hibernateConfigDir.exists() && hibernateConfigDir.isDirectory()) {
      entries.add(hibernateConfigDir.getAbsolutePath());
    }

    File springConfigDir = new File(workingDir, DriverConstants.SPRING_CONFIG_DIR_NAME);
    if (springConfigDir.exists() && springConfigDir.isDirectory()) {
      entries.add(springConfigDir.getAbsolutePath());
    }
  }

  public JavaProcessBuilder setWorkingDirectory(String workingDirectory) {
    this.workingDirectory = new File(workingDirectory).getAbsolutePath();
    return this;
  }

  public JavaProcessBuilder addClasspathEntry(String classpathEntry) {
    this.classpathEntries.add(classpathEntry);
    return this;
  }

  public JavaProcessBuilder addArgument(String argument) {
    this.mainClassArguments.add(argument);
    return this;
  }

  public JavaProcessBuilder setjvmRuntime(String jvmRuntime) {
    this.jvmRuntime = jvmRuntime;
    return this;
  }

  /**
   * Load coordinator metric properties from configuration file.
   *
   * @param workingDir working directory of driver
   * @return coordinator metric properties
   * @throws IOException if failed to access coordinator configuration file.
   */
  Properties loadCoordinatorMetricProps(String workingDir) throws IOException {
    Properties coordinatorMetricProps;
    File configFile;

    // properties file for metric named "metric.properties"
    configFile = new File(workingDir, "config");
    if (!configFile.exists() || !configFile.isDirectory()) {
      logger.error("can not find an directory name config:{},{}", configFile.exists(),
          configFile.isDirectory());
      return null;
    }

    File metricPropsFile;
    metricPropsFile = new File(configFile, PROP_FILE_NAME_METRICS);
    if (!metricPropsFile.exists()) {
      logger.error("can not find metric config file:{} at:{}", PROP_FILE_NAME_METRICS,
          configFile.toString());
      return null;
    }

    FileInputStream fis;
    fis = new FileInputStream(metricPropsFile);
    try {
      coordinatorMetricProps = new Properties();
      coordinatorMetricProps.load(fis);
    } finally {
      try {
        fis.close();
      } catch (IOException e) {
        logger.warn("Unable to close file: {}", metricPropsFile, e);
      }
    }

    return coordinatorMetricProps;
  }


  /**
   * xx.
   */
  public Process startProcess() throws IOException {
    if (jvmConfig.getMainClass() == null) {
      throw new IOException("Missing main class.");
    }

    classpathEntries.clear();
    if (workingDirectory == null) {
      String classpath = System.getProperty("java.class.path");
      classpathEntries.add(classpath);
    } else {
      fillClasspathEntries(classpathEntries, workingDirectory);
    }

    // we need to clear the list first, or the process arguments list may be too long.
    List<String> argumentsList = new ArrayList<>();
    Properties coordinatorMetricProps;
    boolean isMetricEnable;

    coordinatorMetricProps = loadCoordinatorMetricProps(workingDirectory);
    if (coordinatorMetricProps == null) {
      isMetricEnable = false;
    } else {
      isMetricEnable = Boolean
          .parseBoolean(coordinatorMetricProps.getProperty(PROP_KEY_METRIC_ENABLE));
    }

    argumentsList.add(this.jvmRuntime);
    logger.warn("get a property switch of metric: {}", isMetricEnable);
    // if enable coordinator metrics, start coordinator with these params
    argumentsList.add("-Dmetric.enable=" + isMetricEnable);
    if (isMetricEnable) {
      String metricProfiles = coordinatorMetricProps.getProperty(PROP_KEY_METRIC_PROFILE);
      logger.warn("get a property of metric enable profiles:{}", metricProfiles);
      argumentsList.add(String.format("-Dmetric.enable.profiles=%s", metricProfiles));

      argumentsList.add(String.format("-Dprocess.identifier=%s.%s", volumeId, snapshotId));
      /*
       * JMX options
       */
      argumentsList.add("-Dcom.sun.management.jmxremote");
      String portString = "-Dcom.sun.management.jmxremote.port=" + metricPort;
      argumentsList.add(portString);
      argumentsList.add("-Dcom.sun.management.jmxremote.ssl=false");
      argumentsList.add("-Dcom.sun.management.jmxremote.local.only=false");
      argumentsList.add("-Dcom.sun.management.jmxremote.authenticate=false");
    }

    argumentsList.add("-noverify");
    argumentsList.add("-server");
    argumentsList.add(String.format("-Djava.library.path=%s/lib", workingDirectory));

    String detectionLevel =
        "-Dio.netty.leakDetectionLevel=" + jvmConfig.getNettyLeakDetectionLevel();
    logger.warn("detection level:{}", detectionLevel);
    argumentsList.add(detectionLevel);
    String detectionTargetLimit =
        "-Dio.netty.leakDetection.targetRecords=" + jvmConfig.getNettyLeakDetectionTargetRecords();
    logger.warn("detection target records:{}", detectionTargetLimit);
    argumentsList.add(detectionTargetLimit);

    // argumentsList.add("-Dio.netty.noUnsafe=true");
    // argumentsList.add("-Dio.netty.noPreferDirect=true");
    // argumentsList.add("-Dio.netty.recycler.maxCapacity=0");
    argumentsList.add("-Dio.netty.allocator.pageSize=8192"); // 8K
    String nettyAllocatorMaxOrder =
        "-Dio.netty.allocator.maxOrder=" + jvmConfig.getNettyAllocatorMaxOrder();
    argumentsList.add(nettyAllocatorMaxOrder); // chunkSize = 8K * 2^maxOrder

    argumentsList.add(String.format("-Xms%s", jvmConfig.getMinMemPoolSize()));
    argumentsList.add(String.format("-Xmx%s", jvmConfig.getMaxMemPoolSize()));
    argumentsList
        .add(String.format("-XX:MaxDirectMemorySize=%s", jvmConfig.getMaxDirectMemorySize()));
    argumentsList.add("-XX:+UseG1GC");
    argumentsList.add("-XX:+HeapDumpOnOutOfMemoryError");
    argumentsList.add("-XX:OnOutOfMemoryError=kill -9 %p");

    argumentsList.add("-XX:+AlwaysPreTouch");
    argumentsList.add(
        String.format("-XX:MetaspaceSize=%s", String.valueOf(jvmConfig.getInitMetaSpaceSize())));
    argumentsList.add(String.format("-XX:G1RSetUpdatingPauseTimePercent=%s",
        String.valueOf(jvmConfig.getG1rSetUpdatingPauseTimePercent())));
    argumentsList
        .add(String.format("-XX:ConcGCThreads=%s", String.valueOf(jvmConfig.getConcGcThreads())));

    argumentsList.add("-XX:-OmitStackTraceInFastThrow");

    // can not use, cause we use -XX:+UseG1GC
    // argumentsList.add("-XX:+UseConcMarkSweepGC");
    // argumentsList.add("-XX:CMSFULLGCsBeforeCompaction=1");

    String yourkitAgentPath = jvmConfig.getYourkitAgentPath();
    if (yourkitAgentPath != null && !yourkitAgentPath.isEmpty()) {
      File file = new File(yourkitAgentPath);
      if (file.exists()) {
        yourkitAgentPath = String.format("-agentpath:%s", yourkitAgentPath);
        argumentsList.add(yourkitAgentPath);
        logger.warn("enable yourkit and yourkit agent path:{}", yourkitAgentPath);
      } else {
        logger.warn("yourkit file={} is not exist", yourkitAgentPath);
      }
    }
    argumentsList.add(
        String.format("-XX:MaxGCPauseMillis=%s", String.valueOf(jvmConfig.getMaxGcPauseMillis())));
    argumentsList.add(
        String.format("-XX:GCPauseIntervalMillis=%s",
            String.valueOf(jvmConfig.getGcPauseIntervalMillis())));
    argumentsList.add(String
        .format("-XX:ParallelGCThreads=%s", String.valueOf(jvmConfig.getParallelGcThreads())));

    double javaVersion = getJavaVersion();
    createSubDirectory(this.workingDirectory, "logs");
    if (javaVersion > 1.8) {
      argumentsList.add(
          "-Xlog:gc*:file=logs/gc.log:uptime,time,level,tags,tid:filecount=5,filesize=100M");
    } else {
      // can no use
      // argumentsList.add(String.format("-Xloggc:%s/logs/gc.log", this.workingDirectory));
      argumentsList.add("-Xloggc:logs/gc.log");
      argumentsList.add("-XX:+PrintGCDetails");
      argumentsList.add("-XX:+PrintGCTimeStamps");
      argumentsList.add("-XX:+PrintAdaptiveSizePolicy");

      // can no use
      // argumentsList.add("-XX:+PrintFlagsFinal");

      argumentsList.add("-XX:+PrintReferenceGC");
      argumentsList.add("-verbose:gc");
      argumentsList.add("-XX:+UnlockDiagnosticVMOptions");
      argumentsList.add("-XX:+G1SummarizeConcMark");
      argumentsList.add("-XX:+G1SummarizeRSetStats");
      argumentsList.add("-XX:G1SummarizeRSetStatsPeriod=1");
    }

    argumentsList.add("-classpath");
    argumentsList.add(getClasspath());
    argumentsList.add(jvmConfig.getMainClass());
    for (String arg : mainClassArguments) {
      logger.debug("mainClassArgument: {}", arg);
      argumentsList.add(arg);
    }

    String[] arguments = argumentsList.toArray(new String[argumentsList.size()]);
    logger.debug("The cmd of starting a java process is {}", StringUtils.join(arguments, " "));

    ProcessBuilder processBuilder = new ProcessBuilder(arguments);
    processBuilder.redirectErrorStream(false);
    if (workingDirectory != null) {
      processBuilder.directory(new File(this.workingDirectory));
    }
    Process process = null;
    try {
      process = processBuilder.start();
    } catch (Throwable t) {
      logger.error("can not start driver", t);
    }

    return process;
  }

  private String getClasspath() {
    StringBuilder builder = new StringBuilder();
    for (String classpathEntry : classpathEntries) {
      builder.append(classpathEntry);
      builder.append(System.getProperty("path.separator"));
    }
    return builder.toString();
  }

  public JavaProcessBuilder setMetricPort(int metricPort) {
    this.metricPort = metricPort;
    return this;
  }

  public void setVolumeId(long volumeId) {
    this.volumeId = volumeId;
  }

  public void setSnapshotId(int snapshotId) {
    this.snapshotId = snapshotId;
  }
}
