package ssec.jhu.flfm;

import ij.ImagePlus;
import ij.gui.GUI;
import ij.plugin.frame.PlugInFrame;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginView extends PlugInFrame {

  private static final Logger logger = LoggerFactory.getLogger(PluginView.class);
  private static final String[] placeholder = new String[] {"    "}; // Choices

  private PluginController pluginController;

  // UI components
  protected Button btnPsf;
  protected Button btnInput;
  protected Button btnCalculate;
  protected Choice iterationChoice;
  protected Choice deviceChoice;
  protected TextField textFieldPsf;
  protected TextField textFieldInput;

  public PluginView() {
    this("PluginUI");
  }

  public PluginView(String title) {
    super(title);
    this.pluginController = new PluginController(this);
  }

  public void run(String arg) {
    this.initComponents();
    this.pack();
    GUI.center(this);
    this.pluginController.postInit();
    this.setVisible(true);
  }

  public static void main(String[] args) {
    PluginView ui = new PluginView("Standalone UI");
    ui.initComponents();
    ui.pack();
    ui.pluginController.postInit();
    ui.setVisible(true);
    ui.setLocationRelativeTo(null);
  }

  // Make initComponents method public for testing
  public void initComponents() {
    logger.debug("Initializing PluginUI components");
    this.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    int row = 0;

    logger.debug("Initializing PSF Button");
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 2;
    this.btnPsf = addButton(this, Constants.BTN_PSF, gbc, this.pluginController);

    logger.debug("Initializing PSF Text Field");
    gbc.gridx = 2;
    gbc.gridy = row;
    gbc.gridwidth = 4;
    this.textFieldPsf = addTextField(this, Constants.LBL_NO_PSF, gbc, false);

    row++;
    logger.debug("Initializing Input Button");
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 2;
    this.btnInput = addButton(this, Constants.BTN_INPUT, gbc, this.pluginController);

    logger.debug("Initializing Input Text Field");
    gbc.gridx = 2;
    gbc.gridy = row;
    gbc.gridwidth = 4;
    this.textFieldInput = addTextField(this, Constants.LBL_NO_INPUT, gbc, false);

    row++;
    logger.debug("Initializing Model Choice");
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 1;
    this.iterationChoice = addChoice(this, PluginView.placeholder, gbc);

    logger.debug("Initializing Device Choice");
    gbc.gridx = 1;
    gbc.gridy = row;
    gbc.gridwidth = 1;
    this.deviceChoice = addChoice(this, PluginView.placeholder, gbc);

    logger.debug("Initializing Calculate Button");
    gbc.gridx = 2;
    gbc.gridy = row;
    gbc.gridwidth = 4;
    this.btnCalculate = addButton(this, Constants.BTN_CALCULATE, gbc, this.pluginController);

    logger.debug("Finished initializing UI components");
  }

  protected static Label addLabel(Frame frame, String label, GridBagConstraints gbc) {
    Label lbl = new Label(label);
    frame.add(lbl, gbc);
    return lbl;
  }

  protected static Button addButton(
      Frame frame, String label, GridBagConstraints gbc, ActionListener actionListener) {
    Button button = new Button(label);
    if (actionListener != null) {
      button.addActionListener(actionListener);
    }
    frame.add(button, gbc);
    return button;
  }

  protected static TextField addTextField(
      Frame frame, String label, GridBagConstraints gbc, boolean isEditable) {
    TextField textField = new TextField(label);
    textField.setEditable(isEditable);
    frame.add(textField, gbc);
    return textField;
  }

  protected static Choice addChoice(Frame frame, String[] items, GridBagConstraints gbc) {
    Choice choice = new Choice();
    for (String item : items) {
      choice.add(item);
    }
    frame.add(choice, gbc);
    return choice;
  }

  public void setPsfTextField(String text) {
    this.textFieldPsf.setText(text);
  }

  public void setInputTextField(String text) {
    this.textFieldInput.setText(text);
  }

  public void setIterations(String[] iterations) {
    this.iterationChoice.removeAll();
    for (String iteration : iterations) {
      this.iterationChoice.add(iteration);
    }

    this.repaint();
  }

  public void setDevices(String[] devices) {
    this.deviceChoice.removeAll();
    for (String device : devices) {
      this.deviceChoice.add(device);
    }

    this.repaint();
  }

  public int getSelectedIterationIndex() {
    return this.iterationChoice.getSelectedIndex();
  }

  public int getSelectedDeviceIndex() {
    return this.deviceChoice.getSelectedIndex();
  }

  public void startProcessImage() {
    this.btnCalculate.setLabel(Constants.BTN_WORKING);
    this.btnCalculate.setEnabled(false);
    this.repaint();
  }

  public void endProcessedImage(ImagePlus processedImage) {
    if (processedImage == null) {
      logger.error("Processed image is null");
    } else {
      logger.debug("Displaying processed image: {}", processedImage.getTitle());
      processedImage.show();
    }
    this.btnCalculate.setLabel(Constants.BTN_CALCULATE);
    this.btnCalculate.setEnabled(true);
    this.repaint();
  }
}
