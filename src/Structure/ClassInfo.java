package Structure;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 17.04.14.
 */
public class ClassInfo {


    public static final String ROOT_CLASS_NAME = "Thing";
    public static final String IS_A_RELATION_NAME = "istEin";
    public static final String INTERSECTION = "\u2229";
    public static final String UNION = "\u222A";
    public static final String ELEMENT_OF = "\u2208";
    public static final String NOT = "\u00AC";

    public String name;
    public ArrayList<Relation> relationen = new ArrayList<Relation>();
    public ArrayList<Relation> attribute = new ArrayList<Relation>();
    public String equivalentTo = null;

    public ClassInfo()
    {

    }

    public boolean hasProperty(OntProperty prop)
    {
        String name = prop.getLocalName();
        if(prop.isObjectProperty())
        {
            for(Relation r : relationen)
            {
                if(r.name.equals(name))
                    return true;
            }
        }
        else if(prop.isDatatypeProperty())
        {
            for(Relation r : attribute)
            {
                if(r.name.equals(name))
                    return true;
            }
        }

        return false;
    }

    public boolean hasEquivalentClass()
    {
        return (equivalentTo != null);
    }

    public String getEquivalentClass()
    {
        return equivalentTo;
    }

    public String getName()
    {
        return name;
    }

    public ArrayList<Relation> getRelations()
    {
        return relationen;
    }

    public ArrayList<Relation> getAttributs()
    {
        return attribute;
    }

}
