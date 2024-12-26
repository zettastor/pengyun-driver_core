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

package py.drivercontainer.lio.saveconfig.jsonobj;

import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.drivercontainer.lio.saveconfig.LioNodeAcl;

public class LioNodeAclImpl implements LioNodeAcl {

  private static final Logger logger = LoggerFactory.getLogger(LioNodeAclImpl.class);


  private JSONObject nodeAcl;

  public LioNodeAclImpl(JSONObject nodeAcl) {
    this.nodeAcl = nodeAcl;

  }

  @Override
  public String getPassword() {
    return (String) nodeAcl.get(ConfigFileConstant.CHAP_PASSWORD);
  }

  @Override
  public void setPassword(String password) {
    nodeAcl.put(ConfigFileConstant.CHAP_PASSWORD, password);

  }

  @Override
  public String getUserId() {
    return (String) nodeAcl.get(ConfigFileConstant.CHAP_USERID);
  }

  @Override
  public void setUserId(String userId) {
    nodeAcl.put(ConfigFileConstant.CHAP_USERID, userId);

  }


  @Override
  public String getMutualPassword() {
    return (String) nodeAcl.get(ConfigFileConstant.CHAP_MUTUAL_PASSWORD);
  }

  @Override
  public void setMutualPassword(String password) {
    nodeAcl.put(ConfigFileConstant.CHAP_MUTUAL_PASSWORD, password);

  }

  @Override
  public String getMutualUserId() {
    return (String) nodeAcl.get(ConfigFileConstant.CHAP_MUTUAL_USERID);
  }

  @Override
  public void setMutualUserId(String userId) {
    nodeAcl.put(ConfigFileConstant.CHAP_MUTUAL_USERID, userId);

  }

  @Override
  public String getNodeWwn() {
    return (String) nodeAcl.get(ConfigFileConstant.NODE_WWN);
  }

  @Override
  public void setNodeWwn(String nodeWwn) {
    nodeAcl.put(ConfigFileConstant.NODE_WWN, nodeWwn);

  }

  @Override
  public void setNodeWwn(String initiatorName, String initiatorIpAddr) {
    String nodeWwn = initiatorName + ":" + initiatorIpAddr;
    setNodeWwn(nodeWwn);

  }

  public JSONObject getNodeAcl() {
    return nodeAcl;
  }


}
