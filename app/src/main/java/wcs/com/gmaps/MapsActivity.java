package wcs.com.gmaps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final int REQUEST_COARSE_ACCESS = 100;
    private static LatLngBounds BORDEAUX_GPS = new LatLngBounds(new LatLng(44.789078f, -0.651964f),
            new LatLng(44.870402f, -0.506396f));

    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_maps);
        checkPermission();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if(mapFragment != null)
            mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    private void checkPermission() {

        // vérification de l'autorisation d'accéder à la position GPS
        if (ContextCompat.checkSelfPermission(this,
                ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ACCESS_COARSE_LOCATION}, REQUEST_COARSE_ACCESS );
        } else {
            // TODO : autorisation déjà acceptée, on peut faire une action ici
            initLocation();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_ACCESS: {
                // cas de notre demande d'autorisation
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO : l'autorisation a été donnée, nous pouvons agir
                    initLocation();
                } else {
                    Toast.makeText( getApplicationContext(), "GPS access denied", Toast.LENGTH_LONG ).show();
                    // l'autorisation a été refusée :(
                }
            }
        }
    }


    private void initLocation() {

        LocationManager mLocationManager = (LocationManager) this.getSystemService( Context.LOCATION_SERVICE );
        boolean isGpsEnabled = mLocationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER );

        String myProvider = LocationManager.PASSIVE_PROVIDER;
        if(isGpsEnabled) {
            myProvider = LocationManager.GPS_PROVIDER;
        } else if(isNetworkEnabled) {
            myProvider = LocationManager.NETWORK_PROVIDER;
        }

        final String providerEnabled = myProvider;

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                moveCameraToLocation( new LatLng( location.getLatitude(), location.getLongitude() ) );
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        if (ContextCompat.checkSelfPermission( this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mLocationManager.requestLocationUpdates( providerEnabled, 0, 0, locationListener );
        }

    }

    private void moveCameraToLocation(LatLng position) {
        if(mMap != null) {
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom( position, 5 );
            mMap.animateCamera( yourLocation );
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission( this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled( true );
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMaxZoomPreference( 15f );
        mMap.setMinZoomPreference( 12f );
        mMap.setLatLngBoundsForCameraTarget(BORDEAUX_GPS);
/*
        Location myLocation = mMap.getMyLocation();
        if(myLocation != null)
            moveCameraToLocation(new LatLng( myLocation.getLatitude(), myLocation.getLongitude()));
*/
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng));
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Uri lUri = Uri.parse("google.navigation:q=" + marker.getPosition().latitude
                        + ", " + marker.getPosition().longitude + "&mode=b");
                Intent gMapsIntent = new Intent(Intent.ACTION_VIEW, lUri);
                gMapsIntent.setPackage("com.google.android.apps.maps");
                startActivity(gMapsIntent);
                return true;
            }
        });
    }

}