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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.OsCmdExecutor;
import py.common.Utils;
import py.coordinator.DriverFailureSignal;
import py.coordinator.IscsiAclProcessType;
import py.coordinator.IscsiTargetManager;
import py.driver.IscsiAccessRule;
import py.drivercontainer.lio.saveconfig.LioLun;
import py.drivercontainer.lio.saveconfig.LioNodeAcl;
import py.drivercontainer.lio.saveconfig.LioStorage;
import py.drivercontainer.lio.saveconfig.LioTarget;
import py.drivercontainer.lio.saveconfig.LioTpg;
import py.drivercontainer.lio.saveconfig.jsonobj.ConfigFileConstant;
import py.drivercontainer.lio.saveconfig.jsonobj.SaveConfigImpl;
import py.drivercontainer.utils.DriverContainerUtils;

/**
 * The class use to create iscsi target with lio command mode, create "storage_objects" and
 * "targets" save to default file "/etc/target/saveconfig.json
 */
public class LioCommandManager extends LioTargetManager implements IscsiTargetManager {

  private static final Logger logger = LoggerFactory.getLogger(LioCommandManager.class);

  //set this variable to createTarget method using struct to reduce the num of input parameters
  private static String DefaultCmdsnDepthPath = "";
  private static String corePath = "/sys/kernel/config/target/core";
  private String createStorageCmd;
  private String createTargetCmd;
  private String createLunCmd;
  private String createPortalCmd;
  private String createAccuessRuleCmd;
  private String createChapUserCmd;
  private String createChapPasswordCmd;
  private String createMutualChapUserCmd;
  private String createMutualChapPasswordCmd;
  private String saveConfigCmd;
  private String clearConfigCmd;
  private String deleteTargetCmd;
  private String deleteStorageCmd;
  private String deleteAccuessRuleCmd;
  private String defaultSaveconfigPath;
  private String deletePortalCmd;
  private String setAttributeAuthenticationCmd;
  private String setAttributeDemoModeDiscoveryCmd;
  private String setAttributeDefaultCmdsnDepthCmd;
  private String setEmulateTpuValueCmd;
  private String setAutoAddDefaultPortalCmd;
  private String getAutoAddDefaultPortalCmd;
  private int defaultPort;
  private int ioDepthEffectiveFlag;
  private int ioDepth;
  private Map<String, List<IscsiAccessRule>> ruleMap = new ConcurrentHashMap<>();

  //keep chapcontrol status
  private Map<String, Integer> chapControlMap = new ConcurrentHashMap<String, Integer>();

  public static String getDefaultCmdsnDepthPath() {
    return DefaultCmdsnDepthPath;
  }

  public static void setDefaultCmdsnDepthPath(String path) {
    DefaultCmdsnDepthPath = path;
  }

  @Override
  public synchronized String createTarget(String targetName, String targetNameIp, String ipAddress,
      String nbdDev, long volumeId, int snapshotid) throws Exception {
    logger
        .warn("createTarget | going to create iscsi target use command mode for :{},nbdDev is :{}",
            targetName, nbdDev);
    List<String> availableNbdDeviceList = getAvailabeNbdDeviceList();
    Iterator iter = availableNbdDeviceList.iterator();
    String nbdDevice = null;
    boolean useIterdevFlag = false;
    while (iter.hasNext()) {
      final String iterDev = (String) iter.next();
      boolean existTarget = existTarget(targetName);
      boolean existStorage = false;
      boolean existLun = false;
      if (nbdDev == null) {
        if (existTarget) {
          String bindNbdDev = getPydDevByTargetName(targetName);
          if (bindNbdDev != null) {
            existStorage = existStorage(bindNbdDev);
            existLun = existLun(targetName, bindNbdDev);
            nbdDev = bindNbdDev;
          }
        }
      } else {
        existStorage = existStorage(nbdDev);
        existLun = existLun(targetName, nbdDev);
      }
      boolean createTargetFlag = false;
      boolean createStorageFlag = false;
      boolean createLunFlag = false;
      if (existTarget) {
        if (!existStorage) {
          logger.debug(
              "createTarget | target exist, but lun and storage not exist,going to create lun and"
                  + " storage for :{}",
              targetName);
          createStorageFlag = true;
          createLunFlag = true;
        } else {
          if (!existLun) {
            if (nbdDev == null) {
              logger.debug(
                  "createTarget | target exist , but lun not exist ,so it can not find which"
                      + " storage is belong to for :{}, and incoming nbdDev is null , so we will "
                      + "choose a new device to create storage",
                  targetName);
              createStorageFlag = true;
            }
            logger.debug(
                "createTarget | target and storage exist ,but lun not exist ,goint to create lun "
                    + "for :{}", targetName);
            createLunFlag = true;
          }
        }

      }
      if (!existTarget) {
        if (!existStorage) {
          logger.debug(
              "createTarget | target , lun , storage all not exist , going to create for :{} , {}",
              targetName);
          createStorageFlag = true;
          createTargetFlag = true;
          createLunFlag = true;
        } else {
          logger.debug(
              "createTarget | target and lun not exist ,storage exist , going to create target and"
                  + " lun for :{}", targetName);
          if (nbdDev == null) {
            logger.debug(
                "createTarget | target and lun not exit ,storage exist but it can not find which"
                    + " storage is belong to for :{}, and incoming nbdDev is null , so we will "
                    + "choose a new device to create storage",
                targetName);
            createStorageFlag = true;
          }
          createTargetFlag = true;
          createLunFlag = true;
        }

      }

      nbdDevice = (useIterdevFlag) ? iterDev : nbdDev;
      logger
          .warn("createTarget | iterDev is :{},nbdDev:{},choose nbdDevice is :{}", iterDev, nbdDev,
              nbdDevice);
      if (nbdDevice == null && createStorageFlag) {
        String dev;
        if ((dev = isPydAlive(volumeId, snapshotid)) != null) {
          unbindNbdDriver(dev);
        }
        if (!bindNbdDriverWrap(volumeId, snapshotid, ipAddress, iterDev)) {
          continue;
        }
        logger.warn("createTarget | succeed to bind device:{}", iterDev);
        nbdDevice = iterDev;
      }

      logger.warn("createTarget | going to bind nbd for:{}", targetName);
      if (!isPydAlive(volumeId, snapshotid, nbdDevice)) {
        if (!bindNbdDriverWrap(volumeId, snapshotid, ipAddress, nbdDevice)) {
          return null;
        }
      }
      if (createStorageFlag) {
        logger.warn("createTarget | going to create storage for:{}", targetName);
        if (!newStorageWrap(nbdDevice, targetName)) {
          logger.error("createTarget | nbdDevice :{} is busy ,choose another one", nbdDevice);
          unbindNbdDriver(nbdDevice);
          if (nbdDev != null) {
            useIterdevFlag = true;
          }
          continue;
        }
      }
      if (createTargetFlag) {
        logger.warn("createTarget | going to create target for: {} use: {}", targetName, nbdDevice);
        // check targetcli global auto_add_default_portal, if true, set it to false
        // when drivercontainer bootstraps, it set this value to false.
        // if the user deploys drivercontainer before targetcli and the default value of
        // auto_add_default_portal is true, fail to create portal if not check this value here.
        // dynamically create a new target without lun info
        if (!newTargetWrap(targetName, targetNameIp, nbdDevice)) {
          logger.error("create target for :{} failed", targetName);
          return null;
        }

        // add acl if exist only for restart system case
        logger.warn("createTarget | going to apply acl for target: {}, use :{}", targetName,
            nbdDevice);
        if (ruleMap.containsKey(targetName) && ruleMap.get(targetName) != null
            && ruleMap.get(targetName).size() > 0) {
          logger.debug("add acls before create portal");
          saveAccessRuleToConfigFile(targetName);
        }

        logger.warn("createTarget | going to create lun for target: {}, use :{}", targetName,
            nbdDevice);
        if (createLunFlag) {
          logger.warn("createTarget | going to create lun for target: {} use :{}", targetName,
              nbdDevice);
          createLunFlag = false;
          if (!newLunWrap(targetName, nbdDevice)) {
            logger.error("createTarget | create lun for :{} failed", targetName);
            return null;
          }
        }

        logger.warn("createTarget | going to create portal for target: {}, use :{}", targetName,
            nbdDevice);
        if (!newPortalWrap(targetName, targetNameIp)) {
          logger.error("createTarget | create portal for :{} failed", targetName);
          return null;
        }
      }

      if (createLunFlag) {
        logger.warn("createTarget | going to create lun for target:{} use :{}", targetName,
            nbdDevice);
        if (!newLunWrap(targetName, nbdDevice)) {
          logger.error("createTarget | create lun for :{} failed", targetName);
          return null;
        }
      }
      //We should set emulate_tpu value as 1 for supporting VAAI function,so after ISCSI target
      // create successful we use 1 to replace the original value(0) of emulate_tpu.
      setEmulateTpuValueWrap(nbdDevice);
      return nbdDevice;
    }
    return null;

  }

  private boolean createTarget(String path, String targetName) {
    String cmdAsParam = String.format(createTargetCmd, path, targetName);
    return executeCommand(cmdAsParam);
  }


  /**
   * xx.
   */
  public String createTarget_bak(String targetName, String targetNameIp, String ipAddress,
      String nbdDev, long volumeId, int snapshotid) throws Exception {
    logger.warn("going to create iscsi target use command mode for :{},nbdDev is :{}", targetName,
        nbdDev);
    String nbdDevice = null;
    if (nbdDev == null) {
      /// new add target.
      List<String> availableNbdDeviceList = getAvailabeNbdDeviceList();
      Iterator iter = availableNbdDeviceList.iterator();
      while (iter.hasNext()) {
        String iterDev = (String) iter.next();
        boolean existTarget = existTarget(targetName);
        if (existTarget) {
          logger.warn("exist target {} and go to clear it.", targetName);
          if (!deleteTarget(targetName, null, volumeId, snapshotid)) {
            logger.error("fail to clear target {}", targetName);
          }
        }
        logger.warn("iterDev is :{}", iterDev);
        String dev;
        if ((dev = isPydAlive(volumeId, snapshotid)) != null) {
          unbindNbdDriver(dev);
        }
        if (!bindNbdDriverWrap(volumeId, snapshotid, ipAddress, iterDev)) {
          continue;
        }
        logger.warn("succeed to bind device:{}", iterDev);
        nbdDevice = iterDev;

        logger.warn("going to create storage for:{}", targetName);
        if (!newStorageWrap(nbdDevice, targetName)) {
          logger.error("nbdDevice :{} is busy ,choose another one", nbdDevice);
          unbindNbdDriver(nbdDevice);
          continue;
        }

        logger.warn("going to create target for :{}", targetName);
        // check targetcli global auto_add_default_portal, if true, set it to false
        // when drivercontainer bootstraps, it set this value to false.
        // if the user deploys drivercontainer before targetcli and the default value of
        // auto_add_default_portal is true,
        // fail to create portal if not check this value here.

        // dynamically create a new target without lun info
        if (!newTargetWrap(targetName, targetNameIp, nbdDevice)) {
          logger.error("create target for :{} failed", targetName);
          return null;
        }

        if (!newPortalWrap(targetName, targetNameIp)) {
          logger.error("create portal for :{} failed", targetName);
          return null;
        }

        logger.warn("going to create lun for target:{} use :{}", targetName, nbdDevice);
        if (!newLunWrap(targetName, nbdDevice)) {
          logger.error("create lun for :{} failed", targetName);
          return null;
        }

        //We should set emulate_tpu value as 1 for supporting VAAI function,so after ISCSI target
        // create successful we use
        //1 to replace the original value(0) of emulate_tpu.
        setEmulateTpuValueWrap(nbdDevice);
        return nbdDevice;
      }
      return null;
    } else {
      /// retrieve target.
      List<Object> list = getThreeInfoByTargetName(targetName);
      if (list.get(3) != null) {
        if (!isPydAlive(volumeId, snapshotid, (String) list.get(3))) {
          logger.warn("pyd is not alive and connect it.");
          if (!bindNbdDriverWrap(volumeId, snapshotid, ipAddress, (String) list.get(3))) {
            return null;
          }
        }
        return (String) list.get(3);
      }

      boolean existTarget = (boolean) list.get(0);
      boolean existStorage = (boolean) list.get(1);
      boolean existLun = (boolean) list.get(2);

      boolean createTargetFlag = false;
      boolean createStorageFlag = false;
      boolean createLunFlag = false;

      if (existTarget) {
        if (!existStorage) {
          logger.debug(
              "target exist, but lun and storage not exist,going to create lun and storage for :{}",
              targetName);
          createStorageFlag = true;
          createLunFlag = true;
        } else {
          if (!existLun) {
            logger.debug("target and storage exist ,but lun not exist ,goint to create lun for :{}",
                targetName);
            createLunFlag = true;
          }
        }
      } else {
        createTargetFlag = true;
        createLunFlag = true;
        if (!existStorage) {
          logger.debug("target , lun , storage all not exist , going to create for :{} , {}",
              targetName);
          createStorageFlag = true;
        }
      }

      nbdDevice = nbdDev;
      logger.warn("nbdDev: {}", nbdDev);
      String dev;
      if ((dev = isPydAlive(volumeId, snapshotid)) != null) {
        if (!dev.equals(nbdDevice)) {
          if (!unbindNbdDriver(dev)) {
            logger.error("fail to unbind dev {}", dev);
            return null;
          } else {
            if (!bindNbdDriverWrap(volumeId, snapshotid, ipAddress, nbdDevice)) {
              logger.error("fail to bind dev {}", dev);
              return null;
            }
          }
        }
      }

      logger.warn("succeed to bind device:{}", nbdDevice);
      if (createStorageFlag) {
        logger.warn("going to create storage for:{}", targetName);
        if (!newStorageWrap(nbdDevice, targetName)) {
          logger.error("fail to create storage with nbdDevice: {}", nbdDevice);
          unbindNbdDriver(nbdDevice);
          return null;
        }
      }
      if (createTargetFlag) {
        logger.warn("going to create target for :{}", targetName);
        // check targetcli global auto_add_default_portal, if true, set it to false
        // when drivercontainer bootstraps, it set this value to false.
        // if the user deploys drivercontainer before targetcli and the default value of
        // auto_add_default_portal is true,
        // fail to create portal if not check this value here.

        // dynamically create a new target without lun info
        if (!newTargetWrap(targetName, targetNameIp, nbdDevice)) {
          logger.error("create target for :{} failed", targetName);
          return null;
        }

        // add acl if exist only for restart system case
        if (ruleMap.containsKey(targetName) && ruleMap.get(targetName) != null
            && ruleMap.get(targetName).size() > 0) {
          logger.debug("add acls before create portal");
          saveAccessRuleToConfigFile(targetName);
        }

        if (!newPortalWrap(targetName, targetNameIp)) {
          logger.error("create portal for :{} failed", targetName);
          return null;
        }
      }

      if (createLunFlag) {
        logger.warn("going to create lun for target:{} use :{}", targetName, nbdDevice);
        if (!newLunWrap(targetName, nbdDevice)) {
          logger.error("create lun for :{} failed", targetName);
          return null;
        }
      }
      //We should set emulate_tpu value as 1 for supporting VAAI function,so after ISCSI target
      // create successful we use
      //1 to replace the original value(0) of emulate_tpu.
      setEmulateTpuValueWrap(nbdDevice);
      return nbdDevice;
    }
  }

  private boolean autoAddDefaultPortalValue(String cmd) {
    return DriverContainerUtils.getGlobalAutoAddDefaultPortalValue(cmd);
  }

  @Override
  public boolean deleteTarget(String targetName, String nbdDevice, long volumeId, int snapShotId)
      throws Exception {
    logger.warn("going to delete target and storage targetName:{},nbdDevice:{}", targetName,
        nbdDevice);
    if (nbdDevice == null) {
      if ((nbdDevice = isPydAlive(volumeId, snapShotId)) != null) {
        unbindNbdDriver(nbdDevice);
      }
    } else {
      if (isPydAlive(volumeId, snapShotId, nbdDevice)) {
        logger.warn("pyd-client is alive ,unbind it firstly");
        unbindNbdDriver(nbdDevice);
      }
    }

    if (existTarget(targetName)) {
      if (!deleteTargetWrap(targetName)) {
        logger.error("delete target for :{} failed", targetName);
        return false;
      }
    }

    if (existStorage(nbdDevice)) {
      if (!deleteStorageWrap(nbdDevice)) {
        logger.error("delete storage for target :{} failed ", targetName);
        return false;
      }
    }
    // should clean map
    ruleMap.remove(targetName);
    chapControlMap.remove(targetName);
    return true;
  }

  private boolean deleteTarget(String path, String targetName) {
    String cmdAsParam = String.format(deleteTargetCmd, path, targetName);
    return executeCommand(cmdAsParam);
  }

  /**
   * check if target, storage or lun exist.
   */
  public synchronized DriverFailureSignal exceptionTarget(String targetName, String pydDev)
      throws Exception {
    SaveConfigImpl saveConfigImpl;

    saveConfigImpl = saveConfigBuilder.build();
    if (!saveConfigImpl.load()) {
      return null;
    }
    List<LioTarget> targets = saveConfigImpl.getTargets();
    List<LioStorage> storages = saveConfigImpl.getStorages();
    List<LioTpg> tpgs = new ArrayList<>();
    boolean targetExist = existTarget(targets, targetName, tpgs);
    if (!targetExist) {
      logger.warn("Driver is in sick due to missing target with name {} ...", targetName);
      return DriverFailureSignal.TARGET;
    }

    String storageName = existStorage(storages, pydDev);
    if (storageName == null) {
      logger.warn("Driver is in sick due to missing storage for name {}...", targetName);
      return DriverFailureSignal.STORAGE;
    }
    boolean lunExist = existLun(tpgs, storageName);
    if (!lunExist) {
      logger.warn("Driver is in sick due to missing lun for name {} ...", targetName);
      return DriverFailureSignal.LUN;
    }
    return null;
  }

  /**
   * check if storage exists.
   *
   * @return storage name if exist
   */
  public String existStorage(List<LioStorage> storages, String pydDev) {
    String storageName = null;
    if (storages != null && !storages.isEmpty()) {
      for (LioStorage storage : storages) {
        if (storage.getDev().equals(pydDev)) {
          storageName = ConfigFileConstant.BLOCKPATH + "/" + storage.getName();
          break;
        }
      }
    }
    return storageName;
  }


  /**
   * xx.
   */
  public boolean existStorage(String pydDev) {
    SaveConfigImpl saveConfigImpl;

    saveConfigImpl = saveConfigBuilder.build();
    if (!saveConfigImpl.load()) {
      return false;
    }
    List<LioStorage> storages = saveConfigImpl.getStorages();
    if (existStorage(storages, pydDev) != null) {
      return true;
    }
    return false;
  }

  /**
   * check if lun exists.
   */
  public boolean existLun(List<LioTpg> tpgs, String storageName) {
    boolean lunExist = false;
    if (tpgs != null && !tpgs.isEmpty()) {
      for (LioTpg tpg : tpgs) {
        List<LioLun> luns = tpg.getLuns();
        if (luns != null && !luns.isEmpty()) {
          for (LioLun lun : luns) {
            if (lun.getStorageObj().equals(storageName)) {
              lunExist = true;
              break;
            }
          }
        }
        if (lunExist) {
          break;
        }
      }
    }
    return lunExist;
  }


  /**
   * xx.
   */
  public boolean existLun(String targetName, String pydDev) throws Exception {
    String storageName = null;
    SaveConfigImpl saveConfigImpl;

    saveConfigImpl = saveConfigBuilder.build();
    if (!saveConfigImpl.load()) {
      return false;
    }
    List<LioStorage> storages = saveConfigImpl.getStorages();
    if ((storageName = existStorage(storages, pydDev)) == null) {
      return false;
    }

    List<LioTarget> targets = saveConfigImpl.getTargets();
    if (targets != null && !targets.isEmpty()) {
      for (LioTarget target : targets) {
        if (target.getWwn().equals(targetName)) {
          List<LioTpg> tpgs = target.getTpgs();
          if (existLun(tpgs, storageName)) {
            return true;
          }
        }
      }
    }

    return false;
  }


  /**
   * xx.
   */
  public boolean existPortal(String targetName, String targetNameIp, int port) {
    StringBuilder portalFilePath = new StringBuilder(128);
    portalFilePath.append(ConfigFileConstant.PORTAL_FILE_PATH);
    portalFilePath.append(ConfigFileConstant.SEPARATOR);
    portalFilePath.append(targetName);
    portalFilePath.append(ConfigFileConstant.SEPARATOR);
    portalFilePath.append(ConfigFileConstant.TPGT_ONE);
    portalFilePath.append(ConfigFileConstant.SEPARATOR);
    portalFilePath.append(ConfigFileConstant.NP);
    portalFilePath.append(ConfigFileConstant.SEPARATOR);
    portalFilePath.append(targetNameIp);
    portalFilePath.append(":");
    portalFilePath.append(port);
    File file = new File(portalFilePath.toString());
    if (file.exists()) {
      return true;
    }
    return false;

  }

  @Override
  public DriverFailureSignal iscsiTargetIsAvailable(String targetName, String nbdDev, long volumeId,
      int snapshotId, boolean refreshDevices) throws Exception {
    DriverFailureSignal res = exceptionTarget(targetName, nbdDev);
    if (res != null) {
      return res;
    }
    if (!isPydAlive(volumeId, snapshotId, nbdDev)) {
      logger.warn("Driver is in sick due to missing PYD with device {} ...", nbdDev);
      return DriverFailureSignal.PYDCLIENT;
    }

    return null;
  }

  @Override
  public boolean saveAccessRuleHostToConfigFile(String iqn, List<String> incomingHostList) {
    throw new NotImplementedException();
  }

  //new target command example :
  // "/usr/bin/targetcli /iscsi create wwn=iqn.2017-08.zettastor.iqn:1354558691799955929-0"
  protected synchronized boolean newTargetWrap(String targetName, String targetNameIp,
      String nbdDev) {

    if (!createTarget(ConfigFileConstant.ISCSIPATH, targetName)) {
      logger.error("newTargetWrap createTarget fail targetName {}", targetName);
      return false;
    }

    if (!saveConfig(defaultSaveconfigPath)) {
      logger.error("newTargetWrap saveConfig fail defaultSaveconfigPath {}", defaultSaveconfigPath);
      return false;
    }
    int switcher = 1;
    if (chapControlMap != null && chapControlMap.containsKey(targetName)) {
      switcher = chapControlMap.get(targetName);
      logger.debug("chap control {}", switcher);
    }
    if (!setAttributeAuthenticationWrap(targetName, switcher)) {
      logger.error("newTargetWrap enable chap ctrl fail tpgFilePath {}", targetName);
      return false;
    }

    setSetAttributeDemoModeDiscovery(targetName);

    if (!setAttributeDefaultCmdsnDepthWrap(targetName)) {
      logger.error("setAttributeDefaultCmdsnDepthWrap fail");
    }
    saveConfig(defaultSaveconfigPath);
    return true;
  }

  //new storage command example :
  // "/usr/bin/targetcli /backstores/block create name=pyd1 dev=/dev/pyd1"
  protected synchronized boolean newStorageWrap(String nbdDev, String targetName) {
    File nbdDevName = new File(nbdDev);
    if (!createStorage(ConfigFileConstant.BLOCKPATH, nbdDevName.getName(), nbdDev)) {
      logger.error("create storage fot target :{} failed with dev:{}", targetName, nbdDev);
      return false;
    }
    if (!saveConfig(defaultSaveconfigPath)) {
      logger.error("save failed when new storagr for target {}", targetName);
      return false;
    }

    if (!setWwn(nbdDevName.getName(), lioNameBuilder.getVolumeId(targetName))) {
      return false;
    }

    if (!saveConfig(defaultSaveconfigPath)) {
      logger.error("save failed when set wwn for target {}", targetName);
      return false;
    }

    return true;
  }

  /**
   * use volumeId as storage wwn for multipath,because targetcli has no command to set wwn ,so we
   * modify the volume. write wwn to file vpd_unit_serial echo "(wwn)" >
   * /sys/kernel/config/target/core/(type)/(storage name)/wwn/vpd_unit_serial example: echo
   * "889181e6-e8c3-11e8-8423-74d435ca727e" > /sys/kernel/config/target/core/iblock_2/pyd2/wwn
   * /vpd_unit_serial.
   */
  private boolean setWwn(String nbdDevName, String wwn) {
    String iblockPath = null;
    // go through /sys/kernel/config/target/core to get type for current storage name
    try {
      DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(corePath));
      boolean pathFound = false;
      for (Path curPath : stream) {
        DirectoryStream<Path> nextStream = Files.newDirectoryStream(curPath);
        for (Path nextPath : nextStream) {
          String finalPath = nextPath.toString();
          String[] pathArray = finalPath.split("/");
          if (pathArray[pathArray.length - 1].equals(nbdDevName)) {
            iblockPath = finalPath;
            pathFound = true;
            break;
          }
        }
        if (pathFound) {
          break;
        }
      }
    } catch (IOException e) {
      logger.error("fail to newDirectoryStream {} for {}", e, corePath);
      return false;
    }
    Path wwnPath = null;
    if (iblockPath != null) {
      wwnPath = Paths.get(iblockPath, "wwn/vpd_unit_serial");
      logger.debug("nbdDevName {}, wwnPath {}", nbdDevName, wwnPath.toString());
    } else {
      logger.error("fail to get iblock path");
      return false;
    }

    FileOutputStream fop = null;

    if (Files.exists(wwnPath)) {
      try {
        File file = new File(wwnPath.toString());
        fop = new FileOutputStream(file);
        byte[] contentInBytes = wwn.getBytes();
        fop.write(contentInBytes);
        fop.flush();
      } catch (Exception e) {
        logger.error("exception happens {}", e);
        return false;
      } finally {
        try {
          if (fop != null) {
            fop.close();
          }
        } catch (IOException e) {
          logger.error("exception happens {}", e);
        }
      }
    } else {
      logger.error("file not exist");
      return false;
    }

    return true;
  }

  //new lun command example : "/usr/bin/targetcli /iscsi/iqn.2017-08.zettastor.iqn:1354558691799955
  // 929-0/tpg1/luns create /backstores/block/pyd1"
  protected synchronized boolean newLunWrap(String targetName, String nbdDev) {
    StringBuilder lunPath = new StringBuilder(128);
    lunPath.append(ConfigFileConstant.ISCSIPATH);
    lunPath.append(ConfigFileConstant.SEPARATOR);
    lunPath.append(targetName);
    lunPath.append(ConfigFileConstant.SEPARATOR);
    lunPath.append(ConfigFileConstant.TPGONE);
    lunPath.append(ConfigFileConstant.SEPARATOR);
    lunPath.append(ConfigFileConstant.LUNS);
    File file = new File(nbdDev);
    StringBuilder backstoreName = new StringBuilder(128);
    backstoreName.append(ConfigFileConstant.BLOCKPATH);
    backstoreName.append(ConfigFileConstant.SEPARATOR);
    backstoreName.append(file.getName());
    if (!createLun(lunPath.toString(), backstoreName.toString())) {
      logger.error("crrate lun failed for target :{} with dev:{}", targetName, nbdDev);
      return false;
    }
    if (!saveConfig(defaultSaveconfigPath)) {
      logger.error("save failed when new lun for target:{}", targetName);
      return false;
    }
    return true;
  }

  //new portal command example :
  // "/usr/bin/targetcli /iscsi/iqn.2017-08.zettastor.iqn:1354558691799955929-0/tpg1/portals
  // create 192.168.2.104 3260"
  protected synchronized boolean newPortalWrap(String targetName, String targetNameIp) {
    StringBuilder portalPath = new StringBuilder(128);
    portalPath.append(ConfigFileConstant.ISCSIPATH);
    portalPath.append(ConfigFileConstant.SEPARATOR);
    portalPath.append(targetName);
    portalPath.append(ConfigFileConstant.SEPARATOR);
    portalPath.append(ConfigFileConstant.TPGONE);
    portalPath.append(ConfigFileConstant.SEPARATOR);
    portalPath.append(ConfigFileConstant.PORTALS);
    if (!createPortal(portalPath.toString(), targetNameIp, defaultPort)) {
      logger.error("create portal failed for :{}", targetName);
      return false;
    }
    if (!saveConfig(defaultSaveconfigPath)) {
      logger.error("save failed when new portal for :{}", targetName);
      return false;
    }
    return true;
  }

  //delete portal command example :"/usr/bin/targetcli /iscsi/iqn.2017-08.zettastor.iqn:25448946485
  // 94622579-0/tpg1/portals delete ip_address=192.168.2.1 ip_port=3260"
  protected synchronized boolean deletePortalWrap(String targetName, String targetNameIp,
      int port) {
    StringBuilder portalPath = new StringBuilder(128);
    portalPath.append(ConfigFileConstant.ISCSIPATH);
    portalPath.append(ConfigFileConstant.SEPARATOR);
    portalPath.append(targetName);
    portalPath.append(ConfigFileConstant.SEPARATOR);
    portalPath.append(ConfigFileConstant.TPGONE);
    portalPath.append(ConfigFileConstant.SEPARATOR);
    portalPath.append(ConfigFileConstant.PORTALS);
    if (!deletePortal(portalPath.toString(), targetNameIp, port)) {
      logger.error("delete portal failed for :{}", targetName);
      return false;
    }
    if (!saveConfig(defaultSaveconfigPath)) {
      logger.error("save failed when delete portal for :{}", targetName);
      return false;
    }
    return true;
  }

  //delete target command example :
  // "/usr/bin/targetcli /iscsi delete wwn=iqn.2017-08.zettastor.iqn:2544894648594622579-0"
  protected synchronized boolean deleteTargetWrap(String targetName) {
    if (!deleteTarget(ConfigFileConstant.ISCSIPATH, targetName)) {
      logger.error("delete target failed for :{}", targetName);
      return false;
    }
    if (!saveConfig(defaultSaveconfigPath)) {
      logger.error("save failed when delete target for :{}", targetName);
      return false;
    }
    return true;
  }

  //delete storage command example :"/usr/bin/targetcli /backstores/block delete name=pyd1"
  protected synchronized boolean deleteStorageWrap(String nbdDevice) {
    File file = new File(nbdDevice);
    if (!deleteStorage(ConfigFileConstant.BLOCKPATH, file.getName())) {
      logger.error("delete storage failed for :{}", nbdDevice);
      return false;
    }
    if (!saveConfig(defaultSaveconfigPath)) {
      logger.error("save failed when delete storage for :{}", nbdDevice);
      return false;
    }
    return true;
  }

  @Override
  public boolean bindNbdDriverWrap(long volumeId, int snapshotId, String ipAddress, String nbdDev) {
    if (!bindNbdDriver(volumeId, snapshotId, ipAddress, nbdDev)) {
      logger.error(
          "Something wrong when binding nbd driver on port {} to path {} in volumeId :{} ,"
              + "snapshotId:{}",
          ipAddress, nbdDev, volumeId, snapshotId);
      return false;
    }
    return true;
  }

  @Override
  public List<String> listClientIps(String targetName) throws Exception {
    List<String> clientIpList = new ArrayList<>();
    final String separator = "-";
    String iqn = targetName.toLowerCase();
    SaveConfigImpl saveConfigImpl = saveConfigBuilder.build();

    if (!saveConfigImpl.load()) {
      return clientIpList;
    }
    List<LioTarget> targets = saveConfigImpl.getTargets();
    List<String> dev2ClientList = new ArrayList<>();

    class ProcessorCon implements Utils.CommandProcessor {

      String clientIp = null;
      String backstoreStr = null;

      @Override
      public List<String> getNormalStream(String line) {
        if (line.contains(ConfigFileConstant.BACKSTORE)) {
          backstoreStr = line;
        }
        // client address line looks like following:
        // "      address: [fe80::5054:ff:fe54:4d0c] (TCP)  cid: 0 connection-state: LOGGED_IN"
        // the line showed upper is for client connecting server on IPV6;
        // "      address: 192.168.2.102 (TCP)  cid: 0 connection-state: LOGGED_IN"
        // the line showed upper is for client connecting server on IPV4.
        if (line != null && line.contains(ConfigFileConstant.ADDRESS)) {
          InetAddress inetAddr;

          clientIp = line.split("\\s+")[2];
          try {
            inetAddr = InetAddress.getByName(clientIp);
            clientIp = inetAddr.getHostAddress();
          } catch (UnknownHostException e) {
            logger.error("unrecognized ip address: {}", clientIp, e);
            clientIp = null;
          }
        }

        // line content as "mapped-lun: 0 backstore: block/pyd1 mode: rw",get "pyd1"
        if (clientIp != null && backstoreStr != null) {
          String regex = "pyd(\\d+)";
          String dev = getString(regex, backstoreStr);
          dev2ClientList.add(dev + separator + clientIp);
          backstoreStr = null;
          clientIp = null;
        }

        //logger.debug("dev2ClientList:{}", dev2ClientList);
        return dev2ClientList;
      }


      @Override
      public List<String> getErrorStream(String line) {
        logger.error("error is :{}" + line);
        return null;
      }
    }

    ProcessorCon processorCon = new ProcessorCon();
    int exitCode = Utils.executeCommand(sessionCommand, processorCon);
    if (exitCode != 0) {
      throw new Exception("Unable to execute command " + sessionCommand);
    }

    for (String str : dev2ClientList) {
      String device = str.split(separator)[0];
      String client = str.split(separator)[1];
      String target = getTargetNameByStorageName(targets, device);
      if (iqn.equals(target)) {
        clientIpList.add(client);
      }
    }
    logger.debug("Target is:{},clientIpList is:{}", targetName, clientIpList);
    return clientIpList;
  }

  /*
   * /backstores/block/pyd0
   * */
  @Override
  public void getIqnForPydDevice(Map<String, String> pydToIqn) {
    SaveConfigImpl saveConfigImpl = saveConfigBuilder.build();

    if (!saveConfigImpl.load()) {
      return;
    }
    List<LioTarget> targets = saveConfigImpl.getTargets();
    Iterator<Map.Entry<String, String>> it = pydToIqn.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, String> entry = it.next();
      String target = getWwnByStorageObject(targets, entry.getKey());
      logger.debug("pyd device {}, iqn {}", entry.getKey(), target);
      entry.setValue(target);
    }
  }


  /**
   * xx.
   */
  public String getWwnByStorageObject(List<LioTarget> targets, String storageName) {
    logger.debug("input para storage name:{}", storageName);
    String targetName = null;
    for (LioTarget target : targets) {
      List<LioTpg> tpgs = target.getTpgs();
      for (LioTpg tpg : tpgs) {
        List<LioLun> luns = tpg.getLuns();
        for (LioLun lun : luns) {
          logger.debug("storage object name:{}", lun.getStorageObj());
          if (lun.getStorageObj().equals(storageName)) {
            targetName = target.getWwn();
          }
        }

      }
    }
    return targetName;
  }


  /**
   * xx.
   */
  public String getString(String regex, String str) {
    String dev = null;
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(str);
    if (matcher.find()) {
      dev = matcher.group();
    }
    return dev;
  }

  //set emulate_tpu value as 1 to support VAAI

  /**
   * xx.
   */
  public boolean setEmulateTpuValueWrap(String nbdDevice) {
    File file = new File(nbdDevice);
    StringBuilder path = new StringBuilder(64);
    path.append(ConfigFileConstant.BLOCKPATH);
    path.append(ConfigFileConstant.SEPARATOR);
    path.append(file.getName());
    setEmulateTpuValue(path.toString());
    if (!saveConfig(defaultSaveconfigPath)) {
      return false;
    }
    return true;
  }

  /**
   * Get access rule hosts from /etc/target/saveconfig.json ,and then compare with hosts from
   * infocenter.
   */
  @Override
  public List getVolumAccessHostList(String targetName) {
    List<LioTarget> targets;
    List<String> volumeAccessHostList = new ArrayList<>();
    SaveConfigImpl saveConfigImpl;

    saveConfigImpl = saveConfigBuilder.build();
    if (!saveConfigImpl.load()) {
      return volumeAccessHostList;
    }
    targets = saveConfigImpl.getTargets();
    for (LioTarget target : targets) {
      logger.debug("target.getWWN():{}", target.getWwn());
      if (target.getWwn().equals(targetName)) {
        List<LioTpg> tpgs = target.getTpgs();
        for (LioTpg tpg : tpgs) {
          List<LioNodeAcl> nodeAcls = tpg.getNodeAcls();
          logger.debug("nodeAcls size is:{}", nodeAcls.size());
          for (LioNodeAcl nodeAcl : nodeAcls) {
            String nodeWwn = nodeAcl.getNodeWwn();
            String host = lioNameBuilder.getNodeWwnIp(nodeWwn);
            volumeAccessHostList.add(host);
          }
        }
      }
    }
    return volumeAccessHostList;
  }

  @Override
  public boolean deleteAccessRule(String targetName, String initiatorName) {
    StringBuilder aclsPath = new StringBuilder(128);
    aclsPath.append(ConfigFileConstant.ISCSIPATH);
    aclsPath.append(ConfigFileConstant.SEPARATOR);
    aclsPath.append(targetName);
    aclsPath.append(ConfigFileConstant.SEPARATOR);
    aclsPath.append(ConfigFileConstant.TPGONE);
    aclsPath.append(ConfigFileConstant.SEPARATOR);
    aclsPath.append(ConfigFileConstant.ACLS);
    deleteAccuessRule(aclsPath.toString(), initiatorName);
    saveConfig(defaultSaveconfigPath);
    return true;
  }

  @Override
  public List getIscsiAccessRuleList(String targetName) {
    return ruleMap.get(targetName);
  }

  @Override
  public void saveAccessRuleToMap(String targetName, List<IscsiAccessRule> rules) {
    ruleMap.put(targetName, rules);
  }

  /**
   * execute targetcli command and set acl to saveconfig.json and including rollback mechanism.
   */
  private void saveAccessRuleToConfigFile(String targetName) {
    StringBuilder aclsPath = new StringBuilder(128);
    aclsPath.append(ConfigFileConstant.ISCSIPATH);
    aclsPath.append(ConfigFileConstant.SEPARATOR);
    aclsPath.append(targetName);
    aclsPath.append(ConfigFileConstant.SEPARATOR);
    aclsPath.append(ConfigFileConstant.TPGONE);
    aclsPath.append(ConfigFileConstant.SEPARATOR);
    aclsPath.append(ConfigFileConstant.ACLS);

    List<IscsiAccessRule> localRules = getIscsiAccessRuleList(targetName);
    if (localRules == null) {
      logger.error("localRules is null");
      return;
    }
    List<IscsiAccessRule> failedRules = new ArrayList<IscsiAccessRule>();
    boolean res = false;
    for (IscsiAccessRule rule : localRules) {
      res = createAccuessRule(aclsPath.toString(), rule.getInitiatorName());
      if (!res) {
        failedRules.add(rule);
        logger.warn("fail to createAccuessRule");
        continue;
      }
      StringBuilder sb = new StringBuilder(256);
      sb.append(aclsPath.toString());
      sb.append(ConfigFileConstant.SEPARATOR);
      sb.append(rule.getInitiatorName());
      String chapPath = sb.toString();
      res = createChapUser(chapPath, rule.getIncomingUser());
      if (!res) {
        failedRules.add(rule);
        logger.warn("fail to createChapUser");
        deleteAccuessRule(chapPath, rule.getInitiatorName());
        continue;
      }
      res = createChapPassword(chapPath, rule.getIncomingPasswd());
      if (!res) {
        failedRules.add(rule);
        logger.warn("fail to createChapPassword");
        deleteAccuessRule(chapPath, rule.getInitiatorName());
        continue;
      }
      String outUser = rule.getOutgoingUser();
      if (outUser != null && !outUser.isEmpty()) {
        res = createMutualChapUser(chapPath, rule.getOutgoingUser());
        if (!res) {
          failedRules.add(rule);
          logger.warn("fail to createMutualChapUser");
          deleteAccuessRule(chapPath, rule.getInitiatorName());
          continue;
        }
      }
      String outPasswd = rule.getOutgoingPasswd();
      if (outPasswd != null && !outPasswd.isEmpty()) {
        res = createMutualChapPassword(chapPath, rule.getOutgoingPasswd());
        if (!res) {
          failedRules.add(rule);
          logger.warn("fail to createMutualChapPassword");
          deleteAccuessRule(chapPath, rule.getInitiatorName());
          continue;
        }
      }
      res = saveConfig(defaultSaveconfigPath);
      if (!res) {
        logger.warn("fail to saveConfig");
      }
    }

    for (IscsiAccessRule rule : failedRules) {
      logger.warn("fail to apply acl for initator {}", rule.getInitiatorName());
      localRules.remove(rule);
    }
    return;
  }

  @Override
  public boolean saveAccessRuleToConfigFile(String targetName, List<IscsiAccessRule> rules,
      List<IscsiAclProcessType> opTypes) {
    StringBuilder sb = new StringBuilder(128);
    sb.append(ConfigFileConstant.ISCSIPATH);
    sb.append(ConfigFileConstant.SEPARATOR);
    sb.append(targetName);
    sb.append(ConfigFileConstant.SEPARATOR);
    sb.append(ConfigFileConstant.TPGONE);
    sb.append(ConfigFileConstant.SEPARATOR);
    sb.append(ConfigFileConstant.ACLS);
    String aclsPath = sb.toString();
    int indexType = 0;
    boolean res = true;
    List<IscsiAccessRule> localRules = getIscsiAccessRuleList(targetName);
    if (localRules == null) {
      localRules = new ArrayList<>();
    }

    for (IscsiAccessRule rule : rules) {
      IscsiAclProcessType opType = opTypes.get(indexType);
      switch (opType) {
        case CREATE:
          res = createAccuessRule(aclsPath, rule.getInitiatorName());
          if (!res) {
            logger.warn("fail to createAccuessRule");
            continue;
          }
          StringBuilder chapPathSb = new StringBuilder(256);
          chapPathSb.append(aclsPath);
          chapPathSb.append(ConfigFileConstant.SEPARATOR);
          chapPathSb.append(rule.getInitiatorName());
          String chapPath = chapPathSb.toString();
          res = createChapUser(chapPath, rule.getIncomingUser());
          if (!res) {
            logger.warn("fail to createChapUser");
            continue;
          }
          res = createChapPassword(chapPath, rule.getIncomingPasswd());
          if (!res) {
            logger.warn("fail to createChapPassword");
            continue;
          }
          String outUser = rule.getOutgoingUser();
          if (outUser != null && !outUser.isEmpty()) {
            res = createMutualChapUser(chapPath, rule.getOutgoingUser());
            if (!res) {
              logger.warn("fail to createMutualChapUser");
              continue;
            }
          }
          String outPasswd = rule.getOutgoingPasswd();
          if (outPasswd != null && !outPasswd.isEmpty()) {
            res = createMutualChapPassword(chapPath, rule.getOutgoingPasswd());
            if (!res) {
              logger.warn("fail to createMutualChapPassword");
              continue;
            }
          }
          IscsiAccessRule ruleNewAdd = new IscsiAccessRule(rule.getInitiatorName(),
              rule.getIncomingUser(),
              rule.getIncomingPasswd(), rule.getOutgoingUser(), rule.getOutgoingPasswd());
          localRules.add(ruleNewAdd);
          res = saveConfig(defaultSaveconfigPath);
          if (!res) {
            logger.warn("fail to saveConfig");
          }
          break;
        case DELETE:
          res = deleteAccuessRule(aclsPath, rule.getInitiatorName());
          if (!res) {
            logger.warn("fail to deleteAccuessRule");
            continue;
          }
          ListIterator<IscsiAccessRule> iterator = localRules.listIterator();
          while (iterator.hasNext()) {
            IscsiAccessRule next = iterator.next();
            if (next.getInitiatorName().compareTo(rule.getInitiatorName()) == 0) {
              iterator.remove();
              break;
            }
          }
          res = saveConfig(defaultSaveconfigPath);
          if (!res) {
            logger.warn("fail to saveConfig");
          }
          break;
        case UPDATE:
          logger.error("not support currently");
          break;
        default:
          logger.error("unknown operation type {}", opType);
          break;
      }
      indexType++;
    }
    saveAccessRuleToMap(targetName, localRules);
    return true;
  }

  @Override
  public Integer getChapControlStatus(String targetName) {
    return chapControlMap.get(targetName);
  }

  @Override
  public void setChapControlStatus(String targetName, int switcher) {
    logger.debug("set chap control status");
    chapControlMap.put(targetName, switcher);
  }

  @Override
  public boolean addAccount(String targetName, String user, String passwd) {
    throw new org.apache.commons.lang.NotImplementedException();
  }

  @Override
  public boolean deleteAccount(String targetName, String user) {
    throw new org.apache.commons.lang.NotImplementedException();
  }

  @Override
  public boolean addOutAccount(String targetName, String user, String passwd) {
    throw new org.apache.commons.lang.NotImplementedException();
  }

  @Override
  public boolean deleteOutAccount(String targetName, String user) {
    throw new org.apache.commons.lang.NotImplementedException();
  }

  @Override
  public boolean lunSizeMismatched(String targetName, boolean refreshDevices) throws IOException {
    return false;
  }


  /**
   * xx.
   */
  public void setTargetCliCmdLogInfo(String logFilePath, String consoleLogLevel,
      String fileLogLevel) {
    String cmdAsParam = String.format("targetcli set global logfile=%s", logFilePath);
    executeCommand(cmdAsParam);

    cmdAsParam = String.format("targetcli set global loglevel_console=%s", consoleLogLevel);
    executeCommand(cmdAsParam);

    cmdAsParam = String.format("targetcli set global loglevel_file=%s", fileLogLevel);
    executeCommand(cmdAsParam);
  }

  private boolean createStorage(String path, String name, String dev) {
    String cmdAsParam = String.format(createStorageCmd, path, name, dev);
    return executeCommand(cmdAsParam);
  }

  private boolean createLun(String path, String backstoreName) {
    String cmdAsParam = String.format(createLunCmd, path, backstoreName);
    return executeCommand(cmdAsParam);
  }

  private boolean createPortal(String path, String targetNameIp, int port) {
    String cmdAsParam = String.format(createPortalCmd, path, targetNameIp, port);
    return executeCommand(cmdAsParam);
  }

  //save command example : "/usr/bin/targetcli saveconfig /etc/target/saveconfig.json"
  private boolean saveConfig(String path) {
    String cmdAsParam = String.format(saveConfigCmd, path);
    boolean saveSuccess = false;
    if (executeCommand(cmdAsParam)) {
      saveSuccess = true;
    }
    return saveSuccess;
  }

  public boolean saveConfig() {
    return saveConfig(defaultSaveconfigPath);
  }

  private boolean deleteStorage(String path, String name) {
    String cmdAsParam = String.format(deleteStorageCmd, path, name);
    return executeCommand(cmdAsParam);
  }

  //delete accuess rule command example :"/usr/bin/targetcli /iscsi/iqn.2017-08.zettastor.iqn:
  // 1354558691799955929-0/tpg1/acls
  // delete wwn=iqn.1994-05.com.redhat:bb113e6aa102"
  private boolean deleteAccuessRule(String path, String nodeWwn) {
    String cmdAsParam = String.format(deleteAccuessRuleCmd, path, nodeWwn);
    return executeCommand(cmdAsParam);
  }

  private boolean deletePortal(String path, String targetNameIp, int port) {
    String cmdAsParam = String.format(deletePortalCmd, path, targetNameIp, port);
    return executeCommand(cmdAsParam);
  }

  private boolean bindNbdDriver(long volumeId, int snapshotId, String ip, String path) {
    String cmdAsParam = String.format(bindNbdCmd, volumeId, snapshotId, ip, path);
    return executeCommand(cmdAsParam);
  }

  private boolean unbindNbdDriver(String path) {
    String cmdAsParam = String.format(unbindNbdCmd, path);
    return executeCommand(cmdAsParam);
  }

  //create accuess rule command example :"/usr/bin/targetcli /iscsi/iqn.2017-08.zettastor.iqn:
  // 1354558691799955929-0/tpg1/acls
  // create wwn=iqn.1994-05.com.redhat:bb113e6aa102"
  private boolean createAccuessRule(String path, String nodeWwn) {
    String cmdAsParam = String.format(createAccuessRuleCmd, path, nodeWwn);
    return executeCommand(cmdAsParam);
  }

  //create user chap command example :"/usr/bin/targetcli /iscsi/iqn.2017-08.zettastor.iqn:
  // 1354558691799955929-0/tpg1/
  // acls/iqn.1994-05.com.redhat:bb113e6aa102 set auth userid=root"
  private boolean createChapUser(String path, String userName) {
    String cmdAsParam = String.format(createChapUserCmd, path, userName);
    return executeCommand(cmdAsParam);
  }

  //create password chap command example :"/usr/bin/targetcli /iscsi/iqn.2017-08.zettastor.iqn:
  // 1354558691799955929-0/tpg1/acls
  // /iqn.1994-05.com.redhat:bb113e6aa102 set auth password=312"
  private boolean createChapPassword(String path, String password) {
    String cmdAsParam = String.format(createChapPasswordCmd, path, password);
    return executeCommand(cmdAsParam);
  }

  //create user multi chap command example :"/usr/bin/targetcli /iscsi/iqn.2017-08.zettastor.iqn:
  // 1354558691799955929-0/tpg1/
  // acls/iqn.1994-05.com.redhat:bb113e6aa102 set auth mutual_userid=root"
  private boolean createMutualChapUser(String path, String userName) {
    String cmdAsParam = String.format(createMutualChapUserCmd, path, userName);
    return executeCommand(cmdAsParam);
  }

  //create multi password chap command example :"/usr/bin/targetcli /iscsi/iqn.2017-08.
  // zettastor.iqn:1354558691799955929-0/tpg1/acls
  // /iqn.1994-05.com.redhat:bb113e6aa102 set auth mutual_password=312"
  private boolean createMutualChapPassword(String path, String password) {
    String cmdAsParam = String.format(createMutualChapPasswordCmd, path, password);
    return executeCommand(cmdAsParam);
  }

  //set emulate_tpu command example :"/usr/bin/targetcli /backstores/block/pyd0 set attribute
  // emulate_tpu=1"
  private boolean setEmulateTpuValue(String path) {
    String cmdAsParam = String.format(setEmulateTpuValueCmd, path);
    return executeCommand(cmdAsParam);
  }

  //set authentication command example :"/usr/bin/targetcli /iscsi/iqn.zettastor:254/tpg1 set
  // attribute authentication=1 "
  //1: enable
  //0: disable
  @Override
  public boolean setAttributeAuthenticationWrap(String targetName, int switcher) {
    StringBuilder tpgFilePath = new StringBuilder(128);
    tpgFilePath.append(ConfigFileConstant.ISCSIPATH);
    tpgFilePath.append(ConfigFileConstant.SEPARATOR);
    tpgFilePath.append(targetName);
    tpgFilePath.append(ConfigFileConstant.SEPARATOR);
    tpgFilePath.append(ConfigFileConstant.TPGONE);
    String path = tpgFilePath.toString();
    if (!setAttributeAuthentication(path, switcher)) {
      return false;
    }
    logger.warn("succeed to set authentication attribute to {}", switcher);
    if (!saveConfig(defaultSaveconfigPath)) {
      logger.warn("fail to saveConfig");
      return false;
    }
    // save to map
    chapControlMap.put(targetName, switcher);
    return true;
  }

  private boolean setAttributeAuthentication(String path, int switcher) {
    String cmdAsParam = String.format(setAttributeAuthenticationCmd, path, switcher);
    logger.debug("setAttributeAuthentication cmdAsParam {} ", cmdAsParam);
    return executeCommand(cmdAsParam);
  }


  /**
   * xx.
   */
  public boolean setAttributeDefaultCmdsnDepthWrap(String targetName) {
    StringBuilder tpgFilePath = new StringBuilder(128);
    tpgFilePath.append(ConfigFileConstant.ISCSIPATH);
    tpgFilePath.append(ConfigFileConstant.SEPARATOR);
    tpgFilePath.append(targetName);
    tpgFilePath.append(ConfigFileConstant.SEPARATOR);
    tpgFilePath.append(ConfigFileConstant.TPGONE);
    String path = tpgFilePath.toString();
    int depth = 128;
    if (ioDepthEffectiveFlag == 0) {
      //example: /var/testing/packages/pengyun-drivercontainer/var/2.4.0-internal-20181009193122
      // /1947797069356252306/0/iscsi/config/coordinator.properties
      String depthPropertyPath =
          LioCommandManager.getDefaultCmdsnDepthPath() + "/coordinator.properties";
      logger.debug("file path:{}", depthPropertyPath);
      FileInputStream fis = null;
      try {
        Properties prop = new Properties();
        fis = new FileInputStream(depthPropertyPath);
        prop.load(fis);
        depth = Integer.parseInt(prop.getProperty("io.depth"));
      } catch (IOException e) {
        logger.warn("file not exist {}", e);
        return false;
      } finally {
        if (fis != null) {
          try {
            fis.close();
          } catch (IOException e) {
            logger.warn("File input stream close failure");
          }
        }
      }
      logger.debug("use coordinator io depth {}", depth);
    } else {
      logger.debug("use drivercontainer self-defined io depth {}", ioDepth);
      depth = ioDepth;
    }

    return setAttributeDefaultCmdsnDepth(path, depth);
  }

  private boolean setAttributeDefaultCmdsnDepth(String path, int depth) {
    String cmdAsParam = String.format(setAttributeDefaultCmdsnDepthCmd, path, depth);
    logger.debug("setAttributeDefaultCmdsnDepth cmdAsParam {} ", cmdAsParam);
    return executeCommand(cmdAsParam);
  }

  //set attribute command example :"/usr/bin/targetcli /iscsi/iqn.zettastor:254/tpg1 set attribute
  // demo_mode_discovery=0 "
  @Override
  public void setSetAttributeDemoModeDiscovery(String targetName) {
    StringBuilder tpgFilePath = new StringBuilder(128);
    tpgFilePath.append(ConfigFileConstant.ISCSIPATH);
    tpgFilePath.append(ConfigFileConstant.SEPARATOR);
    tpgFilePath.append(targetName);
    tpgFilePath.append(ConfigFileConstant.SEPARATOR);
    tpgFilePath.append(ConfigFileConstant.TPGONE);
    String path = tpgFilePath.toString();
    String cmdAsParam = String.format(setAttributeDemoModeDiscoveryCmd, path);
    logger.debug("set attribute cmd is: {}", cmdAsParam);
    OsCmdExecutor.OsCmdStreamConsumer stdoutStreamConsumer;
    OsCmdExecutor.OsCmdOutputLogger stderrStreamConsumer;
    stdoutStreamConsumer = new OsCmdExecutor.OsCmdStreamConsumer() {
      @Override
      public void consume(InputStream stream) throws IOException {
        String line = null;
        StringBuilder errorMessageBuilder = new StringBuilder(512);
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(stream));
        while ((line = errorReader.readLine()) != null) {
          errorMessageBuilder.append(line);
          errorMessageBuilder.append("\n");
        }
        logger.debug("Command output: \"{}\"", errorMessageBuilder.toString());
      }
    };
    stderrStreamConsumer = new OsCmdExecutor.OsCmdOutputLogger(logger, cmdAsParam);
    stderrStreamConsumer.setErrorStream(true);

    try {
      OsCmdExecutor.exec(cmdAsParam, DriverContainerUtils.osCMDThreadPool, stdoutStreamConsumer,
          stderrStreamConsumer);
    } catch (IOException | InterruptedException e) {
      logger.error("Caught an exception when execute command {}, {}", cmdAsParam, e);
    }
  }

  public String getClearConfigCmd() {
    return clearConfigCmd;
  }

  public void setClearConfigCmd(String clearConfigCmd) {
    this.clearConfigCmd = clearConfigCmd;
  }

  public String getSaveConfigCmd() {
    return saveConfigCmd;
  }

  public void setSaveConfigCmd(String saveConfigCmd) {
    this.saveConfigCmd = saveConfigCmd;
  }

  public String getCreateChapPasswordCmd() {
    return createChapPasswordCmd;
  }

  public void setCreateChapPasswordCmd(String createChapPasswordCmd) {
    this.createChapPasswordCmd = createChapPasswordCmd;
  }

  public String getCreateChapUserCmd() {
    return createChapUserCmd;
  }

  public void setCreateChapUserCmd(String createChapUserCmd) {
    this.createChapUserCmd = createChapUserCmd;
  }

  public String getCreateAccuessRuleCmd() {
    return createAccuessRuleCmd;
  }

  public void setCreateAccuessRuleCmd(String createAccuessRuleCmd) {
    this.createAccuessRuleCmd = createAccuessRuleCmd;
  }

  public String getCreatePortalCmd() {
    return createPortalCmd;
  }

  public void setCreatePortalCmd(String createPortalCmd) {
    this.createPortalCmd = createPortalCmd;
  }

  public String getCreateLunCmd() {
    return createLunCmd;
  }

  public void setCreateLunCmd(String createLunCmd) {
    this.createLunCmd = createLunCmd;
  }

  public String getCreateTargetCmd() {
    return createTargetCmd;
  }

  public void setCreateTargetCmd(String createTargetCmd) {
    this.createTargetCmd = createTargetCmd;
  }

  public String getCreateStorageCmd() {
    return createStorageCmd;
  }

  public void setCreateStorageCmd(String createStorageCmd) {
    this.createStorageCmd = createStorageCmd;
  }

  public String getDeleteStorageCmd() {
    return deleteStorageCmd;
  }

  public void setDeleteStorageCmd(String deleteStorageCmd) {
    this.deleteStorageCmd = deleteStorageCmd;
  }

  public String getDeleteTargetCmd() {
    return deleteTargetCmd;
  }

  public void setDeleteTargetCmd(String deleteTargetCmd) {
    this.deleteTargetCmd = deleteTargetCmd;
  }

  public String getDefaultSaveconfigPath() {
    return defaultSaveconfigPath;
  }

  public void setDefaultSaveconfigPath(String defaultSaveconfigPath) {
    this.defaultSaveconfigPath = defaultSaveconfigPath;
  }

  public int getDefaultPort() {
    return defaultPort;
  }

  public void setDefaultPort(int defaultPort) {
    this.defaultPort = defaultPort;
  }

  public String getDeleteAccuessRuleCmd() {
    return deleteAccuessRuleCmd;
  }

  public void setDeleteAccuessRuleCmd(String deleteAccuessRuleCmd) {
    this.deleteAccuessRuleCmd = deleteAccuessRuleCmd;
  }

  public String getDeletePortalCmd() {
    return deletePortalCmd;
  }

  public void setDeletePortalCmd(String deletePortalCmd) {
    this.deletePortalCmd = deletePortalCmd;
  }

  public String getCreateMutualChapPasswordCmd() {
    return createMutualChapPasswordCmd;
  }

  public void setCreateMutualChapPasswordCmd(String createMutualChapPasswordCmd) {
    this.createMutualChapPasswordCmd = createMutualChapPasswordCmd;
  }

  public String getCreateMutualChapUserCmd() {
    return createMutualChapUserCmd;
  }

  public void setCreateMutualChapUserCmd(String createMutualChapUserCmd) {
    this.createMutualChapUserCmd = createMutualChapUserCmd;
  }

  public String getSetAttributeAuthenticationCmd() {
    return setAttributeAuthenticationCmd;
  }

  public void setSetAttributeAuthenticationCmd(String setAttributeAuthenticationCmd) {
    this.setAttributeAuthenticationCmd = setAttributeAuthenticationCmd;
  }

  public String getSetAttributeDemoModeDiscoveryCmd() {
    return setAttributeDemoModeDiscoveryCmd;
  }

  public void setSetAttributeDemoModeDiscoveryCmd(String setAttributeDemoModeDiscoveryCmd) {
    this.setAttributeDemoModeDiscoveryCmd = setAttributeDemoModeDiscoveryCmd;
  }

  public String getSetAttributeDefaultCmdsnDepthCmd() {
    return setAttributeDefaultCmdsnDepthCmd;
  }

  public void setSetAttributeDefaultCmdsnDepthCmd(String setAttributeDefaultCmdsnDepthCmd) {
    this.setAttributeDefaultCmdsnDepthCmd = setAttributeDefaultCmdsnDepthCmd;
  }

  public String getSetEmulateTpuValueCmd() {
    return setEmulateTpuValueCmd;
  }

  public void setSetEmulateTpuValueCmd(String setEmulateTpuValueCmd) {
    this.setEmulateTpuValueCmd = setEmulateTpuValueCmd;
  }

  public String getSetAutoAddDefaultPortalCmd() {
    return setAutoAddDefaultPortalCmd;
  }

  public void setSetAutoAddDefaultPortalCmd(String setAutoAddDefaultPortalCmd) {
    this.setAutoAddDefaultPortalCmd = setAutoAddDefaultPortalCmd;
  }

  public String getGetAutoAddDefaultPortalCmd() {
    return getAutoAddDefaultPortalCmd;
  }

  public void setGetAutoAddDefaultPortalCmd(String getAutoAddDefaultPortalCmd) {
    this.getAutoAddDefaultPortalCmd = getAutoAddDefaultPortalCmd;
  }

  public int getIoDepthEffectiveFlag() {
    return ioDepthEffectiveFlag;
  }

  public void setIoDepthEffectiveFlag(int ioDepthEffectiveFlag) {
    this.ioDepthEffectiveFlag = ioDepthEffectiveFlag;
  }

  public int getIoDepth() {
    return ioDepth;
  }

  public void setIoDepth(int ioDepth) {
    this.ioDepth = ioDepth;
  }
}
