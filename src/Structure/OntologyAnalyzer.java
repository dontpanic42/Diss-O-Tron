package Structure;

import Settings.InputSettings;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by daniel on 19.04.14.
 */
public class OntologyAnalyzer {

    public interface AnalyzerEventHandler
    {
        public void onProgress(int current, int numClasses, String name);
        public void onFinish(ArrayList<ClassInfo> list);
    }

    private OntModel model;
    private ArrayList<OntClass> classes = null;
    private InputSettings settings;

    public OntologyAnalyzer(InputSettings settings)
    {
        OntDocumentManager dm = OntDocumentManager.getInstance();
        OntModelSpec modelSpec = OntModelSpec.OWL_MEM_MICRO_RULE_INF; //OntModelSpec.OWL_MEM;
        modelSpec.setDocumentManager(dm);
        OntModel ontModel = ModelFactory.createOntologyModel();
        ontModel.read(FileManager.get().open(settings.ontFilename), settings.ontFiletype);
        this.settings = settings;
        this.model = ontModel;
    }

    private ArrayList<OntClass> getNamedClasses()
    {
        if(this.classes != null)
        {
            return this.classes;
        }

        ArrayList<OntClass> list = new ArrayList<OntClass>();
        int counter = 0;
        for(OntClass o : model.listClasses().toList())
        {
            if(!o.isAnon() && o.getLocalName() != null)
            {
                //if(!o.getLocalName().equals("Projekttyp")) continue;

                if(counter++ >= settings.maxClasses)
                {
                    System.err.println("Reached maximum class count: " + settings.maxClasses + " - Ignoring rest.");
                    break;
                }

                list.add(o);
            }
        }

        this.classes = list;
        return list;
    }

    public int getNumNamedClasses()
    {
        return getNamedClasses().size();
    }

    public void analyze(InputSettings ips, AnalyzerEventHandler handler)
    {

        Analyzer a = new Analyzer(getNamedClasses(), handler);
        new Thread(a).start();

    }



    class Analyzer implements Runnable
    {
        AnalyzerEventHandler handler;
        ArrayList<OntClass> classes;

        public Analyzer(ArrayList<OntClass> classes, AnalyzerEventHandler handler)
        {
            this.handler = handler;
            this.classes = classes;
        }

        public void run()
        {
            int counter = 0;
            int max = classes.size();
            ArrayList<ClassInfo> info = new ArrayList<ClassInfo>();
            ClassInfo classInfo;
            for(OntClass o : classes)
            {
                //System.out.println("Reading " + (++counter) + " von " + max);
                classInfo = new ClassInfo(o);
                info.add(classInfo);

                handler.onProgress(++counter, max, classInfo.getName());
            }

            handler.onFinish(info);
        }
    }
}
