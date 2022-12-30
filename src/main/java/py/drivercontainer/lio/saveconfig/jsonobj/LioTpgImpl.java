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

import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import py.drivercontainer.lio.saveconfig.LioLun;
import py.drivercontainer.lio.saveconfig.LioNodeAcl;
import py.drivercontainer.lio.saveconfig.LioPortal;
import py.drivercontainer.lio.saveconfig.LioTpg;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class LioTpgImpl implements LioTpg {

  private JSONObject tpg;

  public LioTpgImpl(JSONObject tpg) {
    this.tpg = tpg;
  }

  @Override
  public List<LioLun> getLuns() {
    List<LioLun> lunsList = new ArrayList<>();
    JSONArray luns = tpg.getJSONArray(ConfigFileConstant.LUNS);
    for (int i = 0; i < luns.size(); i++) {
      JSONObject lunTemplate = luns.getJSONObject(i);
      LioLun lun = new LioLunImpl(lunTemplate);
      lunsList.add(lun);
    }
    return lunsList;
  }

  @Override
  public void addLun(LioLun lun) {
    throw new NotImplementedException();

  }

  @Override
  public void removeLun(String storageObj) {
    throw new NotImplementedException();
  }

  @Override
  public List<LioNodeAcl> getNodeAcls() {
    List<LioNodeAcl> nodeAclsList = new ArrayList<>();
    JSONArray nodeAcls = tpg.getJSONArray(ConfigFileConstant.NODE_ACLS);
    for (int i = 0; i < nodeAcls.size(); i++) {
      JSONObject nodeAclTemplate = nodeAcls.getJSONObject(i);
      LioNodeAcl nodeAcl = new LioNodeAclImpl(nodeAclTemplate);
      nodeAclsList.add(nodeAcl);
    }
    return nodeAclsList;
  }

  @Override
  public void addNodeAcl(LioNodeAcl nodeAcl) {
    JSONArray nodeAcls = tpg.getJSONArray(ConfigFileConstant.NODE_ACLS);
    LioNodeAclImpl lioNodeAcl = (LioNodeAclImpl) nodeAcl;

    nodeAcls.add(lioNodeAcl.getNodeAcl());
  }

  @Override
  public void clearNodeAcls() {
    JSONArray nodeAcls = tpg.getJSONArray(ConfigFileConstant.NODE_ACLS);
    nodeAcls.clear();
  }

  @Override
  public void removeNodeAcl(String nodeWwn) {
    throw new NotImplementedException();
  }

  @Override
  public List<LioPortal> getPortals() {
    List<LioPortal> portalsList = new ArrayList<>();
    JSONArray portals = tpg.getJSONArray(ConfigFileConstant.PORTALS);
    for (int i = 0; i < portals.size(); i++) {
      JSONObject portalTemplate = portals.getJSONObject(i);
      LioPortal portal = new LioPortalImpl(portalTemplate);
      portalsList.add(portal);
    }
    return portalsList;
  }

  @Override
  public void addPortal(LioPortal portal) {
    throw new NotImplementedException();
  }

  public JSONObject getTpg() {
    return tpg;
  }

}
