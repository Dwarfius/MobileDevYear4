package com.uni.dpriho200.mobdev4;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel Prihodko, S1338994 on 11/22/2016.
 */

public class DetailView extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        ResultCallback<LocationSettingsResult>, NoteDialogInterface, ListView.OnItemClickListener {

    private static final int REQUEST_CHECK_SETTINGS = 1;
    private static final int NETWORK_CHECK_SETTINGS = 2;

    private String googleApiKey;
    private GoogleApiClient googleApiClient;
    private GoogleMap map;
    private String user;
    private UniClass uniClass;
    private boolean requestingLocations;
    private LocationRequest locationRequest;
    private Polyline pathToTarget = null;
    private NotesAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        // our data to fill out all the details
        Intent intent = getIntent();
        uniClass = intent.getParcelableExtra("Class");
        user = intent.getStringExtra("User");

        if(savedInstanceState != null)
            requestingLocations = savedInstanceState.getBoolean("requestingLocations");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Notes - " + user);

        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            googleApiKey = ai.metaData.getString("com.google.android.geo.API_KEY");
        } catch (Exception e) { Log.e("CW", "Couldn't read API key from metadata"); }

        // views initialization
        List<Note> notes = NotesDB.select(uniClass.getId(), user);
        ListView notesView = (ListView)findViewById(R.id.listView);
        listAdapter = new NotesAdapter(this, notes);
        notesView.setAdapter(listAdapter);
        notesView.setOnItemClickListener(this);

        TextView title = (TextView)findViewById(R.id.title);
        title.setText(uniClass.toString());

        // initializing our map
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("requestingLocations", requestingLocations); // just to be safe
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Need to override the default action in order to retain the activity stack
            // Default action creates a new instance of the activity, which swaps out the intent
            // which I rely on to contain specific data
            case android.R.id.home:
                finish();
                return true;
            case R.id.add_note:
                TypeDialog dialog = new TypeDialog();
                Bundle bundle = new Bundle();
                bundle.putInt("ClassId", uniClass.getId());
                bundle.putString("UserId", user);
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), "Type");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {
        if (googleApiClient.isConnected() && requestingLocations)
            startLocationUpdates();
        super.onResume();
    }

    @Override
    protected void onPause() {
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (googleApiClient.isConnected())
            stopLocationUpdates();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS: // if the settings were changed - great, start updates
                if(resultCode == Activity.RESULT_OK)
                    startLocationUpdates();
                break;
            case NETWORK_CHECK_SETTINGS: // if networking issue got resolved - try to connect to service
                if(resultCode == Activity.RESULT_OK)
                    googleApiClient.connect();
                break;
        }
    }

    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle)
    {
        Log.i("CW", "Connected to Google Play API");
        // before we activate the location tracking, need to make sure we have the capability
        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, settingsRequest);
        result.setResultCallback(this);
    }

    @Override
    public void onConnectionSuspended(int reason)
    {
        Log.i("CW", "Connection to Google Play API suspended");
        requestingLocations = false;
    }

    // GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result)
    {
        Log.i("CW", "Connection failed, trying to resolve...");
        // try to resolve, if possible
        if(result.hasResolution()) {
            try {
                result.startResolutionForResult(this, NETWORK_CHECK_SETTINGS);
            } catch (IntentSender.SendIntentException e) {
                Log.e("CW", e.toString());
            }
        }
        else { // if not - tell the user he's not requestingLocations
            Log.i("CW", "Can't resolve");
            Toast.makeText(this, "Failed to connect to location services", Toast.LENGTH_SHORT).show();
            requestingLocations = false;
        }
    }

    // ResultCallback<LocationSettingsResult>
    @Override
    public void onResult(@NonNull LocationSettingsResult result) {
        final Status status = result.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i("CW", "Location settings different from requested, asking to change");
                try {
                    status.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.e("CW", "Could not resolve location settings issue");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.w("CW", "Location settings unavailable");
                break;
        }
    }

    // OnMapReadyCallback
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("CW", "Map initialized");
        map = googleMap;
        if(checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            map.setMyLocationEnabled(true);

        LatLng classLoc = uniClass.getLocation();
        String title = uniClass.getRoom();
        map.addMarker(new MarkerOptions().position(classLoc).title(title));
        map.moveCamera(CameraUpdateFactory.newLatLng(classLoc));
    }

    // LocationListener
    @Override
    public void onLocationChanged(Location location) {
        Log.v("CW", "Got new location: " + location.toString());
        GoogleDirection.withServerKey(googleApiKey)
                .from(new LatLng(location.getLatitude(), location.getLongitude()))
                .to(uniClass.getLocation())
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if(direction.isOK()) {
                            if (pathToTarget != null)
                                pathToTarget.remove();

                            // there is always only 1 Path and Leg, according to the documentation
                            ArrayList<LatLng> points = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                            PolylineOptions options = DirectionConverter.createPolyline(DetailView.this, points, 2, Color.BLUE);
                            pathToTarget = map.addPolyline(options);
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        Log.e("CW", "Requesting directions failed: " + t.toString());
                    }
                });
    }

    // NoteDialogInterface
    @Override
    public void onFinishedEditing(Note note) {
        if(note.getId() == -1) {
            NotesDB.insert(note);
            listAdapter.add(note);
        } else {
            NotesDB.update(note);
            listAdapter.notifyDataSetInvalidated();
        }
        // create or reschedule, even it's the same
        if(note instanceof AlarmNote)
            NoteAlarmsManager.createAlarm((AlarmNote)note, this);
    }

    @Override
    public void onDeleted(Note note) {
        NotesDB.delete(note);
        listAdapter.remove(note);

        if(note instanceof AlarmNote)
            NoteAlarmsManager.cancelAlarm((AlarmNote)note, this);
    }

    // ListView.OnItemClickListener
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        Note note = (Note)parent.getItemAtPosition(pos);
        Bundle bundle = new Bundle();
        bundle.putInt("ClassId", uniClass.getId());
        bundle.putString("UserId", user);
        bundle.putParcelable("Note", note);

        AppCompatDialogFragment dialog = note instanceof AlarmNote ? new AlarmDialog() : new NoteDialog();
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "Note");
    }

    // finally, our own methods
    void startLocationUpdates() {
        Log.i("CW", "Requesting location updates");
        if(checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess())
                                requestingLocations = true;
                            else
                                Log.w("CW", "Couldn't start location tracking - " + status.toString());
                        }
                    });
        }
    }

    protected void stopLocationUpdates() {
        Log.i("CW", "Stopping location updates");
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        requestingLocations = false;
                    }
                });
    }
}
