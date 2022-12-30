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
