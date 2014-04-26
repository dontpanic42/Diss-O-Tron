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

    public ClassInfo(OntClass oclass)
    {
        //OntClass sclass = oclass.getSuperClass();
        name = oclass.getLocalName();

        if(name.equals("DanielsImaginäresAuto"))
        {
            System.out.println("..............");

            for(OntClass o : oclass.listSuperClasses().toList())
            {
                System.out.println("Sup: " + o.getLocalName());
                System.out.println("is u: " + o.isUnionClass());
                System.out.println("is i: " + o.isIntersectionClass());

                IntersectionClass i = o.asIntersectionClass();
                System.out.println("i is r: " + i.isRestriction());

                for(OntResource inst : i.listInstances().toList())
                {
                    System.out.println("Has instance: " + inst.getLocalName());
                }

                for(OntClass op: i.listOperands().toList())
                {
                    System.out.println("Has op: " + op.getLocalName());

                    if(op.isAnon())
                    {
                        System.out.println("Anon has instances: " + op.listInstances().toList().size());
                        System.out.println("Anon is restriction: " + op.isRestriction());
                        System.out.println("Anon is op: " + op.asRestriction().getOnProperty().isObjectProperty());
                        System.out.println("Anon is dp: " + op.asRestriction().getOnProperty().isDatatypeProperty());
                        System.out.println("Anon r op on: " + op.asRestriction().getOnProperty().getLocalName());

                        Restriction anonR = op.asRestriction();
                        System.out.println("Is has Value restriction: " + anonR.isHasValueRestriction());
                        HasValueRestriction hvr = anonR.asHasValueRestriction();
                        System.out.println("HVR has instances: " + hvr.listInstances().toList().size());
                        System.out.println(hvr.getHasValue().canAs(Individual.class));
                        System.out.println(hvr.getHasValue().getClass().getSimpleName());

                        Individual iv = hvr.getHasValue().as(Individual.class);
                        System.out.println(iv.getLocalName());      //Eureka!
                    }
                }
            }

            System.out.println("..............");
        }

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
            System.out.println("Object Property Restriction: " + r.getOnProperty().getLocalName());
            rel = addObjectProperty(r.getOnProperty());
            System.out.println("Value: " + rel);
        }
        else
        {
            System.out.println("Is unknown restriction: " + r.getOnProperty().getLocalName());
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
                System.out.println("IS RESTRCTION");
                RDFNode n = r.asHasValueRestriction().getHasValue();
                rel.restrictionType = RestrictionType.LIT_VALUE;
                if(n.canAs(Individual.class))
                {
                    System.out.println("IS INDIVIDUAL");
                    rel.restrictionValue = "Individual[" + n.as(Individual.class).getLocalName() + "]";
                }
                else if(n.isLiteral())
                {
                    rel.restrictionValue = n.asLiteral().getLexicalForm();
                }
                else
                {
                    System.out.println("Has other restriction");
                    rel.restrictionValue = "[ERROR: UNKNOWN]";
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
        String name;
        ArrayList<String> tmpNames = new ArrayList<String>();
        for(int i = 0; i < ops.size(); i++)
        {
            if(ops.get(i).isRestriction())
            {
                System.out.println(":::::Is restriction::::::");
                addRestriction(ops.get(i).asRestriction());
                continue;
            }
            name = getClassName(ops.get(i));
            if(name != null)
            {
                tmpNames.add(name);
            }
        }

        if(tmpNames.size() == 0)
        {
            return "";
        }
        if(tmpNames.size() == 1)
        {
            return tmpNames.get(0);
        }

        String str = "";

        for(int i = 0; i < tmpNames.size(); i++)
        {
            name = tmpNames.get(i);

            if(i >= tmpNames.size() - 1)
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

            if(name == null)
                continue;

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
        //System.out.println("OP has range: " + p.listRange().toList().size());
        ObjectProperty op = p.asObjectProperty();
        System.out.println("OP has range: " + op.listRange().toList().size());
        System.out.println(op.getRange());
        System.out.println("Refering: " + op.listReferringRestrictions().toList().size());
        System.out.println("Has iv: " + op.hasInverse());


        //Standard
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

        //Infer range from inverse (->listDomain)
//        if(list.size() == 0 && op.hasInverse())
//        {
//            System.out.println("Has inverse.");
//            OntProperty inv = op.getInverse();
//            for(OntResource o : inv.listDomain().toList())
//            {
//                System.out.println("Got inverse domain: " + o.getLocalName());
//                if(o.isClass())
//                {
//                    list.add(getClassName(o.asClass()));
//                }
//            }
//        }

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




}
