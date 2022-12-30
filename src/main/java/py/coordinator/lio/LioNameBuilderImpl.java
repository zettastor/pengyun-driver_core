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
