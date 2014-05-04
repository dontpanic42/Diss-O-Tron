package Structure;

import java.util.Comparator;

/**
 * Created by daniel on 04.05.14.
 */
public class ClassInfoComparator implements Comparator<ClassInfo> {

    @Override
    public int compare(ClassInfo o1, ClassInfo o2) {
        return o1.getName().compareTo(o2.getName());
    }
}