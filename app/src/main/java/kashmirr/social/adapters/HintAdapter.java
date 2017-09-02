package kashmirr.social.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by PC-Comp on 9/12/2016.
 */
public class HintAdapter extends ArrayAdapter<String> {


    public HintAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
    }
}