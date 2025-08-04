package ssec.jhu.flfm;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.engine.Engine;
import ai.djl.engine.EngineException;
import ai.djl.engine.EngineProvider;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.TranslateException;
import ai.djl.util.ClassLoaderUtils;
import ij.ImagePlus;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for running deep learning models using DJL (Deep Java Library) within the context
 * of ImageJ plugins.
 *
 * <p>Handles engine initialization, device selection, and model inference.
 */
public class Algorithm {

  /** Logger for the Algorithm class. */
  private static final Logger logger = LoggerFactory.getLogger(Algorithm.class);

  /** Singleton instance of the DJL Engine. */
  private static Engine engine = null;

  /** Private constructor to prevent instantiation. */
  private Algorithm() {}

  /**
   * Initializes and returns the DJL Engine instance.
   *
   * <p>Attempts to get the default engine. If unavailable, tries to load the PyTorch engine
   * manually.
   *
   * @return the initialized {@link Engine} instance
   */
  private static Engine initEngine() {
    if (engine != null) {
      return engine;
    }

    // first try to get the default engine but if that fails, try to manually
    // get the PyTorch engine.
    try {
      engine = Engine.getInstance();
    } catch (EngineException e) {
      logger.debug("Default engine not available, trying PyTorch engine. Error: " + e.getMessage());
      logger.debug("Stack trace: ");
      for (StackTraceElement ste : e.getStackTrace()) {
        logger.debug(ste.toString());
      }
      EngineProvider provider =
          ClassLoaderUtils.initClass(
              Algorithm.class.getClassLoader(), EngineProvider.class, Constants.PT_ENGINE_CLASS);
      if (provider != null) {
        try {
          engine = provider.getEngine();
        } catch (Exception ee) {
          logger.error("PyTorch error: " + ee.getMessage());
          logger.error("torch library name: " + System.mapLibraryName("torch"));
          logger.error("djl_torch library name: " + System.mapLibraryName("djl_torch"));
          logger.error("Stack trace: ");
          for (StackTraceElement ste : ee.getStackTrace()) {
            logger.error(ste.toString());
          }
        }
      } else {
        logger.error("PyTorch engine not available.");
      }
    }

    return engine;
  }

  /**
   * Returns the available devices for model inference.
   *
   * <p>If the engine is not available, returns a single error device.
   *
   * @return an array of {@link DeviceInfo} representing available devices
   */
  public static DeviceInfo[] getDevices() {
    // Returns the available devices for model inference
    Device[] devices;
    try {
      devices = initEngine().getDevices();
    } catch (EngineException | NullPointerException e) {
      logger.error("Engine not available or error retrieving devices: " + e.getMessage());
      logger.error("Stack trace: ");
      for (StackTraceElement ste : e.getStackTrace()) {
        logger.error(ste.toString());
      }

      return new DeviceInfo[] {
        new DeviceInfo("ERR", -2) // show an error device if the engine is not available
      };
    }

    DeviceInfo[] deviceInfos;
    if (devices[0].getDeviceType().toLowerCase().equals("gpu")) {
      // if the first device is a GPU then all listed devices are GPUs and we
      // can add one more CPU device to the list.
      deviceInfos = new DeviceInfo[devices.length + 1];

      for (int i = 0; i < devices.length; i++) {
        deviceInfos[i] = new DeviceInfo("GPU", devices[i].getDeviceId());
      }
      deviceInfos[devices.length] = new DeviceInfo("CPU", -1); // Add CPU as last device
    } else {
      // If the first device is not a GPU, then there is just a single CPU device
      deviceInfos = new DeviceInfo[1];
      deviceInfos[0] = new DeviceInfo("CPU", -1); // Only CPU
    }
    return deviceInfos;
  }

  /**
   * Runs the specified model on the given input and PSF images using the selected device.
   *
   * @param modelPathStr the path to the model file
   * @param deviceInfo the device to use for inference
   * @param psfImage the point spread function image
   * @param inputImage the input image to process
   * @return the output {@link ImagePlus} after model inference, or {@code null} if an error occurs
   */
  public static ImagePlus runModel(
      String modelPathStr, DeviceInfo deviceInfo, ImagePlus psfImage, ImagePlus inputImage) {
    ImagePlus outputImage = null;

    try (NDManager manager = NDManager.newBaseManager(Device.fromName(deviceInfo.toDeviceName()))) {
      NDArray psfArray = ArrayUtils.convertImageToArray(psfImage, manager);
      NDArray inputArray = ArrayUtils.convertImageToArray(inputImage, manager);
      psfArray.divi(psfArray.sum()); // Normalize PSF

      String modelName = FilenameUtils.getName(modelPathStr);

      try (Model model = Model.newInstance(modelName, "PyTorch")) {
        try {
          // The model will either be loaded from the specified path
          // if it's being run in the IDE or from the resources/models
          // directory if it's being run as a packaged JAR.
          ClassLoader classLoader = ClassLoaderUtils.getContextClassLoader();
          InputStream modelStream = classLoader.getResourceAsStream("models/" + modelPathStr);
          // Load the model from the stream:
          if (modelStream != null) {
            // If the model is found in the resources, load it from the stream
            logger.debug("Loading model from resources: " + modelPathStr);
            model.load(modelStream);
            // Load the model from the file system.
          } else {
            // Define a safe base directory for models, This prevents path traversal attacks.
            // This path will likely be used in the IDE
            Path baseDir =
                Paths.get("flfm-ij/src/main/resources/models").toAbsolutePath().normalize();
            Path resolvedModelPath = baseDir.resolve(modelName).normalize();
            if (!resolvedModelPath.startsWith(baseDir)) {
              logger.error("Potential path traversal attempt detected: " + modelPathStr);
              return null;
            }
            logger.debug("Loading model " + modelName + " from: " + modelPathStr);
            model.load(resolvedModelPath);
          }
        } catch (IOException | MalformedModelException e) {
          logger.debug("Error loading model: " + e.getMessage());
          logger.error("Stack trace: ");
          for (StackTraceElement ste : e.getStackTrace()) {
            logger.error(ste.toString());
          }
          return null;
        }

        try (Predictor<NDArray[], NDArray> predictor = model.newPredictor(new ModelTranslator())) {
          long start = System.currentTimeMillis();
          NDArray out;
          try {
            out = predictor.predict(new NDArray[] {inputArray, psfArray});
          } catch (TranslateException e) {
            logger.error("Error during prediction: " + e.getMessage());
            logger.error("Stack trace: ");
            for (StackTraceElement ste : e.getStackTrace()) {
              logger.error(ste.toString());
            }
            return null;
          }
          long end = System.currentTimeMillis();
          logger.debug("Prediction took " + (end - start) / 1000.0 + " s");
          outputImage = ArrayUtils.convertArrayToImage(out);
        }
      }
    }
    return outputImage;
  }
}
