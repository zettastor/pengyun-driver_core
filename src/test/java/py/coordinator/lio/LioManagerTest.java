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

package py.coordinator.lio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import py.drivercontainer.lio.saveconfig.LioNodeAcl;
import py.drivercontainer.lio.saveconfig.LioTarget;
import py.drivercontainer.lio.saveconfig.LioTpg;
import py.drivercontainer.lio.saveconfig.jsonobj.SaveConfigImpl;

public class LioManagerTest extends LioTargetTestBase {

  @Before
  public void initailize() throws Exception {
    super.init();
  }

  /**
   * create a target with targetName in initailize method,and then use e.
   */
  @Test
  public void existTargetTest() throws Exception {
    Assert.assertTrue(lioManager.existTarget(targetName));
    Assert.assertFalse(lioManager.existTarget("iqn_123456789"));
  }

  /**
   * use targetName can find pyd dev.
   */
  @Test
  public void getPydDevByTargetName() {
    Assert.assertTrue(lioManager.getPydDevByTargetName(targetName).equals(pydDev));
  }

  /**
   * 127.0.0.1 is default value in "node_acls" JsonArray ,when create a new node_acl,127.0.0.1 will
   * be replace by the new ip
   */
  @Test
  public void saveAccessRuleHostToConfigFileTest() {
    String nodeWwn = "iqn.123456789";
    List<String> hostList = new ArrayList<>();
    hostList.add("192.168.2.101");

    SaveConfigImpl saveConfigImpl = saveConfigBuilder.build();
    List<LioNodeAcl> nodeAcls = null;
    List<LioTarget> targets = saveConfigImpl.getTargets();
    for (LioTarget target : targets) {
      List<LioTpg> tpgs = target.getTpgs();
      for (LioTpg tpg : tpgs) {
        nodeAcls = tpg.getNodeAcls();
        for (LioNodeAcl nodeAcl : nodeAcls) {
          Assert.assertFalse(nodeAcl.getNodeWwn().contains("192.168.2.101"));
          Assert.assertTrue(nodeAcl.getNodeWwn().contains("127.0.0.1"));
        }
      }
    }
    lioManager.saveAccessRuleHostToConfigFile(targetName, hostList);
    saveConfigImpl.load();
    targets = saveConfigImpl.getTargets();
    for (LioTarget target : targets) {
      List<LioTpg> tpgs = target.getTpgs();
      for (LioTpg tpg : tpgs) {
        nodeAcls = tpg.getNodeAcls();
        for (LioNodeAcl nodeAcl : nodeAcls) {
          Assert.assertTrue(nodeAcl.getNodeWwn().contains("192.168.2.101"));
          Assert.assertFalse(nodeAcl.getNodeWwn().contains("127.0.0.1"));
        }
      }
    }

  }

  @Test
  public void getVolumAccessHostListTest() {
    List<String> hostList = lioManager.getVolumAccessHostList(targetName);
    Assert.assertTrue(hostList.size() == 1);
    Assert.assertTrue(hostList.contains("127.0.0.1"));

  }

  @Test
  public void testListClientIps() throws Exception {
    final String[] ips = {"[fe80::5054:ff:fe54:4d0c]", "10.0.1.16"};
    final File sessionScript = new File("/tmp/session-script." + getClass().getSimpleName());

    lioManager.createTarget(targetName, "localhost", "localhost", pydDev, 1234, 0);

    for (String ip : ips) {
      StringBuilder sessionScriptCmds;
      sessionScriptCmds = new StringBuilder();
      sessionScriptCmds.append("echo \"mapped-lun: 0 backstore: /block/pyd0 mode: rw\"");
      sessionScriptCmds.append("\n");
      sessionScriptCmds
          .append(String
              .format("echo \"    address: %s (TCP)  cid: 0 connection-state: LOGGED_IN\"", ip));
      sessionScriptCmds.append("\n");

      BufferedWriter writer;
      writer = new BufferedWriter(new FileWriter(sessionScript));
      try {
        writer.write(sessionScriptCmds.toString());
        writer.flush();
        writer.close();
      } catch (Exception e) {
        writer.close();
        if (sessionScript.exists()) {
          sessionScript.delete();
        }
      }

      List<String> clientIps;

      try {
        lioManager.setSessionCommand("bash " + sessionScript.getAbsolutePath());
        clientIps = lioManager.listClientIps(targetName);
        Assert.assertEquals(1, clientIps.size());
        Assert.assertTrue(clientIps.get(0).contains(InetAddress.getByName(ip).getHostAddress()));
      } finally {
        if (sessionScript.exists()) {
          sessionScript.delete();
        }
      }
    }
  }

  /**
   * use storage name which in "storage_objects" JsonArray can find targetName in "targets".
   * JsonArray
   */
  @Test
  public void getTargetNameByStorageNameTest() {
    SaveConfigImpl saveConfigImpl = saveConfigBuilder.build();
    List<LioTarget> targets = saveConfigImpl.getTargets();
    lioManager.getTargetNameByStorageName(targets, "pyd0").equals(targetName);
  }

  @After
  public void clean() {
    FileUtils.deleteQuietly(Paths.get("/tmp/saveconfigTest").toFile());
  }

  public static class FakeLioManager extends LioManager {

    /**
     * If some machine doesn't have nbd module in kernel, use this function.
     */
    @Override

    public List<String> getAvailabeNbdDeviceList() {
      List<String> nbdList = new ArrayList<String>();
      nbdList.add("/tmp/dev0");
      return nbdList;
    }

    @Override
    public boolean executeCommand(String command) {
      return true;
    }

    public boolean isPydAlive(String pydDev, int servicePort) throws Exception {
      return true;

    }

  }
}
