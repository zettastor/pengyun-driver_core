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

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * The interface provide add and remove methods for manager class to change saveconfig.json file
 *
 */
public interface SaveConfig {

  public boolean load();

  public List<LioStorage> getStorages();

  public void addStorage(LioStorage storage) throws Exception;

  public void removeStorage(String dev);

  public List<LioTarget> getTargets();

  public void addTarget(LioTarget target) throws Exception;

  public void removeTarget(String iqn);

  public boolean persist(File configFile);

  public void removeAllTargets();

  public void removeAllStorages();

  /**
   * get target and initiator name map from saveconfig.json.
   *
   * @return Map. key: TargetName, value: initiator name list
   */
  public Map<String, List<String>> getTargetsMap();

  /**
   * check whether the target exists in saveconfig.json now.
   */
  boolean existTarget(String targetName);
}
