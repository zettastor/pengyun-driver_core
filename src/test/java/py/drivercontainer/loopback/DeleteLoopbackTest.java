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

public class DeleteLoopbackTest extends TestBase {

  String volumeId = "053040ddc3f";
  String volumeId2 = "0550adfa496";
  LoopbackManager loopback;
  JsonFile jsonFile = new JsonFile();
  String tempPath = "src/test/resources/config/loopback.json";
  String filePath = "/tmp/loopbacktest/deleteloopback.json";

  @Before
  public void init() throws IOException {
    File file = new File(filePath);
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      file.createNewFile();
    }
    loopback = new LoopbackManager();

    loopback.setFilePath(filePath);
    JSONObject rootTempFile = jsonFile.getJsonObject(tempPath);
    jsonFile.save(rootTempFile, filePath);
  }

  @Test
  public void deleteLoopbackTest() {
    JSONObject root = jsonFile.getJsonObject(filePath);
    JSONArray readTargets = root.getJSONArray("targets");
    JSONArray readStorage = root.getJSONArray("storage_objects");
    Assert.assertTrue(readTargets.size() == 2);
    Assert.assertTrue(readStorage.size() == 2);
    loopback.deleteLoopback("/dev/pyd6");
    JSONObject afterRoot = jsonFile.getJsonObject(loopback.getFilePath());
    JSONArray afterDeleteTargets = afterRoot.getJSONArray("targets");
    Assert.assertTrue(afterDeleteTargets.size() == 1);

  }

  @After
  public void clean() {
    FileUtils.deleteQuietly(Paths.get("/tmp/loopbacktest").toFile());
  }


}
