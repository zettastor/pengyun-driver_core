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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({"classpath:config/lioCommandManager.properties"})
public class LioCommandManagerConfiguration {

  @Value("${lio.create.storage}")
  private String lioCreateStorage = "/usr/bin/targetcli %s create name=%s dev=%s";

  @Value("${lio.create.target}")
  private String lioCreateTarget = "/usr/bin/targetcli %s create wwn=%s";

  @Value("${lio.create.lun}")
  private String lioCreateLun = "/usr/bin/targetcli %s create %s";

  @Value("${lio.create.portal}")
  private String lioCreatePortal = "/usr/bin/targetcli %s create %s %s";

  @Value("${lio.create.accuessRule}")
  private String lioCreateAccuessRule = "/usr/bin/targetcli %s create wwn=%s";

  @Value("${lio.create.chap.user}")
  private String lioCreateChapUser = "/usr/bin/targetcli %s set auth userid=%s";

  @Value("${lio.create.chap.password}")
  private String lioCreateChapPassword = "/usr/bin/targetcli %s set auth password=%s";

  @Value("${lio.create.mutual.chap.user}")
  private String lioCreateMutualChapUser = "/usr/bin/targetcli %s set auth mutual_userid=%s";

  @Value("${lio.create.mutual.chap.password}")
  private String lioCreateMutualChapPassword = "/usr/bin/targetcli %s set auth mutual_password=%s";

  @Value("${lio.save.config}")
  private String lioSaveConfig = "/usr/bin/targetcli saveconfig %s";

  @Value("${lio.clear.config}")
  private String lioClearConfig = "/usr/bin/targetcli clearconfig confirm=true";

  @Value("${lio.delete.target}")
  private String lioDeleteTarget = "/usr/bin/targetcli %s delete wwn=%s";

  @Value("${lio.delete.storage}")
  private String lioDeleteStorage = "/usr/bin/targetcli %s delete name=%s";

  @Value("${lio.delete.accuessRule}")
  private String lioDeleteAccuessRule = "/usr/bin/targetcli %s delete wwn=%s";

  @Value("${default.liotarget.port}")
  private int defaultLiotargetPort = 3260;

  @Value("${default.saveConfig.file.path}")
  private String defaultSaveConfigFilePath = "/etc/target/saveconfig.json";

  @Value("${session.command}")
  private String sessionCommand = "/usr/bin/targetcli sessions detail";

  @Value("${start.lio.service.command}")
  private String startLioserviceCommand = "service target start";

  @Value("${stop.lio.service.command}")
  private String stopLioserviceCommand = "service target stop";

  @Value("${nbd.device.name}")
  private String nbdDeviceName = "pyd";

  @Value("${nbd.device.max.num}")
  private int nbdDeviceMaxNum = 0;

  @Value("${bind.nbd.cmd}")
  private String bindNbdCmd = "/opt/pyd/pyd-client %s %s %s %s";

  @Value("${unbind.nbd.cmd}")
  private String unbindNbdCmd = "/opt/pyd/pyd-client -f %s";

  @Value("${lio.delete.portal}")
  private String lioDeletePortal = "/usr/bin/targetcli %s delete ip_address=%s ip_port=%s";

  @Value("${lio.chap.control}")
  private String lioChapControl = "/usr/bin/targetcli %s set attribute authentication=%s";

  @Value("${lio.demo.mode.discovery}")
  private String lioDemoModeDiscovery = "/usr/bin/targetcli %s set attribute demo_mode_discovery=0";

  @Value("${lio.default.cmdsn.depth}")
  private String lioDefaultCmdsnDepth
      = "/usr/bin/targetcli %s set attribute default_cmdsn_depth=%s";

  @Value("${lio.set.global.autoadddefaultportal.command}")
  private String lioAutoAddDefaultPortal
      = "/usr/bin/targetcli /iscsi set global auto_add_default_portal=false";

  @Value("${lio.get.global.autoadddefaultportal.command}")
  private String lioGetAutoAddDefaultPortalValue
      = "/usr/bin/targetcli /iscsi get global auto_add_default_portal";

  @Value("${set.emulateTpu.value}")
  private String setEmulateTpualue = "/usr/bin/targetcli %s set attribute emulate_tpu=1";

  @Value("${io.depth.effective.flag:1}")
  private int ioDepthEffectiveFlag = 1;

  @Value("${io.depth:8}")
  private int ioDepth = 8;

  public String getSetEmulateTpualue() {
    return setEmulateTpualue;
  }

  public void setSetEmulateTpualue(String setEmulateTpualue) {
    this.setEmulateTpualue = setEmulateTpualue;
  }

  public String getLioChapControl() {
    return lioChapControl;
  }

  public void setLioChapControl(String lioChapControl) {
    this.lioChapControl = lioChapControl;
  }

  public String getLioDemoModeDiscovery() {
    return lioDemoModeDiscovery;
  }

  public void setLioDemoModeDiscovery(String lioDemoModeDiscovery) {
    this.lioDemoModeDiscovery = lioDemoModeDiscovery;
  }

  public String getLioDefaultCmdsnDepth() {
    return lioDefaultCmdsnDepth;
  }

  public void setLioDefaultCmdsnDepth(String lioDefaultCmdsnDepth) {
    this.lioDefaultCmdsnDepth = lioDefaultCmdsnDepth;
  }

  public String getLioAutoAddDefaultPortal() {
    return lioAutoAddDefaultPortal;
  }

  public void setLioAutoAddDefaultPortal(String lioAutoAddDefaultPortal) {
    this.lioAutoAddDefaultPortal = lioAutoAddDefaultPortal;
  }

  public String getLioGetAutoAddDefaultPortalValue() {
    return lioGetAutoAddDefaultPortalValue;
  }

  public void setLioGetAutoAddDefaultPortalValue(String lioGetAutoAddDefaultPortalValue) {
    this.lioGetAutoAddDefaultPortalValue = lioGetAutoAddDefaultPortalValue;
  }

  public String getLioCreateMutualChapPassword() {
    return lioCreateMutualChapPassword;
  }

  public void setLioCreateMutualChapPassword(String lioCreateMutualChapPassword) {
    this.lioCreateMutualChapPassword = lioCreateMutualChapPassword;
  }

  public String getLioCreateMutualChapUser() {
    return lioCreateMutualChapUser;
  }

  public void setLioCreateMutualChapUser(String lioCreateMutualChapUser) {
    this.lioCreateMutualChapUser = lioCreateMutualChapUser;
  }

  public String getLioDeletePortal() {
    return lioDeletePortal;
  }

  public void setLioDeletePortal(String lioDeletePortal) {
    this.lioDeletePortal = lioDeletePortal;
  }

  public String getLioDeleteAccuessRule() {
    return lioDeleteAccuessRule;
  }

  public void setLioDeleteAccuessRule(String lioDeleteAccuessRule) {
    this.lioDeleteAccuessRule = lioDeleteAccuessRule;
  }

  public String getUnbindNbdCmd() {
    return unbindNbdCmd;
  }

  public void setUnbindNbdCmd(String unbindNbdCmd) {
    this.unbindNbdCmd = unbindNbdCmd;
  }

  public String getBindNbdCmd() {
    return bindNbdCmd;
  }

  public void setBindNbdCmd(String bindNbdCmd) {
    this.bindNbdCmd = bindNbdCmd;
  }

  public String getDefaultSaveConfigFilePath() {
    return defaultSaveConfigFilePath;
  }

  public void setDefaultSaveConfigFilePath(String defaultSaveConfigFilePath) {
    this.defaultSaveConfigFilePath = defaultSaveConfigFilePath;
  }

  public String getLioClearConfig() {
    return lioClearConfig;
  }

  public void setLioClearConfig(String lioClearConfig) {
    this.lioClearConfig = lioClearConfig;
  }

  public String getLioSaveConfig() {
    return lioSaveConfig;
  }

  public void setLioSaveConfig(String lioSaveConfig) {
    this.lioSaveConfig = lioSaveConfig;
  }

  public String getLioCreateChapPassword() {
    return lioCreateChapPassword;
  }

  public void setLioCreateChapPassword(String lioCreateChapPassword) {
    this.lioCreateChapPassword = lioCreateChapPassword;
  }

  public String getLioCreateChapUser() {
    return lioCreateChapUser;
  }

  public void setLioCreateChapUser(String lioCreateChapUser) {
    this.lioCreateChapUser = lioCreateChapUser;
  }

  public String getLioCreateAccuessRule() {
    return lioCreateAccuessRule;
  }

  public void setLioCreateAccuessRule(String lioCreateAccuessRule) {
    this.lioCreateAccuessRule = lioCreateAccuessRule;
  }

  public String getLioCreatePortal() {
    return lioCreatePortal;
  }

  public void setLioCreatePortal(String lioCreatePortal) {
    this.lioCreatePortal = lioCreatePortal;
  }

  public String getLioCreateLun() {
    return lioCreateLun;
  }

  public void setLioCreateLun(String lioCreateLun) {
    this.lioCreateLun = lioCreateLun;
  }

  public String getLioCreateTarget() {
    return lioCreateTarget;
  }

  public void setLioCreateTarget(String lioCreateTarget) {
    this.lioCreateTarget = lioCreateTarget;
  }

  public String getLioDeleteStorage() {
    return lioDeleteStorage;
  }

  public void setLioDeleteStorage(String lioDeleteStorage) {
    this.lioDeleteStorage = lioDeleteStorage;
  }

  public String getLioCreateStorage() {
    return lioCreateStorage;
  }

  public void setLioCreateStorage(String lioCreateStorage) {
    this.lioCreateStorage = lioCreateStorage;
  }

  public int getDefaultLiotargetPort() {
    return defaultLiotargetPort;
  }

  public void setDefaultLiotargetPort(int defaultLiotargetPort) {
    this.defaultLiotargetPort = defaultLiotargetPort;
  }

  public String getLioDeleteTarget() {
    return lioDeleteTarget;
  }

  public void setLioDeleteTarget(String lioDeleteTarget) {
    this.lioDeleteTarget = lioDeleteTarget;
  }

  public String getNbdDeviceName() {
    return nbdDeviceName;
  }

  public void setNbdDeviceName(String nbdDeviceName) {
    this.nbdDeviceName = nbdDeviceName;
  }

  public int getNbdDeviceMaxNum() {
    return nbdDeviceMaxNum;
  }

  public void setNbdDeviceMaxNum(int nbdDeviceMaxNum) {
    this.nbdDeviceMaxNum = nbdDeviceMaxNum;
  }

  public String getStopLioserviceCommand() {
    return stopLioserviceCommand;
  }

  public void setStopLioserviceCommand(String stopLioserviceCommand) {
    this.stopLioserviceCommand = stopLioserviceCommand;
  }

  public String getStartLioserviceCommand() {
    return startLioserviceCommand;
  }

  public void setStartLioserviceCommand(String startLioserviceCommand) {
    this.startLioserviceCommand = startLioserviceCommand;
  }

  public String getSessionCommand() {
    return sessionCommand;
  }

  public void setSessionCommand(String sessionCommand) {
    this.sessionCommand = sessionCommand;
  }

  public int getIoDepthEffectiveFlag() {
    return ioDepthEffectiveFlag;
  }

  public void setIoDepthEffectiveFlag(int ioDepthEffectiveFlag) {
    this.ioDepthEffectiveFlag = ioDepthEffectiveFlag;
  }

  public int getIoDepth() {
    return ioDepth;
  }

  public void setIoDepth(int ioDepth) {
    this.ioDepth = ioDepth;
  }
}
