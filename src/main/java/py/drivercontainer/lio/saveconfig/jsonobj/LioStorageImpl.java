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
