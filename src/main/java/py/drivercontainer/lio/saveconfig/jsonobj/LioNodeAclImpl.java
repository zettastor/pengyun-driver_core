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
