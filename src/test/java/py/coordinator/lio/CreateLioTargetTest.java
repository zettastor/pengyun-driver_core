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
import py.drivercontainer.lio.saveconfig.jsonobj.SaveConfigBuilder;
import py.drivercontainer.lio.saveconfig.jsonobj.SaveConfigImpl;

public class CreateLioTargetTest extends LioTargetTestBase {

  static boolean isAlive = false;

  /**
   * xx.
   */
  @Before
  public void initailize() throws Exception {
    super.init();

    saveConfigBuilder = new SaveConfigBuilder();
    saveConfigBuilder.setLioCmdMaConfig(lioCmdMaConfig);
    saveConfigBuilder.setLioMaConfig(lioManagerCon);

    lioManager = new FakeLioManager();
    lioManager.setBindNbdCmd("/opt/pyd/pyd-client -p %s %s %s");
    lioManager.setTemplatePath(tempPath);
    lioManager.setFilePath(filePath);
    lioManager.setSaveConfigBuilder(saveConfigBuilder);
    lioManager.setLioManagerCon(lioManagerCon);
    lioManager.setLioNameBuilder(lioNameBuilder);
  }

  /**
   * Test targetName and pyd-client not exist.
   */
  @Test
  public void targetNameAndPydNotExist() throws Exception {
    String device = null;

    try {
      isAlive = lioManager.isPydAlive(1234, 0, "/tmp/pyd0");
      Assert.assertFalse(isAlive);
      device = lioManager.createTarget(targetName, "localhost", "localhost", pydDev, 1234, 0);
    } catch (Exception e) {
      logger.warn("Catch an exception {}", e);
    }
    logger.warn("****{}", device);
    Assert.assertTrue(device.equals(pydDev));
    Assert.assertTrue(isAlive);

    SaveConfigImpl saveConfigImpl = saveConfigBuilder.build();
    saveConfigImpl = new SaveConfigImpl(filePath);
    List<LioTarget> targets = saveConfigImpl.getTargets();
    List<String> volumeList = lioManager.getVolumeList(targets);
    Assert.assertTrue(volumeList.size() == 1);
    Assert.assertTrue(volumeList.contains(targetName));

  }

  /**
   * pyd dev is not null,pyd-client process exist .and then test create targetName.
   */
  @Test
  public void targetNameNotExitProcessExistTest() throws Exception {
    String device = null;
    try {
      boolean isAlive = lioManager.isPydAlive(2345, 0, "/tmp/pyd0");
      Assert.assertTrue(isAlive);
      device = lioManager.createTarget(targetName, "localhost", "localhost", "/tmp/pyd0", 2345, 0);
    } catch (Exception e) {
      logger.warn("Catch an exception ", e);
    }
    Assert.assertEquals(pydDev, device);

    SaveConfigImpl saveConfigImpl = saveConfigBuilder.build();
    saveConfigImpl = new SaveConfigImpl(filePath);
    List<LioTarget> targets = saveConfigImpl.getTargets();
    List<String> volumeList = lioManager.getVolumeList(targets);
    Assert.assertTrue(volumeList.contains(targetName));

  }

  /**
   * pyd dev is null ,pyd-client and targetName not exist.
   */
  @Test
  public void createOneTargetWithNullPyd() {
    String device = null;
    try {
      device = lioManager.createTarget(targetName, "localhost", "localhost", null, 1234, 0);
    } catch (Exception e) {
      logger.warn("Catch an exception:{}", e);
    }
    Assert.assertTrue(device.equals("/tmp/pyd0"));
  }

  @After
  public void clean() {
    FileUtils.deleteQuietly(Paths.get("/tmp/saveconfigTest").toFile());
  }

  /**
   * xx.
   */
  public static class FakeLioManager extends LioManager {

    /**
     * If some machine doesn't have nbd module in kernel, use this function.
     */
    @Override

    public List<String> getAvailabeNbdDeviceList() {
      List<String> nbdList = new ArrayList<String>();
      nbdList.add("/tmp/pyd0");
      return nbdList;
    }

    @Override
    public boolean executeCommand(String command) {
      String[] str = command.split(" ");
      if (str[0].equals("/opt/pyd/pyd-client")) {
        isAlive = true;
      }

      return true;
    }

    @Override
    public boolean isPydAlive(long volumeId, int snapShotid, String pydDev) throws Exception {
      boolean alive = false;
      if (volumeId == 2345) {
        alive = true;
      }
      return alive;

    }

  }

}
