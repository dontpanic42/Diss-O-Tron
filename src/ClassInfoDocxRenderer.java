import Settings.OutputSettings;
import Structure.ClassInfo;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by daniel on 18.04.14.
 */
public class ClassInfoDocxRenderer implements ClassInfoRenderer {

    public final static String INFINITY = "\u221E";
    private Settings.OutputSettings settings;

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
        table.setHeader(info.getName());
    }

    private void renderRelations(TableDocumentTable table, ClassInfo info)
    {
        for(ClassInfo.Relation r : info.getRelations())
        {
            ArrayList<String> col = new ArrayList<String>();
            col.add(null);      //Merged row header
            col.add(r.name);
            col.add(getRangeString(r));
            col.add("");
            col.add(getCardinalityString(r));
            col.add("");

            table.addRelation(col);
        }
    }

    private void renderAttributes(TableDocumentTable table, ClassInfo info)
    {
        for(ClassInfo.Relation r : info.getAttributs())
        {
            ArrayList<String> col = new ArrayList<String>();
            ClassInfo.Attribut a = (ClassInfo.Attribut) r;

            col.add(null);
            col.add(a.name);
            col.add(getWertebereich(a));
            col.add(a.unit);
            col.add(getCardinalityString(a));
            col.add("");

            table.addAttribut(col);
        }
    }

    private String getWertebereich(ClassInfo.Attribut a)
    {
        if(a.restrictionType == ClassInfo.RestrictionType.LIT_VALUE)
        {
            return "{" + a.restrictionValue + "}";
        }
        else
        {
            return "";
        }
    }

    private String getCardinalityString(ClassInfo.Relation r)
    {
        switch(r.restrictionType)
        {
            case SOME:
            {
                return "[1,+" + INFINITY + ")";
            }
            case MIN:
            {
                return "min. " + r.cardinality;
            }
            case MAX:
            {
                return "max. " + r.cardinality;
            }
            case EXACT:
            {
                return ""+r.cardinality;
            }
            case ALL:
            {
                return "[0,+" + INFINITY + ")";
            }
            default:    //NONE
            {
                return "";
            }
        }
    }

    private String getRangeString(ClassInfo.Relation r)
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
