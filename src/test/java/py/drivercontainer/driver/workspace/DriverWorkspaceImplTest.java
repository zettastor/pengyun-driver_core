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
