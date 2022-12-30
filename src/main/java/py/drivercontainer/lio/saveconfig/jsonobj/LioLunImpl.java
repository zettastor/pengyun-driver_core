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
import py.drivercontainer.lio.saveconfig.LioLun;

public class LioLunImpl implements LioLun {

  private JSONObject lun;

  public LioLunImpl(JSONObject lun) {
    this.lun = lun;
  }

  @Override
  public String getStorageObj() {
    return (String) lun.get(ConfigFileConstant.STORAGE_OBJECT);
  }

  @Override
  public void setStorageObj(String storageObj) {
    lun.put(ConfigFileConstant.STORAGE_OBJECT, storageObj);
  }


}
