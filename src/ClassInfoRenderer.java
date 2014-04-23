import Settings.OutputSettings;
import Structure.ClassInfo;

import java.util.ArrayList;

/**
 * Created by daniel on 17.04.14.
 */
public interface ClassInfoRenderer {
    public void render(OutputSettings settings, ArrayList<ClassInfo> list);
}
