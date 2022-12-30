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
