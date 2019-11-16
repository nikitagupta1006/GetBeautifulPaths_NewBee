package com.example.getallpossiblepaths;

import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getallpossiblepaths.Modules.DirectionFinder;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


import com.example.getallpossiblepaths.Modules.DirectionFinder;
import com.example.getallpossiblepaths.Modules.DirectionFinderListener;
import com.example.getallpossiblepaths.Modules.Route;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {

    private GoogleMap mMap;
    private FloatingActionButton btnFindPath;
    private EditText etOrigin;
    private EditText etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private String origin = "";
    private String dest = "";
    private List<Route> routes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setContentView(R.layout.activity_maps);
        Places.initialize(getApplicationContext(), "AIzaSyC8glAUHZbPM1gzikYcGm-wQIX3PS6MMkU");
        PlacesClient placesClient = Places.createClient(this);

        btnFindPath = (FloatingActionButton) findViewById(R.id.btnFindPath);
        etOrigin = (EditText) findViewById(R.id.etOrigin);
        etDestination = (EditText) findViewById(R.id.etDestination);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteFragment.setHint("Enter start location");
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
               origin = "" + place.getId();
                Log.i("Fragment", "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Fragment", "An error occurred: " + status);
            }
        });



        AutocompleteSupportFragment autocompleteFragment2 = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment2);
        autocompleteFragment2.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteFragment2.setHint("Enter destination");
        autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
               dest = ""+ place.getId();
                Log.i("Fragment", "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Fragment", "An error occurred: " + status);
            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });

        showBottomSheetDialog();
    }

    private void sendRequest() {
        String origin = this.origin;
        String destination = this.dest;
        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
    }


    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding directions..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();
        Random rnd = new Random();
        this.routes = routes;

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                    .title(route.endAddress)
                    .position(route.endLocation)));
//
//            float sum = 0;
//            for(int i: route.aqi) {
//                sum = sum + i;
//            }
//            int avg = Math.round(sum / route.aqi.size());
//
//            int color;
//
//            switch(avg){
//                case 0:
//                    color = Color.GREEN;
//                    break;
//                case 1:
//                    color = Color.YELLOW;
//                    break;
//                case 2:
//                    color = getResources().getColor(R.color.orange);
//                    break;
//                case 3:
//                    color = Color.RED;
//                    break;
//                case 4:
//                    color = getResources().getColor(R.color.purple);
//                    break;
//                case 5:
//                    color = getResources().getColor(R.color.maroon);
//                    break;
//                default:
//                    color = Color.GRAY;
//                    break;
//            }


            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).color(Color.argb(255,rnd.nextInt(256),rnd.nextInt(256),rnd.nextInt(256))).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }
    public void showBottomSheetDialog() {
        View view = getLayoutInflater().inflate(R.layout.fragment_bottom_sheet_dialog, null);

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        dialog.show();
    }

    public void showBottomSheetDialogFragment() {
        BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

    //custom ArrayAdapter
//    class CustomArrayAdapter extends ArrayAdapter<Route> {
//
//        private Context context;
//        private List<Route> route;
//
//        //constructor, call on creation
//        public CustomArrayAdapter(Context context, int resource, ArrayList<Route> objects) {
//            super(context, resource, objects);
//
//            this.context = context;
//            this.route = objects;
//        }
//
//        //called when rendering the list
//        public View getView(int position, View convertView, ViewGroup parent) {
//
//            //get the property we are displaying
//            Property property = rentalProperties.get(position);
//
//            //get the inflater and inflate the XML layout for each item
//            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
//            View view = inflater.inflate(R.layout.property_layout, null);
//
//            TextView description = (TextView) view.findViewById(R.id.description);
//            TextView address = (TextView) view.findViewById(R.id.address);
//            ImageView image = (ImageView) view.findViewById(R.id.image);
//
//            //set address and description
//            String completeAddress = property.getStreetNumber() + " " + property.getStreetName() + ", " + property.getSuburb() + ", " + property.getState();
//            address.setText(completeAddress);
//
//            //display trimmed excerpt for description
//            int descriptionLength = property.getDescription().length();
//            if(descriptionLength >= 100){
//                String descriptionTrim = property.getDescription().substring(0, 100) + "...";
//                description.setText(descriptionTrim);
//            }else{
//                description.setText(property.getDescription());
//            }
//
//            //set price and rental attributes
//            price.setText("$" + String.valueOf(property.getPrice()));
//            bedroom.setText("Bed: " + String.valueOf(property.getBedrooms()));
//            bathroom.setText("Bath: " + String.valueOf(property.getBathrooms()));
//            carspot.setText("Car: " + String.valueOf(property.getCarspots()));
//
//            //get the image associated with this property
//            int imageID = context.getResources().getIdentifier(property.getImage(), "drawable", context.getPackageName());
//            image.setImageResource(imageID);
//
//            return view;
//        }
//    }
}

