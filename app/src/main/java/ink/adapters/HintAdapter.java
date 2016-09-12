package ink.adapters;

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

    @Override
    public int getCount() {
        // don't display last item. It is used as hint.
        int count = super.getCount();
        return count > 0 ? count - 1 : count;
    }
}