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
    public enum RestrictionType
    {
        ALL,
        SOME,
        MIN,
        MAX,
        EXACT,
        LIT_VALUE,
        NONE
    }

    public static final String ROOT_CLASS_NAME = "Thing";
    public static final String IS_A_RELATION_NAME = "istEin";
    public static final String INTERSECTION = "\u2229";
    public static final String UNION = "\u222A";

    private String name;
    private ArrayList<Relation> relationen = new ArrayList<Relation>();
    private ArrayList<Relation> attribute = new ArrayList<Relation>();
    private String equivalentTo = null;

    public ClassInfo(OntClass oclass)
    {
        //OntClass sclass = oclass.getSuperClass();
        name = oclass.getLocalName();

        addSuperclassRelation(oclass);
        addProperties(oclass);
    }

    private void addSuperclassRelation(OntClass oclass)
    {
        ArrayList<String> list = new ArrayList<String>();
        //If the class is an equivalent-To class or subclass of "Thing"
        if(oclass.isHierarchyRoot())
        {
            List<OntClass> equis = oclass.listEquivalentClasses().toList();
            if(equis.size() > 0)
            {
                addSuperClass(equis.get(0), list);
                equivalentTo = getClassName(equis.get(0));
                relationen.add(new Relation(IS_A_RELATION_NAME, list));
                return;
            }
        }
        else
        {
            ExtendedIterator<OntClass> it = oclass.listSuperClasses(true);
            OntClass sclass;
            while(it.hasNext())
            {
                sclass = it.next();
                addSuperClass(sclass, list);
            }
        }

        if(list.size() == 0)
        {
            list.add(ROOT_CLASS_NAME);
        }

        relationen.add(new Relation(IS_A_RELATION_NAME, list));
    }

    public boolean hasEquivalentClass()
    {
        return (equivalentTo != null);
    }

    public String getEquivalentClass()
    {
        return equivalentTo;
    }

    private void addSuperClass(OntClass sclass, ArrayList<String> list)
    {
        if(sclass != null)
        {
            if(sclass.isAnon())
            {
                if(sclass.isIntersectionClass())
                {
                    IntersectionClass s = sclass.asIntersectionClass();
                    for(OntClass o : s.listOperands().toList())
                    {
                        if(o.isRestriction())
                        {
                            addRestriction(o.asRestriction());
                        }
                        else
                        {
                            System.out.println(getClassName(o));
                            list.add(getClassName(o));
                        }
                    }
                }
                else if(sclass.isUnionClass())
                {
                    UnionClass u = sclass.asUnionClass();
                    list.add(getUnionString(u));
                }
            }
            else
            {
                String className = getClassName(sclass);
                System.out.println("Class name: " + className);
                list.add(getClassName(sclass));
            }

        }
    }

    private void addRestriction(Restriction r)
    {
        Relation rel = null;
        if(r.getOnProperty().isDatatypeProperty())
        {
            System.out.println("Data Property Restriction: " + r.getOnProperty().getLocalName());
            rel = addDataProperty(r.getOnProperty());
        }
        else if(r.getOnProperty().isObjectProperty())
        {
            rel = addObjectProperty(r.getOnProperty());
        }

        if(rel != null)
        {
            if(r.isAllValuesFromRestriction())
            {
                rel.restrictionType = RestrictionType.ALL;
            }
            else if(r.isSomeValuesFromRestriction())
            {
                rel.restrictionType = RestrictionType.SOME;
            }
            else if(r.isMaxCardinalityRestriction())
            {
                rel.restrictionType = RestrictionType.MAX;
                rel.cardinality = r.asMaxCardinalityRestriction().getMaxCardinality();
            }
            else if(r.isMinCardinalityRestriction())
            {
                rel.restrictionType = RestrictionType.MIN;
                rel.cardinality = r.asMinCardinalityRestriction().getMinCardinality();
            }
            else if(r.isCardinalityRestriction())
            {
                rel.restrictionType = RestrictionType.EXACT;
                rel.cardinality = r.asCardinalityRestriction().getCardinality();
            }
            else if(r.isHasValueRestriction())
            {
                RDFNode n = r.asHasValueRestriction().getHasValue();
                if(n.isLiteral())
                {
                    rel.restrictionType = RestrictionType.LIT_VALUE;
                    rel.restrictionValue = n.asLiteral().getLexicalForm();
                }
                else
                {
                    System.out.println("Has other restriction");
                }
            }
        }
    }

    private String getClassName(OntClass o)
    {
        if(o.isUnionClass())
        {
            return getUnionString(o.asUnionClass());
        }
        else if(o.isIntersectionClass())
        {
            return getIntersectionString(o.asIntersectionClass());
        }
        else
        {
            return o.getLocalName();
        }
    }

    private String getIntersectionString(IntersectionClass inters)
    {
        List<? extends OntClass> ops = inters.listOperands().toList();
        if(ops.size() == 0)
        {
            return "";
        }
        if(ops.size() == 1)
        {
            return getClassName(ops.get(0));
        }

        String str = "";
        String name;
        for(int i = 0; i < ops.size(); i++)
        {
            name = getClassName(ops.get(i));
            if(i >= ops.size() - 1)
            {
                str += name;
            }
            else
            {
                str += name + " " + INTERSECTION + " ";
            }
        }
        return str;
    }

    //TODO make this less hackish -> this class should not do language related stuff.
    private String getUnionString(UnionClass union)
    {
        List<? extends OntClass> ops = union.listOperands().toList();
        if(ops.size() == 0)
        {
            return "";
        }
        if(ops.size() == 1)
        {
            return getClassName(ops.get(0));
        }

        String str = "(";
        String name;
        for(int i = 0; i < ops.size(); i++)
        {
            name = getClassName(ops.get(i));
            if(i >= ops.size() - 1)
            {
                str += name;
            }
            else
            {
                str += name + " " + UNION + " ";
            }
        }
        str += ")";
        return str;
    }

    private void addProperties(OntClass oclass)
    {
        ExtendedIterator<OntProperty> it = oclass.listDeclaredProperties(true);
        OntProperty p;
        while(it.hasNext())
        {
            p = it.next();
            if(p != null)
            {
                if(p.isDatatypeProperty())
                {
                    //System.out.println("Data Property for: " + oclass.getLocalName() + ": " + p.getLocalName());
                    addDataProperty(p);
                }
                else
                {
                    addObjectProperty(p);
                }
            }
        }
    }

    private Relation addDataProperty(OntProperty p)
    {
        DatatypeProperty d = p.asDatatypeProperty();
        OntResource r = d.getRange();



        //Relation r = new Relation(d.getLocalName(), new ArrayList<String>());

        Attribut a = new Attribut();
        a.name = p.getLocalName();

        if(r != null && r.isClass())
        {
            OntClass c = r.asClass();
            a.unit = getClassName(c);
        }
        else
        {
            a.unit = "(Ohne Einheit)";
        }

        //System.out.println("Attribut: " + a.name + ", unit: " + a.unit);
        attribute.add(a);
        return a;
    }

    private Relation addObjectProperty(OntProperty p)
    {
        ExtendedIterator<? extends OntResource> it = p.listRange();
        ArrayList<String> list = new ArrayList<String>();
        while(it.hasNext())
        {
            OntResource o = it.next();
            if(OntClass.class.isAssignableFrom(o.getClass()))
            {
                list.add(getClassName((OntClass) o));
            }
            else
            {
                System.err.println("Error: expecting OntClass, got " + o.getClass().getSimpleName() + ": " + o.getLocalName());
            }
        }

        if(list.size() > 0)
        {
            Relation r = new Relation(p.getLocalName(), list, p.getCardinality(p));
            relationen.add(r);
            return r;
        }

        return null;
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

    public class Relation
    {
        public String name;
        public ArrayList<String> range;
        public int cardinality = -1;
        public RestrictionType restrictionType = RestrictionType.NONE;
        public String restrictionValue;

        public Relation()
        {

        }

        public Relation(String name, ArrayList<String> range)
        {
            this.name = name;
            this.range = range;
        }

        public Relation(String name, ArrayList<String> range, int cardinality)
        {
            this(name, range);
            this.cardinality = cardinality;
        }
    }

    public class Attribut extends Relation
    {
        public String unit;
    }
}
