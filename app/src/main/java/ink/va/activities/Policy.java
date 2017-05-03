package ink.va.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ink.va.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import ink.va.interfaces.RequestCallback;
import ink.va.models.Config;
import ink.va.utils.Constants;
import ink.va.utils.Retrofit;

public class Policy extends BaseActivity {

    @BindView(R.id.policyLoading)
    ProgressBar policyLoading;
    @BindView(R.id.webView)
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy);
        ButterKnife.bind(this);
        makeRequest(Retrofit.getInstance().getInkService().getConfigs(Constants.CONFIG_TYPE_POLICY),
                null, new RequestCallback() {
                    @Override
                    public void onRequestSuccess(Object result) {
                        final Config config = (Config) result;

                        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
                        webView.setHorizontalScrollBarEnabled(false);
                        webView.setWebViewClient(new WebViewClient() {
                            @Override
                            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                policyLoading.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onPageFinished(WebView view, String url) {
                                policyLoading.setVisibility(View.GONE);
                            }

                            @Override
                            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                                view.loadUrl(config.getConfigArgument());
                                return true;
                            }
                        });
                        webView.loadUrl(config.getConfigArgument());
                    }

                    @Override
                    public void onRequestFailed(Object[] result) {
                        policyLoading.setVisibility(View.GONE);
                        Toast.makeText(Policy.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
