

package py.drivercontainer.exception;

public class DriverLaunchArgumentsNotSupportException extends Exception {

  private static final long serialVersionUID = 1L;

  public DriverLaunchArgumentsNotSupportException() {
    super();
  }

  public DriverLaunchArgumentsNotSupportException(String message) {
    super(message);
  }

  public DriverLaunchArgumentsNotSupportException(String message, Throwable cause) {
    super(message, cause);
  }

  public DriverLaunchArgumentsNotSupportException(Throwable cause) {
    super(cause);
  }
}
