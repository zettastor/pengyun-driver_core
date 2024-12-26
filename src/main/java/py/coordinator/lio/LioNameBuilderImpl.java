
package py.coordinator.lio;

public class LioNameBuilderImpl implements LioNameBuilder {

  private static final String LIO_TARGET_PREFIX = "iqn.2017-08.zettastor.iqn";

  public String buildLioWwn(long volumeId, int snapshotId) {
    return String.format("%s:%s-%s", LIO_TARGET_PREFIX, volumeId, snapshotId);
  }

  public String getVolumeId(String wwn) {
    String volumeId = wwn.split(":")[1];
    return volumeId;
  }

  public String getNodeWwnIp(String nodeWwn) {
    return nodeWwn.split(":")[1]; //node_wwn build as iqn.2017-08.zettastor.iqn:127.0.0.1
  }
}
