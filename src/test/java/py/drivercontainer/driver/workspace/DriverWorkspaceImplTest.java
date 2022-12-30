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

package py.drivercontainer.driver.workspace;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.RequestIdBuilder;
import py.driver.DriverType;
import py.drivercontainer.driver.version.Version;
import py.drivercontainer.driver.version.file.VersionImpl;
import py.icshare.DriverKey;
import py.instance.InstanceId;
import py.test.TestBase;

/**
 * A class contains some tests for {@link DriverWorkspaceImpl}.
 *
 */
public class DriverWorkspaceImplTest extends TestBase {

  private static final Logger logger = LoggerFactory.getLogger(DriverWorkspaceImplTest.class);

  @Override
  public void init() throws Exception {
    super.init();
  }

  /**
   * In this test plan, it constructs driver workspace with given driver key and driver version. And
   * this test plan expects a correct result.
   */
  @Test
  public void testBuildPath() throws Exception {
    Version version;
    DriverKey driverKey;
    final DriverWorkspace workspaceAfterFormat;
    final DriverWorkspace workspaceAfterDecoding;

    version = VersionImpl.get("2.3.0-internal-20170911142835");
    driverKey = new DriverKey(0, RequestIdBuilder.get(), 0, DriverType.NBD);
    workspaceAfterFormat = new DriverWorkspaceImpl("/tmp/", version, driverKey);

    String path;

    path = workspaceAfterFormat.getPath();
    logger.debug("Driver workspace path is {}", path);
    workspaceAfterDecoding = new DriverWorkspaceImpl(
        new InstanceId(driverKey.getDriverContainerId()), path);

    Assert.assertEquals(workspaceAfterFormat, workspaceAfterDecoding);
  }
}
