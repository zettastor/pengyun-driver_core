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
