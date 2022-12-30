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

package py.drivercontainer.driver.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Time-stamp is a part of driver version. Its format is as following.
 * <li>[year][month][day of month][hour][minute][second]</li>
 *
 * <p>Example:
 * <li>20170909112113</li>
 *
 * <p>One of purposes of this class is to check whether the format of version is valid.
 *
 */
public class Timestamp {

  public static final int VERSION_TIMESTAMP_YEAR_LEN = 4;

  public static final int VERSION_TIMESTAMP_MONTH_LEN = 2;

  public static final int VERSION_TIMESTAMP_DAY_LEN = 2;

  public static final int VERSION_TIMESTAMP_HOUR_LEN = 2;

  public static final int VERSION_TIMESTAMP_MINUTE_LEN = 2;

  public static final int VERSION_TIMESTAMP_SECOND_LEN = 2;

  public static final int VERSION_TIMESTAMP_LEN =
      VERSION_TIMESTAMP_YEAR_LEN + VERSION_TIMESTAMP_MONTH_LEN
          + VERSION_TIMESTAMP_DAY_LEN + VERSION_TIMESTAMP_HOUR_LEN + VERSION_TIMESTAMP_MINUTE_LEN
          + VERSION_TIMESTAMP_SECOND_LEN;
  private static final Logger logger = LoggerFactory.getLogger(Timestamp.class);
  private int year;
  private int month;
  private int dayOfMonth;
  private int hourOfDay;
  private int minute;
  private int second;

  private Timestamp() {
  }


  /**
   * xx.
   */
  public static Timestamp get(String timestampStr) {
    //logger.debug("Parsing timestamp from string {}", timestampStr);

    if (timestampStr.length() != VERSION_TIMESTAMP_LEN) {
      logger.error("Illegal timestamp format! Timestamp string: {}", timestampStr);
      return null;
    }

    int elementOff = 0;
    char[] timestampChars = timestampStr.toCharArray();

    char[] yearChars = new char[VERSION_TIMESTAMP_YEAR_LEN];
    System.arraycopy(timestampChars, elementOff, yearChars, 0, yearChars.length);
    elementOff += yearChars.length;

    char[] monthChars = new char[VERSION_TIMESTAMP_MONTH_LEN];
    System.arraycopy(timestampChars, elementOff, monthChars, 0, monthChars.length);
    elementOff += monthChars.length;

    char[] dayChars = new char[VERSION_TIMESTAMP_DAY_LEN];
    System.arraycopy(timestampChars, elementOff, dayChars, 0, dayChars.length);
    elementOff += dayChars.length;

    char[] hourChars = new char[VERSION_TIMESTAMP_HOUR_LEN];
    System.arraycopy(timestampChars, elementOff, hourChars, 0, hourChars.length);
    elementOff += hourChars.length;

    char[] minuteChars = new char[VERSION_TIMESTAMP_MINUTE_LEN];
    System.arraycopy(timestampChars, elementOff, minuteChars, 0, minuteChars.length);
    elementOff += minuteChars.length;

    char[] secondChars = new char[VERSION_TIMESTAMP_SECOND_LEN];
    System.arraycopy(timestampChars, elementOff, secondChars, 0, secondChars.length);
    elementOff += secondChars.length;

    Timestamp timestamp = new Timestamp();

    try {
      timestamp.setYear(Integer.parseInt(new String(yearChars)));
      timestamp.setMonth(Integer.parseInt(new String(monthChars)));
      timestamp.setDayOfMonth(Integer.parseInt(new String(dayChars)));
      timestamp.setHourOfDay(Integer.parseInt(new String(hourChars)));
      timestamp.setMinute(Integer.parseInt(new String(minuteChars)));
      timestamp.setSecond(Integer.parseInt(new String(secondChars)));
    } catch (NumberFormatException e) {
      logger.error("Illegal timestamp format! Timestamp string: {}", timestampStr);
      return null;
    }

    //logger.debug("Parsed timestamp {}", timestamp);
    return timestamp;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public int getMonth() {
    return month;
  }

  public void setMonth(int month) {
    this.month = month;
  }

  public int getDayOfMonth() {
    return dayOfMonth;
  }

  public void setDayOfMonth(int dayOfMonth) {
    this.dayOfMonth = dayOfMonth;
  }

  public int getHourOfDay() {
    return hourOfDay;
  }

  public void setHourOfDay(int hourOfDay) {
    this.hourOfDay = hourOfDay;
  }

  public int getMinute() {
    return minute;
  }

  public void setMinute(int minute) {
    this.minute = minute;
  }

  public int getSecond() {
    return second;
  }

  public void setSecond(int second) {
    this.second = second;
  }

  public String format() {
    return String
        .format("%04d%02d%02d%02d%02d%02d", year, month, dayOfMonth, hourOfDay, minute, second);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + dayOfMonth;
    result = prime * result + hourOfDay;
    result = prime * result + minute;
    result = prime * result + month;
    result = prime * result + second;
    result = prime * result + year;
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
    Timestamp other = (Timestamp) obj;
    if (dayOfMonth != other.dayOfMonth) {
      return false;
    }
    if (hourOfDay != other.hourOfDay) {
      return false;
    }
    if (minute != other.minute) {
      return false;
    }
    if (month != other.month) {
      return false;
    }
    if (second != other.second) {
      return false;
    }
    if (year != other.year) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Timestamp [year=" + year + ", month=" + month + ", dayOfMonth=" + dayOfMonth
        + ", hourOfDay="
        + hourOfDay + ", minute=" + minute + ", second=" + second + "]";
  }
}
