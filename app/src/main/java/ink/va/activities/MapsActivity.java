package ink.va.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ink.va.R;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fab.FloatingActionButton;
import ink.va.callbacks.GeneralCallback;
import ink.va.utils.LocationUtils;
import ink.va.utils.PermissionsChecker;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_LOCATION_CODE = 454;
    @BindView(R.id.mapsToolbar)
    Toolbar mMapsToolbar;
    @BindView(R.id.mapsToolbarTitle)
    TextView mMapsToolbarTitle;
    @BindView(R.id.acceptLocation)
    FloatingActionButton mAcceptLocation;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private GoogleMap mGoogleMap;
    private Thread mWorkerThread;
    private String chosenValue = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        chosenValue = getString(R.string.nothingChosen);
        mAcceptLocation.hide(true);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (!LocationUtils.isLocationEnabled(this)) {
            Snackbar.make(mMapsToolbar, getString(R.string.turnLocationOn), BaseTransientBottomBar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent settingsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(settingsIntent, 0);
                }
            }).show();
        }
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_CODE);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mGoogleMap = googleMap;
        boolean isGranted = PermissionsChecker.isLocationPermissionGranted(this);
        if (isGranted) {
            getLastKnownLocation(googleMap);
        } else {
            requestPermission();
        }
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        googleMap.clear();
                        mMapsToolbarTitle.setText(getString(R.string.loadingText));
                        getAddress(latLng.latitude, latLng.longitude, new GeneralCallback<String>() {
                            @Override
                            public void onSuccess(String address) {
                                if (mAcceptLocation.isHidden()) {
                                    mAcceptLocation.show(true);
                                }
                                chosenValue = address;
                                mMapsToolbarTitle.setText(address);
                            }

                            @Override
                            public void onFailure(String o) {
                                Snackbar.make(mMapsToolbar, getString(R.string.addresNotAvailable), Snackbar.LENGTH_LONG).show();
                            }
                        });
                        googleMap.addMarker(new MarkerOptions().position(latLng));
                    }
                });
            }
        });
    }


    @OnClick(R.id.backGoogleMaps)
    public void mapsBack() {
        finish();
    }

    @OnClick(R.id.acceptLocation)
    public void acceptLocation() {
        Intent intent = new Intent(getPackageName() + "MakePost");
        intent.putExtra("value", chosenValue);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        finish();
    }


    public void getLastKnownLocation(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermission();
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(),
                    mLastLocation.getLongitude()), 20));
        }

    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE: {
                if (PermissionsChecker.isLocationPermissionGranted(this)) {
                    getLastKnownLocation(mGoogleMap);
                } else {
                    Snackbar.make(mMapsToolbar, getString(R.string.permissionsRequired), Snackbar.LENGTH_LONG).show();
                }
            }

        }
    }

    private void getAddress(final double latitude, final double longitude, final GeneralCallback generalCallback) {
        if (mWorkerThread != null) {
            mWorkerThread = null;
        }
        mWorkerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                Looper mainLooper = Looper.getMainLooper();
                String strAdd = "";
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses != null) {
                        Address returnedAddress = addresses.get(0);
                        StringBuilder strReturnedAddress = new StringBuilder("");

                        for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                            strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                        }
                        strAdd = strReturnedAddress.toString();

                        Handler handler = new Handler(mainLooper);
                        final String finalStrAdd = strAdd;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                generalCallback.onSuccess(finalStrAdd);
                            }
                        });
                        mWorkerThread = null;
                    } else {
                        Handler handler = new Handler(mainLooper);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                generalCallback.onFailure(null);
                                mWorkerThread = null;
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Handler handler = new Handler(mainLooper);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            generalCallback.onFailure(null);
                            mWorkerThread = null;
                        }
                    });
                }


            }
        });
        mWorkerThread.start();
    }
}
