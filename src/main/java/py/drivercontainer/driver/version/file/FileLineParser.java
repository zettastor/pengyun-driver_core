

package py.drivercontainer.driver.version.file;

public interface FileLineParser<T> {

  public T parse(String line);

  public String fomat(T content);
}
