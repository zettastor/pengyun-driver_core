
package py.coordinator;

public enum IscsiAclProcessType {
  CREATE(1),
  DELETE(2),
  UPDATE(3);
  private final int value;

  private IscsiAclProcessType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
