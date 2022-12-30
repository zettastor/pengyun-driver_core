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
import java.util.UUID;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.Utils;

public class SaveConfigJson {

  private static final Logger logger = LoggerFactory.getLogger(SaveConfigJson.class);

  public JSONObject getJsonObject(String templatePath) throws IOException {
    File file = new File(templatePath);
    BufferedReader br = new BufferedReader(new FileReader(file));
    StringBuffer sb = new StringBuffer();
    String line;
    while ((line = br.readLine()) != null) {
      sb.append(line);
    }
    JSONObject root = JSONObject.fromObject(sb.toString());
    return root;
  }

  public JSONObject createStorageObject(JSONArray storages, String nbdDev) throws IOException {

    JSONObject storage = new JSONObject();
    storage = storages.getJSONObject(0);
    storage.put("dev", nbdDev);
    storage.put("name", nbdDev.split("/")[2]);
    storage.put("wwn", UUID.randomUUID().toString());
    return storage;

  }

  public JSONObject createTargets(JSONArray targets, String targetName, String ipAddress,
      String nbdDev) throws IOException {
    JSONObject target = targets.getJSONObject(0);
    target.put("wwn", targetName);
    JSONArray tpgs = target.getJSONArray("tpgs");
    for (int j = 0; j < tpgs.size(); j++) {
      JSONObject tpg = tpgs.getJSONObject(j);
      JSONArray luns = tpg.getJSONArray("luns");
      JSONArray portals = tpg.getJSONArray("portals");
      JSONArray nodeAcls = tpg.getJSONArray("node_acls");
      nodeAcls.removeAll(nodeAcls);
      for (int k = 0; k < luns.size(); k++) {
        JSONObject lun = luns.getJSONObject(k);
        lun.put("storage_object", "/backstores/block/" + nbdDev.split("/")[2]);
      }
      for (int l = 0; l < portals.size(); l++) {
        JSONObject portal = portals.getJSONObject(l);
        portal.put("ip_address", ipAddress);

      }
    }
    return target;

  }

  public String getPydDevByTargetName(JSONArray targets, String targetName) {
    String pydDev = null;
    for (int i = 0; i < targets.size(); i++) {
      JSONObject target = targets.getJSONObject(i);
      logger.warn("targetName:{}", targetName);
      logger.warn("targetName in json file is {}", target.get("wwn"));
      if (target.get("wwn").equals(targetName)) {
        JSONArray tpgs = target.getJSONArray("tpgs");
        for (int j = 0; j < tpgs.size(); j++) {
          JSONObject tpg = tpgs.getJSONObject(j);
          JSONArray luns = tpg.getJSONArray("luns");
          for (int k = 0; k < luns.size(); k++) {
            JSONObject lun = luns.getJSONObject(k);
            pydDev = lun.get("storage_object").toString().split("/")[3];
            logger.warn("Get pydDev from json file by targetName is :{}", pydDev);
          }
        }
      }
    }
    return pydDev;

  }

  public JSONObject getOneTargetByTargetName(JSONArray targets, String targetName) {
    JSONObject returnTarget = new JSONObject();
    for (int i = 0; i < targets.size(); i++) {
      JSONObject target = targets.getJSONObject(i);
      if (target.get("wwn").equals(targetName)) {
        returnTarget = target;
      }
    }
    return returnTarget;
  }

  public JSONObject getOneStorageByPydName(JSONArray storages, String pydDev) {
    JSONObject returnStorage = new JSONObject();
    for (int i = 0; i < storages.size(); i++) {
      JSONObject storage = storages.getJSONObject(i);
      if (storage.get("name").equals(pydDev)) {
        returnStorage = storage;
      }
    }
    return returnStorage;

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


  public JSONObject createAcls(JSONArray nodeAcls, String iqn, String incomingHost)
      throws IOException {

    JSONObject acl = new JSONObject();
    acl = nodeAcls.getJSONObject(0);
    acl.put("chap_password", 312);
    acl.put("chap_userid", "root");
    acl.put("node_wwn", iqn + ":" + incomingHost);
    logger.warn("acl is :{}", acl);
    return acl;

  }


  public List<String> listClientIps(String targetName) throws Exception {
    List<String> clientIpList = new ArrayList<String>();
    List<String> sessionList = new ArrayList<>();
    Map<String, List<String>> clientMap = new HashMap<>();
    String command = "targetcli sessions detail";
    class ProcessorCon implements Utils.CommandProcessor {

      @Override
      public List<String> getNormalStream(String line) {
        logger.warn("clinet line is :{}", line);
        if (line.contains("name")) {
          sessionList.add(line.split(":")[3].split("\\s+")[0]);
          clientMap.put(line.split(":")[1].trim() + ":" + line.split(":")[2].trim(), sessionList);
        }
        logger.warn("&&&sessionList is:{}", sessionList);
        logger.warn("&&&&clientMap :{}", clientMap);

        return sessionList;
      }

      @Override
      public List<String> getErrorStream(String line) {
        return null;
      }
    }

    ProcessorCon processorCon = new ProcessorCon();
    int exitCode = Utils.executeCommand(command, processorCon);
    logger.warn("sessionList is:{}", sessionList);
    logger.warn("clientMap :{}", clientMap);
    if (exitCode != 0) {
      throw new Exception("Unable to execute command " + command);
    }
    targetName = targetName.toLowerCase();
    logger.warn("targetName:{}", targetName);
    if (clientMap.get(targetName) != null) {
      for (String str : clientMap.get(targetName)) {
        clientIpList.add(str);
      }
    }
    logger.warn("clientIpList:{}", clientIpList);

    return clientIpList;
  }

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
