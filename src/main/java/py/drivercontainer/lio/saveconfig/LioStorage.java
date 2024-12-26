
package py.drivercontainer.lio.saveconfig;

/**
 * The interface use to construct "storage_objects" JsonArray in saveconfig.json
 *
 */
public interface LioStorage {

  public String getDev();

  public void setDev(String dev);

  public String getName();

  public void setName(String name);

  public String getWwn();

  public void setWwn(String wwn);

}
