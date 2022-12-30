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

package py.coordinator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import py.driver.IscsiAccessRule;

public interface IscsiTargetManager {

  public String createTarget(String targetName, String targetNameIp, String ipAddress,
      String nbdDev, long volumeId,
      int snapshotid) throws Exception;

  public boolean deleteTarget(String targetName, String nbdDevice, long volumeId, int snapShotId)
      throws Exception;

  /**
   * the method use to check iscsi service is available or not ,such as target,portal,lun, pyd
   * client and so on.
   */
  public DriverFailureSignal iscsiTargetIsAvailable(String targetName, String nbdDev, long volumeId,
      int snapshotId, boolean refreshDevices) throws Exception;

  public boolean saveAccessRuleHostToConfigFile(String iqn, List<String> incomingHostList);

  public boolean bindNbdDriverWrap(long volumeId, int snapshotId, String ipAddress, String nbdDev);

  public List<String> listClientIps(String targetName) throws Exception;

  public List getVolumAccessHostList(String targetName);

  /**
   * save acl rules list to local hash map.
   */
  public void saveAccessRuleToMap(String targetName, List<IscsiAccessRule> rules);

  // the second parameter rules' subscript is in accordance with opTypes' subscript. They are used
  // at the same time.
  public boolean saveAccessRuleToConfigFile(String targetName, List<IscsiAccessRule> rules,
      List<IscsiAclProcessType> opTypes);

  public boolean deleteAccessRule(String targetName, String initiatorName);

  public List getIscsiAccessRuleList(String targetName);

  public boolean addAccount(String targetName, String user, String passwd);

  public boolean deleteAccount(String targetName, String user);

  public boolean addOutAccount(String targetName, String user, String passwd);

  public boolean deleteOutAccount(String targetName, String user);

  /**
   * The method use to get used chap control on target.
   *
   * @return 0: no authentication  1: exist authentication
   */
  public Integer getChapControlStatus(String targetName);

  /**
   * The method use to set used chap control on target.
   */
  public void setChapControlStatus(String targetName, int switcher);

  /**
   * The method use to enable or disable lio chap control.
   *
   * @param switcher   0:disable 1:enable
   */
  public boolean setAttributeAuthenticationWrap(String targetName, int switcher);

  /**
   * The method use to disable lio demo_mode_discovery.
   */
  public void setSetAttributeDemoModeDiscovery(String targetName);

  /**
   * The method use to compare size from target lun and exist pyd dev size.
   */
  public boolean lunSizeMismatched(String targetName, boolean refreshDevices) throws IOException;

  public void getIqnForPydDevice(Map<String, String> pydToIqn);
}
