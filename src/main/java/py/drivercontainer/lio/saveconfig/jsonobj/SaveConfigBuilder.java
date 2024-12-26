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

package py.drivercontainer.lio.saveconfig.jsonobj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.coordinator.lio.LioCommandManagerConfiguration;
import py.coordinator.lio.LioManagerConfiguration;

/**
 * A class to build instance of {@link SaveConfigImpl}.
 *
 */
public class SaveConfigBuilder {

  private static final Logger logger = LoggerFactory.getLogger(SaveConfigBuilder.class);

  private LioCommandManagerConfiguration lioCmdMaConfig;
  private LioManagerConfiguration lioMaConfig;

  /**
   * Build instance of {@link SaveConfigImpl}.
   *
   * @return instance of {@link SaveConfigImpl}
   */
  public SaveConfigImpl build() {
    SaveConfigImpl saveConfig;

    saveConfig = new SaveConfigImpl(lioCmdMaConfig.getDefaultSaveConfigFilePath());
    saveConfig.setRestoreCommand(lioMaConfig.getRestoreCommand());

    return saveConfig;
  }

  public LioCommandManagerConfiguration getLioCmdMaConfig() {
    return lioCmdMaConfig;
  }

  public void setLioCmdMaConfig(LioCommandManagerConfiguration lioCmdMaConfig) {
    this.lioCmdMaConfig = lioCmdMaConfig;
  }

  public LioManagerConfiguration getLioMaConfig() {
    return lioMaConfig;
  }

  public void setLioMaConfig(LioManagerConfiguration lioMaConfig) {
    this.lioMaConfig = lioMaConfig;
  }
}
