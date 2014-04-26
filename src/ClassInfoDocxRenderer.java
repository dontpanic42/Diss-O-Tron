import Settings.OutputSettings;
import Structure.Attribut;
import Structure.ClassInfo;
import Structure.Relation;
import Structure.RestrictionType;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by daniel on 18.04.14.
 */
public class ClassInfoDocxRenderer implements ClassInfoRenderer {

    public final static String INFINITY = "\u221E";
    private Settings.OutputSettings settings;
    public static final String EQUIVALENT = "\u2261";
    public static final String ELEMENT_OF = "\u2208";
    public static final String GREATER_EQUAL = "≥";
    public static final String LESSER_EQUAL = "≤";

    public void render(OutputSettings settings, ArrayList<ClassInfo> list)
    {
        this.settings = settings;
        System.out.print("Initializing docx document... ");
        TableDocument doc = new TableDocument();
        System.out.println("Done.");

        //Structure.ClassInfo info = list.get(0);
        int counter = 0;
        for(ClassInfo info : list)
        {
            TableDocumentTable table = new TableDocumentTable();
            table.setHeaderStyle(settings.headerStyle);
            table.setRowHeaderStyle(settings.rowHeaderStyle);

            renderTableHeader(table, info);
            renderRelations(table, info);
            renderAttributes(table, info);

            doc.addTable(table);
            System.out.println("Created table " + (++counter) + "/" + list.size() + ":\t" + info.getName());
        }

        System.out.println("Writing docx file: " + settings.filename);
        doc.save(new File(settings.filename));

    }

    private void renderTableHeader(TableDocumentTable table, ClassInfo info)
    {
        String header = info.getName();
        if(info.hasEquivalentClass()) header += " (" + EQUIVALENT + " " + info.getEquivalentClass() + ")";
        table.setHeader(header);
    }

    private void renderRelations(TableDocumentTable table, ClassInfo info)
    {
        for(Relation r : info.getRelations())
        {
            ArrayList<String> col = new ArrayList<String>();
            col.add(null);      //Merged row header
            col.add(r.name);
            col.add(getRangeString(r));
            col.add((r.restrictionType == RestrictionType.LIT_VALUE)? r.restrictionValue : "");
            col.add(getCardinalityString(r));
            col.add("");

            table.addRelation(col);
        }
    }

    private void renderAttributes(TableDocumentTable table, ClassInfo info)
    {
        for(Relation r : info.getAttributs())
        {
            ArrayList<String> col = new ArrayList<String>();
            Attribut a = (Attribut) r;

            col.add(null);
            col.add(a.name);
            col.add(getWertebereich(a));
            col.add(a.unit);
            col.add(getCardinalityString(a));
            col.add("");

            table.addAttribut(col);
        }
    }

    private String getWertebereich(Attribut a)
    {
        if(a.restrictionType == RestrictionType.LIT_VALUE)
        {
            return String.format("%s \u2208 {%s}", a.baseType, a.restrictionValue);
        }
        else
        {
            return a.baseType;
        }
    }

    private String getCardinalityString(Relation r)
    {
        switch(r.restrictionType)
        {
            case SOME:
            {
                return String.format(GREATER_EQUAL + " 1");//"[1,+" + INFINITY + ")";
            }
            case MIN:
            {
                return String.format(GREATER_EQUAL + " ") + r.restrictionValue;
            }
            case MAX:
            {
                return String.format(LESSER_EQUAL + " ") + r.restrictionValue;
            }
            case EXACT:
            {
                return ""+r.restrictionValue;
            }
            case LIT_VALUE:
            {
                return "1";
            }
            case NOT_APPLICABLE:
            {
                return "";
            }
            default:    //NONE
            {
                return String.format(GREATER_EQUAL + " 0");//"[1,+" + INFINITY + ")";
            }
        }
    }

    private String getRangeString(Relation r)
    {
        ArrayList<String> l = r.range;
        if(l == null || l.size() == 0)
        {
            return "";
        }
        else if(l.size() == 1)
        {
            return l.get(0);
        }
        else
        {
            String s = "";
            for(int i = 0; i < l.size(); i++)
            {
                if(i >= l.size() - 1)
                {
                    s += l.get(i);
                }
                else
                {
                    s += l.get(i) + " \u2229 ";
                }
            }

            return s;
        }
    }
}
