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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.drivercontainer.lio.saveconfig.LioTarget;
import py.drivercontainer.lio.saveconfig.LioTpg;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class LioTargetImpl implements LioTarget {

  private static final Logger logger = LoggerFactory.getLogger(LioTargetImpl.class);

  private JSONObject target;

  public LioTargetImpl(JSONObject target) {
    this.target = target;

  }

  @Override
  public String getWwn() {
    return (String) target.get(ConfigFileConstant.WWN);
  }

  @Override
  public void setWwn(String wwn) {
    target.put(ConfigFileConstant.WWN, wwn);
  }


  @Override
  public List<LioTpg> getTpgs() {
    List<LioTpg> tpgsList = new ArrayList<>();
    //logger.debug("target in lioTpg is :{}",target);
    JSONArray tpgs = target.getJSONArray(ConfigFileConstant.TPGS);
    for (int i = 0; i < tpgs.size(); i++) {
      JSONObject tpgTemplate = tpgs.getJSONObject(i);
      LioTpg tpg = new LioTpgImpl(tpgTemplate);
      tpgsList.add(tpg);
    }
    return tpgsList;

  }

  @Override
  public void addTpg(LioTpg tpg) {
    throw new NotImplementedException();

  }

  public JSONObject getTarget() {
    return target;
  }

}
