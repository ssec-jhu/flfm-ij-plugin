package ssec.jhu.flfm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import ij.ImagePlus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestPluginView {

  private PluginView pluginView;

  @BeforeEach
  public void setUp() {
    pluginView = new PluginView("Test PluginView");
    pluginView.initComponents();
  }

  @Test
  public void testInitialization() {
    assertNotNull(pluginView.btnPsf, "PSF Button should be initialized");
    assertNotNull(pluginView.btnInput, "Input Button should be initialized");
    assertNotNull(pluginView.btnCalculate, "Calculate Button should be initialized");
    assertNotNull(pluginView.iterationChoice, "Iteration Choice should be initialized");
    assertNotNull(pluginView.deviceChoice, "Device Choice should be initialized");
    assertNotNull(pluginView.textFieldPsf, "PSF TextField should be initialized");
    assertNotNull(pluginView.textFieldInput, "Input TextField should be initialized");
  }

  @Test
  public void testSetPsfTextField() {
    String psfText = "PSF Path";
    pluginView.setPsfTextField(psfText);
    assertEquals(psfText, pluginView.textFieldPsf.getText(), "PSF TextField should be updated");
  }

  @Test
  public void testSetInputTextField() {
    String inputText = "Input Path";
    pluginView.setInputTextField(inputText);
    assertEquals(
        inputText, pluginView.textFieldInput.getText(), "Input TextField should be updated");
  }

  @Test
  public void testSetIterations() {
    String[] iterations = {"1", "2", "3"};
    pluginView.setIterations(iterations);
    assertEquals(
        iterations.length,
        pluginView.iterationChoice.getItemCount(),
        "Iteration Choice should have correct item count");
  }

  @Test
  public void testSetDevices() {
    String[] devices = {"CPU", "GPU"};
    pluginView.setDevices(devices);
    assertEquals(
        devices.length,
        pluginView.deviceChoice.getItemCount(),
        "Device Choice should have correct item count");
  }

  @Test
  public void testGetSelectedIterationIndex() {
    String[] iterations = {"1", "2", "3"};
    pluginView.setIterations(iterations);
    pluginView.iterationChoice.select(1);
    assertEquals(
        1, pluginView.getSelectedIterationIndex(), "Selected iteration index should be correct");
  }

  @Test
  public void testGetSelectedDeviceIndex() {
    String[] devices = {"CPU", "GPU"};
    pluginView.setDevices(devices);
    pluginView.deviceChoice.select(0);
    assertEquals(0, pluginView.getSelectedDeviceIndex(), "Selected device index should be correct");
  }

  @Test
  public void testStartProcessImage() {
    pluginView.startProcessImage();
    assertEquals(
        Constants.BTN_WORKING,
        pluginView.btnCalculate.getLabel(),
        "Calculate button label should be updated");
    assertFalse(pluginView.btnCalculate.isEnabled(), "Calculate button should be disabled");
  }

  @Test
  public void testEndProcessedImage() {
    ImagePlus mockImage = mock(ImagePlus.class);
    when(mockImage.getTitle()).thenReturn("Processed Image");

    pluginView.endProcessedImage(mockImage);
    assertEquals(
        Constants.BTN_CALCULATE,
        pluginView.btnCalculate.getLabel(),
        "Calculate button label should be reset");
    assertTrue(pluginView.btnCalculate.isEnabled(), "Calculate button should be enabled");
  }
}
