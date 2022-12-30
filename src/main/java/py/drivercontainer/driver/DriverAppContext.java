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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import py.app.context.AppContextImpl;
import py.common.struct.EndPoint;
import py.instance.PortType;

public class DriverAppContext extends AppContextImpl {

  public DriverAppContext(String name) {
    super(name);
  }


  @Override
  public Map<PortType, EndPoint> getEndPointsThrift() {
    Map<PortType, EndPoint> endPointsToListenTo = new HashMap<>();
    for (Entry<PortType, EndPoint> endPoint : endPoints.entrySet()) {
      if (endPoint.getKey() != PortType.MONITOR && endPoint.getKey() != PortType.IO) {
        endPointsToListenTo.put(endPoint.getKey(), endPoint.getValue());
      }
    }
    return endPointsToListenTo;
  }
}
