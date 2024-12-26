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
