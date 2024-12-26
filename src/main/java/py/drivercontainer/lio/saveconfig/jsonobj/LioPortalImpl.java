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
import py.drivercontainer.lio.saveconfig.LioPortal;

public class LioPortalImpl implements LioPortal {

  private JSONObject portal;

  public LioPortalImpl(JSONObject portal) {
    this.portal = portal;

  }

  @Override
  public String getIpAddr() {
    return (String) portal.get(ConfigFileConstant.IPADRRESS);
  }

  @Override
  public void setIpAddr(String ipAddr) {
    portal.put(ConfigFileConstant.IPADRRESS, ipAddr);
  }

  @Override
  public int getPort() {
    return (int) portal.get(ConfigFileConstant.PORT);
  }

  @Override
  public void setPort(int port) {
    portal.put(ConfigFileConstant.PORT, port);

  }

  public JSONObject getPortal() {
    return portal;
  }


}
