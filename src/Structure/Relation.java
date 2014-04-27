package Structure;

import java.util.ArrayList;


/**
 * Created by daniel on 26.04.14.
 */
public class Relation
{
    public String name;
    public ArrayList<String> range;
    public int cardinality = -1;
    public RestrictionType restrictionType = RestrictionType.NONE;
    public String restrictionValue = null;

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
