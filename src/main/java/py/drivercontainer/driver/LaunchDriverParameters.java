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

package py.drivercontainer.driver;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import py.driver.DriverMetadata;
import py.driver.DriverStatus;
import py.driver.DriverType;
import py.driver.PortalType;

/**
 * A class whose instance contains all configuration required for driver launching. The instance of
 * {@link LaunchDriverParameters} is initialized with spring framework.
 *
 * <p>All scope value in the class is passed from command line. To make invoker easy to pass all
 * arguments to command line, {@link LaunchDriverParameters} wrapped a method {@link
 * #buildCommandLineParameters()}to do this.
 *
 */
@Configuration
public class LaunchDriverParameters {

  @Value("${driver.name:null}")
  public String driverName;

  @Value("${driver.container.instance.id:0}")
  public long driverContainerInstanceId;

  @Value("${driver.type:nbd}")
  public String driverType;

  @Value("${driver.instance.id:0}")
  public long instanceId;

  @Value("${account.id:0}")
  public long accountId;

  @Value("${driver.backend.volume.id:0}")
  public long volumeId;

  @Value("${driver.backend.snapshot.id:0}")
  public int snapshotId;

  @Value("${driver.host.name:null}")
  public String hostName;
  @Value("${driver.port:0}")
  public int port = 0;
  @Value("${driver.coordinator.port:0}")
  public int coordinatorPort = 0;
  @Value("${iscsi.targetName.ip")
  public String iscsiTargetNameIp = "0.0.0.0";
  //driverIp is driver actually host name instead of "127.0.0.1"
  @Value("${driver.ip}")
  public String driverIp;
  @Value("${queryServer.ip}")
  public String queryServerIp;
  @Value("${queryServer.port}")
  public int queryServerPort;
  private PortalType portalType;
  /**
   * Network interface card name. For IPV6, open-iscsi (initiator) needs this info to do discovery.
   * The info combined with IPV6 looks like following:
   *
   * <p>fe80::5054:ff:fe34:4f86%eth0
   *
   * <p>"fe80::5054:ff:fe34:4f86" is IPV6 address, "eth0" is network interface name. They are
   * separated by "%".
   */
  private String nicName;
  /**
   * IPV6 address binding to network interface card named {@link DriverMetadata#nicName}.
   */
  private String ipv6Addr;


  public String getIscsiTargetNameIp() {
    return iscsiTargetNameIp;
  }

  public void setIscsiTargetNameIp(String iscsiTargetNameIp) {
    this.iscsiTargetNameIp = iscsiTargetNameIp;
  }

  /**
   * Wrap all scope value in the instance of the class {@link LaunchDriverParameters} in a string
   * array to make invoker easy to pass all these arguments to command line.
   *
   * @return an array of driver properties
   */
  public String[] buildCommandLineParameters() {
    List<String> cmdLineParameterList = new ArrayList<String>();
    if (DriverType.findByName(driverType) == DriverType.FSD) {
      cmdLineParameterList.add(String.format("--fs.account.id=%s", accountId));
      cmdLineParameterList.add(String.format("--fs.volume.id=%s", volumeId));
      cmdLineParameterList.add(String.format("--app.main.endpoint=%s:%s", hostName, port));
    } else {
      cmdLineParameterList
          .add(String.format("--driver.container.instance.id=%s", driverContainerInstanceId));
      cmdLineParameterList.add(String.format("--driver.type=%s", driverType));
      cmdLineParameterList.add(String.format("--driver.instance.id=%s", instanceId));
      cmdLineParameterList.add(String.format("--account.id=%s", accountId));
      cmdLineParameterList.add(String.format("--driver.backend.volume.id=%s", volumeId));
      cmdLineParameterList.add(String.format("--driver.backend.snapshot.id=%s", snapshotId));
      cmdLineParameterList.add(String.format("--driver.ip=%s", driverIp));
      cmdLineParameterList.add(String.format("--driver.host.name=%s", hostName));
      cmdLineParameterList.add(String.format("--driver.port=%s", port));
      cmdLineParameterList.add(String.format("--driver.coordinator.port=%s", coordinatorPort));
      cmdLineParameterList.add(String.format("--iscsi.targetName.ip=%s", iscsiTargetNameIp));
      cmdLineParameterList.add(String.format("--queryServer.ip=%s", queryServerIp));
      cmdLineParameterList.add(String.format("--queryServer.port=%s", queryServerPort));
      cmdLineParameterList.add(String.format("--driver.name=%s", driverName));
    }

    String[] parameters = new String[cmdLineParameterList.size()];
    cmdLineParameterList.toArray(parameters);
    return parameters;
  }

  /**
   * Build a driver instance from existing configuration.
   *
   * @return a new driver
   */
  public DriverMetadata buildDriver() {
    DriverMetadata driver = new DriverMetadata();
    driver.setDriverName(driverName);
    driver.setDriverContainerId(getDriverContainerInstanceId());
    driver.setAccountId(accountId);
    driver.setInstanceId(getInstanceId());
    driver.setVolumeId(getVolumeId());
    driver.setSnapshotId(getSnapshotId());
    driver.setDriverType(getDriverType());
    driver.setHostName(getHostName());
    driver.setPort(getPort());
    driver.setCoordinatorPort(getCoordinatorPort());
    driver.setDriverStatus(DriverStatus.LAUNCHING);
    driver.setQueryServerPort(getQueryServerPort());
    driver.setQueryServerIp(getQueryServerIp());
    driver.setPortalType(portalType);
    driver.setIpv6Addr(ipv6Addr);
    driver.setNicName(nicName);

    return driver;
  }

  public String getDriverName() {
    return driverName;
  }

  public void setDriverName(String driverName) {
    this.driverName = driverName;
  }

  public long getInstanceId() {
    return instanceId;
  }

  public LaunchDriverParameters setInstanceId(long instanceId) {
    this.instanceId = instanceId;
    return this;
  }

  public long getAccountId() {
    return accountId;
  }

  public LaunchDriverParameters setAccountId(long accountId) {
    this.accountId = accountId;
    return this;
  }

  public long getVolumeId() {
    return volumeId;
  }

  public LaunchDriverParameters setVolumeId(long volumeId) {
    this.volumeId = volumeId;
    return this;
  }

  public int getSnapshotId() {
    return snapshotId;
  }

  public LaunchDriverParameters setSnapshotId(int snapshotId) {
    this.snapshotId = snapshotId;
    return this;
  }

  public String getHostName() {
    return hostName;
  }

  public LaunchDriverParameters setHostName(String hostName) {
    this.hostName = hostName;
    return this;
  }

  public int getPort() {
    return port;
  }

  public LaunchDriverParameters setPort(int port) {
    this.port = port;
    return this;
  }

  public DriverType getDriverType() {
    return DriverType.findByName(driverType);
  }

  public LaunchDriverParameters setDriverType(DriverType driverType) {
    this.driverType = driverType.name();
    return this;
  }

  public int getCoordinatorPort() {
    return coordinatorPort;
  }

  public void setCoordinatorPort(int coordinatorPort) {
    this.coordinatorPort = coordinatorPort;
  }

  public long getDriverContainerInstanceId() {
    return driverContainerInstanceId;
  }

  public void setDriverContainerInstanceId(long driverContainerInstanceId) {
    this.driverContainerInstanceId = driverContainerInstanceId;
  }

  public String getDriverIp() {
    return driverIp;
  }

  public void setDriverIp(String driverIp) {
    this.driverIp = driverIp;
  }

  public int getQueryServerPort() {
    return queryServerPort;
  }

  public void setQueryServerPort(int queryServerPort) {
    this.queryServerPort = queryServerPort;
  }

  public String getQueryServerIp() {
    return queryServerIp;
  }

  public void setQueryServerIp(String queryServerIp) {
    this.queryServerIp = queryServerIp;
  }

  public PortalType getPortalType() {
    return portalType;
  }


  /**
   * xx.
   */
  public void setPortalType(PortalType portalType) {
    switch (portalType) {
      case IPV4:
        this.iscsiTargetNameIp = "0.0.0.0";
        break;
      case IPV6:
        this.iscsiTargetNameIp = "::0";
        break;
      default:
        break;
    }

    this.portalType = portalType;
  }

  public String getNicName() {
    return nicName;
  }

  public void setNicName(String nicName) {
    this.nicName = nicName;
  }

  public String getIpv6Addr() {
    return ipv6Addr;
  }

  public void setIpv6Addr(String ipv6Addr) {
    this.ipv6Addr = ipv6Addr;
  }

  @Override
  public String toString() {
    return "LaunchDriverParameters [driverName=" + driverName + ", driverContainerInstanceId="
        + driverContainerInstanceId + ", driverType=" + driverType + ", instanceId=" + instanceId
        + ", accountId=" + accountId + ", volumeId=" + volumeId + ", snapshotId=" + snapshotId
        + ", hostName="
        + hostName + ", portalType=" + portalType + ", nicName=" + nicName + ", ipv6Addr="
        + ipv6Addr
        + ", port=" + port + ", coordinatorPort=" + coordinatorPort
        + ", iscsiTargetNameIp=" + iscsiTargetNameIp + ", driverIp=" + driverIp + ", queryServerIp="
        + queryServerIp + ", queryServerPort=" + queryServerPort + "]";
  }
}
