package com.openpositioning.PositionMe.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.openpositioning.PositionMe.R;
import com.openpositioning.PositionMe.sensors.SensorFusion;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass. The startLocation fragment is displayed before the trajectory
 * recording starts. This fragment displays a map in which the user can adjust their location to
 * correct the PDR when it is complete
 *
 * @see HomeFragment the previous fragment in the nav graph.
 * @see RecordingFragment the next fragment in the nav graph.
 * @see SensorFusion the class containing sensors and recording.
 *
 * @author Virginia Cangelosi
 */

public class StartLocationFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener {

    // UI components and Google Map variables
    private Button button;
    private SensorFusion sensorFusion = SensorFusion.getInstance();
    private LatLng position;
    private float[] startPosition = new float[2];
    private float zoom = 19f;
    private GoogleMap mMap;
    // Define the Overlay of two buildings
    private GroundOverlay nucleusOverlay, libraryOverlay;
    // Building Bounds Setting
    private LatLngBounds nucleusBounds = new LatLngBounds(new LatLng(55.922819, -3.174790),
                                                          new LatLng(55.923329, -3.173853));
    private LatLngBounds libraryBounds = new LatLngBounds(new LatLng(55.922732, -3.175183),
                                                          new LatLng(55.923065, -3.174770));
    private Polyline currentRoute; // Record user route data
    // Building Outline setting
    private PolylineOptions nucleusOutline = new PolylineOptions().width(10).color(Color.YELLOW)
            .add(new LatLng(55.92332354102184, -3.1738705511188154))
            .add(new LatLng(55.92287869584856, -3.1738625044921753))
            .add(new LatLng(55.922774998090816, -3.1741092677073977))
            .add(new LatLng(55.92278551816513, -3.174712764705434))
            .add(new LatLng(55.923339118216184, -3.174692827506061))
            .add(new LatLng(55.92332354102184, -3.1738705511188154));
    private PolylineOptions libraryOutline = new PolylineOptions().width(10).color(Color.YELLOW)
            .add(new LatLng(55.92306157485554, -3.174795716624855))
            .add(new LatLng(55.92277404582641, -3.174785620964174))
            .add(new LatLng(55.922788288111725, -3.175195707610495))
            .add(new LatLng(55.923058912559874, -3.1751972187251285))
            .add(new LatLng(55.92306157485554, -3.174795716624855));
    // GPS Location setting
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    // Floor map parameter
    private int selectedFloor = 1;
    private int currentFloor;
    private String currentBuilding = ""; // "library", "nucleus", or ""
    private Button btnSelectFloorMap; // Button for floor map selection
    // Bounds for the nucleus and library buildings to define areas on the map
    private final CharSequence[] LIBRARY_FLOOR_ITEMS = {"","Ground Floor", "First Floor", "Second Floor", "Third Floor"};
    private final CharSequence[] NUCLEUS_FLOOR_ITEMS = {"LG Floor","Ground Floor", "First Floor", "Second Floor", "Third Floor"};
    // Auto change floor map parameter
    private boolean isAutoFloorMapEnabled = false; // Flag for auto-updating the floor map based on elevation
    private Handler elevationUpdateHandler;
    private Runnable elevationUpdateTask;
    private TextView elevation;
    private int elevationOffSet = 80; // The Default elevation data is shift by this value, Change this if it is not fit your device
    private boolean isTracking = false; // Flag indicating if user route tracking is active
    private ArrayList<LatLng> routePoints = new ArrayList<>(); // Stores points along the user's route
    private Button startButton;
    private float elevationVal;

    public StartLocationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        View rootView = inflater.inflate(R.layout.fragment_startlocation, container, false);


        startPosition = sensorFusion.getGNSSLatitude(false);
        zoom = startPosition[0] == 0 && startPosition[1] == 0 ? 1f : 19f;

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.startMap);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                setupMap(googleMap);
            }
        });
        return rootView;
    }

    // Initialize location services and start recording location data
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorFusion.startRecording();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        createLocationRequest();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // Check the Auto Map Switch
                    if (isAutoFloorMapEnabled) {
                        selectedFloor = determineFloor(elevationVal);
                    }

                    // Check if the current position is inside the boundary
                    LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                    manageOverlays(currentPosition ,selectedFloor);

                    if (isTracking) { // Check if tracking is enabled
                        LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());
                        routePoints.add(newPoint);
                        updateMapRoute(); // Update the map with the new route
                    }
                }
            }
        };

        elevationUpdateHandler = new Handler();
        elevationUpdateTask = new Runnable() {
            @Override
            public void run() {
                updateElevationData(); // Your method to update elevation
                elevationUpdateHandler.postDelayed(this, 1000); // Schedule this task again after 500ms
            }
        };
    }

    // Periodic request the position data
    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(100);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    // Updates or creates the polyline on the map based on the user's route
    private void updateMapRoute() {
        if (currentRoute == null) {
            PolylineOptions polylineOptions = new PolylineOptions().addAll(routePoints).width(5).color(Color.RED);
            currentRoute = mMap.addPolyline(polylineOptions);
        } else {
            currentRoute.setPoints(routePoints);
        }
    }

    // Determines the floor number based on elevation data
    private int determineFloor(float elevation) {
        // Assuming ground floor starts at 0 meters and each floor is 3 meters high
        return (int) Math.floor((elevation + elevationOffSet) / 5); // This will floor the division result to get the current floor
    }


    private static final CharSequence[] MAP_TYPE_ITEMS =
            {"Road Map", "Hybrid", "Satellite", "Terrain"};

    private void showMapTypeSelectorDialog() {
        // Prepare the dialog by setting up a Builder.
        final String fDialogTitle = "Select Map Type";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(fDialogTitle);

        // Find the current map type to pre-check the item representing the current state.
        int checkItem = mMap.getMapType();

        // Add an OnClickListener to the dialog, so that the selection will be handled.
        builder.setSingleChoiceItems(
                MAP_TYPE_ITEMS,
                checkItem,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        // Perform an action depending on which item was selected.
                        switch (item) {
                            case 0: // "Road Map"
                                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                break;
                            case 1: // "Hybrid"
                                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                break;
                            case 2: // "Satellite"
                                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                break;
                            case 3: // "Terrain"
                                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                break;
                            default:
                                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        }
                        dialog.dismiss();
                    }
                }
        );

        // Build the dialog and show it.
        AlertDialog fMapTypeDialog = builder.create();
        fMapTypeDialog.setCanceledOnTouchOutside(true);
        fMapTypeDialog.show();
    }

    // Start location updates and elevation data updates when the fragment is resumed
    @Override
    public void onResume() {
        super.onResume();
        startLocationUpdates();
        elevationUpdateHandler.post(elevationUpdateTask); // Start elevation data updates

    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
        elevationUpdateHandler.removeCallbacks(elevationUpdateTask); // Stop elevation data updates

    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @SuppressLint("MissingPermission")
    private void setupMap(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.addPolyline(nucleusOutline);
        mMap.addPolyline(libraryOutline);
        enableMapUIControls();

        position = new LatLng(startPosition[0], startPosition[1]);
        addStartPositionMarker();
        //mMap.setOnMarkerDragListener(new MarkerDragListener());
    }

    private void enableMapUIControls() {
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
    }

    private void addStartPositionMarker() {
        mMap.addMarker(new MarkerOptions().position(position).title("Start Position")).setDraggable(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
    }

    /**
     * Dynamically manages map overlays for the nucleus and library buildings based on the user's current position and selected floor.
     * This method checks if the user's current position is within the predefined bounds of either building. If so, it updates or sets
     * the ground overlay to reflect the correct floor map. If the user moves out of a building's bounds, it removes the respective overlay.
     *
     * @param currentPosition The current geographic coordinates of the user.
     * @param floors The floor number selected by the user or determined by elevation data.
     */
    private void manageOverlays(LatLng currentPosition, int floors) {
        boolean isUserInside = false; // Flag to check if the user is inside any building boundary

        // Check if the user's current position is within the nucleus building bounds.
        if (nucleusBounds.contains(currentPosition)) {
            isUserInside = true;
            currentBuilding = "nucleus";
            // Check if an overlay needs to be updated (either because it doesn't exist or the floor has changed).
            if (nucleusOverlay == null || floors != currentFloor) {
                // If an overlay already exists, remove it to update with the new floor map.
                if (nucleusOverlay != null) {
                    nucleusOverlay.remove(); // Remove the existing overlay
                }
                // Add the new overlay for the current floor
                nucleusOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                        .image(BitmapDescriptorFactory.fromResource(getNucleusFloorResourceId(floors)))
                        .positionFromBounds(nucleusBounds));
                currentFloor = floors; // Update the current floor tracker.
            }
        } else {
            removeOverlayIfPresent("nucleus");
        }

        // Similar logic applied for the library building.
        if (libraryBounds.contains(currentPosition)) {
            isUserInside = true;
            currentBuilding = "library";

            // Check if an overlay needs to be updated (either because it doesn't exist or the floor has changed).
            if (libraryOverlay == null || floors != currentFloor) {
                // If an overlay already exists, remove it to update with the new floor map.
                if (libraryOverlay != null) {
                    libraryOverlay.remove(); // Remove the existing overlay
                }
                // Add the new overlay for the current floor
                libraryOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                        .image(BitmapDescriptorFactory.fromResource(getLibraryFloorResourceId(floors-1)))
                        .positionFromBounds(libraryBounds));
                currentFloor = floors; // Update the current floor tracker.
            }
        } else {
            removeOverlayIfPresent("library");
        }

        // Update the visibility of the btnSelectFloorMap based on user location
        if (btnSelectFloorMap != null) {
            btnSelectFloorMap.setVisibility(isUserInside ? View.VISIBLE : View.GONE);
        }
    }

    // Resource ID mapping for library floors
    private int getLibraryFloorResourceId(int floor) {
        switch (floor) {
            case 0: return R.drawable.libraryg;
            case 1: return R.drawable.library1;
            case 2: return R.drawable.library2;
            case 3: return R.drawable.library3;
            default: return R.drawable.libraryg;// Invalid floor
        }
    }

    // Resource ID mapping for nucleus floors
    private int getNucleusFloorResourceId(int floor) {
        switch (floor) {
            case 0: return R.drawable.nucleuslg;
            case 1: return R.drawable.nucleusg;
            case 2: return R.drawable.nucleus1;
            case 3: return R.drawable.nucleus2;
            case 4: return R.drawable.nucleus3;
            default: return R.drawable.nucleusg; // Invalid floor
        }
    }

    private void removeOverlayIfPresent(String overlayType) {
        if ("nucleus".equals(overlayType) && nucleusOverlay != null) {
            nucleusOverlay.remove();
            nucleusOverlay = null;
            currentBuilding = null; // "library", "nucleus", or ""

        } else if ("library".equals(overlayType) && libraryOverlay != null) {
            libraryOverlay.remove();
            libraryOverlay = null;
            currentBuilding = null;
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    private void updateElevationData() {
        elevationVal = sensorFusion.getElevation(); // Assuming getElevation() returns a float
        elevation.setText(getString(R.string.elevation, String.format("%.1f", elevationVal)));

    }


    /**class MarkerDragListener implements GoogleMap.OnMarkerDragListener {
        @Override
        public void onMarkerDragStart(Marker marker) {}

        @Override
        public void onMarkerDragEnd(Marker marker) {
            startPosition[0] = (float) marker.getPosition().latitude;
            startPosition[1] = (float) marker.getPosition().longitude;
        }

        @Override
        public void onMarkerDrag(Marker marker) {}
    }**/

    // Initialize UI components and set up event listeners
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        button = view.findViewById(R.id.startLocationDone);
        button.setOnClickListener(v -> navigateToRecordingFragment(v));

        button = view.findViewById(R.id.startLocationDone);
        button.setOnClickListener(v -> navigateToRecordingFragment(v));

        Switch switchAutoFloorMap = view.findViewById(R.id.switchAutoFloorMap);
        switchAutoFloorMap.setChecked(isAutoFloorMapEnabled); // Set the switch to reflect the initial state of auto floor map updates
        switchAutoFloorMap.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isAutoFloorMapEnabled = isChecked;
        });

        this.elevation = view.findViewById(R.id.elevationData);

        // Add this part to set up the OnClickListener for your map type button
        Button btnChangeMapType = view.findViewById(R.id.btnChangeMapType);
        btnChangeMapType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMapTypeSelectorDialog();
            }
        });

        btnSelectFloorMap = view.findViewById(R.id.btnSelectFloorMap);
        btnSelectFloorMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFloorMapSelectorDialog();
            }
        });

        this.startButton = view.findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            if (!isTracking) {
                routePoints.clear(); // Clear previous points
                isTracking = true; // Start tracking
                startButton.setText("Stop"); // Change button text to indicate tracking state
            } else {
                isTracking = false; // Stop tracking
                startButton.setText("Start"); // Reset button text
                // Optionally, stop location updates here
            }
        });
    }

    private void navigateToRecordingFragment(View view) {
        sensorFusion.startRecording();
        sensorFusion.setStartGNSSLatitude(startPosition);
        NavDirections action = StartLocationFragmentDirections.actionStartLocationFragmentToRecordingFragment();
        Navigation.findNavController(view).navigate(action);
    }

    private void showFloorMapSelectorDialog() {
        // Prepare the dialog by setting up a Builder.
        final String fDialogTitle = "Select Floor Map";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(fDialogTitle);

        CharSequence[] items = currentBuilding.equals("nucleus") ? NUCLEUS_FLOOR_ITEMS : LIBRARY_FLOOR_ITEMS;
        // Add an OnClickListener to the dialog, so that the selection will be handled.
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Update the selected floor based on user selection
                selectedFloor = item;
                manageOverlays(position, selectedFloor);
                dialog.dismiss();
            }
        });

        // Build the dialog and show it.
        AlertDialog floorMapDialog = builder.create();
        floorMapDialog.setCanceledOnTouchOutside(true);
        floorMapDialog.show();
    }

}
