

package py.drivercontainer.exception;

/**
 * we throw this exception during read and write ghost meta data file.
 *
 */
public class GhostFileException extends Exception {

  private static final long serialVersionUID = 1L;

  public GhostFileException() {
    super();
  }

  public GhostFileException(String message) {
    super(message);
  }

  public GhostFileException(String message, Throwable cause) {
    super(message, cause);
  }

  public GhostFileException(Throwable cause) {
    super(cause);
  }
}
