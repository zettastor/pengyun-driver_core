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

package py.coordinator.loopback;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoopbackManager {

  private static final Logger logger = LoggerFactory.getLogger(LoopbackManager.class);
  private JsonFile jsonFile = new JsonFile();
  private String filePath = "/etc/target/saveconfig.json";
  private String templatePath = "/etc/target/saveconfig_bak.json";

  public static void main(String[] args) {
    LoopbackManager loopbackManager = new LoopbackManager();
    if (args[0].equals("create") && args[1].contains("pyd")) {
      String timeStr = String.valueOf(System.currentTimeMillis());
      String wwn = "naa.50014" + timeStr.substring(timeStr.length() - 11, timeStr.length());
      if (!loopbackManager.createLoopback(args[1], wwn, args[2])) {
        System.exit(1);
      }
      System.exit(0);
    } else if (args[0].equals("delete") && args[1].contains("pyd")) {
      String pydDev = args[1];
      if (!loopbackManager.deleteLoopback(pydDev)) {
        System.exit(1);
      }
      System.exit(0);
    }

    System.exit(1);


  }

  public boolean createLoopback(String nbdDev, String targetName, String volumeId) {
    JSONObject loopRoot = jsonFile.getJsonObject(filePath);
    JSONArray loopStorages = loopRoot.getJSONArray("storage_objects");
    JSONArray loopTargets = loopRoot.getJSONArray("targets");
    JSONObject templateRoot = jsonFile.getJsonObject(templatePath);

    try {
      JSONArray storages = templateRoot.getJSONArray("storage_objects");
      loopStorages.add(jsonFile.createStorageObject(storages, nbdDev, volumeId));
      logger.warn("loopStorages:{}", loopStorages);

      JSONArray targets = templateRoot.getJSONArray("targets");
      loopTargets.add(jsonFile.createTargets(targets, targetName, nbdDev));
      logger.warn("loopTargets is:{}", loopTargets);

    } catch (Exception e) {
      logger.warn("Catch an exception when create loopback :{}", e);
      return false;
    }
    if (!jsonFile.save(loopRoot, filePath)) {
      logger.warn("Save to config file failed");
      return false;
    }
    return true;
  }

  public boolean deleteLoopback(String pydDev) {
    logger.warn("filepath:{}", filePath);
    JSONObject loopRoot = jsonFile.getJsonObject(filePath);
    JSONArray storages = loopRoot.getJSONArray("storage_objects");
    JSONArray targets = loopRoot.getJSONArray("targets");
    logger.warn("before umount storages is:{}", storages);
    logger.warn("before umount targets is:{}", targets);

    if (jsonFile.getVolumeList(targets) == null) {
      logger.warn("Failed to delete target {} due to no target here", pydDev);
      return true;
    }

    logger.warn("deletePydDev is :{}", pydDev);

    JSONObject deleteStorage = null;
    deleteStorage = jsonFile.getOneStorageByPydName(storages, pydDev);
    logger.warn("deleteStorage is :{}", deleteStorage);
    JSONObject deleteTarget = null;
    deleteTarget = jsonFile.getOneTargetByPydName(storages, targets, pydDev);
    logger.warn("deleteTarget is :{}", deleteTarget);
    storages.remove(deleteStorage);
    targets.remove(deleteTarget);
    logger.warn("after umount storages is:{}", storages);
    logger.warn("after umount targets is:{}", targets);
    if (!jsonFile.save(loopRoot, filePath)) {
      logger.warn("Save to config file failed");
      return false;
    }

    return true;
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
}
