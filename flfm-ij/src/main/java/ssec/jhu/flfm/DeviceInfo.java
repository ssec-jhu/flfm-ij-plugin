package ssec.jhu.flfm;

public class DeviceInfo {
  public String type;
  public int id;

  public DeviceInfo(String type, int id) {
    this.type = type;
    this.id = id;
  }

  public String toDeviceName() {
    if (id == -1) {
      return type.toLowerCase(); // cpu
    }
    return type.toLowerCase() + id; // gpu0, gpu1, etc.
  }

  public String toDisplay() {
    if (id == -1) {
      return type.toUpperCase(); // CPU
    }
    return type.toUpperCase() + ":" + id; // GPU:0, GPU:1, etc.
  }
}
