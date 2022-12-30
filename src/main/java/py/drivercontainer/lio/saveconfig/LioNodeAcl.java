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
