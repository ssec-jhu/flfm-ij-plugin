package ssec.jhu.flfm;

import ij.ImagePlus;
import java.awt.Button;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginController implements ActionListener {

  private static final Logger logger = LoggerFactory.getLogger(PluginController.class);

  private final ExecutorService executorService;
  private final PluginView pluginView;
  private ImagePlus psfImage;
  private ImagePlus inputImage;
  private DeviceInfo[] deviceInfos;
  private String[] modelLocations;

  public PluginController(PluginView pluginView) {
    this.pluginView = pluginView;
    this.executorService = Executors.newCachedThreadPool();
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    Button source = (Button) e.getSource();
    String command = source.getLabel();
    logger.debug("Action performed: {}", command);

    switch (command) {
      case Constants.BTN_PSF:
        psfImage = getImage(pluginView, Constants.LBL_SELECT_PSF);
        pluginView.setPsfTextField(psfImage != null ? psfImage.getTitle() : Constants.LBL_NO_PSF);
        break;
      case Constants.BTN_INPUT:
        inputImage = getImage(pluginView, Constants.LBL_SELECT_INPUT);
        pluginView.setInputTextField(
            inputImage != null ? inputImage.getTitle() : Constants.LBL_NO_INPUT);
        break;
      case Constants.BTN_CALCULATE:
        if (psfImage == null || inputImage == null) {
          logger.error("PSF or Input image is not set");
          return;
        }
        runModel();
        break;
      default:
        logger.warn("Unknown action command: {}", command);
    }
  }

  public void postInit() {
    processModelLocations();
    processAvailableDevices();
  }

  /// Async Methods ==================================================
  public void processAvailableDevices() {
    executorService.submit(
        () -> {
          ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
          try {
            Thread.currentThread().setContextClassLoader(PluginController.class.getClassLoader());
            this.deviceInfos = Algorithm.getDevices();
          } catch (Exception e) {
            logger.error("Error occurred while running model: {}", e.getMessage());
            e.printStackTrace();
            this.deviceInfos = new DeviceInfo[] {};
          } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
          }
          if (this.deviceInfos == null || this.deviceInfos.length == 0) {
            logger.error("No devices found, error in engine.");
          }
          logger.debug("Found {} devices", this.deviceInfos.length);
          String[] deviceDisplays =
              java.util.Arrays.stream(this.deviceInfos)
                  .map(DeviceInfo::toDisplay)
                  .toArray(String[]::new);
          EventQueue.invokeLater(() -> pluginView.setDevices(deviceDisplays));
        });
  }

  public void processModelLocations() {
    executorService.submit(
        () -> {
          this.modelLocations = getModelLocations();
          if (this.modelLocations == null || this.modelLocations.length == 0) {
            this.modelLocations = new String[] {}; // Default to an empty array if no models found
          }

          Arrays.sort(
              this.modelLocations,
              (path1, path2) -> {
                // Sort by iteration number extracted from the file name
                int iter1 = Integer.parseInt(path1.replaceAll("[^0-9]", ""));
                int iter2 = Integer.parseInt(path2.replaceAll("[^0-9]", ""));
                return Integer.compare(iter1, iter2);
              });

          logger.debug("Found {} model locations", this.modelLocations.length);

          if (this.modelLocations != null && this.modelLocations.length > 0) {
            String[] iterations =
                java.util.Arrays.stream(this.modelLocations)
                    .map(path -> path.replaceAll("[^0-9]", ""))
                    .filter(path -> !path.isEmpty())
                    .distinct()
                    .map(String::valueOf)
                    .toArray(String[]::new);

            EventQueue.invokeLater(() -> pluginView.setIterations(iterations));
          } else {
            logger.warn("No model locations found.");
          }
        });
  }

  public void runModel() {
    String selectedModel = modelLocations[pluginView.getSelectedIterationIndex()];
    DeviceInfo selectedDevice = deviceInfos[pluginView.getSelectedDeviceIndex()];
    logger.debug("Selected model: {}, Selected device: {}", selectedModel, selectedDevice);
    this.pluginView.startProcessImage();
    executorService.submit(
        () -> {
          if (psfImage == null || inputImage == null) {
            logger.error("PSF or Input image is not set");
            return;
          }
          final ImagePlus[] processedImage = new ImagePlus[1];

          ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
          try {
            Thread.currentThread().setContextClassLoader(PluginController.class.getClassLoader());
            processedImage[0] =
                Algorithm.runModel(selectedModel, selectedDevice, this.psfImage, this.inputImage);
          } catch (Exception e) {
            logger.error("Error occurred while running model: {}", e.getMessage());
            e.printStackTrace();
          } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
          }

          EventQueue.invokeLater(() -> pluginView.endProcessedImage(processedImage[0]));
        });
  }
  // ===========================================================================

  // Image Retrieval Methods ===================================================

  public static ImagePlus getImage(Component parent, String title) {
    return Optional.ofNullable(getImageFromOpenWindows(parent))
        .orElseGet(() -> getImageFromFile(parent, title));
  }

  public static ImagePlus getImageFromFile(Component parent, String title) {
    logger.debug("Opening file dialog to select image");
    FileDialog fileDialog = new FileDialog((java.awt.Frame) parent, title, FileDialog.LOAD);
    fileDialog.setFile("*.tif;*.tiff");
    fileDialog.setVisible(true);

    logger.debug("Parsing selected file from dialog");
    String filePath = fileDialog.getFile();
    logger.debug("Selected file path: {}", filePath);
    if (filePath != null) {
      String directory = fileDialog.getDirectory();
      File selectedFile = new File(directory, filePath);
      return new ImagePlus(selectedFile.getAbsolutePath());
    }
    return null;
  }

  public static ImagePlus getImageFromOpenWindows(Component parent) {
    String[] windowTitles = ij.WindowManager.getImageTitles();
    if (windowTitles.length == 0) {
      logger.debug("No ImageJ windows found going to disk instead.");
      return null;
    }
    String selectedTitle =
        (String)
            javax.swing.JOptionPane.showInputDialog(
                parent,
                Constants.LBL_SELECT_IMG,
                "Open Images",
                javax.swing.JOptionPane.PLAIN_MESSAGE,
                null,
                windowTitles,
                windowTitles[0]);
    if (selectedTitle != null) {
      return ij.WindowManager.getImage(selectedTitle);
    }
    return null;
  }
  // ===========================================================================

  // Model Retrieval Methods ===================================================

  public static String[] getModelLocations() {
    return Optional.ofNullable(getModelLocationsFromJar())
        .orElseGet(() -> getModelLocationsFromFile());
  }

  public static String[] getModelLocationsFromJar() {
    URL url = PluginController.class.getClassLoader().getResource("models");
    String[] result = null;

    if (url != null && url.getProtocol().equals("jar")) {
      String jarPath = url.getPath().substring(5, url.getPath().indexOf("!"));

      try {
        try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
          Enumeration<JarEntry> entries = jar.entries();
          result =
              java.util.Collections.list(entries).stream()
                  .filter(
                      entry ->
                          !entry.isDirectory()
                              && entry.getName().startsWith("models/")
                              && entry.getName().endsWith(".pt"))
                  .map(entry -> entry.getName().substring("models/".length()))
                  .sorted()
                  .toArray(String[]::new);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return result;
  }

  public static String[] getModelLocationsFromFile() {
    Path modelsPath = java.nio.file.Paths.get("flfm-ij", "src", "main", "resources", "models");

    String[] modelPaths = null;
    try (DirectoryStream<Path> stream =
        java.nio.file.Files.newDirectoryStream(modelsPath, "*.pt")) {
      modelPaths =
          java.util.stream.StreamSupport.stream(stream.spliterator(), false)
              .map(Path::toString)
              .sorted()
              .toArray(String[]::new);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return modelPaths;
  }
  // ===========================================================================
}
