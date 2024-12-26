

package py.drivercontainer.lio.saveconfig;

/**
 * The interface use to construct "portals" JsonArray which in "tpgs" JsonArray.
 *
 */
public interface LioPortal {

  public String getIpAddr();

  public void setIpAddr(String ipAddr);

  public int getPort();

  public void setPort(int port);

}
