package com.example.kiran.googlemapchennai;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private GoogleMap googleMap;
    private Marker googleMarker;
    private Button btOk;
    String url;
    private EditText edFromAddress, edToAddress;
    List<Address> addressLocaion, addressLocationTo;
    Address addressFrom, addressTo;
    HttpURLConnection urlConnection;
//    private ArrayList<LatLng>multipleMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        btOk = (Button) findViewById(R.id.bt_map_ok);
        edFromAddress = (EditText) findViewById(R.id.ed_from_adddress);
        edToAddress = (EditText) findViewById(R.id.ed_to_address);
        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.fg_google_map)).getMap();
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        final LatLng CURRENT_LOCATION = new LatLng(35.21843892856462, 33.41662287712097);
        googleMarker = googleMap.addMarker(new MarkerOptions().position(CURRENT_LOCATION).title("Chennai"));
        try {
            googleMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            Log.e("myLocation", "" + e.getMessage());
        }
        btOk.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        getLatLongFromPlace(edFromAddress, edToAddress);
    }

    private void getLatLongFromPlace(EditText edFromAddress, EditText edToAddress) {
        try {
            Geocoder placeAddres = new Geocoder(this);
            addressLocaion = placeAddres.getFromLocationName(edFromAddress.getText().toString(), 3);
            addressLocationTo = placeAddres.getFromLocationName(edToAddress.getText().toString(), 3);
            addressFrom = addressLocaion.get(0);
            addressTo = addressLocationTo.get(0);
            Log.e("fromLat", "" + addressFrom.getLatitude());
            Log.e("fromLong", "" + addressFrom.getLongitude());
            Log.e("ToLat", "" + addressTo.getLatitude());
            Log.e("ToLong", "" + addressTo.getLongitude());
            ArrayList<LatLng> fromtoLatLong = new ArrayList<>();
            fromtoLatLong.add(new LatLng(addressFrom.getLatitude(), addressFrom.getLongitude()));
            fromtoLatLong.add(new LatLng(addressTo.getLatitude(), addressTo.getLongitude()));
            placeMarker(fromtoLatLong);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void placeMarker(ArrayList<LatLng> fromtoLatLong) {
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(fromtoLatLong.get(0)).title(addressFrom.getLocality()));
        googleMap.addMarker(new MarkerOptions().position(fromtoLatLong.get(1)).title(addressTo.getLocality()));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fromtoLatLong.get(0), 15));
        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(6), 2000, null);
        String str_origin = "origin=" + addressFrom.getLatitude() + "," + addressFrom.getLongitude();
        String str_dest = "destination=" + addressTo.getLatitude() + "," + addressTo.getLongitude();
        String Sensor_enable = "sensor=false";
        String paramters = str_origin + "&" + str_dest + "&" + Sensor_enable;
        String Output = "json";
        url = "https://maps.googleapis.com/maps/api/directions/" + Output + "?" + paramters;
        JsonObjectRequest urlRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("response", "" + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("response", "" + error);
            }
        });
        Volley.newRequestQueue(getApplicationContext()).add(urlRequest);
        urlConnection = null;
        strinurlDownload StingDownload = new strinurlDownload();
        StingDownload.execute(url);

    }

    class strinurlDownload extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String data = "";
            try {
                data = downloadUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ParserTask parseTask = new ParserTask();
            parseTask.execute(s);
        }
    }

    private String downloadUrl(String url) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url_connection = new URL(url);
            urlConnection = (HttpURLConnection) url_connection.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            Log.e("inputStream", "" + urlConnection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            Log.e("data", "" + data);
            br.close();
        } catch (Exception e) {
            Log.d("Exception downloading", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {


        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }


        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            Log.e("result", "" + result.size());
            Log.e("resultname", "" + result);
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();


                List<HashMap<String, String>> path = result.get(i);


                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }


                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);

            }
            googleMap.addPolyline(lineOptions);
        }
    }
}
