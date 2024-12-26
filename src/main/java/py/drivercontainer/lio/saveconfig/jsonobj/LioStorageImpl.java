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
import py.drivercontainer.lio.saveconfig.LioStorage;

public class LioStorageImpl implements LioStorage {

  private JSONObject storageObject;

  public LioStorageImpl(JSONObject storageObject) {

    this.storageObject = storageObject;
  }

  @Override
  public String getDev() {
    return (String) storageObject.get(ConfigFileConstant.DEV);
  }

  @Override
  public void setDev(String dev) {
    storageObject.put(ConfigFileConstant.DEV, dev);

  }

  @Override
  public String getName() {
    return (String) storageObject.get(ConfigFileConstant.NAME);
  }

  @Override
  public void setName(String name) {
    storageObject.put(ConfigFileConstant.NAME, name);

  }

  @Override
  public String getWwn() {

    return (String) storageObject.get(ConfigFileConstant.WWN);
  }

  @Override
  public void setWwn(String wwn) {

    storageObject.put(ConfigFileConstant.WWN, wwn);
  }


  public JSONObject getStorageObject() {
    return storageObject;
  }
}
