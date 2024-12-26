

package py.coordinator.lio;

/**
 * the interface build wwn for saveconfig.json file in "targets" JsonArray,and get volumeid from
 * wwn,get ip from node_wwn in "node_acls" JsonArray
 */
public interface LioNameBuilder {

  public String buildLioWwn(long volumeId, int snapshotId);

  public String getVolumeId(String wwn);

  public String getNodeWwnIp(String nodeWwn);


}
