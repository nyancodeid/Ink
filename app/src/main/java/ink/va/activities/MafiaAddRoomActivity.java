package ink.va.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ink.va.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ink.va.service.MafiaGameService;
import ink.va.utils.Keyboard;
import ink.va.utils.MafiaConstants;
import ink.va.utils.Retrofit;
import ink.va.utils.SharedHelper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ink.va.utils.ErrorCause.ALREADY_IN_ROOM;


public class MafiaAddRoomActivity extends BaseActivity {
    @BindView(R.id.roomNameTV)
    EditText roomNameTV;
    @BindView(R.id.durationMorningED)
    EditText durationMorningED;
    @BindView(R.id.nightDurationTV)
    TextView nightDurationTv;
    @BindView(R.id.maxPlayersTV)
    TextView maxPlayersTV;

    @BindView(R.id.languageSpinner)
    AppCompatSpinner languageSpinner;
    @BindView(R.id.gameTypeSpinner)
    AppCompatSpinner gameTypeSpinner;
    @BindView(R.id.gameMorningDurationSpinner)
    AppCompatSpinner gameMorningDurationSpinner;

    @BindView(R.id.addRoomScroll)
    ScrollView addRoomScroll;

    @BindView(R.id.saveAddRoom)
    FloatingActionButton saveAddRoom;

    private List<String> languages;
    private List<String> gameTypes;
    private List<String> timeUnits;
    private String chosenLanguage;
    private int estimatedNightDuration;
    private String chosenGameType;
    private String chosenMorningTimeUnit;
    private String chosenNightTimeUnit;
    private boolean hasTimeError;
    private SharedHelper sharedHelper;
    private android.app.ProgressDialog progressDialog;
    private int maxPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mafia_add_room);
        ButterKnife.bind(this);

        progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle(getString(R.string.creating));
        progressDialog.setMessage(getString(R.string.creatingRoom));


        sharedHelper = new SharedHelper(this);
        getSupportActionBar().setTitle(getString(R.string.addRoom));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        languages = new ArrayList<>();
        languages.add(getString(R.string.english));
        languages.add(getString(R.string.russian));

        gameTypes = new ArrayList<>();
        gameTypes.add(getString(R.string.classic));
//        gameTypes.add(getString(R.string.yakudza));

        timeUnits = new ArrayList<>();
        timeUnits.add(getString(R.string.minutesUnit));
        timeUnits.add(getString(R.string.hoursUnit));
        timeUnits.add(getString(R.string.daysUnit));

        initAdapters();
        initEditTexts();

        chosenLanguage = getString(R.string.english);
        chosenGameType = getString(R.string.classic);
        chosenMorningTimeUnit = getString(R.string.minutesUnit);
        maxPlayers = 10;

        initMaxPlayers();

        if (sharedHelper.getMenuButtonColor() != null) {
            saveAddRoom.setBackgroundTintList((ColorStateList.valueOf(Color.parseColor(sharedHelper.getMenuButtonColor()))));
        }
    }


    private void initEditTexts() {
        durationMorningED.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkEditText();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        durationMorningED.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkEditText();
                }
            }
        });

    }

    private void checkEditText() {
        if (!durationMorningED.getText().toString().trim().isEmpty()) {
            try {
                int chosenNumber = Integer.valueOf(durationMorningED.getText().toString());
                if (chosenMorningTimeUnit.equals(getString(R.string.minutesUnit))) {
                    if (chosenNumber < 5) {
                        hasTimeError = true;
                        durationMorningED.setError(getString(R.string.minimumFiveMinutes));
                    } else if (chosenNumber > 1440) {
                        hasTimeError = true;
                        durationMorningED.setError(getString(R.string.maximumDaysAllowed));
                    } else {
                        hasTimeError = false;
                        durationMorningED.setError(null);
                    }
                } else if (chosenMorningTimeUnit.equals(getString(R.string.hoursUnit))) {
                    if (chosenNumber > 24) {
                        hasTimeError = true;
                        durationMorningED.setError(getString(R.string.maximumDaysAllowed));
                    } else {
                        hasTimeError = false;
                        durationMorningED.setError(null);
                    }
                } else if (chosenMorningTimeUnit.equals(getString(R.string.daysUnit))) {
                    if (chosenNumber > 1) {
                        hasTimeError = true;
                        durationMorningED.setError(getString(R.string.maximumDaysAllowed));
                    } else {
                        hasTimeError = false;
                        durationMorningED.setError(null);
                    }
                }
                initNightDuration();
            } catch (NumberFormatException e) {
                durationMorningED.setError(getString(R.string.onlyNumbersAllowed));
                e.printStackTrace();
            }
        } else {
            initNightDuration();
        }
    }

    private void initNightDuration() {
        chosenNightTimeUnit = chosenMorningTimeUnit;
        if (!hasTimeError && !durationMorningED.getText().toString().trim().isEmpty()) {
            int chosenMorningDuration = Integer.valueOf(durationMorningED.getText().toString());
            estimatedNightDuration = chosenMorningDuration / 2;
            if (estimatedNightDuration == 0) {
                if (chosenMorningTimeUnit.equals(getString(R.string.hoursUnit))) {
                    chosenNightTimeUnit = getString(R.string.minutesUnit);
                    estimatedNightDuration = (int) (java.util.concurrent.TimeUnit.HOURS.toMinutes(chosenMorningDuration) / 2);

                } else if (chosenMorningTimeUnit.equals(getString(R.string.daysUnit))) {
                    chosenNightTimeUnit = getString(R.string.hoursUnit);
                    estimatedNightDuration = (int) (TimeUnit.DAYS.toHours(chosenMorningDuration) / 2);
                }
            }
            nightDurationTv.setText(getString(R.string.estimatedNightDuration, estimatedNightDuration + " " +
                    chosenNightTimeUnit));
        } else {
            nightDurationTv.setText(getString(R.string.chooseMorningFirst));
        }
    }


    private void initAdapters() {
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, languages);
        ArrayAdapter<String> gameTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, gameTypes);
        ArrayAdapter<String> timeUnitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, timeUnits);

        languageSpinner.setAdapter(languageAdapter);
        gameTypeSpinner.setAdapter(gameTypeAdapter);
        gameMorningDurationSpinner.setAdapter(timeUnitAdapter);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                chosenLanguage = languages.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        gameTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                chosenGameType = gameTypes.get(position);
                if (chosenGameType.equals(getString(R.string.classic))) {
                    maxPlayers = 10;
                } else if (chosenGameType.equals(getString(R.string.yakudza))) {
                    maxPlayers = 20;
                }
                initMaxPlayers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        gameMorningDurationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                chosenMorningTimeUnit = timeUnits.get(position);
                checkEditText();
                initNightDuration();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.saveAddRoom)
    public void saveAddRoomClicked() {
        proceedChecking();
    }

    private void proceedChecking() {
        Keyboard.hideKeyboard(this);
        if (hasTimeError) {
            Toast.makeText(this, getString(R.string.fixTimeErros), Toast.LENGTH_SHORT).show();
        } else if (roomNameTV.getText().toString().trim().isEmpty()) {
            scroll(ScrollView.FOCUS_UP);
            roomNameTV.setError(getString(R.string.emptyField));
        } else if (durationMorningED.getText().toString().isEmpty()) {
            scroll(ScrollView.FOCUS_DOWN);
            durationMorningED.setError(getString(R.string.emptyField));
        } else {
            initNightDuration();
            addRoom();
        }
    }

    private void addRoom() {
        progressDialog.show();
        String finalGameType = "none";
        if (chosenGameType.equals(getString(R.string.classic))) {
            finalGameType = MafiaConstants.GAME_TYPE_CLASSIC;
        } else if (chosenGameType.equals(getString(R.string.yakudza))) {
            finalGameType = MafiaConstants.GAME_TYPE_YAKUDZA;
        }

        String finalNightUnit = "none";
        String finalMorningUnit = "none";

        if (chosenMorningTimeUnit.equals(getString(R.string.minutesUnit))) {
            finalMorningUnit = MafiaConstants.UNIT_MINUTES;
        } else if (chosenMorningTimeUnit.equals(getString(R.string.hoursUnit))) {
            finalMorningUnit = MafiaConstants.UNIT_HOURS;
        } else if (chosenMorningTimeUnit.equals(getString(R.string.daysUnit))) {
            finalMorningUnit = MafiaConstants.UNIT_DAYS;
        }

        if (chosenNightTimeUnit.equals(getString(R.string.minutesUnit))) {
            finalNightUnit = MafiaConstants.UNIT_MINUTES;
        } else if (chosenNightTimeUnit.equals(getString(R.string.hoursUnit))) {
            finalNightUnit = MafiaConstants.UNIT_HOURS;
        } else if (chosenNightTimeUnit.equals(getString(R.string.daysUnit))) {
            finalNightUnit = MafiaConstants.UNIT_DAYS;
        }

        Call<ResponseBody> addRoomCall = Retrofit.getInstance().getInkService().addMafiaRoom(roomNameTV.getText().toString().trim(),
                chosenLanguage, finalGameType, durationMorningED.getText().toString(), finalMorningUnit,
                String.valueOf(estimatedNightDuration), finalNightUnit,
                sharedHelper.getUserId(), maxPlayers);
        addRoomCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response == null) {
                    addRoom();
                    return;
                }
                if (response.body() == null) {
                    addRoom();
                    return;
                }
                progressDialog.dismiss();
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.optBoolean("success");
                    if (success) {
                        startService(new Intent(MafiaAddRoomActivity.this, MafiaGameService.class));
                        Intent intent = new Intent();
                        intent.putExtra("hasAdded", true);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        String cause = jsonObject.optString("cause");
                        if (cause.equals(ALREADY_IN_ROOM)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MafiaAddRoomActivity.this);
                            builder.setTitle(getString(R.string.error));
                            builder.setMessage(getString(R.string.alreadyInRoom));
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        } else {
                            Toast.makeText(MafiaAddRoomActivity.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MafiaAddRoomActivity.this, getString(R.string.serverErrorText), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scroll(int direction) {
        addRoomScroll.fullScroll(direction);
    }

    private void initMaxPlayers() {
        maxPlayersTV.setText(getString(R.string.maxPlayers, maxPlayers));
    }
}
