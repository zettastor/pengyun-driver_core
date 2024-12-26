

package py.drivercontainer.exception;

/**
 * An exception throws out when there is no port available for launching driver.
 *
 */
public class NoAvailablePortException extends Exception {

  private static final long serialVersionUID = 1L;

  public NoAvailablePortException() {
    super();
  }

  public NoAvailablePortException(String message) {
    super(message);
  }

  public NoAvailablePortException(String message, Throwable cause) {
    super(message, cause);
  }

  public NoAvailablePortException(Throwable cause) {
    super(cause);
  }
}
