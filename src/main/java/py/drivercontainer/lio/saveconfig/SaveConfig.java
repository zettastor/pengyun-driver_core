/**
* Copyright (C) 2013-2024 Nanjing Pengyun Network Technology Co., Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
