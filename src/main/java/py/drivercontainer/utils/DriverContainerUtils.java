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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.Validate;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.client.thrift.GenericThriftClientFactory;
import py.common.OsCmdExecutor;
import py.common.struct.EndPoint;
import py.informationcenter.Utils;
import py.thrift.coordinator.service.Coordinator;

/**
 * xx.
 */
public class DriverContainerUtils {

  private static final Logger logger = LoggerFactory.getLogger(DriverContainerUtils.class);

  /**
   * There are always 2 threads in pool. One is for STDOUT stream consumer, and another is for
   * STDERR stream consumer.
   */
  private static final int OS_CMD_THREAD_POOL_SIZE = 2;

  public static ExecutorService osCMDThreadPool;


  /**
   * xx.
   */
  public static void init() {
    osCMDThreadPool = Executors.newFixedThreadPool(OS_CMD_THREAD_POOL_SIZE, new ThreadFactory() {

      @Override
      public Thread newThread(Runnable r) {
        return new Thread(r, "OS CMD Consumer");
      }
    });
  }

  public static void destroy() {
    osCMDThreadPool.shutdown();
  }

  // there is a synchronize problem,because there is multiple thread to bind the same port
  public static boolean isPortAvailable(String ip, int port) {
    return Utils.available(ip, port);
  }


  /**
   * xx.
   */
  public static boolean isProcessExist(String processString) {
    final String osCmd = "ps -ef";
    final AtomicBoolean exist = new AtomicBoolean(false);

    OsCmdExecutor.OsCmdStreamConsumer stdoutStreamConsumer;
    OsCmdExecutor.OsCmdOutputLogger stderrStreamConsumer;

    stdoutStreamConsumer = new OsCmdExecutor.OsCmdStreamConsumer() {

      @Override
      public void consume(InputStream stream) throws IOException {
        String line;
        BufferedReader br;
        boolean flag = false;
        br = new BufferedReader(new InputStreamReader(stream));
        while ((line = br.readLine()) != null) {
          if (flag) {
            continue;
          }
          String[] sourceStrArray = line.split(" ");
          // if element value > 0 and value is equal to processString, means pid is exist
          for (int i = 0; i < sourceStrArray.length; i++) {
            if (sourceStrArray[i] != null) {
              if (sourceStrArray[i].contentEquals(processString)) {
                flag = true;
                exist.set(true);
                break;
              }
            }
          }
        }

      }
    };
    stderrStreamConsumer = new OsCmdExecutor.OsCmdOutputLogger(logger, osCmd);
    stderrStreamConsumer.setErrorStream(true);

    try {
      OsCmdExecutor.exec(osCmd, osCMDThreadPool, stdoutStreamConsumer, stderrStreamConsumer);
    } catch (IOException | InterruptedException e) {
      logger.error("Failed to judge a process is exist or not", e);
    }

    return exist.get();
  }

  /**
   * Check if there is a process with the given PID listening the given port.
   *
   * @param pid  process ID
   * @param port listening port
   * @return true if there is such a process, or false if there is not.
   */
  public static boolean isProcessExist(int pid, int port) throws IOException {
    logger.debug("Check if there is a process with PID {} listening port {}", pid, port);

    final String osCmd = "netstat -npl";
    final String regex = ".*:" + Integer.toString(port) + ".*" + Integer.toString(pid) + ".*";

    final AtomicBoolean exist = new AtomicBoolean(false);
    final Pattern pattern = Pattern.compile(regex);

    OsCmdExecutor.OsCmdStreamConsumer stdoutStreamConsumer;
    OsCmdExecutor.OsCmdOutputLogger stderrStreamConsumer;

    stdoutStreamConsumer = new OsCmdExecutor.OsCmdStreamConsumer() {

      @Override
      public void consume(InputStream stream) throws IOException {
        String line;
        BufferedReader br;
        Matcher matcher;

        br = new BufferedReader(new InputStreamReader(stream));
        while ((line = br.readLine()) != null) {
          logger.debug("Line for command [{}]: {}", osCmd, line);

          matcher = pattern.matcher(line);
          if (matcher.matches()) {
            logger.debug("Matched!");
            exist.set(true);
          }
        }
      }
    };
    stderrStreamConsumer = new OsCmdExecutor.OsCmdOutputLogger(logger, osCmd);
    stderrStreamConsumer.setErrorStream(true);

    try {
      OsCmdExecutor.exec(osCmd, osCMDThreadPool, stdoutStreamConsumer, stderrStreamConsumer);
    } catch (IOException | InterruptedException e) {
      throw new IOException(e);
    }

    return exist.get();
  }


  /**
   * xx.
   */
  public static boolean ping(String ip, int port) {
    EndPoint eps = new EndPoint(ip, port);
    GenericThriftClientFactory<Coordinator.Iface> genericThriftClientFactory
        = GenericThriftClientFactory.create(Coordinator.Iface.class, 1);
    try {
      Coordinator.Iface client = genericThriftClientFactory.generateSyncClient(eps);
      client.ping();
      genericThriftClientFactory.close();
      return true;
    } catch (Exception e) {
      logger.warn("caught an exception when ping {},{}", eps, e);
    }

    return false;
  }

  /**
   * Check if there is a process with the given PID.
   */
  public static boolean processExist(int pid) {
    if (pid == 0) {
      return false;
    }
    final String processPath = "/proc/" + Integer.toString(pid);
    boolean pathExist = false;
    pathExist = Files.exists(Paths.get(processPath));
    if (!pathExist) {
      logger.debug("process with PID {} is nonexistent", pid);
    }
    return pathExist;
  }


  /**
   * xx.
   */
  public static boolean processFound(int pid, int port) {
    StringBuilder sb = new StringBuilder(64);
    sb.append("netstat -npl | grep -E \".*:");
    sb.append(port);
    sb.append(".*");
    sb.append(pid);
    sb.append("\" | grep -v grep");

    String[] cmd = {"/bin/sh", "-c", sb.toString()};
    boolean res = execCmd(cmd, null);
    return res;
  }

  /**
   * If a pyd dev has been occupyed by some process ,use lsof command can check it.
   */
  public static boolean deviceIsOccupyed(String nbdDevice) throws IOException {
    logger.debug("Check if the nbdDevice {} has been occupyed", nbdDevice);
    final String osCmd = "lsof " + nbdDevice;
    final String regex = ".*" + nbdDevice + ".*";

    final AtomicBoolean exist = new AtomicBoolean(false);
    final Pattern pattern = Pattern.compile(regex);

    OsCmdExecutor.OsCmdStreamConsumer stdoutStreamConsumer;
    OsCmdExecutor.OsCmdOutputLogger stderrStreamConsumer;

    stdoutStreamConsumer = new OsCmdExecutor.OsCmdStreamConsumer() {

      @Override
      public void consume(InputStream stream) throws IOException {
        String line;
        BufferedReader br;
        Matcher matcher;

        br = new BufferedReader(new InputStreamReader(stream));
        while ((line = br.readLine()) != null) {
          logger.debug("Line for command [{}]: {}", osCmd, line);

          matcher = pattern.matcher(line);
          if (matcher.matches()) {
            logger.debug("Matched!");
            exist.set(true);
          }
        }
      }
    };
    stderrStreamConsumer = new OsCmdExecutor.OsCmdOutputLogger(logger, osCmd);
    stderrStreamConsumer.setErrorStream(true);

    try {
      OsCmdExecutor.exec(osCmd, osCMDThreadPool, stdoutStreamConsumer, stderrStreamConsumer);
    } catch (IOException | InterruptedException e) {
      throw new IOException(e);
    }

    return exist.get();
  }

  /**
   * /usr/bin/targetcli /iscsi set global auto_add_default_portal = false example output: Parameter
   * auto_add_default_portal is now 'false'.
   */
  public static void setGlobalAutoAddDefaultPortalFalse(String cmd) {
    OsCmdExecutor.OsCmdOutputLogger lioSaveConfigConsumer = new OsCmdExecutor.OsCmdOutputLogger(
        logger, cmd);
    try {
      OsCmdExecutor.exec(cmd, DriverContainerUtils.osCMDThreadPool, lioSaveConfigConsumer,
          lioSaveConfigConsumer);
      logger.debug("cmd {} executed", cmd);
    } catch (Exception e) {
      // don't process exception temporarily for special test environment, ex: domestic
      // environment can't load ISCSI kernel module
      logger.warn("Catch an Exception when exec oscmd {}, exception {}", cmd, e);
    }
  }

  /**
   * /usr/bin/targetcli /iscsi get global auto_add_default_portal example output:
   * [auto_add_default_portal=false ].
   */
  public static boolean getGlobalAutoAddDefaultPortalValue(String cmd) {
    OsCmdExecutor.OsCmdStreamConsumer stdoutStreamConsumer;
    OsCmdExecutor.OsCmdOutputLogger stderrStreamConsumer;
    final AtomicBoolean portalAttribute = new AtomicBoolean(true);

    stdoutStreamConsumer = new OsCmdExecutor.OsCmdStreamConsumer() {

      @Override
      public void consume(InputStream stream) throws IOException {
        String line;
        BufferedReader br;
        br = new BufferedReader(new InputStreamReader(stream));
        if ((line = br.readLine()) != null) {
          String[] splitLine = line.split("=");
          String attributeValue = splitLine[splitLine.length - 1]
              .trim(); //the second string [false ] or [true ] include a space at the end
          if (attributeValue.equals("false")) {
            logger.debug("the portal attribute is false");
            portalAttribute.set(false);
          }
          logger.debug("portalAttribute {}, Line for command [{}]: {}", portalAttribute.get(), cmd,
              line);
        }
      }
    };
    stderrStreamConsumer = new OsCmdExecutor.OsCmdOutputLogger(logger, cmd);
    stderrStreamConsumer.setErrorStream(true);
    try {
      OsCmdExecutor.exec(cmd, DriverContainerUtils.osCMDThreadPool, stdoutStreamConsumer,
          stderrStreamConsumer);
      logger.debug("cmd {} executed", cmd);
    } catch (Exception e) {
      // don't process exception temporarily for special test environment, ex: domestic
      // environment can't load ISCSI kernel module
      logger.warn("Catch an Exception when exec oscmd {}, exception {}", cmd, e);
    }
    return portalAttribute.get();
  }

  // add method to kill a process by wzy @2014-10-11 18:23:00

  /**
   * xx.
   */
  public static void killProcessByPid(int pid) throws Exception {
    logger.warn("Try to kill driver with id {}", pid);

    if (0 == pid) {
      return;
    }

    Process process = Runtime.getRuntime().exec("kill -9 " + pid);
    process.waitFor();
  }


  /**
   * xx.
   */
  public static String replaceBlank(String str) {
    String dest = "";
    if (str != null) {
      Pattern p = Pattern.compile("\\s*|\t|\r|\n");
      Matcher m = p.matcher(str);
      dest = m.replaceAll("");
    }
    return dest;
  }


  /**
   * xx.
   */
  public static int getExponentialBackoffSleepTime(int sleepTimeUnit, int failureTimes,
      int maxBackoffTime) {
    int exponentialBackoff = sleepTimeUnit * (2 << failureTimes);
    if (exponentialBackoff <= 0 || exponentialBackoff > maxBackoffTime) {
      // overflowed or too large
      return maxBackoffTime;
    } else {
      return exponentialBackoff;
    }
  }


  /**
   * xx.
   */
  public static int getSysCpuNum() {
    Sigar sigar = new Sigar();
    int cpuNum = 0;
    try {
      CpuInfo[] cpuInfos = sigar.getCpuInfoList();
      cpuNum = cpuInfos.length;
    } catch (SigarException e) {
      logger.error("Caught an exception when get system cpu num.", e);
      return -1;
    }
    return cpuNum;
  }


  /**
   * xx.
   */
  public static long getSysFreeMem() {
    Sigar sigar = new Sigar();
    Mem memory = null;
    try {
      memory = sigar.getMem();
    } catch (SigarException e) {
      logger.error("Caught an exception when get the real machine memory usage.", e);
      return -1;
    }
    return memory.getActualFree();
  }


  /**
   * xx.
   */
  public static long getSysTotalMem() {
    Sigar sigar = new Sigar();
    Mem memory = null;
    try {
      memory = sigar.getMem();
    } catch (SigarException e) {
      logger.error("Caught an exception when get the real machine memory usage.", e);
      return -1;
    }

    return memory.getTotal();
  }


  /**
   * xx.
   */
  public static long getBytesSizeFromString(String stingSize) {
    Pattern pattern = Pattern.compile("(\\d+)([k|K|m|M|g|G])");
    Matcher matcher = pattern.matcher(stingSize);

    logger.debug(
        "result=" + matcher.matches() + ", results=" + matcher.toMatchResult() + ", " + matcher
            .groupCount());

    Validate.isTrue(matcher.groupCount() == 1 || matcher.groupCount() == 2);
    String value = matcher.group(1);
    String unit = "";
    if (matcher.groupCount() == 2) {
      unit = matcher.group(2);
    }
    long longValue = Long.parseLong(value);
    if (unit.isEmpty()) {
      return longValue;
    } else if (unit.compareToIgnoreCase("k") == 0) {
      return longValue * 1024;
    } else if (unit.compareToIgnoreCase("m") == 0) {
      return longValue * 1024 * 1024;
    } else if (unit.compareToIgnoreCase("g") == 0) {
      return longValue * 1024 * 1024 * 1024;
    } else {
      throw new RuntimeException();
    }
  }

  /**
   * execute command using ProcessBuilder.
   */
  static synchronized boolean execCmd(String[] cmd, List<String> output) {
    BufferedReader reader = null;
    try {
      Process process = null;
      ProcessBuilder pbuilder = new ProcessBuilder(cmd);
      pbuilder.redirectErrorStream(true);
      process = pbuilder.start();
      reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
      String line = null;
      while ((line = reader.readLine()) != null) {
        if (output != null) {
          output.add(line);
        }
      }
      process.waitFor();
      if (process.exitValue() != 0) {
        return false;
      }
    } catch (IOException e) {
      logger.warn("IOException happens {}", e);
      return true;
    } catch (InterruptedException e) {
      logger.warn("InterruptedException happens {}", e);
      return true;
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          logger.error("caught an exception when try to close reader {}", e);
        }
      }
    }
    return true;
  }
}
