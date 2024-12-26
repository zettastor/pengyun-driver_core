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

package py.drivercontainer.driver.workspace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.PyService;
import py.common.RequestIdBuilder;
import py.common.Utils;
import py.driver.DriverType;
import py.drivercontainer.driver.version.Version;
import py.drivercontainer.driver.version.file.VersionImpl;
import py.icshare.DriverKey;
import py.instance.InstanceId;
import py.test.TestBase;

/**
 * A class contains some tests for {@link DriverWorkspaceManagerImpl}.
 *
 */
public class DriverWorkspaceManagerImplTest extends TestBase {

  private static final Logger logger = LoggerFactory
      .getLogger(DriverWorkspaceManagerImplTest.class);

  private final InstanceId dcInstanceId = new InstanceId(RequestIdBuilder.get());

  private Version version = VersionImpl.get("2.3.0-internal-20170913111632");

  private File testRoot = new File("/tmp", getClass().getSimpleName());

  private File libraryPathDir;

  private File rootPathDir = new File(testRoot, "var");

  @Before
  @Override
  public void init() throws Exception {
    super.init();

    cleanUp();
    if (!testRoot.mkdirs()) {
      throw new IOException("Unable to create directory " + testRoot);
    }

    libraryPathDir = new File(testRoot,
        PyService.COORDINATOR.getServiceProjectKeyName() + "-" + version
            .formatInstallationAppendix());

    File libFile = new File(libraryPathDir, DriverWorkspaceManagerImpl.LINK_SRC_LIB_NAME);
    File configFile = new File(libraryPathDir, DriverWorkspaceManagerImpl.LINK_SRC_CONFIG_NAME);

    libFile.mkdirs();
    configFile.mkdirs();
  }

  @Test
  public void testCreateAndDeleteWorkspace() throws Exception {
    DriverWorkspaceManagerImpl workspaceManager;
    DriverWorkspace workspace;
    DriverKey key;

    key = new DriverKey(dcInstanceId.getId(), 0, 0, DriverType.NBD);
    workspaceManager = new DriverWorkspaceManagerImpl(dcInstanceId, rootPathDir.getPath(),
        testRoot.getPath());
    workspace = workspaceManager.createWorkspace(version, key);

    Assert.assertTrue(workspace.getDir().exists());

    File libFile = new File(workspace.getPath(), DriverWorkspaceManagerImpl.LINK_SRC_LIB_NAME);
    Assert.assertTrue(libFile.exists());
    Assert.assertTrue(libFile.isDirectory());

    File configFile = new File(workspace.getPath(),
        DriverWorkspaceManagerImpl.LINK_SRC_CONFIG_NAME);
    Assert.assertTrue(configFile.exists());
    Assert.assertTrue(configFile.isDirectory());

    workspaceManager.deleteWorkspace(version, key);
    Assert.assertFalse(libFile.exists());
    Assert.assertFalse(configFile.exists());
    Assert.assertFalse(workspace.getDir().exists());

    libFile = new File(libraryPathDir, DriverWorkspaceManagerImpl.LINK_SRC_LIB_NAME);
    Assert.assertTrue(libFile.exists());
    Assert.assertTrue(libFile.isDirectory());

    configFile = new File(libraryPathDir, DriverWorkspaceManagerImpl.LINK_SRC_CONFIG_NAME);
    Assert.assertTrue(configFile.exists());
    Assert.assertTrue(configFile.isDirectory());
  }

  @Test
  public void testListWorkspaces() throws Exception {
    int nworkspaces = 100;
    int nversions = 10;

    DriverWorkspaceManagerImpl workspaceManager;
    DriverWorkspace workspace;
    DriverKey key;
    List<DriverWorkspace> workspaces;

    workspaces = new ArrayList<DriverWorkspace>();
    workspaceManager = new DriverWorkspaceManagerImpl(dcInstanceId, rootPathDir.getPath(),
        testRoot.getPath());

    for (int i = 0; i < nversions; i++) {
      version = VersionImpl.get("2.3.0-internal-201709041119" + String.format("%02d", i));
      libraryPathDir = new File(testRoot,
          PyService.COORDINATOR.getServiceProjectKeyName() + "-" + version
              .formatInstallationAppendix());

      File libFile = new File(libraryPathDir, DriverWorkspaceManagerImpl.LINK_SRC_LIB_NAME);
      File configFile = new File(libraryPathDir, DriverWorkspaceManagerImpl.LINK_SRC_CONFIG_NAME);

      Assert.assertTrue(libFile.mkdirs());
      Assert.assertTrue(configFile.mkdirs());
      for (int j = 0; j < nworkspaces / nversions; j++) {
        key = new DriverKey(dcInstanceId.getId(), j, 0, DriverType.NBD);
        workspace = workspaceManager.createWorkspace(version, key);
        workspaces.add(workspace);
      }
    }

    List<DriverWorkspace> realWorkspaces;

    realWorkspaces = workspaceManager.listWorkspaces();
    Assert.assertEquals(workspaces.size(), realWorkspaces.size());
  }


  /**
   * xx.
   */
  @After
  public void cleanUp() throws Exception {
    if (testRoot.exists()) {
      Utils.deleteFileOrDirectory(testRoot);
    }
  }
}
