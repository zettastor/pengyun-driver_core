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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import py.common.RequestIdBuilder;
import py.drivercontainer.lio.saveconfig.LioLun;
import py.drivercontainer.lio.saveconfig.LioPortal;
import py.drivercontainer.lio.saveconfig.LioStorage;
import py.drivercontainer.lio.saveconfig.LioTarget;
import py.drivercontainer.lio.saveconfig.LioTpg;
import py.drivercontainer.lio.saveconfig.jsonobj.ConfigFileConstant;
import py.drivercontainer.lio.saveconfig.jsonobj.SaveConfigBuilder;
import py.drivercontainer.lio.saveconfig.jsonobj.SaveConfigImpl;
import py.drivercontainer.utils.DriverContainerUtils;
import py.processmanager.utils.PmUtils;
import py.test.TestBase;

/**
 * The class use to test create lio driver in different case ,such as target exist but storage and
 * lun not exist adn so on...
 */
public class LioCommandManagerTest extends TestBase {

  private static SaveConfigImpl saveConfigImpl;
  private static SaveConfigImpl templateSaveConfigImpl;
  private static LioNameBuilder lioNameBuilder;
  long volumeId;
  int snapshotId;
  private LioCommandManager lioCommandManager;
  private String filePath = "/tmp/saveconfigTest/LioCommandManagerTest.json";
  private String templatePath = "src/test/resources/config/lio.json";
  private String templateNullContentPath = "src/test/resources/config/initialtargetname.json";
  private String targetName = "iqn.2017-08.zettastor.iqn:1376131110553535176-0";

  /**
   * xx.
   */
  @Before
  public void init() throws Exception {
    LioCommandManagerConfiguration lioCmdMaConfig;
    LioCommandManagerConfiguration lioCmdMaConfigForTemplate;

    final LioManagerConfiguration lioMaConfig;
    final LioManagerConfiguration lioMaConfigForTemplate;

    final SaveConfigBuilder saveConfigBuilder;
    final SaveConfigBuilder saveConfigBuilderForTemplate;

    lioCmdMaConfig = new LioCommandManagerConfiguration();
    lioCmdMaConfig.setDefaultSaveConfigFilePath(filePath);

    lioCmdMaConfigForTemplate = new LioCommandManagerConfiguration();
    lioCmdMaConfigForTemplate.setDefaultSaveConfigFilePath(templatePath);

    lioMaConfig = new LioManagerConfiguration();
    lioMaConfig.setRestoreCommand("ls /tmp");

    lioMaConfigForTemplate = new LioManagerConfiguration();
    lioMaConfigForTemplate.setRestoreCommand("ls /tmp");

    saveConfigBuilder = new SaveConfigBuilder();
    saveConfigBuilder.setLioCmdMaConfig(lioCmdMaConfig);
    saveConfigBuilder.setLioMaConfig(lioMaConfig);

    saveConfigBuilderForTemplate = new SaveConfigBuilder();
    saveConfigBuilderForTemplate.setLioCmdMaConfig(lioCmdMaConfigForTemplate);
    saveConfigBuilderForTemplate.setLioMaConfig(lioMaConfigForTemplate);

    final String lioCreateStorage = "/usr/bin/targetcli %s create name=%s dev=%s";
    final String lioCreateTarget = "/usr/bin/targetcli %s create wwn=%s";
    final String lioCreateLun = "/usr/bin/targetcli %s create %s";
    final String lioCreatePortal = "/usr/bin/targetcli %s create %s %s";
    final String lioSaveConfig = "/usr/bin/targetcli saveconfig /etc/target/saveconfig.json";
    final String bindNbdCmd = "/opt/pyd/pyd-client %s %s %s %s";
    final String unbindNbdCmd = "/opt/pyd/pyd-client -f %s";
    final String setEmulateTpuValueCmd = "ls";
    DriverContainerUtils.init();
    File file = new File(filePath);
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      file.createNewFile();
    }
    initiatorSaveconfigFile();
    lioNameBuilder = new LioNameBuilderImpl();
    templateSaveConfigImpl = saveConfigBuilderForTemplate.build();
    volumeId = PmUtils.getCurrentProcessPid();
    snapshotId = PmUtils.getCurrentProcessPid();
    lioCommandManager = new FakeLioCommandManager();
    saveConfigImpl = new SaveConfigImpl(filePath);
    saveConfigImpl.setRestoreCommand("ls /tmp");
    lioCommandManager.setSaveConfigBuilder(saveConfigBuilder);
    lioCommandManager.setCreateStorageCmd(lioCreateStorage);
    lioCommandManager.setCreateTargetCmd(lioCreateTarget);
    lioCommandManager.setCreateLunCmd(lioCreateLun);
    lioCommandManager.setCreatePortalCmd(lioCreatePortal);
    lioCommandManager.setSaveConfigCmd(lioSaveConfig);
    lioCommandManager.setBindNbdCmd(bindNbdCmd);
    lioCommandManager.setUnbindNbdCmd(unbindNbdCmd);
    lioCommandManager.setSetEmulateTpuValueCmd(setEmulateTpuValueCmd);
    clearFileAndMemory(filePath);
  }

  /**
   * target , storage ,lun all not exist case 1 : pydDev from request is null , we will choose an
   * available device to create targetName case 2 :pydDev from request is "java", we will use device
   * "java" to create targetName.
   */
  @Test
  public void targetStorageLunAllNotExistTest() throws Exception {

    //case 1:given pydDev is null ,will return /tmp/dev0
    clearFileAndMemory(filePath);
    Assert.assertFalse(lioCommandManager.existTarget(targetName));
    String returnDev = lioCommandManager
        .createTarget(targetName, "localhost", "localhost", null, volumeId, snapshotId);
    Assert.assertTrue(returnDev.equals("/tmp/dev0"));
    Assert.assertTrue(lioCommandManager.existTarget(targetName));
    Assert.assertTrue(lioCommandManager.existStorage("/tmp/dev0"));
    Assert.assertTrue(lioCommandManager.existLun(targetName, "/tmp/dev0"));

    //case 2 : pyd is not bull
    clearFileAndMemory(filePath);
    String pydDev = "java";
    Assert.assertFalse(lioCommandManager.isPydAlive(volumeId, snapshotId, pydDev));
    Assert.assertFalse(lioCommandManager.existTarget(targetName));
    Assert.assertFalse(lioCommandManager.existStorage(pydDev));
    Assert.assertFalse(lioCommandManager.existLun(targetName, pydDev));
    returnDev = lioCommandManager
        .createTarget(targetName, "localhost", "localhost", pydDev, volumeId, snapshotId);
    Assert.assertTrue(returnDev.equals(pydDev));
    Assert.assertTrue(lioCommandManager.existTarget(targetName));
    Assert.assertTrue(lioCommandManager.existStorage(pydDev));
    Assert.assertTrue(lioCommandManager.existLun(targetName, pydDev));

  }

  /**
   * target not exist , lun not exist , storage exist case 1 : pydDev from request is null , target
   * exist but lun not exist ,so it can not find the storage belong to the target, so it will choose
   * a new available device to creat targetName.
   *
   * <p>case 2 :  pydDev from request is not null and equals which in storage , it will use the
   * incoming device to create target and lun for the targetName
   */
  @Test
  public void targetLunAllNotExistButStorageExistTest() throws Exception {
    //case 1 :pydDev is  null
    clearFileAndMemory(filePath);
    String pydDev = "java";
    lioCommandManager.newStorageWrap("java", targetName);
    Assert.assertFalse(lioCommandManager.existTarget(targetName));
    Assert.assertTrue(lioCommandManager.existStorage(pydDev));
    String returnDev = lioCommandManager
        .createTarget(targetName, "localhost", "localhost", null, volumeId, snapshotId);
    Assert.assertTrue(returnDev.equals("/tmp/dev0"));
    Assert.assertTrue(lioCommandManager.existTarget(targetName));
    Assert.assertTrue(lioCommandManager.existStorage("/tmp/dev0"));

    //case 2 :pydDev is not null
    clearFileAndMemory(filePath);
    lioCommandManager.newStorageWrap(pydDev, targetName);
    Assert.assertFalse(lioCommandManager.existTarget(targetName));
    Assert.assertTrue(lioCommandManager.existStorage(pydDev));
    returnDev = lioCommandManager
        .createTarget(targetName, "localhost", "localhost", "java", volumeId, snapshotId);
    Assert.assertTrue(returnDev.equals(pydDev));
    Assert.assertTrue(lioCommandManager.existTarget(targetName));

  }

  /**
   * target exist , lun not exist , storage not exist case 1 : pydDev is null from request , it will
   * choose an available device to create lun and storage for targetName case 2 : pydDev is not null
   * from request , it will use the incoming device to create lun and storage for targetName.
   */
  @Test
  public void targetExistStorageLunNotExistTest() throws Exception {
    //case 1 :pydDev is null
    clearFileAndMemory(filePath);
    lioCommandManager.newTargetWrap(targetName, "localhost", null);
    Assert.assertTrue(lioCommandManager.existTarget(targetName));
    String returnDev = lioCommandManager
        .createTarget(targetName, "localhost", "localhost", null, volumeId, snapshotId);
    Assert.assertTrue(returnDev.equals("/tmp/dev0"));
    Assert.assertTrue(lioCommandManager.existTarget(targetName));
    Assert.assertTrue(lioCommandManager.existStorage("/tmp/dev0"));

    //case 2 :pydDev is not null
    clearFileAndMemory(filePath);
    String pydDev = "java";
    lioCommandManager.newTargetWrap(targetName, "localhost", pydDev);
    Assert.assertTrue(lioCommandManager.existTarget(targetName));
    Assert.assertFalse(lioCommandManager.existStorage(pydDev));
    returnDev = lioCommandManager
        .createTarget(targetName, "localhost", "localhost", pydDev, volumeId, snapshotId);
    Assert.assertTrue(returnDev.equals(pydDev));
    Assert.assertTrue(lioCommandManager.existTarget(targetName));
    Assert.assertTrue(lioCommandManager.existStorage(pydDev));
  }

  /**
   * taregt exist , storage exist , lun not exist case 1 :  pydDev from request is null , target
   * exist but lun not exist ,so it can not find the storage belong to the target, so it will choose
   * a new available device to creat targetName.
   *
   * <p>case 2 : pydDev from request is not null and equals which in storage , it will use the
   * incoming device to create lun for the targetName.
   */
  @Test
  public void targetStorageExistLunNotExistTest() throws Exception {
    //case 1 :pydDev is null
    clearFileAndMemory(filePath);
    String pydDev = "java";
    lioCommandManager.newTargetWrap(targetName, "localhost", pydDev);
    lioCommandManager.newStorageWrap(pydDev, targetName);
    Assert.assertTrue(lioCommandManager.existTarget(targetName));
    Assert.assertTrue(lioCommandManager.existStorage(pydDev));
    Assert.assertFalse(lioCommandManager.existLun(targetName, pydDev));
    String returnDev = lioCommandManager
        .createTarget(targetName, "localhost", "localhost", null, volumeId, snapshotId);
    Assert.assertTrue(returnDev.equals("/tmp/dev0"));
    Assert.assertTrue(lioCommandManager.existTarget(targetName));
    Assert.assertTrue(lioCommandManager.existStorage("/tmp/dev0"));

    //case 2 :pydDev is not null
    clearFileAndMemory(filePath);
    lioCommandManager.newTargetWrap(targetName, "localhost", pydDev);
    lioCommandManager.newStorageWrap(pydDev, targetName);
    Assert.assertTrue(lioCommandManager.existTarget(targetName));
    Assert.assertTrue(lioCommandManager.existStorage(pydDev));
    Assert.assertFalse(lioCommandManager.existLun(targetName, pydDev));
    returnDev = lioCommandManager
        .createTarget(targetName, "localhost", "localhost", pydDev, volumeId, snapshotId);
    Assert.assertTrue(returnDev.equals(pydDev));
    Assert.assertTrue(lioCommandManager.existTarget(targetName));
    Assert.assertTrue(lioCommandManager.existStorage(pydDev));


  }

  /**
   * target exist , lun exist , storage exist case1 :  pydDev from request is null , it will get the
   * device used in targetName from target and storage ,and return it case 2 :  pydDev from request
   * is not  null and equals which used in storage ,will return it.
   */
  @Test
  public void targetStorageLunAllExist() throws Exception {
    //case 1 :pydDev is null , return dev is which bind in storage .
    clearFileAndMemory(filePath);
    String pydDev = "java";
    lioCommandManager.newTargetWrap(targetName, "localhost", pydDev);
    lioCommandManager.newStorageWrap(pydDev, targetName);
    lioCommandManager.newLunWrap(targetName, pydDev);
    Assert.assertTrue(lioCommandManager.existTarget(targetName));
    Assert.assertTrue(lioCommandManager.existStorage(pydDev));
    Assert.assertTrue(lioCommandManager.existLun(targetName, pydDev));
    String returnDev = lioCommandManager
        .createTarget(targetName, "localhost", "localhost", null, volumeId, snapshotId);
    Assert.assertTrue(returnDev.equals(pydDev));
    Assert.assertTrue(lioCommandManager.existTarget(targetName));
    Assert.assertTrue(lioCommandManager.existStorage(pydDev));

    //case 2 :pydDev is not null ,return dev is which bind in storage
    clearFileAndMemory(filePath);
    lioCommandManager.newTargetWrap(targetName, "localhost", pydDev);
    lioCommandManager.newStorageWrap(pydDev, targetName);
    lioCommandManager.newLunWrap(targetName, pydDev);
    Assert.assertTrue(lioCommandManager.existTarget(targetName));
    Assert.assertTrue(lioCommandManager.existStorage(pydDev));
    Assert.assertTrue(lioCommandManager.existLun(targetName, pydDev));
    returnDev = lioCommandManager
        .createTarget(targetName, "localhost", "localhost", pydDev, volumeId, snapshotId);
    Assert.assertTrue(returnDev.equals(pydDev));
    Assert.assertTrue(lioCommandManager.existTarget(targetName));
    Assert.assertTrue(lioCommandManager.existStorage(pydDev));

  }

  @Test
  public void deleteTargetNameTest() throws Exception {
    clearFileAndMemory(filePath);
    Assert.assertFalse(lioCommandManager.existTarget(targetName));
    String returnDev = lioCommandManager
        .createTarget(targetName, "localhost", "localhost", null, volumeId, snapshotId);
    Assert.assertTrue(returnDev.equals("/tmp/dev0"));
    Assert.assertTrue(lioCommandManager.existTarget(targetName));
    Assert.assertTrue(lioCommandManager.existStorage("/tmp/dev0"));
    Assert.assertTrue(lioCommandManager.existLun(targetName, "/tmp/dev0"));

    lioCommandManager.deleteTarget(targetName, "/tmp/dev0", volumeId, snapshotId);

    Assert.assertFalse(lioCommandManager.existTarget(targetName));
    Assert.assertFalse(lioCommandManager.existStorage("/tmp/dev0"));
    Assert.assertFalse(lioCommandManager.existLun(targetName, "/tmp/dev0"));


  }

  @Test
  public void testListClientIps() throws Exception {
    final String[] ips = {"[fe80::5054:ff:fe54:4d0c]", "10.0.1.16"};
    final File sessionScript = new File("/tmp/session-script." + getClass().getSimpleName());

    lioCommandManager
        .createTarget(targetName, "localhost", "localhost", "/dev/pyd1", volumeId, snapshotId);

    for (String ip : ips) {
      StringBuilder sessionScriptCmds;

      sessionScriptCmds = new StringBuilder();
      sessionScriptCmds.append("echo \"mapped-lun: 0 backstore: block/pyd1 mode: rw\"");
      sessionScriptCmds.append("\n");
      sessionScriptCmds
          .append(String
              .format("echo \"    address: %s (TCP)  cid: 0 connection-state: LOGGED_IN\"", ip));
      sessionScriptCmds.append("\n");

      BufferedWriter writer;

      writer = new BufferedWriter(new FileWriter(sessionScript));
      try {
        writer.write(sessionScriptCmds.toString());
        writer.flush();
        writer.close();
      } catch (Exception e) {
        writer.close();
        if (sessionScript.exists()) {
          sessionScript.delete();
        }
      }

      List<String> clientIps;

      try {
        lioCommandManager.setSessionCommand("bash " + sessionScript.getAbsolutePath());
        clientIps = lioCommandManager.listClientIps(targetName);
        Assert.assertEquals(1, clientIps.size());
        Assert.assertTrue(clientIps.get(0).contains(InetAddress.getByName(ip).getHostAddress()));
      } finally {
        if (sessionScript.exists()) {
          sessionScript.delete();
        }
      }
    }
  }

  /**
   * xx.
   */
  public void clearFileAndMemory(String filePath) {
    SaveConfigImpl saveConfig = new SaveConfigImpl(templateNullContentPath);
    saveConfig.setRestoreCommand("ls /tmp");
    saveConfig.persist(new File(filePath));
    saveConfigImpl.removeAllStorages();
    saveConfigImpl.removeAllTargets();
  }

  /**
   * xx.
   */
  public void initiatorSaveconfigFile() {
    SaveConfigImpl saveConfig = new SaveConfigImpl(templateNullContentPath);
    saveConfig.setRestoreCommand("ls /tmp");
    saveConfig.persist(new File(filePath));
  }

  @After
  public void clean() {
    FileUtils.deleteQuietly(Paths.get(filePath).toFile());
  }

  public class FakeLioCommandManager extends LioCommandManager {

    @Override
    public List<String> getAvailabeNbdDeviceList() {
      List<String> nbdList = new ArrayList<String>();
      nbdList.add("/tmp/dev0");
      return nbdList;
    }

    @Override
    public boolean newTargetWrap(String targetName, String targetNameIp, String nbdDev) {
      LioTarget targetTemple = templateSaveConfigImpl.getTargets().get(0);
      targetTemple.setWwn(targetName);
      logger.debug("targetTemple is:{}", targetTemple);
      try {
        saveConfigImpl.addTarget(targetTemple);
        saveConfigImpl.persist(new File(filePath));
      } catch (Exception e) {
        logger.warn("catch an exception", e);
      }

      return true;
    }

    @Override
    public boolean newStorageWrap(String nbdDev, String targetName) {
      LioStorage storageTemple = templateSaveConfigImpl.getStorages().get(0);
      storageTemple.setDev(nbdDev);
      File nbdDevName = new File(nbdDev);
      storageTemple.setName(nbdDevName.getName());
      storageTemple.setWwn(String.valueOf(RequestIdBuilder.get()));
      try {
        saveConfigImpl.addStorage(storageTemple);
        saveConfigImpl.persist(new File(filePath));
      } catch (Exception e) {
        logger.warn("catch an exception", e);
      }
      return true;
    }

    @Override
    public boolean newLunWrap(String targetName, String nbdDev) {
      LioTarget target = saveConfigImpl.getTargets().get(0);
      saveConfigImpl.removeTarget(target.getWwn());
      LioTpg tpg = target.getTpgs().get(0);
      LioLun lunTemplate = tpg.getLuns().get(0);
      File nbdDevName = new File(nbdDev);
      lunTemplate.setStorageObj(ConfigFileConstant.BLOCKPATH + "/" + nbdDevName.getName());

      try {
        saveConfigImpl.addTarget(target);
        saveConfigImpl.persist(new File(filePath));
      } catch (Exception e) {
        logger.warn("catch an exception", e);
      }
      return true;
    }

    @Override
    public boolean newPortalWrap(String targetName, String targetNameIp) {
      LioTarget target = saveConfigImpl.getTargets().get(0);
      saveConfigImpl.removeTarget(target.getWwn());
      LioTpg tpg = target.getTpgs().get(0);
      LioPortal portal = tpg.getPortals().get(0);
      portal.setIpAddr(targetNameIp);
      portal.setPort(3260);
      try {
        saveConfigImpl.addTarget(target);
        saveConfigImpl.persist(new File(filePath));
      } catch (Exception e) {
        logger.warn("catch an exception", e);
      }
      return true;
    }

    @Override
    public boolean bindNbdDriverWrap(long volumeId, int snapshotId, String ipAddress,
        String nbdDev) {
      return true;
    }

    @Override
    public boolean deleteTargetWrap(String targetName) {
      logger.warn("delete target");
      saveConfigImpl.removeTarget(targetName);
      saveConfigImpl.persist(new File(filePath));
      return true;
    }

    @Override
    public boolean deleteStorageWrap(String nbdDev) {
      logger.warn("delete storage");
      saveConfigImpl.removeStorage("/tmp/dev0");
      saveConfigImpl.persist(new File(filePath));
      return true;
    }
  }

}
