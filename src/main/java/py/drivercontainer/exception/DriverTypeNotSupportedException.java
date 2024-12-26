
package py.drivercontainer.exception;

public class DriverTypeNotSupportedException extends Exception {

  private static final long serialVersionUID = 1L;

  public DriverTypeNotSupportedException() {
    super();
  }

  public DriverTypeNotSupportedException(String message) {
    super(message);
  }

  public DriverTypeNotSupportedException(String message, Throwable cause) {
    super(message, cause);
  }

  public DriverTypeNotSupportedException(Throwable cause) {
    super(cause);
  }
}
