import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by daniel on 20.04.14.
 */
public class ErrorUI extends JFrame {
    public ErrorUI(Exception e)
    {
        this.setLocationRelativeTo(null);
        this.setTitle("Diss-O-Tron: Fehler");
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        JLabel jla = new JLabel("Ein Fehler ist aufgetreten:");
        jla.setMaximumSize(new Dimension(400, 30));
        jla.setAlignmentX(Component.LEFT_ALIGNMENT);
        jla.setBorder(new EmptyBorder(5, 5, 5, 5));
        main.add(jla);
        JLabel jlb = new JLabel(e.getClass().getSimpleName());
        jlb.setMaximumSize(new Dimension(400, 30));
        jlb.setAlignmentX(Component.LEFT_ALIGNMENT);
        jlb.setBorder(new EmptyBorder(5, 5, 5, 5));
        main.add(jlb);

        JTextArea text = new JTextArea();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        text.setText(sw.toString());

        JScrollPane sp = new JScrollPane(text);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setBorder(new EmptyBorder(5, 5, 5, 5));
        main.add(sp);

        JButton btnExit = new JButton("Beenden");
        btnExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.exit(-1);
            }
        });
        btnExit.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(btnExit);

        main.setBorder(new EmptyBorder(5, 5, 5, 5));

        this.add(main);
        this.pack();
        this.setSize(new Dimension(500, 600));

        this.setVisible(true);
    }
}
