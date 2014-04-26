import Settings.InputSettings;
import Settings.OutputSettings;
import Settings.SettingsStore;
import Structure.ClassInfo;
import Structure.OntologyAnalyzer;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by daniel on 17.04.14.
 */
public class MainClass {

    public static void runGui()
    {
        final SettingsUI settings = new SettingsUI(new SettingsUI.SettingsActionHandler() {
            @Override
            public void onStart(SettingsUI dialog) {
                try
                {
                    final InputSettings input = dialog.getInputSettings();
                    final OutputSettings output = dialog.getOutputSettings();

                    dialog.setVisible(false);

                    final ProgressUI progress = new ProgressUI();
                    progress.setVisible(true);
                    progress.setTask("Initialisiere.");
                    OntologyAnalyzer analyzer = new OntologyAnalyzer(input);

                    progress.setTask("Analysiere" + analyzer.getNumNamedClasses() + " Klassen.");
                    System.out.println(analyzer.getNumNamedClasses());
                    progress.initProgress(analyzer.getNumNamedClasses());
                    progress.setProgress(0);
                    analyzer.analyze(input, new OntologyAnalyzer.AnalyzerEventHandler() {
                        @Override
                        public void onProgress(int current, int numClasses, String name) {
                            progress.setTask("Analysiere " + current + "/" + numClasses + ":\t" + name);
                            progress.setProgress(current);
                        }

                        @Override
                        public void onFinish(ArrayList<ClassInfo> list) {
                            progress.setIndeterminate();
                            progress.setTask("Schreibe Tabellen.");


                            try
                            {
                                ClassInfoRenderer r = new ClassInfoDocxRenderer();
                                r.render(output, list);

                                progress.setFinished(output);
                            }
                            catch(Exception e)
                            {
                                new ErrorUI(e);
                            }
                        }
                    });
                }
                catch(Exception e)
                {
                    new ErrorUI(e);
                }
            }
        });
        settings.setVisible(true);
    }

    public static void main(String argc[])
    {
        try
        {
            if(System.getProperty("os.name").startsWith("Mac"))
            {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Diss-O-Tron");
            }

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            runGui();
        }
        catch(Exception e)
        {
            new ErrorUI(e);
        }
    }
}
