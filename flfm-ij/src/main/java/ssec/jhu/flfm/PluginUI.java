package ssec.jhu.flfm;

import javax.swing.SwingUtilities;

import ij.gui.*;
import ij.plugin.frame.*;

public class PluginUI extends PlugInFrame {

  public PluginUI() {
    super("PluginUI");
  }

  public PluginUI(String title) {
    super(title);
  }

  public void run(String arg) {
    add(new SharedPanel());
    pack();
    GUI.center(this);
    setVisible(true);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(
      () -> {
        PluginUI ui = new PluginUI("Standalone UI");
        ui.pack();
        ui.setVisible(true);
        ui.setLocationRelativeTo(null);
    });
  }
}
