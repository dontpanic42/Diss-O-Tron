package Structure;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDFS;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 26.04.14.
 *
 * Known issues:
 *      - Inverse properties (without domain) are ignored
 *      - Range restrictions (hasX some y, where y is something else than range(hasX))
 *      are not displayed.
 *      - Compopound datatypes (e.g. (Euro or US-Dollar)) are ignored
 *
 */
public class ClassAnalyzer {

    public ClassInfo analyze(OntClass ontClass)
    {
        ClassInfo ci = new ClassInfo();

        ci.name = getSimpleClassname(ontClass);

        String sup = getSuperclasses(ontClass, ci);

        Relation is_a = new Relation(ClassInfo.IS_A_RELATION_NAME, stringToList(sup));
        is_a.restrictionType = RestrictionType.NOT_APPLICABLE;
        ci.relationen.add(is_a);

        addProperties(ontClass, ci);

        System.out.println(ci.name + " -> " + sup);

        return ci;
    }

    public void addProperties(OntClass ontClass, ClassInfo info)
    {
        for(OntProperty prop : ontClass.listDeclaredProperties(true).toList())
        {
            //If property wasnt added previously (e.g. via restriction)
            if(!info.hasProperty(prop))
            {
                addProperty(prop, info);
            }
        }
    }

    public Relation addProperty(OntProperty prop, ClassInfo info)
    {
        Relation r = getPropertyInfo(prop);
        if(r == null)
        {
            return r;
        }

        if(prop.isObjectProperty())
        {
            info.relationen.add(r);
        }
        else
        {
            info.attribute.add(r);
        }
        return r;
    }

    public Relation getPropertyInfo(OntProperty prop)
    {
        if(prop.listDomain().toList().size() == 0)
        {
            System.err.println(String.format("Ignoring property %s: No Domain. (ivo: %d, iv: %d)",
                    prop.getLocalName(),
                    prop.listInverse().toList().size(),
                    prop.listInverseOf().toList().size()));
            return null;
        }

        if(prop.isObjectProperty())
        {
            return getPropertyInfo(prop.asObjectProperty());
        }
        else if(prop.isDatatypeProperty())
        {
            return getPropertyInfo(prop.asDatatypeProperty());
        }

        return null;
    }

    public Relation getPropertyInfo(ObjectProperty prop)
    {
        Relation rel = new Relation();
        rel.range = stringToList(stringFromResourceList(prop.listRange().toList()));
        rel.name = getSimpleClassname(prop);
        return rel;
    }

    public Relation getPropertyInfo(DatatypeProperty prop)
    {
        Attribut at = new Attribut();
        //at.range = stringToList(stringFromResourceList(prop.listRange().toList()));
        at.name = getSimpleClassname(prop);
        setDataUnit(prop, at);
        return at;
    }

    private void setDataUnit(DatatypeProperty prop, Attribut at)
    {
        for(OntResource r : prop.listRange().toList())
        {
            for(Resource a : r.listRDFTypes(true).toList())
            {
                if(a.getURI().equals("http://www.w3.org/2000/01/rdf-schema#Datatype"))
                {
                    //Datatype is just an alias for a base type
                    final Resource datatype = prop.getPropertyResourceValue( RDFS.range );
                    final Resource alsdfj = datatype.getPropertyResourceValue(OWL.equivalentClass);
                    if(alsdfj != null)
                    {
                        at.baseType = alsdfj.getLocalName();
                        at.unit = r.getLocalName();

                        return;
                    }
                    else
                    {
                        System.err.println("Error determining base Type for " + prop.getLocalName());
                    }

                }
            }

            //Datatype is base type
            at.baseType = r.getLocalName();
            at.unit = "(Ohne Einheit)";
            return;
        }

        at.baseType = "(Ohne Typ)";
        at.unit = "(Ohne Einheit)";
    }

    private ArrayList<String> stringToList(String str)
    {
        ArrayList<String> l = new ArrayList<String>();
        l.add(str);
        return l;
    }

    private String stringFromResourceList(List<? extends OntResource> resources)
    {
        ArrayList<String> list = new ArrayList<String>();
        String tmp;
        for(OntResource o : resources)
        {
            tmp = getSimpleClassname(o);
            if(tmp != null)
            {
                list.add(tmp);
            }
        }

        return stringFromIntersectionList(list);
    }

    private String stringFromList(List<String> list, String glue)
    {
        String format = "%s " + glue + " ";
        String str = new String();

        if(list.size() == 0)
        {
            return "";
        }

        for(String s : list)
        {
            str += String.format(format, s);
        }
        return str.substring(0, str.length() - (glue.length() + 2));
    }

    private String stringFromIntersectionList(List<String> list)
    {
        return stringFromList(list, ClassInfo.INTERSECTION);
    }

    private String stringFromUnionList(List<String> list)
    {
        return stringFromList(list, ClassInfo.UNION);
    }

    private String getSuperclasses(OntClass ocls, ClassInfo info)
    {
        if(ocls.isHierarchyRoot())
        {
            if(ocls.listEquivalentClasses().toList().size() != 0)
            {
                String equivClassName = getSuperclassName(ocls.getEquivalentClass(), info);
                info.equivalentTo = equivClassName;
                return equivClassName;
            }
            else
            {
                return ClassInfo.ROOT_CLASS_NAME;
            }
        }

        if(ocls.isComplementClass())
        {
            System.out.println("    ->  Complement");
        }

        ArrayList<String> intersection = new ArrayList<String>();
        String tmp;
        for(OntResource res : ocls.listSuperClasses(true).toList())
        {
            tmp = getSuperclassName(res, info);
            if(tmp != null)
            {
                intersection.add(tmp);
            }
        }

        return (intersection.size() == 0)? "(-)" : stringFromIntersectionList(intersection);
    }

    private String getSuperclassName(OntResource res, ClassInfo info)
    {
        String tmp;

        if(res.isClass())
        {
            OntClass supcls = res.asClass();
            if(supcls.isIntersectionClass())
            {
                tmp = getIntersection(supcls.asIntersectionClass(), info);
                if(tmp != null)
                {
                    return tmp;
                }
            }
            else if(supcls.isUnionClass())
            {
                tmp = getUnion(supcls.asUnionClass(), info);
                if(tmp != null)
                {
                    return tmp;
                }
            }
            else if(supcls.isRestriction())
            {
                getRestriction(supcls.asRestriction(), info);
            }
            else
            {
                return getSimpleClassname(res);
            }
        }
        else
        {
            System.err.println("Invalid non-class type for classnames");
        }

        return null;
    }

    private void getRestriction(Restriction r, ClassInfo info)
    {
        Relation rel = addProperty(r.getOnProperty(), info);

        if(rel != null)
        {

            if(r.isAllValuesFromRestriction())
            {
                rel.restrictionType = RestrictionType.ALL;
                rel.cardinality = 0;
            }
            else if(r.isSomeValuesFromRestriction())
            {
                rel.restrictionType = RestrictionType.SOME;
                rel.cardinality = 1;

                SomeValuesFromRestriction sr = r.asSomeValuesFromRestriction();
                //System.out.println("Some Values From: " + sr.getSomeValuesFrom().getLocalName());
                if(sr.getSomeValuesFrom().canAs(OntClass.class))
                {
                    rel.restrictionValue = getSimpleClassname(sr.getSomeValuesFrom().as(OntClass.class));
                }
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
                rel.restrictionType = RestrictionType.LIT_VALUE;
                if(n.canAs(Individual.class))
                {
                    //rel.restrictionValue = "Instanz [" + n.as(Individual.class).getLocalName() + "]";
                    rel.restrictionValue = String.format("Instanz \u2208 {%s}", n.as(Individual.class).getLocalName());
                    rel.cardinality = 1;
                }
                else if(n.isLiteral())
                {
                    rel.restrictionValue = n.asLiteral().getLexicalForm();
                    rel.cardinality = 1;
                }
                else
                {
                    System.out.println(info.name + " has unknown restriction type");
                    rel.restrictionValue = "[ERROR: UNKNOWN]";
                }
            }
            else
            {

                if(r.hasProperty(OWL2.minQualifiedCardinality))
                {
                    rel.restrictionType = RestrictionType.MIN;
                    rel.cardinality = r.getRequiredProperty(OWL2.minQualifiedCardinality).getInt();
                }
                else if(r.hasProperty(OWL2.maxQualifiedCardinality))
                {
                    rel.restrictionType = RestrictionType.MAX;
                    rel.cardinality = r.getRequiredProperty(OWL2.maxQualifiedCardinality).getInt();
                }
                else if(r.hasProperty(OWL2.qualifiedCardinality))
                {
                    rel.restrictionType = RestrictionType.EXACT;
                    rel.cardinality = r.getRequiredProperty(OWL2.qualifiedCardinality).getInt();
                }
                else
                {
                    System.err.println("Unknwon restriction on property " + rel.name);
                }
            }

        }
    }

    private String getIntersection(IntersectionClass isc, ClassInfo info)
    {
        String tmp;
        ArrayList<String> intersection = new ArrayList<String>();
        for(OntResource o : isc.listOperands().toList())
        {
            tmp = getSuperclassName(o, info);
            if(tmp != null)
            {
                intersection.add(tmp);
            }
        }

        return (intersection.size() == 0)? null : stringFromIntersectionList(intersection);
    }

    private String getUnion(UnionClass usc, ClassInfo info)
    {
        String tmp;
        ArrayList<String> union = new ArrayList<String>();
        for(OntResource o : usc.listOperands().toList())
        {
            tmp = getSuperclassName(o, info);
            if(tmp != null)
            {
                union.add(tmp);
            }
        }

        return (union.size() == 0)? null : "(" + stringFromUnionList(union) + ")";
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
