package ssec.jhu.flfm;

import static org.junit.jupiter.api.Assertions.*;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;

public class TestModelTranslator {

  public void testProcessInput() {
    try (NDManager manager = NDManager.newBaseManager();
        NDArray img = manager.ones(new Shape(3, 3));
        NDArray psf = manager.zeros(new Shape(3, 3)); ) {
      NDArray[] input = new NDArray[] {img, psf};
      ModelTranslator translator = new ModelTranslator();
      NDList result = translator.processInput(null, input);

      assertEquals(2, result.size());
      assertEquals(img, result.get(0));
      assertEquals(psf, result.get(1));
    }
  }

  public void testProcessOutput() {
    try (NDManager manager = NDManager.newBaseManager();
        NDArray outputArray = manager.ones(new Shape(3, 3));
        NDList outputList = new NDList(outputArray); ) {
      ModelTranslator translator = new ModelTranslator();

      NDArray result = translator.processOutput(null, outputList);

      assertEquals(outputArray, result);
    }
  }
}
