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

package py.coordinator.lio;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import py.coordinator.lio.LioManagerTest.FakeLioManager;
import py.drivercontainer.lio.saveconfig.jsonobj.SaveConfigBuilder;
import py.drivercontainer.lio.saveconfig.jsonobj.SaveConfigImpl;
import py.drivercontainer.utils.DriverContainerUtils;
import py.test.TestBase;

/**
 * A class contains some common fields for LIO target relative tests.
 */
public class LioTargetTestBase extends TestBase {

  final String tempPath = "src/test/resources/config/lio.json";
  final String filePath = "/tmp/saveconfigTest/" + getClass().getSimpleName() + ".json";
  final String initialTemplatePath = "src/test/resources/config/initialtargetname.json";
  final String pydDev = "/tmp/pyd0";
  final String targetName = "iqn.2003-01.org.linux:iscsi.12345";

  SaveConfigBuilder saveConfigBuilder;
  LioManager lioManager;
  LioCommandManagerConfiguration lioCmdMaConfig;
  LioManagerConfiguration lioManagerCon;
  LioNameBuilder lioNameBuilder;
  ExecutorService cmdThreadPool = Executors.newFixedThreadPool(2, new ThreadFactory() {
    @Override
    public Thread newThread(Runnable r) {
      return new Thread(r, "OS CMD Consumer");
    }
  });

  @Override
  public void init() throws Exception {
    super.init();

    DriverContainerUtils.osCMDThreadPool = cmdThreadPool;
    File file = new File(filePath);
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      file.createNewFile();
    }
    createInitiatorSaveconfig();

    lioCmdMaConfig = new LioCommandManagerConfiguration();
    lioCmdMaConfig.setDefaultSaveConfigFilePath(filePath);

    lioManagerCon = new LioManagerConfiguration();
    lioManagerCon.setRestoreCommand("ls /tmp");

    lioManager = new FakeLioManager();
    lioNameBuilder = new LioNameBuilderImpl();
    lioManager.setUnbindNbdCmd("/opt/pyd/pyd-client -f %s");
    lioManager.setBindNbdCmd("/opt/pyd/pyd-client -p %s %s %s");
    lioManager.setTemplatePath(tempPath);
    lioManager.setFilePath(filePath);
    lioManager.setLioManagerCon(lioManagerCon);
    lioManager.setLioNameBuilder(lioNameBuilder);

    saveConfigBuilder = new SaveConfigBuilder();
    saveConfigBuilder.setLioCmdMaConfig(lioCmdMaConfig);
    saveConfigBuilder.setLioMaConfig(lioManagerCon);

    lioManager.setSaveConfigBuilder(saveConfigBuilder);
    lioManager.createTarget(targetName, "localhost", "localhost", pydDev, 1234, 0);
  }

  /**
   * create a saveconfig.json file with null value
   */
  public void createInitiatorSaveconfig() {
    SaveConfigImpl saveConfig = new SaveConfigImpl(initialTemplatePath);
    saveConfig.setRestoreCommand("ls /tmp");
    saveConfig.persist(new File(filePath));
  }

}
