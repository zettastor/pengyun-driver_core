
package py.drivercontainer.exception;

public class FailedToBuildCoordinatorException extends Exception {

  private static final long serialVersionUID = 1L;

  public FailedToBuildCoordinatorException() {
    super();
  }

  public FailedToBuildCoordinatorException(String message) {
    super(message);
  }

  public FailedToBuildCoordinatorException(String message, Throwable cause) {
    super(message, cause);
  }

  public FailedToBuildCoordinatorException(Throwable cause) {
    super(cause);
  }
}
