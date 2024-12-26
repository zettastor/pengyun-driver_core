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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({"classpath:config/liotarget.properties"})
public class LioManagerConfiguration {

  @Value("${chap.userid}")
  private String chapUserid = "312";

  @Value("${chap.password}")
  private String chapPassword = "312";
  @Value("${saveconfig.path}")
  private String saveconfigPath = "/etc/target/saveconfig.json";

  @Value("${bind.nbd.cmd}")
  private String bindNbdCmd = "/opt/pyd/pyd-client -p %s %s %s";
  @Value("${unbind.nbd.cmd}")
  private String unbindNbdCmd = "/opt/pyd/pyd-client -f %s";
  @Value("${nbd.device.name}")
  private String nbdDeviceName = "pyd";

  @Value("${default.liotarget.port}")
  private int defaultLiotargetPort = 3260;

  @Value("${session.command}")
  private String sessionCommand = "/usr/bin/targetcli sessions detail";

  @Value("${restore.command}")
  private String restoreCommand = "/usr/bin/targetctl restore /etc/target/saveconfig.json";

  @Value("${start.lioservice.command}")
  private String startLioserviceCommand = "/usr/sbin/service target start";

  @Value("${stop.lio.service.command}")
  private String stopLioserviceCommand = "/usr/sbin/service target stop";

  @Value("${targetcli.log.file.path}")
  private String targetcliLogFilePath = "/var/testing/targetcliLog.txt";

  @Value("${targetcli.log.level.console}")
  private String targetcliConsoleLogLevel = "warning";

  @Value("${targetcli.log.level.file}")
  private String targetcliFileLogLevel = "warning";


  public String getTargetcliLogFilePath() {
    return targetcliLogFilePath;
  }

  public void setTargetcliLogFilePath(String targetcliLogFilePath) {
    this.targetcliLogFilePath = targetcliLogFilePath;
  }

  public String getTargetcliConsoleLogLevel() {
    return targetcliConsoleLogLevel;
  }

  public void setTargetcliConsoleLogLevel(String targetcliConsoleLogLevel) {
    this.targetcliConsoleLogLevel = targetcliConsoleLogLevel;
  }

  public String getTargetcliFileLogLevel() {
    return targetcliFileLogLevel;
  }

  public void setTargetcliFileLogLevel(String targetcliFileLogLevel) {
    this.targetcliFileLogLevel = targetcliFileLogLevel;
  }

  public String getChapPassword() {
    return chapPassword;
  }

  public void setChapPassword(String chapPassword) {
    this.chapPassword = chapPassword;
  }

  public String getChapUserid() {
    return chapUserid;
  }

  public void setChapUserid(String chapUserid) {
    this.chapUserid = chapUserid;
  }

  public String getSaveconfigPath() {
    return saveconfigPath;
  }

  public void setSaveconfigPath(String saveconfigPath) {
    this.saveconfigPath = saveconfigPath;
  }

  public String getNbdDeviceName() {
    return nbdDeviceName;
  }

  public void setNbdDeviceName(String nbdDeviceName) {
    this.nbdDeviceName = nbdDeviceName;
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

  public int getDefaultLiotargetPort() {
    return defaultLiotargetPort;
  }

  public void setDefaultLiotargetPort(int defaultLiotargetPort) {
    this.defaultLiotargetPort = defaultLiotargetPort;
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

  public String getRestoreCommand() {
    return restoreCommand;
  }

  public void setRestoreCommand(String restoreCommand) {
    this.restoreCommand = restoreCommand;
  }

  public String getSessionCommand() {
    return sessionCommand;
  }

  public void setSessionCommand(String sessionCommand) {
    this.sessionCommand = sessionCommand;
  }
}
