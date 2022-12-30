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
