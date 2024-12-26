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

package py.coordinator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.common.OsCmdExecutor;
import py.drivercontainer.utils.DriverContainerUtils;

public class PydDevice {

  private static final Logger logger = LoggerFactory.getLogger(PydDevice.class);
  private static final String osCMD = "lsblk -b";
  private String pydDevName;
  private long pydDevSize;

  //The method use to get all devices size name as pyd* .

  /**
   * xx.
   */
  public static List<PydDevice> loadPydDevicesSize() throws IOException {
    List<PydDevice> pydDeviceList = new ArrayList<>();
    pydDeviceList.clear();
    logger.debug("Check all pyd dev size");
    OsCmdExecutor.OsCmdStreamConsumer stdoutStreamConsumer;
    OsCmdExecutor.OsCmdOutputLogger stderrStreamConsumer;

    stdoutStreamConsumer = new OsCmdExecutor.OsCmdStreamConsumer() {

      @Override
      public void consume(InputStream stream) throws IOException {
        String line;
        BufferedReader br;
        br = new BufferedReader(new InputStreamReader(stream));
        while ((line = br.readLine()) != null) {
          logger.debug("Line for command [{}]: {}", osCMD, line);
          if (line.contains("pyd")) {
            PydDevice pydDeviceSize = new PydDevice();
            String[] lines = line.trim().split("\\s+");
            //"lsblk -b" result is "pyd0            245:0    0  4294967296  0 disk",split the
            // string use space
            // 4294967296 is size
            pydDeviceSize.setPydDevName("/dev/" + lines[0]);
            pydDeviceSize.setPydDevSize(Long.parseLong(lines[3]));
            pydDeviceList.add(pydDeviceSize);
          }
        }
      }
    };
    stderrStreamConsumer = new OsCmdExecutor.OsCmdOutputLogger(logger, osCMD);
    stderrStreamConsumer.setErrorStream(true);

    try {
      OsCmdExecutor.exec(osCMD, DriverContainerUtils.osCMDThreadPool, stdoutStreamConsumer,
          stderrStreamConsumer);
    } catch (IOException | InterruptedException e) {
      throw new IOException(e);
    }
    return pydDeviceList;
  }

  public long getPydDevSize() {
    return pydDevSize;
  }

  public void setPydDevSize(long pydDevSize) {
    this.pydDevSize = pydDevSize;
  }

  public String getPydDevName() {
    return pydDevName;
  }

  public void setPydDevName(String pydDevName) {
    this.pydDevName = pydDevName;
  }

  @Override
  public String toString() {
    return "PydDevice{"
        + "pydDevName='" + pydDevName + '\''
        + ", pydDevSize=" + pydDevSize
        + '}';
  }
}
