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

package py.drivercontainer.iscsi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.driver.DriverMetadata;
import py.driver.DriverType;
import py.drivercontainer.driver.DriverWorkspaceHelper;
import py.drivercontainer.driver.DriverWorkspaceProvider;
import py.icshare.DriverKey;
import py.test.TestBase;

public class DriverWorkspaceHelperTest extends TestBase {

  private static final Logger logger = LoggerFactory.getLogger(DriverWorkspaceHelperTest.class);
  DriverWorkspaceHelper driverWorkspaceHelper;
  DriverWorkspaceProvider driverWorkspaceProvider;
  DriverKey driverKey1;
  DriverKey driverKey2;

  @Before
  public void init() {
    driverWorkspaceHelper = new DriverWorkspaceHelper();
    driverWorkspaceProvider = new DriverWorkspaceProvider();
    driverKey1 = new DriverKey(1, 1L, 1, DriverType.ISCSI);
    driverKey2 = new DriverKey(2, 2L, 2, DriverType.NBD);
  }

  @Test
  public void getWorkspaceTest() {
    String workSpace = driverWorkspaceHelper.getWorkspace("/tmp/var", driverKey1);
    Assert.assertTrue(workSpace.equals("/tmp/var/1/1/ISCSI/"));
  }

  @Test
  public void getDriverKeyTest() {
    DriverKey driver = driverWorkspaceHelper.getDriverKey(1, "/tmp/var", "/1/1/ISCSI");
    Assert.assertTrue(driverKey1.equals(driver));
  }

  @Test
  public void scanDriversTest() {
    File fileIscsi = new File("/tmp/var/1/1/ISCSI/SPID");
    File fileNbd = new File("/tmp/var/2/2/NBD/SPID");
    fileIscsi.getParentFile().mkdirs();
    fileNbd.getParentFile().mkdirs();
    try {
      fileIscsi.createNewFile();
      fileNbd.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
    DriverMetadata driverMetadata1 = new DriverMetadata();
    driverMetadata1.setDriverContainerId(1);
    driverMetadata1.setVolumeId(1L);
    driverMetadata1.setSnapshotId(1);
    driverMetadata1.setDriverType(DriverType.ISCSI);
    driverMetadata1.saveToFile(Paths.get(fileIscsi.toString()));
    DriverMetadata driverMetadata2 = new DriverMetadata();
    driverMetadata2.setDriverContainerId(2);
    driverMetadata2.setVolumeId(2L);
    driverMetadata2.setSnapshotId(2);
    driverMetadata2.setDriverType(DriverType.NBD);
    driverMetadata2.saveToFile(Paths.get(fileNbd.toString()));
    List<DriverMetadata> drivers = driverWorkspaceHelper.scanDrivers("/tmp/var");
    Assert.assertTrue(drivers.size() == 2);

  }

  @After
  public void clean() {
    FileUtils.deleteQuietly(Paths.get("/tmp/var").toFile());
  }


}
