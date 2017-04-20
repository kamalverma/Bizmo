package com.logicnext.bizmo;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity implements LocationSource
{
	GoogleMap map;
	
	TextView mAddress;
	TextView btnSahre;
	
	String mAddressString;
	
	double lat, lng;
	boolean isViewMode;
	LatLng latlng;
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        
	        setContentView(R.layout.map_activity);
	        
	        isViewMode= getIntent().getExtras().getBoolean("isView");
	        if(isViewMode)
	        {
	        	lat= getIntent().getExtras().getDouble("lat");
	        	lng= getIntent().getExtras().getDouble("lng");
	        }
	        

	        
	        mAddress= (TextView)findViewById(R.id.tvAddress);
	        btnSahre= (TextView)findViewById(R.id.tvShare);
	        
	        if(isViewMode)
	        {
	        	mAddress.setVisibility(View.GONE);
	        	btnSahre.setVisibility(View.GONE);
	        }
	        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	        setUpMapIfNeeded();
	        
	        btnSahre.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent result= new Intent();
					result.putExtra("address", mAddressString+"");
					result.putExtra("lat", latlng.latitude);
					result.putExtra("lng", latlng.longitude);
					setResult(RESULT_OK, result);
					finish();
				}
			});
	        
	    
	//     map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//
        map.setMyLocationEnabled(true);
        
        map.moveCamera(CameraUpdateFactory.zoomBy(13));
        map.getUiSettings().setMyLocationButtonEnabled(true);
     // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location
        Location location = locationManager.getLastKnownLocation(provider);

        if(location!=null){
        // Getting latitude of the current location
        double latitude = location.getLatitude();

        // Getting longitude of the current location
        double longitude = location.getLongitude();

         LatLng myPosition = new LatLng(latitude, longitude);

         if(!isViewMode)
        	 map.animateCamera(CameraUpdateFactory.newLatLng(myPosition));
         
         
        map.addMarker(new MarkerOptions().position(myPosition).draggable(true));
        
        
        if(isViewMode)
        {
        	LatLng l= new LatLng(lat, lng);
        	  map.addMarker(new MarkerOptions().position(l).draggable(false));
        	  map.animateCamera(CameraUpdateFactory.newLatLng(l));
        }
        
        if(!isViewMode)
        	new GetAddressTask().execute(myPosition);
        }
        
        map.setOnMarkerDragListener(new OnMarkerDragListener() {
			
			@Override
			public void onMarkerDragStart(Marker arg0) {
				
			}
			
			@Override
			public void onMarkerDragEnd(Marker arg0) {
				
				new GetAddressTask().execute(arg0.getPosition());
			}
			
			@Override
			public void onMarkerDrag(Marker arg0) {
				
			}
		});
	    }
	 
	 private void setUpMapIfNeeded() {
		    // Do a null check to confirm that we have not already instantiated the map.
		    if (map == null) {
		        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
		                            .getMap();
		        // Check if we were successful in obtaining the map.
		        if (map != null) {
		            // The Map is verified. It is now safe to manipulate the map.
		        }
		    }
		}

	@Override
	public void activate(OnLocationChangedListener arg0) {
		
	}

	@Override
	public void deactivate() {
		
	}
	
	 private class GetAddressTask extends
     AsyncTask<LatLng, Void, String> 
	 {

		/**
		  * Get a Geocoder instance, get the latitude and longitude
		  * look up the address, and return it
		  *
		  * @params params One or more Location objects
		  * @return A string containing the address of the current
		  * location, or an empty string if no address can be found,
		  * or an error message
		  */
				 @Override
				 protected String doInBackground(LatLng... params) {
				     Geocoder geocoder =
				             new Geocoder(MapActivity.this, Locale.getDefault());
				     // Get the current location from the input parameter list
				     LatLng loc = params[0];
				     latlng= loc;
				     // Create a list to contain the result address
				     List<Address> addresses = null;
				     try {
				         /*
				          * Return 1 address.
				          */
				         addresses = geocoder.getFromLocation(loc.latitude,
				                 loc.longitude, 1);
				     } 
				     catch (IOException e1) 
				     {
				    	 String errorString =
					    		 loc.latitude+""+
					             " , " +
					             loc.longitude;
					     return errorString;
				     }
				     catch (IllegalArgumentException e2) 
				     {
				     // Error message to post in the log
				    	 String errorString =
					    		 loc.latitude+""+
					             " , " +
					             loc.longitude;
					     return errorString;
				     }
				     // If the reverse geocode returned an address
				     if (addresses != null && addresses.size() > 0) {
				         // Get the first address
				         Address address = addresses.get(0);
				         /*
				          * Format the first line of address (if available),
				          * city, and country name.
				          */
				         String addressText = String.format(
				                 "%s, %s, %s",
				                 // If there's a street address, add it
				                 address.getMaxAddressLineIndex() > 0 ?
				                         address.getAddressLine(0) : "",
				                 // Locality is usually a city
				                 address.getAdminArea(),
				                 // The country of the address
				                 address.getCountryName());
				         // Return the text
				         return addressText;
				     } else {
				    	  String errorString =
						    		 loc.latitude+""+
						             " , " +
						             loc.longitude;
				    	 
				    	 return errorString;
				     }
				 }
				 
				 /**
			         * A method that's called once doInBackground() completes. Turn
			         * off the indeterminate activity indicator and set
			         * the text of the UI element that shows the address. If the
			         * lookup failed, display the error message.
			         */
			        @Override
			        protected void onPostExecute(String address) {
			            // Set activity indicator visibility to "gone"
			            // Display the results of the lookup.
			        	
			        	mAddressString= address;
			            mAddress.setText(address);
			        }
	 }


}

