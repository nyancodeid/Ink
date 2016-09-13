package ink.va.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ink.va.R;


/**
 * Created by PC-Comp on 9/2/2016.
 */
public class FirstIntroFragment extends Fragment {

    private RelativeLayout newFriendsBubble;
    private ImageView guyVector;
    private TextView firstIntroDescription;
    private Animation slideInRight;
    private Animation slideInLeft;
    private Animation fadeIn;


    public static FirstIntroFragment create() {
        FirstIntroFragment firstIntroFragment = new FirstIntroFragment();
        return firstIntroFragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.first_intro_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        slideInLeft = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_left);
        slideInRight = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
        fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        fadeIn.setDuration(600);
        newFriendsBubble = (RelativeLayout) view.findViewById(R.id.newFriendsBubble);
        firstIntroDescription = (TextView) view.findViewById(R.id.firstIntroDescription);
        guyVector = (ImageView) view.findViewById(R.id.guyVector);
        startAnimation();
    }

    public void startAnimation() {
        if (newFriendsBubble != null) {
            newFriendsBubble.setVisibility(View.VISIBLE);
            guyVector.setVisibility(View.VISIBLE);
            newFriendsBubble.startAnimation(slideInLeft);
            guyVector.startAnimation(slideInRight);
            slideInRight.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    firstIntroDescription.setVisibility(View.VISIBLE);
                    firstIntroDescription.startAnimation(fadeIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    public void hideItems() {
        if (newFriendsBubble != null) {
            newFriendsBubble.setVisibility(View.GONE);
            guyVector.setVisibility(View.GONE);
            firstIntroDescription.setVisibility(View.GONE);
        }
    }
}
