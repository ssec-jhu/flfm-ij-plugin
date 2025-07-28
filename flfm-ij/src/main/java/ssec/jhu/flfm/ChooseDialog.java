package ssec.jhu.flfm;

import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;

public class ChooseDialog {

  public static String showInput(Frame parent){

    Dialog dialog = new Dialog(parent, Constants.LBL_SELECT_IMG);
    dialog.setLayout(new FlowLayout());
    dialog.setSize(300, 150);
    dialog.setLocationRelativeTo(parent);

    return "";
  }
    

}
