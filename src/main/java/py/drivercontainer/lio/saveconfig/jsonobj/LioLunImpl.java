

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
