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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonFile {

  private static final Logger logger = LoggerFactory.getLogger(JsonFile.class);

  public JSONObject getJsonObject(String filePath) {
    JSONObject root;
    BufferedReader br = null;
    try {
      File file = new File(filePath);
      br = new BufferedReader(new FileReader(file));
      StringBuffer sb = new StringBuffer();
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
      root = JSONObject.fromObject(sb.toString());
    } catch (Exception e) {
      logger.warn("Catch an exception {} when get root from file {}", filePath, e);
      return null;
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (Exception e) {
          logger.error("fail to close file reader stream {}.", e);
        }
      }
    }
    return root;
  }

  public JSONObject createStorageObject(JSONArray storages, String nbdDev, String volumeId)
      throws IOException {
    JSONObject storage = null;
    storage = storages.getJSONObject(0);
    storage.put("dev", nbdDev);
    logger.warn("nbdDev:{}", nbdDev);
    storage.put("name", nbdDev.split("/")[2]);
    storage.put("wwn", volumeId);
    return storage;

  }

  public JSONObject createTargets(JSONArray targets, String targetName, String nbdDev)
      throws IOException {
    JSONObject target = targets.getJSONObject(0);
    target.put("wwn", targetName);
    JSONArray tpgs = target.getJSONArray("tpgs");
    for (int j = 0; j < tpgs.size(); j++) {
      JSONObject tpg = tpgs.getJSONObject(j);
      JSONArray luns = tpg.getJSONArray("luns");
      for (int k = 0; k < luns.size(); k++) {
        JSONObject lun = luns.getJSONObject(k);
        lun.put("storage_object", "/backstores/block/" + nbdDev.split("/")[2]);
      }
    }
    return target;

  }

  public List<String> getVolumeList(JSONArray targets) {
    List<String> volumeList = new ArrayList<>();
    for (int i = 0; i < targets.size(); i++) {
      JSONObject target = targets.getJSONObject(i);
      if (target.containsKey("wwn")) {
        volumeList.add(target.get("wwn").toString());
      }
    }
    return volumeList;
  }

  public JSONObject getOneTargetByPydName(JSONArray storages, JSONArray targets, String pydDev) {
    JSONObject returnTarget = new JSONObject();
    String deleteStorageName = null;
    for (int l = 0; l < storages.size(); l++) {
      JSONObject storage = storages.getJSONObject(l);
      if (storage.get("dev").toString().equals(pydDev)) {
        deleteStorageName = storage.get("name").toString();
      }
    }

    for (int i = 0; i < targets.size(); i++) {
      JSONObject target = targets.getJSONObject(i);
      JSONArray tpgs = target.getJSONArray("tpgs");
      for (int j = 0; j < tpgs.size(); j++) {
        JSONObject tpg = tpgs.getJSONObject(j);
        JSONArray luns = tpg.getJSONArray("luns");
        for (int k = 0; k < luns.size(); k++) {
          JSONObject lun = luns.getJSONObject(k);
          String str = lun.get("storage_object").toString();
          String regex = "pyd(\\d+)";
          Pattern pattern = Pattern.compile(regex);
          Matcher matcher = pattern.matcher(str);
          if (matcher.find()) {
            if (matcher.group().equals(deleteStorageName)) {
              logger.warn("deleteTargetPyd is :{}", matcher.group());
              returnTarget = target;
            }
          }
        }
      }
    }
    return returnTarget;
  }

  public JSONObject getOneStorageByPydName(JSONArray storages, String pydDev) {
    JSONObject returnStorage = new JSONObject();
    for (int i = 0; i < storages.size(); i++) {
      JSONObject storage = storages.getJSONObject(i);
      if (storage.get("dev").equals(pydDev)) {
        returnStorage = storage;
      }
    }
    return returnStorage;

  }


  /**
   * xx.
   */
  public boolean save(JSONObject root, String path) {
    File jsonFile = new File(path);
    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter(jsonFile));
      bw.write(root.toString(4));
      bw.flush();
      bw.close();
    } catch (Exception e) {
      return false;
    }
    return true;
  }

}
