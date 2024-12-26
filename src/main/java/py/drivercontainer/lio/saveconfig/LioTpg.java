

package py.drivercontainer.lio.saveconfig;

import java.util.List;

/**
 * The interface use to construct "tpgs" JsonArray which in "targets" JsonArray.
 *
 */
public interface LioTpg {

  public List<LioLun> getLuns();

  public void addLun(LioLun lun);

  public void removeLun(String storageObj);

  public List<LioNodeAcl> getNodeAcls();

  public void addNodeAcl(LioNodeAcl nodeAcl);

  public void removeNodeAcl(String nodeWwn);

  public List<LioPortal> getPortals();

  public void addPortal(LioPortal portal);

  public void clearNodeAcls();


}
