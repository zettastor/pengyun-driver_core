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

package py.drivercontainer.driver.version.file;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.drivercontainer.driver.version.Production;
import py.test.TestBase;

/**
 * A class contains some tests for {@link VersionImplTest}.
 *
 */
public class VersionImplTest extends TestBase {

  private static final Logger logger = LoggerFactory.getLogger(VersionImplTest.class);

  @Override
  public void init() throws Exception {
    super.init();
  }

  @Test
  public void testParseVersionSuccess() throws Exception {
    final String versionStr = "2.3.0-internal-20170908164213";

    VersionImpl version = VersionImpl.get(versionStr);
    Assert.assertNotNull(version);

    Assert.assertEquals("2.3.0", version.getPrimaryVersion());
    Assert.assertEquals(Production.INTERNAL, version.getProduction());
    Assert.assertEquals(2017, version.getTimestamp().getYear());
    Assert.assertEquals(9, version.getTimestamp().getMonth());
    Assert.assertEquals(8, version.getTimestamp().getDayOfMonth());
    Assert.assertEquals(16, version.getTimestamp().getHourOfDay());
    Assert.assertEquals(42, version.getTimestamp().getMinute());
    Assert.assertEquals(13, version.getTimestamp().getSecond());

    Assert.assertEquals(versionStr, version.format());
  }

  @Test
  public void testParseVersionFailure() throws Exception {
    String versionStr = "2.3.0-123-20170908164213";

    VersionImpl version = VersionImpl.get(versionStr);
    Assert.assertNull(version);

    versionStr = "2.3.0_";
    version = VersionImpl.get(versionStr);
    Assert.assertNull(version);
  }
}
