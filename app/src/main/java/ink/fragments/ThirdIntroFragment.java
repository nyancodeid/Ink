package ink.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ink.R;

/**
 * Created by PC-Comp on 9/2/2016.
 */
public class ThirdIntroFragment extends Fragment {

    public static ThirdIntroFragment create() {
        ThirdIntroFragment thirdIntroFragment = new ThirdIntroFragment();
        return thirdIntroFragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.third_intro_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}