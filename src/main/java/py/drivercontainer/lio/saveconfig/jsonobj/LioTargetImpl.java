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
