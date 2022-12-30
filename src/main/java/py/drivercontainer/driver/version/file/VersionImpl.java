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

package py.drivercontainer.driver.version.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.drivercontainer.driver.version.Production;
import py.drivercontainer.driver.version.Timestamp;
import py.drivercontainer.driver.version.Version;

/**
 * Version format is as following: [primary version]-[production]-[timestamp].
 *
 * <p>Example: 2.3.0-internal-201709081003
 *
 */
public class VersionImpl implements Version {

  public static final String VERSION_SEPARATOR = "-";

  public static final int VERSION_SPLITS = 3;
  private static final Logger logger = LoggerFactory.getLogger(VersionImpl.class);
  private String primary;
  private Production production;
  private Timestamp timestamp;

  private VersionImpl() {
  }


  /**
   * xx.
   */
  public static VersionImpl get(String versionStr) {
    String[] splits;

    splits = versionStr.split(VERSION_SEPARATOR);
    if (splits.length != VERSION_SPLITS) {
      logger.error("Illegal version format! Version string: {}", versionStr);
      return null;
    }

    int splitIndex = 0;
    VersionImpl version = new VersionImpl();

    version.primary = splits[splitIndex++];
    if (version.primary == null || version.primary.isEmpty()) {
      logger.error("Illegal version format! Version string: {}", versionStr);
      return null;
    }
    try {
      version.production = Production.valueOf(splits[splitIndex++].toUpperCase());
    } catch (IllegalArgumentException e) {
      logger.error("Illegal version format! Version string: {}", versionStr, e);
      return null;
    }
    if (version.production == null) {
      logger.error("Illegal version format! Version string: {}", versionStr);
      return null;
    }
    version.timestamp = Timestamp.get(splits[splitIndex++]);
    if (version.timestamp == null) {
      logger.error("Illegal version format! Version string: {}", versionStr);
      return null;
    }

    return version;
  }

  @Override
  public String getPrimaryVersion() {
    return primary;
  }

  @Override
  public Production getProduction() {
    return production;
  }

  @Override
  public Timestamp getTimestamp() {
    return timestamp;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((primary == null) ? 0 : primary.hashCode());
    result = prime * result + ((production == null) ? 0 : production.hashCode());
    result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    VersionImpl other = (VersionImpl) obj;
    if (primary == null) {
      if (other.primary != null) {
        return false;
      }
    } else if (!primary.equals(other.primary)) {
      return false;
    }
    if (production != other.production) {
      return false;
    }
    if (timestamp == null) {
      if (other.timestamp != null) {
        return false;
      }
    } else if (!timestamp.equals(other.timestamp)) {
      return false;
    }
    return true;
  }


  /**
   * xx.
   */
  public String format() {
    return this.primary + VERSION_SEPARATOR + this.production.name().toLowerCase()
        + VERSION_SEPARATOR
        + this.timestamp.format();
  }

  public String formatInstallationAppendix() {
    return this.timestamp.format();
  }

  @Override
  public String toString() {
    return "VersionImpl [primary=" + primary + ", production=" + production + ", timestamp="
        + timestamp + "]";
  }

}
