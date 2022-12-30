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

package py.drivercontainer.loopback;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import py.coordinator.loopback.JsonFile;
import py.coordinator.loopback.LoopbackManager;
import py.test.TestBase;

public class CreateLoopbackTest extends TestBase {

  String volumeId = "3698289029951941354";
  String volumeId2 = "1234567890123456789";
  LoopbackManager loopback;
  JsonFile jsonFile = new JsonFile();
  String tempPath = "src/test/resources/config/loopback.json";
  String filePath = "/tmp/loopbacktest/loopback.json";


  /**
   * xx.
   */
  @Before
  public void init() throws IOException {
    File file = new File(filePath);
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      file.createNewFile();
    }
    loopback = new LoopbackManager();

    loopback.setTemplatePath(tempPath);
    loopback.setFilePath(filePath);
    JSONObject rootTempFile = jsonFile.getJsonObject(tempPath);
    JSONArray storageObjects = rootTempFile.getJSONArray("storage_objects");
    JSONArray targets = rootTempFile.getJSONArray("targets");
    storageObjects.clear();
    targets.clear();
    jsonFile.save(rootTempFile, filePath);
  }


  @Test
  public void test() {
    String dev1 = "/tmp/dev0";
    String wwn1 = "naa.50014" + volumeId.substring(0, 11);
    String dev2 = "/tmp/dev1";
    String wwn2 = "naa.50014" + volumeId2.substring(0, 11);
    String volumeid1 = "123456789";
    String volumeid2 = "987654321";
    loopback.createLoopback(dev1, wwn1, volumeid1);
    loopback.createLoopback(dev2, wwn2, volumeid2);
    JSONObject root = jsonFile.getJsonObject(loopback.getFilePath());
    JSONArray readTargets = root.getJSONArray("targets");
    JSONArray readStorage = root.getJSONArray("storage_objects");
    Assert.assertTrue(readTargets.size() == 2);
    Assert.assertTrue(readStorage.size() == 2);
  }

  @After
  public void clean() {
    FileUtils.deleteQuietly(Paths.get("/tmp/loopbacktest").toFile());
  }


}
