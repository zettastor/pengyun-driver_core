

package py.drivercontainer.exception;

public class ProcessPositionException extends Exception {

  private static final long serialVersionUID = 1L;

  public ProcessPositionException() {
    super();
  }

  public ProcessPositionException(String message) {
    super(message);
  }

  public ProcessPositionException(String message, Throwable cause) {
    super(message, cause);
  }

  public ProcessPositionException(Throwable cause) {
    super(cause);
  }
}
