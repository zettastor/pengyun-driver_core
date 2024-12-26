

package py.drivercontainer.exception;

public class RejectException extends Exception {

  private static final long serialVersionUID = 1L;

  public RejectException() {
    super();
  }

  public RejectException(String message) {
    super(message);
  }

  public RejectException(String message, Throwable cause) {
    super(message, cause);
  }

  public RejectException(Throwable cause) {
    super(cause);
  }
}
