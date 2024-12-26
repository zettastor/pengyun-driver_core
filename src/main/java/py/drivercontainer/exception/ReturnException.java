
package py.drivercontainer.exception;

public class ReturnException extends Exception {

  private static final long serialVersionUID = 1L;

  public ReturnException() {
    super();
  }

  public ReturnException(String message) {
    super(message);
  }

  public ReturnException(String message, Throwable cause) {
    super(message, cause);
  }

  public ReturnException(Throwable cause) {
    super(cause);
  }
}
