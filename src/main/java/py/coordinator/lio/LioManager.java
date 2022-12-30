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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.Utils;
import py.coordinator.DriverFailureSignal;
import py.coordinator.IscsiAclProcessType;
import py.coordinator.IscsiTargetManager;
import py.driver.IscsiAccessRule;
import py.drivercontainer.lio.saveconfig.LioLun;
import py.drivercontainer.lio.saveconfig.LioNodeAcl;
import py.drivercontainer.lio.saveconfig.LioPortal;
import py.drivercontainer.lio.saveconfig.LioStorage;
import py.drivercontainer.lio.saveconfig.LioTarget;
import py.drivercontainer.lio.saveconfig.LioTpg;
import py.drivercontainer.lio.saveconfig.jsonobj.ConfigFileConstant;
import py.drivercontainer.lio.saveconfig.jsonobj.LioTpgImpl;
import py.drivercontainer.lio.saveconfig.jsonobj.SaveConfigImpl;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * xx.
 */
public class LioManager extends LioTargetManager implements IscsiTargetManager {

  private static final Logger logger = LoggerFactory.getLogger(LioManager.class);
  private LioManagerConfiguration lioManagerCon;
  private String filePath;
  private String templatePath;

  private Map<String, List<IscsiAccessRule>> ruleMap = new ConcurrentHashMap<>();
  //keep chapcontrol status
  private Map<String, Integer> chapControlMap = new ConcurrentHashMap<String, Integer>();

  /**
   * When launch a Iscsi driver,create "targets" JSONArray and "storage_objects" use targetName
   * which created by volumeid.Save it to /etc/target/saveconfig.json,and bind pyd-client to a
   * coordinator process use ipAddress and port ipAddress is 127.0.0.1,targetNameIp is actually ip
   * such as 192.168.2.104.
   */
  public synchronized String createTarget(String targetName, String targetNameIp, String ipAddress,
      String nbdDev,
      long volumeId, int snapshotid) {
    logger.warn("Going to create lio target use build file mode with targetName: {} nbeDev: {}",
        targetName, nbdDev);
    String nbdDevice = null;
    String bindNbdDev;
    try {
      if (nbdDev == null) {
        if (existTarget(targetName)) {
          logger.warn("targetName {} exist but nbdDev is null", targetName);
          bindNbdDev = getPydDevByTargetName(targetName);
          if (isPydAlive(volumeId, snapshotid, bindNbdDev)) {
            logger.warn(
                "pyd binded by targetName is alive,should not bind again ,return pydDev is :{}",
                bindNbdDev);
            return bindNbdDev;
          } else {
            if (isPydAlive(volumeId, snapshotid, bindNbdDev)) {
              //targetName exist,but pyd dev used by others,should delete the target and create it
              // again
              deleteTarget(targetName, nbdDevice, volumeId, snapshotid);
              return createNewTargetName(targetName, targetNameIp, ipAddress, volumeId, snapshotid);

            } else if (!bindNbdDriverWrap(volumeId, snapshotid, ipAddress, bindNbdDev)) {
              return null;
            }
            return bindNbdDev;
          }
        } else {
          return createNewTargetName(targetName, targetNameIp, ipAddress, volumeId, snapshotid);
        }
      } else {
        if (!isPydAlive(volumeId, snapshotid, nbdDev)) {
          if (existTarget(targetName)) {
            logger.warn("targetName  exist,only bind pyd-client");
            if (!bindNbdDriverWrap(volumeId, snapshotid, ipAddress, nbdDev)) {
              return null;
            }
            nbdDevice = nbdDev;
          } else {
            logger.warn("targetName  not exist,bind pyd-client and then create targetName");
            if (!bindNbdDriverWrap(volumeId, snapshotid, ipAddress, nbdDev)) {
              return null;
            }
            if (!newTargetWrap(targetName, targetNameIp, nbdDev)) {
              return null;
            }
            nbdDevice = nbdDev;

          }
        } else {
          if (existTarget(targetName)) {
            logger.warn("targetName  exist and pyd-client is alive ,just return pydDev:{}", nbdDev);
            nbdDevice = nbdDev;
          } else {
            if (!newTargetWrap(targetName, targetNameIp, nbdDev)) {
              return null;
            }
            nbdDevice = nbdDev;
          }
        }
      }
    } catch (Exception e) {
      logger.warn("Catch an exception when create targetName:{}", e);
      return null;
    }

    return nbdDevice;

  }


  /**
   * xx.
   */
  public String createNewTargetName(String targetName, String targetNameIp, String ipAddress,
      long volumeId, int snapshotId) throws Exception {
    String nbdDevice = null;
    List<String> nbdDeviceList = getAvailabeNbdDeviceList();
    if (nbdDeviceList == null || nbdDeviceList.size() == 0) {
      logger.error("Cannot get an available nbd device to bind");
      return null;
    }
    String dev = null;
    for (String device : nbdDeviceList) {
      if ((dev = isPydAlive(volumeId, snapshotId)) != null) {
        unbindNbdDriver(dev);
      }
      if (!bindNbdDriverWrap(volumeId, snapshotId, ipAddress, device)) {
        continue;
      }
      logger.warn("successed to bind device:{}", device);
      nbdDevice = device;
      break;

    }
    if (nbdDevice == null) {
      logger.error("failed to binding nbd driver  {} to all nbd device", ipAddress);
      return null;
    }
    logger.warn("create a new targetName by pydDev :{} ", nbdDevice);
    if (!newTargetWrap(targetName, targetNameIp, nbdDevice)) {
      return null;
    }
    return nbdDevice;

  }


  /**
   * When umount Iscsi driver,  delete "targets" JSONArray and "storage_objects"  JSONArray from
   * /etc/target/saveconfig.json use targetName which created by volumeid ,and ubind pyd-client.
   */
  public synchronized boolean deleteTarget(String targetName, String nbdDevice, long volumeId,
      int snapShotId) {
    logger.warn("delete target targetName:{},nbdDevice:{}", targetName, nbdDevice);
    try {
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
      SaveConfigImpl saveConfigImpl = saveConfigBuilder.build();
      List<LioTarget> targets = saveConfigImpl.getTargets();
      List<String> volumeList = getVolumeList(targets);
      List<LioStorage> storages = saveConfigImpl.getStorages();
      List<String> pydDevList = getpydDevList(storages);

      if (pydDevList.size() == 0 || !pydDevList.contains(nbdDevice)) {
        logger.warn("No such storage in saveConfig,json file ,should not delete it ");
      } else {
        saveConfigImpl.removeStorage(nbdDevice);
      }

      if (volumeList.size() == 0 || !volumeList.contains(targetName)) {
        logger.warn("No such target in saveConfig,json file ,should not delete it ");
      } else {
        saveConfigImpl.removeTarget(targetName);
      }
      if (!saveConfigImpl.persist(new File(filePath))) {
        return false;
      }

      // should clean map
      ruleMap.remove(targetName);
      chapControlMap.remove(targetName);

    } catch (Exception e) {
      logger.warn("Catch an Exception when deleteTarget ", e);
      return false;
    }
    return true;
  }

  @Override
  public DriverFailureSignal iscsiTargetIsAvailable(String targetName, String nbdDev, long volumeId,
      int snapshotId, boolean refreshDevices) throws Exception {
    if (!existTarget(targetName)) {
      logger.warn("Driver is in sick due to missing target with name {} ...", targetName);
      return DriverFailureSignal.TARGET;
    }
    if (!isPydAlive(volumeId, snapshotId, nbdDev)) {
      logger.warn("Driver is in sick due to missing PYD with device {} ...", nbdDev);
      return DriverFailureSignal.PYDCLIENT;
    }
    return null;
  }


  /**
   * Use saveconfig_bak.json in resources as template to create  /etc/target/saveconfig.json get(0)
   * meanse use saveconfig_bak.json file first object as template.
   */
  public boolean newTargetWrap(String targetName, String targetNameIp, String nbdDev) {
    logger.debug("templatePath is :{}", templatePath);
    SaveConfigImpl saveConfigTemple = new SaveConfigImpl(templatePath);
    if (saveConfigTemple.getStorages().size() != 1 || saveConfigTemple.getTargets().size() != 1) {
      logger.error(
          "There can be only one template storage_object and target in template  "
              + "saveconfig_bak file");
      return false;
    }

    LioStorage storageTemple = this.getTheOnlyObject(saveConfigTemple.getStorages());
    storageTemple.setDev(nbdDev);
    File nbdDevName = new File(nbdDev);
    //nbdDev is /dev/pyd(i),use pyd(i) as "storage_objects" name
    storageTemple.setName(nbdDevName.getName());
    //use volumeId as WWN
    storageTemple.setWwn(lioNameBuilder.getVolumeId(targetName));

    LioTarget targetTemple = this.getTheOnlyObject(saveConfigTemple.getTargets());
    targetTemple.setWwn(targetName);
    logger.debug("targetTemple is:{}", targetTemple);

    if (targetTemple.getTpgs().size() != 1) {
      logger.error("There can be only one template tpg in template saveconfig_bak file");
      throw new IllegalArgumentException();
    }
    LioTpg tpgTemple = this.getTheOnlyObject(targetTemple.getTpgs());
    if (tpgTemple.getLuns().size() != 1 || tpgTemple.getNodeAcls().size() != 1
        || tpgTemple.getPortals().size() != 1) {
      logger.error(
          "There can be only one template lun,nodeAcl, protal in template saveconfig_bak file");
      throw new IllegalArgumentException();
    }
    LioLun lunTemplate = this.getTheOnlyObject(tpgTemple.getLuns());
    lunTemplate.setStorageObj(ConfigFileConstant.BLOCKPATH + "/" + nbdDevName.getName());

    LioNodeAcl nodeAclTemplate = this.getTheOnlyObject(tpgTemple.getNodeAcls());
    nodeAclTemplate
        .setNodeWwn(ConfigFileConstant.NODE_WWN_PREFIX, ConfigFileConstant.DEFAULT_NODEACL_IP);
    nodeAclTemplate.setPassword(lioManagerCon.getChapPassword());
    nodeAclTemplate.setUserId(lioManagerCon.getChapUserid());

    LioPortal portalTemplate = this.getTheOnlyObject(tpgTemple.getPortals());
    portalTemplate.setIpAddr(targetNameIp);
    portalTemplate.setPort(lioManagerCon.getDefaultLiotargetPort());

    SaveConfigImpl saveConfigImpl = saveConfigBuilder.build();
    try {
      saveConfigImpl.addStorage(storageTemple);
      saveConfigImpl.addTarget(targetTemple);
      if (!saveConfigImpl.persist(new File(filePath))) {
        logger.warn("Catch an exception when save to saveconfig.json file");
        return false;
      }
    } catch (Exception e) {
      logger.error("catch an exception when create target:{}", e);
      return false;
    }
    return true;

  }

  <T> T getTheOnlyObject(List<T> objs) {
    return objs.get(0);
  }


  List getpydDevList(List<LioStorage> storages) {
    List<String> pydDevList = new ArrayList<>();
    if (storages != null && storages.size() != 0) {
      for (LioStorage storage : storages) {
        pydDevList.add(storage.getDev());
      }
    }
    return pydDevList;
  }

  private boolean bindNbdDriver(long volumeId, int snapshotId, String ip, String path) {
    String cmdAsParam = String.format(bindNbdCmd, volumeId, snapshotId, ip, path);
    return executeCommand(cmdAsParam);
  }

  private boolean unbindNbdDriver(String path) {
    String cmdAsParam = String.format(unbindNbdCmd, path);
    return executeCommand(cmdAsParam);
  }


  /**
   * xx.
   */
  public boolean bindNbdDriverWrap(long volumeId, int snapshotId, String ipAddress, String nbdDev) {
    if (!bindNbdDriver(volumeId, snapshotId, ipAddress, nbdDev)) {
      logger.error("Something wrong when binding nbd driver  {} to path {}",
          ipAddress, nbdDev);
      return false;
    }
    return true;
  }

  /**
   * Creat node_acls with clientHost from infocenter ,save it to /etc/target/saveconfig.json
   */
  public synchronized boolean saveAccessRuleHostToConfigFile(String iqn,
      List<String> incomingHostList) {
    logger.warn("incominghost from infocenter is :{}", incomingHostList);
    List<LioNodeAcl> nodeAclList = new ArrayList<>();
    LioNodeAcl nodeAcl;
    LioTpg tpgTemple;
    if (incomingHostList.size() > 0) {
      for (String host : incomingHostList) {
        SaveConfigImpl saveConfigTemple = new SaveConfigImpl(templatePath);
        LioTarget targetTemple = this.getTheOnlyObject(saveConfigTemple.getTargets());
        tpgTemple = this.getTheOnlyObject(targetTemple.getTpgs());
        nodeAcl = this.getTheOnlyObject(tpgTemple.getNodeAcls());
        nodeAcl.setUserId(lioManagerCon.getChapUserid());
        nodeAcl.setPassword(lioManagerCon.getChapPassword());
        nodeAcl.setNodeWwn(ConfigFileConstant.NODE_WWN_PREFIX, host);
        nodeAclList.add(nodeAcl);
      }
    } else {
      SaveConfigImpl saveConfigTemple = new SaveConfigImpl(templatePath);
      LioTarget targetTemple = this.getTheOnlyObject(saveConfigTemple.getTargets());
      tpgTemple = this.getTheOnlyObject(targetTemple.getTpgs());
      nodeAcl = this.getTheOnlyObject(tpgTemple.getNodeAcls());
      nodeAcl.setNodeWwn(ConfigFileConstant.NODE_WWN_PREFIX, ConfigFileConstant.DEFAULT_NODEACL_IP);
      nodeAclList.add(nodeAcl);
    }
    SaveConfigImpl saveConfigImpl = saveConfigBuilder.build();
    List<LioTarget> targets = saveConfigImpl.getTargets();
    logger.debug("before add infocenter host targets is :{}", targets);
    for (LioTarget target : targets) {
      if (target.getWwn().equals(iqn)) {
        List<LioTpg> tpgs = target.getTpgs();
        for (LioTpg tpg : tpgs) {
          LioTpgImpl tpgImpl = (LioTpgImpl) tpg;
          tpgImpl.clearNodeAcls();
          for (LioNodeAcl nodeAcl1 : nodeAclList) {
            tpgImpl.addNodeAcl(nodeAcl1);
          }


        }
      }
      if (!saveConfigImpl.persist(new File(filePath))) {
        return false;
      }
    }
    return true;

  }

  /**
   * Get access rule hosts from /etc/target/saveconfig.json ,and then compare with hosts from
   * infocenter.
   */
  public synchronized List getVolumAccessHostList(String targetName) {
    List<LioTarget> targets;
    SaveConfigImpl saveConfigImpl;

    saveConfigImpl = saveConfigBuilder.build();
    targets = saveConfigImpl.getTargets();
    List<String> volumeAccessHostList = new ArrayList<>();
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


  /**
   * Create node_acls with clientHost from infocenter ,save it to /etc/target/saveconfig.json
   */
  @Override
  public synchronized void saveAccessRuleToMap(String targetName, List<IscsiAccessRule> rules) {
    logger.warn("saveAccessRuleToMap rules on target {} from infocenter is :{}", targetName, rules);
    List<LioNodeAcl> nodeAclList = new ArrayList<>();
    LioNodeAcl nodeAcl;
    LioTpg tpgTemple;

    // defaults
    if (rules == null || rules.isEmpty()) {
      logger.debug("saveAccessRuleToMap default");
      SaveConfigImpl saveConfigTemple = new SaveConfigImpl(templatePath);
      LioTarget targetTemple = this.getTheOnlyObject(saveConfigTemple.getTargets());
      tpgTemple = this.getTheOnlyObject(targetTemple.getTpgs());
      nodeAcl = this.getTheOnlyObject(tpgTemple.getNodeAcls());
      nodeAcl.setNodeWwn(ConfigFileConstant.NODE_WWN_PREFIX, ConfigFileConstant.DEFAULT_NODEACL_IP);
      nodeAclList.add(nodeAcl);
    }

    if (rules != null) {
      for (IscsiAccessRule rule : rules) {
        SaveConfigImpl saveConfigTemple = new SaveConfigImpl(templatePath);
        LioTarget targetTemple = this.getTheOnlyObject(saveConfigTemple.getTargets());
        tpgTemple = this.getTheOnlyObject(targetTemple.getTpgs());
        nodeAcl = this.getTheOnlyObject(tpgTemple.getNodeAcls());
        logger.debug("saveAccessRuleToMap nodeAcl {} {} {}", rule.getIncomingUser(),
            rule.getIncomingPasswd(),
            rule.getInitiatorName());
        nodeAcl.setUserId(rule.getIncomingUser());
        nodeAcl.setPassword(rule.getIncomingPasswd());
        // mutual chap if config
        String outUser = rule.getOutgoingUser();
        if (outUser != null && outUser != "") {
          nodeAcl.setMutualUserId(rule.getOutgoingUser());
          nodeAcl.setMutualPassword(rule.getOutgoingPasswd());
        }
        nodeAcl.setNodeWwn(rule.getInitiatorName());
        nodeAclList.add(nodeAcl);

      }
    }
    SaveConfigImpl saveConfigImpl = saveConfigBuilder.build();
    List<LioTarget> targets = saveConfigImpl.getTargets();
    logger.debug("saveAccessRuleToMap before add infocenter host targets {} aclList {}", targets,
        nodeAclList);
    for (LioTarget target : targets) {
      logger.debug("saveAccessRuleToMap targetName {} wwn {}", targetName, target.getWwn());
      if (target.getWwn().equals(targetName)) {
        List<LioTpg> tpgs = target.getTpgs();
        for (LioTpg tpg : tpgs) {
          LioTpgImpl tpgImpl = (LioTpgImpl) tpg;
          tpgImpl.clearNodeAcls();
          for (LioNodeAcl nodeAcl1 : nodeAclList) {
            tpgImpl.addNodeAcl(nodeAcl1);
          }
        }
      }
      if (!saveConfigImpl.persist(new File(filePath))) {
        logger.error("saveAccessRuleToMap persist fail");
        return;
      }
    }

    logger.debug("saveAccessRuleToMap ruleMap targetName {} rules {}", targetName, rules);
    ruleMap.put(targetName, rules);
    return;
  }

  @Override
  public boolean saveAccessRuleToConfigFile(String targetName, List<IscsiAccessRule> rules,
      List<IscsiAclProcessType> opTypes) {
    throw new NotImplementedException();
  }

  /**
   * Get access rule hosts from /etc/target/saveconfig.json ,and then compare with hosts from
   * infocenter.
   */
  @Override
  public synchronized List getIscsiAccessRuleList(String targetName) {
    return ruleMap.get(targetName);
  }

  @Override
  public Integer getChapControlStatus(String targetName) {
    Integer integer = chapControlMap.get(targetName);
    return integer;
  }

  /**
   * Exec targetcli sessions detail to find initiator client info.
   */
  @Override
  public synchronized List<String> listClientIps(String targetName) throws Exception {
    List<String> clientIpList = new ArrayList<>();
    String iqn = targetName.toLowerCase();
    SaveConfigImpl saveConfigImpl = saveConfigBuilder.build();
    List<LioTarget> targets = saveConfigImpl.getTargets();
    class ProcessorCon implements Utils.CommandProcessor {

      String dev;
      String clientIp;
      String backstoreStr;

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
        if (line.contains(ConfigFileConstant.ADDRESS)) {
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

        // line contente as "mapped-lun: 0 backstore: block/pyd1 mode: rw",get "pyd1"
        if (clientIp != null) {
          String regex = "pyd(\\d+)";
          dev = getString(regex, backstoreStr);
          String target = getTargetNameByStorageName(targets, dev);
          if (iqn.equals(target)) {
            clientIpList.add(clientIp);
          }
        }
        logger.debug("clientIpList is:{}", clientIpList);
        return clientIpList;
      }

      @Override
      public List<String> getErrorStream(String line) {
        logger.error("error is :{}" + line);
        return null;
      }
    }

    ProcessorCon processorCon = new ProcessorCon();
    logger.debug("Target is:{},clientIpList is:{}", targetName, clientIpList);
    int exitCode = Utils.executeCommand(sessionCommand, processorCon);
    if (exitCode != 0) {
      throw new Exception("Unable to execute command " + sessionCommand);
    }
    return clientIpList;
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
  public boolean setAttributeAuthenticationWrap(String targetName, int switcher) {
    // save to map
    chapControlMap.put(targetName, switcher);
    return true;
  }

  @Override
  public void setSetAttributeDemoModeDiscovery(String targetName) {
    // only lio need set this cmd
    return;
  }

  @Override
  public boolean lunSizeMismatched(String targetName, boolean refreshDevices) throws IOException {
    return false;
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

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getTemplatePath() {
    return templatePath;
  }

  public void setTemplatePath(String templatePath) {
    this.templatePath = templatePath;
  }

  public LioManagerConfiguration getLioManagerCon() {
    return lioManagerCon;
  }

  public void setLioManagerCon(LioManagerConfiguration lioManagerCon) {
    this.lioManagerCon = lioManagerCon;
  }

  public boolean deleteAccessRule(String targetName, String initiatorName) {
    throw new org.apache.commons.lang.NotImplementedException();
  }

  public void setChapControlStatus(String targetName, int switcher) {
    throw new org.apache.commons.lang.NotImplementedException();
  }

  public void getIqnForPydDevice(Map<String, String> pydToIqn) {
    throw new org.apache.commons.lang.NotImplementedException();
  }
}
