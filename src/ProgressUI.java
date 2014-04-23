import Settings.OutputSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Created by daniel on 19.04.14.
 */
public class ProgressUI extends JFrame {
    JLabel currentTask = new JLabel("Nothing todo...");
    JProgressBar progressBar = new JProgressBar();
    JButton exit;
    JButton open;
    public ProgressUI()
    {
        this.setTitle("Diss-O-Tron: Fortschritt");
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());

        currentTask.setVerticalAlignment(SwingConstants.CENTER);
        currentTask.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.add(currentTask, BorderLayout.NORTH);
        progressBar.setBorder(new EmptyBorder(5, 5, 5, 5));
        progressBar.setIndeterminate(true);
        this.add(progressBar, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
        buttons.setAlignmentX(Component.RIGHT_ALIGNMENT);

        exit = new JButton("Beenden");
        exit.setAlignmentX(Component.RIGHT_ALIGNMENT);
        exit.setEnabled(false);
        buttons.add(exit);

        open = new JButton("Tabellen öffnen");
        open.setAlignmentX(Component.RIGHT_ALIGNMENT);
        open.setEnabled(false);
        buttons.add(open);

       // buttons.setBackground(Color.black);
        buttons.setPreferredSize(new Dimension(400, 50));
        this.add(buttons, BorderLayout.SOUTH);

        this.pack();
        this.setSize(new Dimension(400, this.getHeight()));
    }

    public void setTask(String task)
    {
        currentTask.setText(task);
    }

    public void initProgress(int max)
    {
        progressBar.setMinimum(0);
        progressBar.setMaximum(max);
        progressBar.setIndeterminate(false);
    }

    public void setProgress(int prog)
    {
        progressBar.setValue(prog);
    }

    public void setIndeterminate()
    {
        progressBar.setIndeterminate(true);
    }

    public void setFinished(final OutputSettings out)
    {
        progressBar.setIndeterminate(false);
        progressBar.setMaximum(100);
        progressBar.setValue(100);
        setTask("Fertig!");

        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                File f = new File(out.filename);
                try
                {
                    Desktop.getDesktop().open(f);
                }
                catch(Exception e)
                {
                    new ErrorUI(e);
                }
            }
        });
        open.setEnabled(true);

        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.exit(0);
            }
        });
        exit.setEnabled(true);
    }
}
