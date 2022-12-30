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
