
package py.drivercontainer.lio.saveconfig;

/**
 * The interface use to construct "luns" JsonArray which in "tpgs" JsonArray.
 *
 */

public interface LioLun {

  public String getStorageObj();

  public void setStorageObj(String storageObj);
}
