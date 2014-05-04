package Structure;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import java.util.ArrayList;

/**
 * Created by daniel on 04.05.14.
 */
public class IndividualAnalyzer {

    public InstanceInfo analyze(Individual i)
    {
        InstanceInfo ci = new InstanceInfo();


        ci.name = getSimpleClassname(i);

        OntClass ofClass = i.getOntClass();
        ci.parentClass = getSimpleClassname(ofClass);

        addProperties(ci, i);

        return ci;
    }

    private void addProperties(InstanceInfo info, Individual i)
    {
        ArrayList<Attribut> dp = getDataProperties(i);
        ArrayList<Relation> op = getObjectProperties(i);

        for(Attribut a : dp)
        {
            info.attribute.add(a);
        }

        for(Relation r : op)
        {
            info.relationen.add(r);
        }
    }

    ArrayList<Attribut> getDataProperties(Individual i)
    {
        ArrayList<Attribut> list = new ArrayList<Attribut>();
        for(Statement s : i.listProperties().toList())
        {
            if(s.getObject().isLiteral())
            {
                Attribut r = new Attribut();
                r.name = s.getPredicate().getLocalName();
                r.restrictionValue = s.getLiteral().getLexicalForm();

                list.add(r);
            }
        }

        return list;
    }

    ArrayList<Relation> getObjectProperties(Individual i)
    {
        ArrayList<Relation> list = new ArrayList<Relation>();
        for(Statement s : i.listProperties().toList())
        {
            if(s.getObject().canAs(Individual.class))
            {
                String predicat = s.getPredicate().getLocalName();
                if(predicat.equals("type"))
                    continue;

                Relation r = new Relation();
                r.name = predicat;
                r.restrictionValue = s.getObject().as(Individual.class).getLocalName();

                list.add(r);
            }
        }

        return list;
    }

    //Resolves class name without side effects.
    public String getSimpleClassname(OntResource res)
    {
        if(!res.isAnon())
        {
            return res.getLocalName();
        }
        else
        {
            if(res.isClass() && res.canAs(ComplementClass.class))
            {
                ComplementClass comp = res.asClass().asComplementClass();
                return String.format(ClassInfo.NOT + "(%s)", getSimpleClassname(comp.getOperand()));
            }
            return "(Anonym)";
        }
    }
}
