

package py.drivercontainer.lio.saveconfig;

/**
 * The interface use to construct "node_acls" JsonArray which in "tpgs" JsonArray.
 *
 */
public interface LioNodeAcl {

  public String getPassword();

  public void setPassword(String password);

  public String getUserId();

  public void setUserId(String userId);

  public String getMutualUserId();

  public void setMutualUserId(String mutualUserId);

  public String getMutualPassword();

  public void setMutualPassword(String mutualPassword);

  public String getNodeWwn();

  public void setNodeWwn(String nodeWwn);

  public void setNodeWwn(String initiatorName, String initiatorIpAddr);

}
