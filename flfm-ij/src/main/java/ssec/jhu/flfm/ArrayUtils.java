package ssec.jhu.flfm;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.types.Shape;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

public class ArrayUtils {
  private ArrayUtils() {}

  public static NDArray convertImageToArray(ImagePlus image, NDManager manager) {
    if (image == null) {
      throw new IllegalArgumentException("Image cannot be null");
    }

    int width = image.getWidth();
    int height = image.getHeight();
    int numSlices = image.getStackSize();

    NDArray fullStack = manager.zeros(new Shape(numSlices, height, width));
    // slices are one indexed in ImageJ, but zero indexed in NDArray
    for (int slice = 1; slice <= numSlices; slice++) {
      ImageProcessor processor = image.getStack().getProcessor(slice);
      short[] pixels = (short[]) processor.getPixels();

      // Convert slice to a float array
      float[] floatPixels = new float[pixels.length];
      for (int i = 0; i < pixels.length; i++) {
        floatPixels[i] = pixels[i] & 0xFFFF; // Unsigned conversion
      }

      // Add slice to the NDArray stack
      NDArray sliceNDArray = manager.create(floatPixels, new Shape(height, width));
      fullStack.set(
          new NDIndex(slice - 1 + ",:,:"),
          sliceNDArray); // Set the slice into the stack (0-indexed)
    }

    return fullStack;
  }

  public static ImagePlus convertArrayToImage(NDArray array) {
    if (array == null) {
      throw new IllegalArgumentException("Array cannot be null");
    }
    ImageStack stack = new ImageStack((int) array.getShape().get(1), (int) array.getShape().get(2));
    int numSlices = (int) array.getShape().get(0);

    for (int slice = 0; slice < numSlices; slice++) {
      // Get each slice from the NDArray (convert to 2D)
      NDArray sliceNDArray = array.get(slice);
      float[] slicePixels = sliceNDArray.toFloatArray();

      short[] pixels = new short[slicePixels.length];
      for (int i = 0; i < slicePixels.length; i++) {
        pixels[i] = (short) slicePixels[i];
      }

      stack.addSlice(null, pixels);
    }
    // Create a new ImagePlus object with the stack
    ImagePlus image = new ImagePlus("Image", stack);

    return image;
  }
}
