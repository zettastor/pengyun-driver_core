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

import static java.lang.Thread.sleep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.Utils;
import py.drivercontainer.lio.saveconfig.LioLun;
import py.drivercontainer.lio.saveconfig.LioNodeAcl;
import py.drivercontainer.lio.saveconfig.LioStorage;
import py.drivercontainer.lio.saveconfig.LioTarget;
import py.drivercontainer.lio.saveconfig.LioTpg;
import py.drivercontainer.lio.saveconfig.jsonobj.ConfigFileConstant;
import py.drivercontainer.lio.saveconfig.jsonobj.SaveConfigBuilder;
import py.drivercontainer.lio.saveconfig.jsonobj.SaveConfigImpl;
import py.drivercontainer.utils.DriverContainerUtils;

public abstract class LioTargetManager {

  private static final Logger logger = LoggerFactory.getLogger(LioTargetManager.class);
  protected String nbdDeviceName;
  protected String bindNbdCmd;
  protected String unbindNbdCmd;
  protected LioNameBuilder lioNameBuilder;
  protected String sessionCommand;
  protected SaveConfigBuilder saveConfigBuilder;

  private int nbdDeviceMaxNum;

  private String nbdMaxFile = "/sys/module/pyd/parameters/nbds_max";


  /**
   * xx.
   */
  public static boolean isPydAliveStatic(long volumeId, int snapShotid, String pydDev)
      throws Exception {
    String[] command = {"/bin/sh", "-c",
        "ps -ef | grep -E \"" + volumeId + "\\ +" + snapShotid + "\\ +.*" + pydDev + "\""
            + " | grep -v grep"};
    List<String> normalList = new ArrayList<>();
    class ProcessorCon implements Utils.CommandProcessor {

      @Override
      public List<String> getNormalStream(String line) {
        logger.debug("psef for pydDev Command normal message output: \"{}\"", line);
        normalList.add(line);
        return normalList;
      }

      @Override
      public List<String> getErrorStream(String line) {
        return null;
      }
    }

    ProcessorCon processorCon = new ProcessorCon();
    BufferedReader normReader = null;
    BufferedReader errorReader = null;
    try {
      Process process = Runtime.getRuntime().exec(command);
      String line = null;
      normReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      while ((line = normReader.readLine()) != null) {
        processorCon.getNormalStream(line);
      }
      StringBuilder errorMessageBuilder = new StringBuilder();
      errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      while ((line = errorReader.readLine()) != null) {
        errorMessageBuilder.append(line);
        errorMessageBuilder.append("\n");
        processorCon.getErrorStream(line);
      }

      process.waitFor();
      if (process.exitValue() != 0) {
        logger.debug("no output when execute command {}: \"{}\"", command,
            errorMessageBuilder.toString());
        return false;
      }
    } catch (Exception e) {
      logger.error("Caught an exception when execute command {}", command, e);
      throw e;
    } finally {
      if (errorReader != null) {
        try {
          errorReader.close();
        } catch (Exception e) {
          logger.error("fail to close error reader {}.", e);
        }
      }
      if (normReader != null) {
        try {
          normReader.close();
        } catch (Exception e) {
          logger.error("fail to close normal reader {}.", e);
        }
      }
    }

    boolean isPydAlive = false;
    if (normalList != null && !normalList.isEmpty()) {
      isPydAlive = true;
    }

    return isPydAlive;
  }


  /**
   * xx.
   */
  public static boolean executeCommandStatic(String command) {
    /* the reason of adding tryTime: when succeed to create acl and fail to set userid,
     * trying more time can increase rate of success. it happened in import and export ltd.
     *  environment.
     * */
    logger.debug("targetcli command start {}", command);
    boolean ret = false;
    int tryTime = 3;
    BufferedReader normReader = null;
    BufferedReader errorReader = null;
    try {
      while (tryTime > 0) {
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
        String line = null;
        StringBuilder errorMessageBuilder = new StringBuilder();
        errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = errorReader.readLine()) != null) {
          errorMessageBuilder.append(line);
          errorMessageBuilder.append("\n");
        }

        StringBuilder normMessageBuilder = new StringBuilder();
        normReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while ((line = normReader.readLine()) != null) {
          normMessageBuilder.append(line);
          normMessageBuilder.append("\n");
        }

        logger.debug("Command output: \"{}\"", normMessageBuilder.toString());
        if (process.exitValue() != 0) {
          logger.debug("Something wrong when execute command {}, errno {}: \"{}\"", command,
              process.exitValue(), errorMessageBuilder.toString());
          tryTime--;
          if (tryTime == 0) {
            logger.error("fail to executeCommand {}, try time {}, errno {}", command, 3 - tryTime,
                process.exitValue());
          } else {
            logger.debug("fail to executeCommand {}, try time {}", command, 3 - tryTime);
          }
          sleep(100);
        } else {
          // when tryTime is not equal to 3, the command has retried before. output the log for
          // checking issue
          if (tryTime != 3) {
            logger.debug("succeed to executeCommand {}, try time {}", command, 3 - tryTime);
          }
          ret = true;
          break;
        }
      }
    } catch (Exception e) {
      logger.error("Caught an exception when execute command {} , exception {}", command, e);
      ret = false;
    } finally {
      if (errorReader != null) {
        try {
          errorReader.close();
        } catch (Exception e) {
          logger.error("fail to close error reader {}.", e);
        }
      }
      if (normReader != null) {
        try {
          normReader.close();
        } catch (Exception e) {
          logger.error("fail to close normal reader {}.", e);
        }
      }
    }
    logger.debug("targetcli command end {}", command);
    return ret;
  }

  /**
   * System available pyd dev number can be controlled by
   * {@link LioCommandManagerConfiguration#nbdDeviceMaxNum}
   * or read from {@link nbdMaxFile}, not include which used in /etc/target/saveconfig.json.
   */
  public synchronized List<String> getAvailabeNbdDeviceList() throws Exception {
    SaveConfigImpl saveConfigImpl;
    List<String> bindNbdDevList = new ArrayList<String>();

    saveConfigImpl = saveConfigBuilder.build();
    if (saveConfigImpl.load()) {
      List<LioStorage> storages = saveConfigImpl.getStorages();
      if (storages != null && storages.size() != 0) {
        for (LioStorage storage : storages) {
          bindNbdDevList.add(storage.getDev());
        }
      }
    }
    List<String> availableNbdDeviceList = new ArrayList<String>();
    int nbdDeviceNum = 0;
    if (nbdDeviceMaxNum > 0) {
      nbdDeviceNum = nbdDeviceMaxNum;
      logger.debug("nbdDeviceMaxNum {} is set", nbdDeviceMaxNum);
    } else {
      nbdDeviceNum = getNbdMaxNum();
    }

    for (int i = 0; i < nbdDeviceNum; i++) {
      String nbdDevicePath = String.format("/dev/%s%d", nbdDeviceName, i);

      if (!new File(nbdDevicePath).exists()) {
        logger.warn("kernal devcie {} doesn't exit", nbdDevicePath);
        continue;
      }
      if (!bindNbdDevList.contains(nbdDevicePath)) {
        if (!DriverContainerUtils.isProcessExist(nbdDevicePath)) {
          logger.debug("Got an available nbd device {}", nbdDevicePath);
          availableNbdDeviceList.add(nbdDevicePath);
        } else {
          logger.warn("nbdDevicePath is alive:{}", nbdDevicePath);
        }

      }
    }
    return availableNbdDeviceList;
  }

  /**
   * Check  "targets" JSONArray from /etc/target/saveconfig.json file exist or not
   */

  public synchronized boolean existTarget(String targetName) throws Exception {
    SaveConfigImpl saveConfigImpl;

    saveConfigImpl = saveConfigBuilder.build();
    if (!saveConfigImpl.load()) {
      return false;
    }
    List<LioTarget> targets = saveConfigImpl.getTargets();
    List<LioTpg> tpgs = new ArrayList<>();
    if (existTarget(targets, targetName, tpgs)) {
      return true;
    }
    return false;
  }

  /**
   * check if target exists.
   */
  public boolean existTarget(List<LioTarget> targets, String targetName, List<LioTpg> tpgs) {
    boolean targetExist = false;
    for (LioTarget target : targets) {
      if (target.getWwn().equals(targetName)) {
        List<LioTpg> tpgsCurrent = target.getTpgs();
        for (int i = 0; i < tpgsCurrent.size(); i++) {
          tpgs.add(tpgsCurrent.get(i));
        }
        targetExist = true;
        break;
      }
    }

    return targetExist;
  }

  List getVolumeList(List<LioTarget> targets) {
    List<String> volumeList = new ArrayList<>();
    if (targets != null && targets.size() != 0) {
      for (LioTarget target : targets) {
        volumeList.add(target.getWwn());
      }
    }
    return volumeList;
  }

  /**
   * Cut content as following "dev": "/dev/pyd0", "name": "pyd0".
   *
   * <p>luns": [            { "index": 0, "storage_object": "/backstores/block/pyd0" }],
   *
   * <p>use storage name in “storage_objects” JsonArray to find targetName in “targets” JsonArray
   */
  public String getTargetNameByStorageName(List<LioTarget> targets, String storageName) {
    String targetName = null;
    for (LioTarget target : targets) {
      List<LioTpg> tpgs = target.getTpgs();
      for (LioTpg tpg : tpgs) {
        List<LioLun> luns = tpg.getLuns();
        for (LioLun lun : luns) {
          //get "storage_objects" NAME value
          File storageNameFile = new File(lun.getStorageObj());
          if (storageNameFile.getName().equals(storageName)) {
            targetName = target.getWwn();
          }
        }

      }
    }
    return targetName;
  }

  /**
   * nodeAcl wwn is applied accuessRule initiatorName,the method use to get all applied accuessRule
   * for every target.
   */
  public synchronized Map<String, List<String>> listIscsiAppliedAccuessRule() {
    SaveConfigImpl saveConfigImpl;
    Map<String, List<String>> initiatorNameTable = new HashMap<>();

    saveConfigImpl = saveConfigBuilder.build();
    if (!saveConfigImpl.load()) {
      return initiatorNameTable;
    }
    List<LioTarget> targets = saveConfigImpl.getTargets();
    if (targets != null && !targets.isEmpty()) {
      for (LioTarget target : targets) {
        logger.debug("targetName in json file is {}", target.getWwn());
        List<String> initiatorNames = new ArrayList<>();
        List<LioTpg> tpgs = target.getTpgs();
        for (LioTpg tpg : tpgs) {
          List<LioNodeAcl> nodeAcls = tpg.getNodeAcls();
          for (LioNodeAcl acl : nodeAcls) {
            if (!acl.getNodeWwn().contains(ConfigFileConstant.DEFAULT_NODEACL_IP)) {
              initiatorNames.add(acl.getNodeWwn());
            }
          }
        }
        initiatorNameTable.put(target.getWwn(), initiatorNames);
      }
    }
    return initiatorNameTable;
  }

  /**
   * use targetName which in "targets" JsonArray  to find storage name,and then use the name to find
   * pyd dev in "storage_objects" JsonArray.
   */
  public synchronized String getPydDevByTargetName(String targetName) {
    String pydDev = null;
    String storageName = null;
    SaveConfigImpl saveConfigImpl;
    List<LioTarget> targets;

    saveConfigImpl = saveConfigBuilder.build();
    if (!saveConfigImpl.load()) {
      return null;
    }
    targets = saveConfigImpl.getTargets();
    List<LioStorage> storages;
    storages = saveConfigImpl.getStorages();
    for (LioTarget target : targets) {
      logger.debug("targetName {} in json file is {}", targetName, target.getWwn());
      if (target.getWwn().equals(targetName)) {
        List<LioTpg> tpgs = target.getTpgs();
        for (LioTpg tpg : tpgs) {
          List<LioLun> luns = tpg.getLuns();
          if (luns != null && !luns.isEmpty()) {
            for (LioLun lun : luns) {
              File storageNameFile = new File(lun.getStorageObj());
              //get "storage_objects" NAME value
              storageName = storageNameFile.getName();
            }
          }
        }
      }
    }
    if (storageName != null) {
      for (LioStorage storage : storages) {
        if (storage.getName().equals(storageName)) {
          pydDev = storage.getDev();
        }
      }
    }

    return pydDev;

  }


  /**
   * xx.
   */
  public synchronized List<Object> getThreeInfoByTargetName(String targetName) {
    List<Object> list = new ArrayList<>();
    boolean targetExist = false;
    boolean storageExist = false;
    boolean lunExist = false;
    String pydDev = null;
    String storageName = null;
    SaveConfigImpl saveConfigImpl;
    List<LioTarget> targets;

    saveConfigImpl = saveConfigBuilder.build();
    if (!saveConfigImpl.load()) {
      list.add(targetExist);
      list.add(storageExist);
      list.add(lunExist);
      list.add(pydDev);
      return null;
    }
    targets = saveConfigImpl.getTargets();
    List<LioStorage> storages;
    storages = saveConfigImpl.getStorages();
    for (LioTarget target : targets) {
      logger.debug("targetName {} in json file is {}", targetName, target.getWwn());
      if (target.getWwn().equals(targetName)) {
        targetExist = true;
        List<LioTpg> tpgs = target.getTpgs();
        for (LioTpg tpg : tpgs) {
          List<LioLun> luns = tpg.getLuns();
          if (luns != null && !luns.isEmpty()) {
            lunExist = true;
            for (LioLun lun : luns) {
              File storageNameFile = new File(lun.getStorageObj());
              //get "storage_objects" NAME value
              storageName = storageNameFile.getName();
            }
          }
        }
      }
    }
    if (storageName != null) {
      for (LioStorage storage : storages) {
        if (storage.getName().equals(storageName)) {
          storageExist = true;
          pydDev = storage.getDev();
        }
      }
    }
    list.add(targetExist);
    list.add(storageExist);
    list.add(lunExist);
    list.add(pydDev);
    return list;
  }

  /**
   * check if pyd is alive according to pydDev, volumeId and snapshotId. the command is below.
   * /bin/sh -c ps -ef|grep "volumeId\ snapshotId.*pydDev$" example output: root     10761     1  0
   * 10:49 ?        00:00:00 /opt/pyd/pyd-client 695076417031639169 0 127.0.0.1 /dev/pyd0
   */
  public boolean isPydAlive(long volumeId, int snapShotid, String pydDev) throws Exception {
    return isPydAliveStatic(volumeId, snapShotid, pydDev);
  }

  /**
   * check if pyd is alive according to volumeId and snapshotId. the command is below: /bin/sh -c ps
   * -ef|grep "volumeId\ snapshotId" | grep -v grep.
   *
   * @return if pyd is alive, return pyd name, like /dev/pyd1
   */
  public String isPydAlive(long volumeId, int snapshotId) throws Exception {
    String[] command = {"/bin/sh", "-c",
        "ps -ef | grep -E \"" + volumeId + "\\ +" + snapshotId + "\"" + " | grep -v grep"};
    List<String> normalList = new ArrayList<>();
    String dev = null;
    class ProcessorCon implements Utils.CommandProcessor {

      @Override
      public List<String> getNormalStream(String line) {
        logger.debug("psef for volumeId Command normal message output: \"{}\"", line);
        normalList.add(line);
        return normalList;
      }

      @Override
      public List<String> getErrorStream(String line) {
        return null;
      }
    }

    ProcessorCon processorCon = new ProcessorCon();
    BufferedReader normReader = null;
    BufferedReader errorReader = null;
    try {
      Process process = Runtime.getRuntime().exec(command);
      String line = null;
      normReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      while ((line = normReader.readLine()) != null) {
        processorCon.getNormalStream(line);
      }
      StringBuilder errorMessageBuilder = new StringBuilder();
      errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      while ((line = errorReader.readLine()) != null) {
        errorMessageBuilder.append(line);
        errorMessageBuilder.append("\n");
        processorCon.getErrorStream(line);
      }

      process.waitFor();
      if (process.exitValue() != 0) {
        logger.debug("device is not alive, no output when execute command {}: \"{}\"", command,
            errorMessageBuilder.toString());
        return null;
      }
    } catch (Exception e) {
      logger.error("Caught an exception when execute command {}", command, e);
      throw e;
    } finally {
      if (errorReader != null) {
        try {
          errorReader.close();
        } catch (Exception e) {
          logger.error("fail to close error reader {}.", e);
        }
      }
      if (normReader != null) {
        try {
          normReader.close();
        } catch (Exception e) {
          logger.error("fail to close normal reader {}.", e);
        }
      }
    }

    boolean isPydAlive = false;
    String nbdDevicePath = String.format("/dev/%s", nbdDeviceName);
    if (normalList != null && !normalList.isEmpty()) {
      for (String outputLine : normalList) {
        if (outputLine.contains(Long.toString(volumeId)) && outputLine
            .contains(Integer.toString(snapshotId))) {
          String[] portStrings = outputLine.split(" ");
          for (int i = 0; i < portStrings.length; i++) {
            if (portStrings[i].equals(Long.toString(volumeId)) && portStrings[i + 1]
                .equals(Integer.toString(snapshotId))) {
              isPydAlive = true;
            }
            if (portStrings[i].contains(nbdDevicePath)) {
              dev = portStrings[i];
            }
          }
        }
        if (isPydAlive) {
          break;
        }
      }
    }
    if (isPydAlive) {
      logger.debug("deviceName {} is alive", dev);
    } else {
      logger.debug("deviceName is not alive");
    }
    return isPydAlive ? dev : null;
  }

  public boolean executeCommand(String command) {
    return executeCommandStatic(command);
  }

  /**
   * Get the maximum number of nbd device, the number is saved in file
   * /sys/module/pyd/parameters/nbds_max if the file is nonexistent, the pyd.ko is not inserted.
   *
   * @return the maximum number
   */
  private int getNbdMaxNum() {
    int nbdMaxNum = 10;
    if (Files.exists(Paths.get(nbdMaxFile))) {
      File file = new File(nbdMaxFile);
      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new FileReader(file));
        String tempString = null;
        if ((tempString = reader.readLine()) != null) {
          nbdMaxNum = Integer.parseInt(tempString);
          logger.debug("max nbd num {}", nbdMaxNum);
        }
      } catch (IOException e) {
        logger.error("catch an exception when operate BufferedReader, exception {}", e);
      } finally {
        if (reader != null) {
          try {
            reader.close();
          } catch (IOException e) {
            logger.error("catch an exception when close reader {}, exception {}", nbdMaxFile, e);
          }
        }
      }
    } else {
      logger.error("{} is nonexistent", nbdMaxFile);
    }
    return nbdMaxNum;
  }

  public String getNbdDeviceName() {
    return nbdDeviceName;
  }

  public void setNbdDeviceName(String nbdDeviceName) {
    this.nbdDeviceName = nbdDeviceName;
  }

  public SaveConfigBuilder getSaveConfigBuilder() {
    return saveConfigBuilder;
  }

  public void setSaveConfigBuilder(SaveConfigBuilder saveConfigBuilder) {
    this.saveConfigBuilder = saveConfigBuilder;
  }

  public LioNameBuilder getLioNameBuilder() {
    return lioNameBuilder;
  }

  public void setLioNameBuilder(LioNameBuilder lioNameBuilder) {
    this.lioNameBuilder = lioNameBuilder;
  }

  public String getUnbindNbdCmd() {
    return unbindNbdCmd;
  }

  public void setUnbindNbdCmd(String unbindNbdCmd) {
    this.unbindNbdCmd = unbindNbdCmd;
  }

  public String getBindNbdCmd() {
    return bindNbdCmd;
  }

  public void setBindNbdCmd(String bindNbdCmd) {
    this.bindNbdCmd = bindNbdCmd;
  }

  public String getSessionCommand() {
    return sessionCommand;
  }

  public void setSessionCommand(String sessionCommand) {
    this.sessionCommand = sessionCommand;
  }

  public synchronized int getNbdDeviceMaxNum() {
    return nbdDeviceMaxNum;
  }

  public synchronized void setNbdDeviceMaxNum(int nbdDeviceMaxNum) {
    this.nbdDeviceMaxNum = nbdDeviceMaxNum;
  }
}
