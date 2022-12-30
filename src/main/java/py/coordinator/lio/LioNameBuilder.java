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

/**
 * the interface build wwn for saveconfig.json file in "targets" JsonArray,and get volumeid from
 * wwn,get ip from node_wwn in "node_acls" JsonArray
 */
public interface LioNameBuilder {

  public String buildLioWwn(long volumeId, int snapshotId);

  public String getVolumeId(String wwn);

  public String getNodeWwnIp(String nodeWwn);


}
