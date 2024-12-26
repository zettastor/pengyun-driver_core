

package py.drivercontainer.exception;

public class FailedToStartDriverException extends Exception {

  private static final long serialVersionUID = 1L;

  public FailedToStartDriverException() {
    super();
  }

  public FailedToStartDriverException(String message) {
    super(message);
  }

  public FailedToStartDriverException(String message, Throwable cause) {
    super(message, cause);
  }

  public FailedToStartDriverException(Throwable cause) {
    super(cause);
  }
}
