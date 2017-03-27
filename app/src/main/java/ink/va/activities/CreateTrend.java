package ink.va.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.ink.va.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateTrend extends BaseActivity {

    private List<String> trendCategories;
    @BindView(R.id.autoCompleteTrendCategoriesTV)
    AutoCompleteTextView autoCompleteTrendCategoriesTV;
    @BindView(R.id.isPremiumTV)
    TextView isPremiumTV;
    @BindView(R.id.trendNoticeTV)
    TextView trendNoticeTV;
    @BindView(R.id.isPremiumSwitch)
    Switch isPremiumSwitch;
    @BindView(R.id.trendExternalED)
    EditText trendExternalED;
    @BindView(R.id.trendImageUrlED)
    EditText trendImageUrlED;
    @BindView(R.id.trendContentED)
    EditText trendContentED;
    @BindView(R.id.trendTitleED)
    EditText trendTitleED;

    private String advertisementPrice;
    private String topTrendPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trend);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        trendCategories = new ArrayList<>();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            trendCategories = extras.getStringArrayList("trendCategories");
            advertisementPrice = extras.getString("advertisementPrice");
            topTrendPrice = extras.getString("topTrendPrice");

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, trendCategories);
            autoCompleteTrendCategoriesTV.setAdapter(adapter);
        }
        isPremiumTV.setText(getString(R.string.isPremiumText, Integer.valueOf(topTrendPrice)));
        trendNoticeTV.setText(getString(R.string.createTrendNotice, Integer.valueOf(advertisementPrice)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.createTrend)
    public void createTrendClicked() {

    }
}
