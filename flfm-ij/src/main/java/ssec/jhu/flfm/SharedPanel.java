package ssec.jhu.flfm;

import ij.ImagePlus;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ClassLoader.getResourceAsStream("models/model.onnx")

public class SharedPanel extends JPanel {

  private static final Logger logger = LoggerFactory.getLogger(SharedPanel.class);

  private JButton getImageButton;
  private JButton getPSFButton;
  private JTextField psfTextField;
  private JTextField imageTextField;
  private ij.ImagePlus psfImage;
  private ij.ImagePlus inputImage;
  private JComboBox<String> iterationJComboBox;
  private JComboBox<String> deviceComboBox;
  private JButton calculateButton;
  private String[] modelPaths;
  private DeviceInfo[] deviceInfos;

  public SharedPanel() {
    initComponents();
  }

  private void initComponents() {
    // Initialize components here
    // This method should be overridden in subclasses to set up the UI
    setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    int row = 0;

    logger.debug("Initializing PSF Button");
    // Row 1 - PSF Button and Text Field ===================================
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 2;
    getPSFButton = new JButton(Constants.BTN_PSF);
    // Add action listener to the PSF button to open the file in psfTextField
    getPSFButton.addActionListener(
        e -> {
          psfImage = UtilsUI.getImage(this, Constants.LBL_SELECT_PSF);

          psfTextField.setText(
              Optional.ofNullable(psfImage).map(ImagePlus::getTitle).orElse(Constants.LBL_NO_PSF));
        });
    this.add(getPSFButton, gbc);

    logger.debug("Initializing PSF Text Field");
    gbc.gridx = 2;
    gbc.gridy = row;
    gbc.gridwidth = 4;
    psfTextField = new JTextField(Constants.LBL_NO_PSF);
    psfTextField.setEditable(false); // Make the text field non-editable
    this.add(psfTextField, gbc);
    // =====================================================================

    row++; // Move to the next row
    logger.debug("Initializing Image Button");
    // Row 2 - Image Button and Text Field =================================
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 2;
    getImageButton = new JButton(Constants.BTN_INPUT);
    // Add action listener to the Input button to open the file in imageTextField
    getImageButton.addActionListener(
        e -> {
          inputImage = UtilsUI.getImage(this, Constants.LBL_SELECT_INPUT);

          imageTextField.setText(
              Optional.ofNullable(inputImage)
                  .map(ImagePlus::getTitle)
                  .orElse(Constants.LBL_NO_INPUT));
        });
    this.add(getImageButton, gbc);

    logger.debug("Initializing Image Text Field");
    gbc.gridx = 2;
    gbc.gridy = row;
    gbc.gridwidth = 4;
    imageTextField = new JTextField(Constants.LBL_NO_INPUT);
    imageTextField.setEditable(false); // Make the text field non-editable
    this.add(imageTextField, gbc);
    // =====================================================================

    // Row 3 - Iteration and Calculate Button ==============================
    row++; // Move to the next row
    logger.debug("Initializing Iteration Label");
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 1; // Reset grid width to 1 for the label
    this.add(new JLabel(Constants.LBL_ITERATIONS), gbc);

    gbc.gridx = 1;
    gbc.gridy = row;

    logger.debug("Getting model locations");
    modelPaths = Optional.of(UtilsUI.getModelLocations()).orElse(new String[] {});

    // extract the valid choices for iterations from the model paths
    String[] validChoices =
        java.util.Arrays.stream(modelPaths)
            .map(path -> path.replaceAll("[^0-9]", ""))
            .filter(path -> !path.isEmpty())
            .distinct()
            .map(Integer::parseInt)
            .sorted()
            .map(String::valueOf)
            .toArray(String[]::new);

    logger.debug("Valid choices for iterations: {}", (Object) validChoices);
    logger.debug("Initializing Iteration JComboBox");
    iterationJComboBox = new JComboBox<String>(validChoices);
    iterationJComboBox.setSelectedIndex(0); // Set default selection to the first item
    this.add(iterationJComboBox, gbc);

    gbc.gridx = 2;

    logger.debug("Getting available devices");
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(SharedPanel.class.getClassLoader());
      deviceInfos = Algorithm.getDevices(); // Get available devices
    } finally {
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    }
    // Create a JComboBox for device selection
    logger.debug("Initializing Device JComboBox");
    deviceComboBox =
        new JComboBox<String>(
            java.util.Arrays.stream(deviceInfos).map(DeviceInfo::toDisplay).toArray(String[]::new));
    deviceComboBox.setSelectedIndex(0); // Set default selection to the first item
    this.add(deviceComboBox, gbc);

    logger.debug("Initializing Calculate Button");
    gbc.gridx = 3;
    gbc.gridwidth = 3;
    gbc.gridy = row;
    calculateButton = new JButton(Constants.BTN_CALCULATE);
    calculateButton.addActionListener(
        e -> {
          // Get the selected model name
          logger.debug("Getting selected model name");
          String modelName = modelPaths[iterationJComboBox.getSelectedIndex()];
          logger.debug("Selected model name: {}", modelName);

          calculateButton.setEnabled(false);
          calculateButton.setText("Working...");

          SwingWorker<ImagePlus, Void> worker =
              new SwingWorker<ImagePlus, Void>() {

                @Override
                protected ImagePlus doInBackground() throws Exception {
                  // Run the algorithm in the background and return the result
                  logger.debug("Running model: {}", modelName);
                  ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
                  ImagePlus processedImage = null;
                  try {
                    Thread.currentThread()
                        .setContextClassLoader(SharedPanel.class.getClassLoader());
                    processedImage =
                        Algorithm.runModel(
                            modelName,
                            deviceInfos[deviceComboBox.getSelectedIndex()], // Get selected device
                            psfImage,
                            inputImage);
                  } finally {
                    Thread.currentThread().setContextClassLoader(originalClassLoader);
                  }
                  logger.debug("Model run completed, returning processed image.");
                  return processedImage;
                }

                @Override
                protected void done() {
                  ImagePlus result = null;
                  logger.debug("Model run completed, processing result.");
                  try {
                    result = get(); // Retrieve the result from doInBackground
                  } catch (Exception ex) {
                    logger.error("Error occurred while retrieving result: {}", ex.getMessage());
                  }
                  calculateButton.setEnabled(true); // Re-enable the button after calculation
                  calculateButton.setText(Constants.BTN_CALCULATE); // Reset button text
                  if (result != null) {
                    result.show(); // Show the result image
                  }
                }
              };

          worker.execute();
        });

    this.add(calculateButton, gbc);
    // =====================================================================
  }
}
