package ssec.jhu.flfm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Unit tests for the DeviceInfo class. */
public class TestDeviceInfo {

  @ParameterizedTest
  @CsvSource({"CPU, 0", "GPU, 1", "TPU, 2"})
  public void testConstructor(String type, int id) {
    DeviceInfo device = new DeviceInfo(type, id);
    assertEquals(type, device.type);
    assertEquals(id, device.id);
  }

  @ParameterizedTest
  @CsvSource({"CPU, -1", "GPU, 0", "TPU, 3"})
  public void testToDeviceName(String type, int id) {
    DeviceInfo device = new DeviceInfo(type, id);
    if (id == -1) {
      assertEquals(type.toLowerCase(), device.toDeviceName());
    } else {
      assertEquals(type.toLowerCase() + ":" + id, device.toDeviceName());
    }
  }

  @ParameterizedTest
  @CsvSource({"CPU, -1", "GPU, 0", "TPU, 3"})
  public void testToDisplay(String type, int id) {
    DeviceInfo device = new DeviceInfo(type, id);
    if (id == -1) {
      assertEquals(type, device.toDisplay());
    } else {
      assertEquals(type + ":" + id, device.toDisplay());
    }
  }
}
