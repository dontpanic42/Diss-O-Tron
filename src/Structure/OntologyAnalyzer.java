package Structure;

import Settings.InputSettings;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerFactory;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.util.ArrayList;
import java.util.List;
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
        public void onFinish(ArrayList<ClassInfo> classes, ArrayList<InstanceInfo> inst);
    }

    private OntModel model;
    private ArrayList<OntClass> classes = null;
    private ArrayList<Individual> individuals = null;
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

    private ArrayList<Individual> getNamedIndividuals()
    {
        if(this.individuals != null)
        {
            return this.individuals;
        }

        ArrayList<Individual> list = new ArrayList<Individual>();
        int counter = 0;
        Individual i;
        OntResource r;
        for(OntClass o : model.listHierarchyRootClasses().toList())
        {
            ExtendedIterator<? extends OntResource> it = o.listInstances();
            while(it.hasNext())
            {
                r = it.next();
                if(!r.canAs(Individual.class))
                {
                    continue;
                }

                i = r.as(Individual.class);
                if(!i.isAnon() && i.getLocalName() != null)
                {
                    if(counter++ >= settings.maxClasses)
                    {
                        System.err.println("Reached maximum individual count: " + settings.maxClasses + " - Ignoring rest.");
                        break;
                    }

                    System.out.println("Scanned individual " + counter);

                    list.add(i);
                }
            }
        }

        this.individuals = list;
        return list;
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

        Analyzer a = new Analyzer(getNamedClasses(), getNamedIndividuals(), handler);
        new Thread(a).start();

    }



    class Analyzer implements Runnable
    {
        AnalyzerEventHandler handler;
        ArrayList<OntClass> classes;
        ArrayList<Individual> individuals;
        ClassAnalyzer ca = new ClassAnalyzer();
        IndividualAnalyzer ia = new IndividualAnalyzer();

        public Analyzer(ArrayList<OntClass> classes, ArrayList<Individual> individuals, AnalyzerEventHandler handler)
        {
            this.handler = handler;
            this.classes = classes;
            this.individuals = individuals;
        }

        public void run()
        {
            int counter = 0;
            int max = classes.size() + individuals.size();
            ArrayList<ClassInfo> info = new ArrayList<ClassInfo>();
            ClassInfo classInfo;
            for(OntClass o : classes)
            {
                //if(!o.getLocalName().equals("Kraftfahrer"))
                //    continue;

                //System.out.println("Reading " + (++counter) + " von " + max);
                classInfo = ca.analyze(o);//new ClassInfo(o);
                info.add(classInfo);

                handler.onProgress(++counter, max, "Klasse " + classInfo.getName());
            }

            ArrayList<InstanceInfo> indv = new ArrayList<InstanceInfo>();
            InstanceInfo instanceInfo;
            for(Individual i : individuals)
            {
                instanceInfo = ia.analyze(i);
                indv.add(instanceInfo);

                handler.onProgress(++counter, max, "Individual " + instanceInfo.getName());
            }

            handler.onFinish(info, indv);
        }
    }
}
