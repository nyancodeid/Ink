/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 * <p>
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Facebook.
 * <p>
 * As with any software that integrates with the Facebook platform, your use of
 * this software is subject to the Facebook Developer Principles and Policies
 * [http://developers.facebook.com/policy/]. This copyright notice shall be
 * included in all copies or substantial portions of the software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ink.friendsmash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ink.R;

import ink.StartupApplication;

public class ScoreboardFragment extends Fragment {

    private StartupApplication application;

    private LinearLayout scoreboardContainer;

    private FrameLayout progressContainer;

    private Handler uiHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (StartupApplication) getActivity().getApplication();

        uiHandler = new Handler();

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_scoreboard, parent, false);

        scoreboardContainer = (LinearLayout) v.findViewById(R.id.scoreboardContainer);
        progressContainer = (FrameLayout) v.findViewById(R.id.progressContainer);

        progressContainer.setVisibility(View.INVISIBLE);

        return v;
    }

    private void closeAndShowError(String error) {
        Bundle bundle = new Bundle();
        bundle.putString("error", error);

        Intent i = new Intent();
        i.putExtras(bundle);

        getActivity().setResult(Activity.RESULT_CANCELED, i);
        getActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();

        // TODO: 8/29/2016 fetch scores
        progressContainer.setVisibility(View.VISIBLE);
        populateScoreboard();
        fetchScoreboardEntries();
    }

    private void fetchScoreboardEntries() {
        // TODO: 8/29/2016 fetch scoreboard entries
    }

    private void populateScoreboard() {
        scoreboardContainer.removeAllViews();

        progressContainer.setVisibility(View.INVISIBLE);
        // TODO: 8/29/2016 populate score
    }
}
