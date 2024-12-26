
package py.drivercontainer.lio.saveconfig;

import java.util.List;

/**
 * The interface use to construct "targets" JsonArray in saveconfig.json
 *
 */
public interface LioTarget {

  public String getWwn();

  public void setWwn(String wwn);

  public List<LioTpg> getTpgs();

  public void addTpg(LioTpg tpg);


}
