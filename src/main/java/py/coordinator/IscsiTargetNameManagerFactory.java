

package py.coordinator;

public class IscsiTargetNameManagerFactory {

  private IscsiTargetManager iscsiTargetManager;

  public IscsiTargetManager getIscsiTargetManager() {
    return iscsiTargetManager;
  }

  public void setIscsiTargetManager(IscsiTargetManager iscsiTargetManager) {
    this.iscsiTargetManager = iscsiTargetManager;
  }
}
