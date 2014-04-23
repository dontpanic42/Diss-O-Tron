import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import Settings.InputSettings;
import Settings.OutputSettings;
import Settings.SettingsStore;
import sun.awt.VerticalBagLayout;

/**
 * Created by daniel on 19.04.14.
 */
public class SettingsUI extends JFrame {

    public interface SettingsActionHandler
    {
        public void onStart(SettingsUI dialog);
    }

    InputSettingsPanel input;
    OutputSettingsPanel output;
    SettingsActionHandler handler;

    public SettingsUI(SettingsActionHandler handler)
    {
        this.setTitle("Diss-O-Tron: Einstellungen");
        this.setLocationRelativeTo(null);
        this.handler = handler;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        //VerticalBagLayout vbl = new VerticalBagLayout();
        //this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        mainPanel.add((input = new InputSettingsPanel()));
        input.setAlignmentX(Component.RIGHT_ALIGNMENT);
        InputSettings is = new InputSettings();
        SettingsStore.getInstance().load(is);
        input.setFrom(is);

        mainPanel.add((output = new OutputSettingsPanel()));
        output.setAlignmentX(Component.RIGHT_ALIGNMENT);
        OutputSettings os = new OutputSettings();
        SettingsStore.getInstance().load(os);
        output.setFrom(os);

        this.add(mainPanel);

        JPanel buttons = new JPanel();
        buttons.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
        buttons.setAlignmentX(Component.RIGHT_ALIGNMENT);
        buttons.setPreferredSize(new Dimension(400, 30));

        JButton btnStart = new JButton("Start");
        btnStart.setAlignmentX(Component.RIGHT_ALIGNMENT);
        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onOk();
            }
        });
        buttons.add(btnStart);

        JButton btnExit = new JButton("Beenden");
        btnExit.setAlignmentX(Component.RIGHT_ALIGNMENT);
        btnExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.exit(0);
            }
        });
        buttons.add(btnExit);

        mainPanel.add(buttons);

        this.setPreferredSize(new Dimension(400, 400));
        this.pack();
    }

    private void onOk()
    {
        if(handler != null)
        {
            handler.onStart(this);
        }
    }

    public OutputSettings getOutputSettings()
    {
        OutputSettings os = output.getSettings();
        SettingsStore.getInstance().persist(os);
        return os;
    }

    public InputSettings getInputSettings()
    {
        InputSettings is = input.getSettings();
        SettingsStore.getInstance().persist(is);
        return is;
    }

    class OutputSettingsPanel extends JPanel
    {
        String[] styles = new String[]{
                "Heading1",
                "Heading2",
                "Heading3",
                "Heading4",
                "DefaultParagraphFont",
                "Header",
                "HeaderChar",
                "Heading1Char",
                "Heading2Char",
                "Heading3Char",
                "Heading4Char",
                "NormalIndent",
                "Subtitle",
                "SubtitleChar",
                "Title",
                "TitleChar",
                "Emphasis",
                "Hyperlink",
                "TableGrid",
                "TableNormal"
        };

        JTextField txtOuptPath = new JTextField();
        JButton btnOutputFile = new JButton("...");
        //JTextField txtHeaderStyle = new JTextField();
        //JTextField txtRowHeaderStyle = new JTextField();

        JComboBox cmbHeaderStyle = new JComboBox(styles);
        JComboBox cmbRowHeaderStyle = new JComboBox(styles);
        JFileChooser fileChooser = new JFileChooser();

        public OutputSettingsPanel()
        {
            this.setBorder(BorderFactory.createTitledBorder("Output"));

            GridBagLayout layout = new GridBagLayout();
            this.setLayout(layout);

            GridBagConstraints c = new GridBagConstraints();
            int y = 1;

            c.gridy = y++;
            c.gridx = 1;
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(5, 5, 5, 5);

            this.add(new JLabel("Dateiname"), c);

            c.gridx = 2;
            c.gridwidth = 2;
            txtOuptPath.setPreferredSize(new Dimension(200, 25));
            txtOuptPath.setEditable(false);
            this.add(txtOuptPath, c);

            c.gridx = 4;
            c.gridwidth = 1;
            this.add(btnOutputFile, c);

            btnOutputFile.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    onChooseFile();
                }
            });


            c.gridy = y++;
            c.gridx = 1;
            c.gridwidth = 1;
            this.add(new JLabel("Style TH"), c);

            c.gridx = 2;
            c.gridwidth = 2;
            cmbHeaderStyle.setPreferredSize(new Dimension(200, 25));
            this.add(cmbHeaderStyle, c);

            c.gridy = y++;
            c.gridx = 1;
            c.gridwidth = 1;
            this.add(new JLabel("Style RH"), c);

            c.gridx = 2;
            c.gridwidth = 2;
            cmbRowHeaderStyle.setPreferredSize(new Dimension(200, 25));
            this.add(cmbRowHeaderStyle, c);
        }

        private void onChooseFile()
        {
            if(fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                String path = fileChooser.getSelectedFile().getAbsolutePath();
                if(!path.endsWith(".docx"))
                {
                    path += ".docx";
                }

                txtOuptPath.setText(path);
            }
        }

        public void setFrom(OutputSettings settings)
        {
            txtOuptPath.setText(settings.filename);
            //txtHeaderStyle.setText(settings.headerStyle);
            cmbHeaderStyle.setSelectedIndex(settings.headerStyleIndex);
            //txtRowHeaderStyle.setText(settings.rowHeaderStyle);
            cmbRowHeaderStyle.setSelectedIndex(settings.rowHeaderStyleIndex);
        }

        public OutputSettings getSettings()
        {
            OutputSettings s = new OutputSettings();
            //s.headerStyle = txtHeaderStyle.getText();
            s.headerStyle = cmbHeaderStyle.getSelectedItem().toString();
            s.headerStyleIndex = cmbHeaderStyle.getSelectedIndex();
            //s.rowHeaderStyle = txtRowHeaderStyle.getText();
            s.rowHeaderStyle = cmbRowHeaderStyle.getSelectedItem().toString();
            s.rowHeaderStyleIndex = cmbRowHeaderStyle.getSelectedIndex();
            s.filename = txtOuptPath.getText();


            return s;
        }
    }

    class InputSettingsPanel extends JPanel
    {
        SpinnerNumberModel spModel = new SpinnerNumberModel();
        JTextField txtInputFile = new JTextField();
        JButton btnInputFile = new JButton("...");
        JComboBox cmbInputFormat = new JComboBox(new String[]{"RDF/XML"});
        JSpinner spMaxClasses = new JSpinner(spModel);

        JFileChooser fileChooser = new JFileChooser();

        public InputSettingsPanel()
        {
            this.setBorder(BorderFactory.createTitledBorder("Input"));

            GridBagLayout layout = new GridBagLayout();
            this.setLayout(layout);

            GridBagConstraints c = new GridBagConstraints();
            int y = 1;

            c.gridy = y++;
            c.gridx = 1;
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(5, 5, 5, 5);

            this.add(new JLabel("Ontologie"), c);

            c.gridx = 2;
            c.gridwidth = 2;
            txtInputFile.setPreferredSize(new Dimension(200, 25));
            txtInputFile.setEditable(false);
            this.add(txtInputFile, c);

            c.gridx = 4;
            c.gridwidth = 1;
            this.add(btnInputFile, c);

            btnInputFile.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    onChooseFile();
                }
            });

            c.gridy = y++;
            c.gridx = 1;
            this.add(new JLabel("Dateiformat"), c);

            c.gridx = 2;
            c.gridwidth = 2;
            cmbInputFormat.setPreferredSize(new Dimension(200, 25));
            this.add(cmbInputFormat, c);

            c.gridy = y++;
            c.gridx = 1;
            c.gridwidth = 1;
            this.add(new JLabel("Max. Klassen"), c);

            c.gridx = 2;
            c.gridwidth = 2;
            spModel.setMinimum(1);
            spModel.setStepSize(1);
            spMaxClasses.setPreferredSize(new Dimension(200, 25));
            this.add(spMaxClasses, c);
        }

        private void onChooseFile()
        {
            if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                txtInputFile.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        }

        public void setFrom(InputSettings settings)
        {
            txtInputFile.setText(settings.ontFilename);
            cmbInputFormat.setSelectedIndex(settings.ontFiletypeIndex);
            spModel.setValue(settings.maxClasses);
            spMaxClasses.setValue(settings.maxClasses);
        }

        public InputSettings getSettings()
        {
            InputSettings s = new InputSettings();
            s.ontFilename = txtInputFile.getText();
            s.ontFiletype = cmbInputFormat.getSelectedItem().toString();
            s.ontFiletypeIndex = cmbInputFormat.getSelectedIndex();
            s.maxClasses = Integer.valueOf((Integer) spMaxClasses.getValue());

            return s;
        }
    }
}
