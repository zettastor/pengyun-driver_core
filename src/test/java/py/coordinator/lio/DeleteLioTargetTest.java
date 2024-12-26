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
