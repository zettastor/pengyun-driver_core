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

package py.drivercontainer.lio.saveconfig.jsonobj;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.OsCmdExecutor;
import py.drivercontainer.lio.saveconfig.LioNodeAcl;
import py.drivercontainer.lio.saveconfig.LioStorage;
import py.drivercontainer.lio.saveconfig.LioTarget;
import py.drivercontainer.lio.saveconfig.LioTpg;
import py.drivercontainer.lio.saveconfig.SaveConfig;
import py.drivercontainer.utils.DriverContainerUtils;

public class SaveConfigImpl implements SaveConfig {

  private static final Logger logger = LoggerFactory.getLogger(SaveConfigImpl.class);
  private String filePath;
  private JSONObject root;
  private String restoreCommand;


  /**
   * xx.
   */
  public SaveConfigImpl(String filePath) {
    this.filePath = filePath;
    try {
      root = getJsonObject(filePath);
    } catch (Exception e) {
      logger.warn("catch an exception :{}", e);
    }
  }


  /**
   * xx.
   */
  public boolean load() {
    try {
      root = getJsonObject(filePath);
      if (root == null) {
        logger.error("fail to getJsonObject, path {}", filePath);
        return false;
      }
    } catch (Exception e) {
      logger.error("catch an exception :{}", e);
      return false;
    }
    return true;
  }

  @Override
  public List<LioStorage> getStorages() {
    JSONArray storageObjects = root.getJSONArray(ConfigFileConstant.STORAGE_OBJECTS);
    List<LioStorage> lioStorageList = new ArrayList<>();
    for (int i = 0; i < storageObjects.size(); i++) {
      JSONObject storageObject = storageObjects.getJSONObject(i);
      LioStorage storage = new LioStorageImpl(storageObject);
      lioStorageList.add(storage);
    }
    return lioStorageList;
  }

  @Override
  public void addStorage(LioStorage storage) throws Exception {
    JSONArray storages = root.getJSONArray(ConfigFileConstant.STORAGE_OBJECTS);
    LioStorageImpl storageImpl = (LioStorageImpl) storage;
    logger.debug("before add storages is :{}", storages);
    storages.add(storageImpl.getStorageObject());
    logger.debug("after add storages is :{}", storages);

  }


  @Override
  public void removeStorage(String dev) {
    JSONArray storageObjectsFile = root.getJSONArray(ConfigFileConstant.STORAGE_OBJECTS);
    logger.debug("before remove storage_objectsFile is:{} ", storageObjectsFile);
    for (int i = 0; i < storageObjectsFile.size(); i++) {
      JSONObject storage = storageObjectsFile.getJSONObject(i);
      if (storage.get(ConfigFileConstant.DEV).equals(dev)) {
        storageObjectsFile.remove(storage);
      }
    }
    logger.debug("after remove storage_objectsFile is:{} ", storageObjectsFile);

  }

  @Override
  public void removeAllStorages() {
    JSONArray storageObjectsFile = root.getJSONArray(ConfigFileConstant.STORAGE_OBJECTS);
    for (int i = 0; i < storageObjectsFile.size(); i++) {
      JSONObject storage = storageObjectsFile.getJSONObject(i);
      storageObjectsFile.remove(storage);
    }
  }

  @Override
  public void removeAllTargets() {
    JSONArray targetsFile = root.getJSONArray(ConfigFileConstant.TARGETS);
    logger.debug("before remove targetsFile is:{} ", targetsFile);
    for (int i = 0; i < targetsFile.size(); i++) {
      JSONObject target = targetsFile.getJSONObject(i);
      targetsFile.remove(target);
    }
  }

  @Override
  public List<LioTarget> getTargets() {
    JSONArray targets = root.getJSONArray(ConfigFileConstant.TARGETS);
    List<LioTarget> targetsList = new ArrayList<>();
    for (int i = 0; i < targets.size(); i++) {
      JSONObject targetTemplate = targets.getJSONObject(i);
      LioTarget target = new LioTargetImpl(targetTemplate);
      targetsList.add(target);
    }
    return targetsList;
  }


  @Override
  public void addTarget(LioTarget target) throws Exception {
    JSONArray targetsFile = root.getJSONArray(ConfigFileConstant.TARGETS);
    LioTargetImpl targetImpl = (LioTargetImpl) target;
    logger.debug("before add targetsFile is :{}", targetsFile);
    targetsFile.add(targetImpl.getTarget());
    logger.debug("after add targetsFile is :{}", targetsFile);
  }

  @Override
  public void removeTarget(String iqn) {
    JSONArray targetsFile = root.getJSONArray(ConfigFileConstant.TARGETS);
    logger.debug("before remove targetsFile is:{} ", targetsFile);
    for (int i = 0; i < targetsFile.size(); i++) {
      JSONObject target = targetsFile.getJSONObject(i);
      if (target.get(ConfigFileConstant.WWN).equals(iqn)) {
        targetsFile.remove(target);
      }
    }
    logger.debug("after remove targetsFile is:{} ", targetsFile);
  }


  @Override
  public boolean persist(File configFile) {
    File jsonFile = configFile;
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(jsonFile));
      bw.write(root.toString(4));
      bw.flush();
    } catch (Exception e) {
      logger.warn("Catch an exception when save to saveconfig.json file", e);
      return false;
    } finally {
      try {
        bw.close();
      } catch (IOException e) {
        logger.warn("Catch an exception when close BufferedWriter  ", e);
      }
    }
    try {
      OsCmdExecutor.OsCmdOutputLogger consumer = new OsCmdExecutor.OsCmdOutputLogger(logger,
          restoreCommand);
      int existCode = OsCmdExecutor
          .exec(restoreCommand, DriverContainerUtils.osCMDThreadPool, consumer, consumer);
      if (existCode != 0) {
        logger.warn("Catch an exception when exec restore command:{} to restore  saveconfig.json",
            restoreCommand);
        return false;
      }
    } catch (IOException | InterruptedException e) {
      logger.error("Catch an Exception when restore saveconfig file", e);
      return false;

    }
    return true;
  }


  /**
   * xx.
   */
  public JSONObject getJsonObject(String filePath) throws Exception {
    JSONObject root = null;
    File file = new File(filePath);
    if (!file.exists() || file.length() == 0) {
      return root;
    }
    BufferedReader br = null;
    StringBuffer sb;
    try {
      br = new BufferedReader(new FileReader(file));
      sb = new StringBuffer();

      String line;

      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
      root = JSONObject.fromObject(sb.toString());
    } finally {
      br.close();
    }

    return root;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getRestoreCommand() {
    return restoreCommand;
  }

  public void setRestoreCommand(String restoreCommand) {
    this.restoreCommand = restoreCommand;
  }


  /**
   * xx.
   */
  public Map<String, List<String>> getTargetsMap() {
    Map<String, List<String>> retrievedAcls = new HashMap<String, List<String>>();
    List<LioTarget> targets = getTargets();
    if (targets.size() == 0) {
      logger.debug("there is no target");
      return null;
    }
    for (LioTarget target : targets) {
      List<LioTpg> tpgs = target.getTpgs();
      if (tpgs != null && tpgs.size() == 0) {
        continue;
      }
      List<String> list = new ArrayList<>();
      for (LioTpg tpg : tpgs) {
        List<LioNodeAcl> acls = tpg.getNodeAcls();
        for (LioNodeAcl acl : acls) {
          logger.debug("NodeWWN {}", acl.getNodeWwn());
          list.add(acl.getNodeWwn());
        }
      }
      logger.debug("target WWN {}", target.getWwn());
      retrievedAcls.put(target.getWwn(), list);
    }
    return retrievedAcls;
  }


  /**
   * xx.
   */
  public boolean existTarget(String targetName) {
    List<LioTarget> targets = getTargets();
    if (targets.size() == 0) {
      logger.debug("no target");
      return false;
    }
    for (LioTarget target : targets) {
      if (target.getWwn().equals(targetName)) {
        return true;
      }
    }
    logger.debug("currently there is such target");
    return false;
  }
}
