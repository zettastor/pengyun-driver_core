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

package py.drivercontainer.utils;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.Utils;
import py.processmanager.utils.PmUtils;
import py.test.TestBase;

/**
 * A class contains some tests for {@link DriverContainerUtils}.
 *
 */
public class DriverContainerUtilsTest extends TestBase {

  private static final Logger logger = LoggerFactory.getLogger(DriverContainerUtilsTest.class);

  @Override
  public void init() throws Exception {
    super.init();
    super.setLogLevel(Level.ALL);
    DriverContainerUtils.init();
  }

  @Test
  public void testProcessExist() throws Exception {
    int port;
    int processId;

    processId = PmUtils.getCurrentProcessPid();
    port = 54321;
    while (!Utils.isPortAvailable(port)) {
      port++;
    }

    ServerSocket ss = null;
    DatagramSocket ds = null;
    try {
      ss = new ServerSocket(port);
      ss.setReuseAddress(true);

      ds = new DatagramSocket(port);
      ds.setReuseAddress(true);

      while (Utils.isPortAvailable(port)) {
        Thread.sleep(1000);
      }

      super.setLogLevel(Level.DEBUG);
      Assert.assertTrue(DriverContainerUtils.processExist(processId));
    } catch (IOException e) {
      logger.error("caught exception", e);
    } finally {
      if (ds != null) {
        ds.close();
      }

      if (ss != null) {
        try {
          ss.close();
        } catch (IOException e) {
          logger.error("caught exception", e);
        }
      }
    }
  }

  @Test
  public void testProccessNotExist() throws Exception {
    int port;
    int processId;
    processId = PmUtils.getCurrentProcessPid();
    port = 54321;
    while (!Utils.isPortAvailable(port)) {
      port++;
    }

    Assert.assertTrue(DriverContainerUtils.processExist(processId));
  }

  @Test
  public void testPyddevOccupyed() throws IOException {
    String devPyd = "/usr/bin/bash";
    Assert.assertTrue(DriverContainerUtils.deviceIsOccupyed(devPyd));
  }

  @Test
  public void testPyddevNotOccupyed() throws IOException {
    File file = new File("/tmp/testNotOccupyed");
    file.mkdirs();
    String devPyd = "/tmp/testNotOccupyed";
    Assert.assertFalse(DriverContainerUtils.deviceIsOccupyed(devPyd));
  }


  @After
  public void clean() throws Exception {
    DriverContainerUtils.destroy();
    FileUtils.deleteQuietly(Paths.get("/tmp/testNotOccupyed").toFile());
  }
}
