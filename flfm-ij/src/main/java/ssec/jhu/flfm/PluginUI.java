package ssec.jhu.flfm;

import ij.gui.*;
import ij.plugin.frame.*;

public class PluginUI extends PlugInFrame {

  public PluginUI() {
    super("PluginUI");
  }

  public void run(String arg) {
    add(new SharedPanel());
    pack();
    GUI.center(this);
    setVisible(true);
  }
}
