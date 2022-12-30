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

package py.coordinator.lio;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import py.drivercontainer.lio.saveconfig.LioTarget;
import py.drivercontainer.lio.saveconfig.jsonobj.SaveConfigImpl;

public class DeleteLioTargetTest extends LioTargetTestBase {

  @Before
  public void initailize() throws Exception {
    super.init();
  }

  /**
   * Create a targetName firstly ,after delete it ,the volumeList get from saveconfig.json is null
   */
  @Test
  public void deleteTargetTest() throws Exception {
    SaveConfigImpl saveConfigImpl = saveConfigBuilder.build();
    List<LioTarget> beforeTargets = saveConfigImpl.getTargets();
    Assert.assertTrue(lioManager.getVolumeList(beforeTargets).contains(targetName));
    try {
      lioManager.deleteTarget(targetName, "/tmp/dev0", 1234, 0);
    } catch (Exception e) {
      logger.warn("Catch an exception ", e);
    }
    List<LioTarget> afterTargets;

    saveConfigImpl.load();
    afterTargets = saveConfigImpl.getTargets();
    Assert.assertTrue(lioManager.getVolumeList(afterTargets).size() == 0);
  }

  @After
  public void clean() {
    FileUtils.deleteQuietly(Paths.get("/tmp/saveconfigTest").toFile());
  }

  public static class FakeLioManager extends LioManager {

    @Override

    public List<String> getAvailabeNbdDeviceList() {

      List<String> nbdList = new ArrayList<String>();
      nbdList.add("/tmp/dev0");
      return nbdList;
    }

    @Override
    public boolean executeCommand(String command) {
      return true;

    }
  }

}
