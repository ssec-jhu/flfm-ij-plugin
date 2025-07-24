package ssec.jhu.flfm;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class StandaloneUI extends JFrame {
  public StandaloneUI() {
    setTitle(Constants.TITLE_STANDALONE_UI);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setContentPane(new SharedPanel());
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(
        () -> {
          StandaloneUI ui = new StandaloneUI();
          ui.pack();
          ui.setVisible(true);
          ui.setLocationRelativeTo(null);
        });
  }
}
