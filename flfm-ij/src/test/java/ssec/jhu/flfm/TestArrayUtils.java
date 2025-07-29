package ssec.jhu.flfm;

import static org.junit.jupiter.api.Assertions.*;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ShortProcessor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class TestArrayUtils {

  @ParameterizedTest
  @ValueSource(ints = {1, 3, 5})
  void testConvertImageToArray(int numSlices) {
    try (NDManager manager = NDManager.newBaseManager()) {
      // Create a mock ImagePlus object
      int width = 10;
      int height = 10;
      ImageStack stack = new ImageStack(width, height);
      for (int i = 0; i < numSlices; i++) {
        short[] pixels = new short[width * height];
        for (int j = 0; j < pixels.length; j++) {
          pixels[j] = (short) (i + j);
        }
        stack.addSlice(new ShortProcessor(width, height, pixels, null));
      }
      ImagePlus image = new ImagePlus("TestImage", stack);

      // Convert ImagePlus to NDArray
      try (NDArray array = ArrayUtils.convertImageToArray(image, manager)) {

        // Validate the NDArray shape
        assertEquals(numSlices, array.getShape().get(0));
        assertEquals(height, array.getShape().get(1));
        assertEquals(width, array.getShape().get(2));

        // Validate the NDArray content
        for (int slice = 0; slice < numSlices; slice++) {
          for (int j = 0; j < width; j++) {
            for (int k = 0; k < height; k++) {
              float arrayValue = array.getFloat(slice, k, j);
              float imageValue = (float) stack.getProcessor(slice + 1).getPixel(j, k);
              assertEquals(arrayValue, imageValue, 1e-5);
            }
          }
        }
      }
    }
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 3, 5}) // Test with 1, 3, and 5 slices
  void testConvertArrayToImage(int numSlices) {
    try (NDManager manager = NDManager.newBaseManager()) {
      // Create a mock NDArray
      int width = 10;
      int height = 10;
      try (NDArray array = manager.zeros(new Shape(numSlices, height, width))) {
        for (int slice = 0; slice < numSlices; slice++) {
          float[] sliceData = new float[width * height];
          for (int j = 0; j < sliceData.length; j++) {
            sliceData[j] = slice + j;
          }
          try (NDArray sliceArray = manager.create(sliceData, new Shape(height, width))) {
            array.set(new ai.djl.ndarray.index.NDIndex(slice + ",:,:"), sliceArray);
          }
        }

        // Convert NDArray to ImagePlus
        ImagePlus image = ArrayUtils.convertArrayToImage(array);

        // Validate the ImagePlus dimensions
        assertEquals(width, image.getWidth());
        assertEquals(height, image.getHeight());
        assertEquals(numSlices, image.getStackSize());

        // Validate the NDArray content
        for (int slice = 0; slice < numSlices; slice++) {
          for (int j = 0; j < width; j++) {
            for (int k = 0; k < height; k++) {
              float arrayValue = array.getFloat(slice, k, j);
              float imageValue = (float) image.getStack().getProcessor(slice + 1).getPixel(j, k);
              assertEquals(arrayValue, imageValue, 1e-5);
            }
          }
        }
      }
    }
  }
}
