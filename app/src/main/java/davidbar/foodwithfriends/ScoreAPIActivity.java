package davidbar.foodwithfriends;

//import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import android.view.View;
//
//public class ScoreAPIActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_score_api);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//    }
//
//}

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.factual.driver.Circle;
import com.factual.driver.Factual;
import com.factual.driver.Query;
import com.factual.driver.ReadResponse;
import com.google.common.collect.Lists;

public class ScoreAPIActivity extends Activity {

    private final String authKey = "Y0NdQGfaA3M05zw0LGtPY2y5lp7CrDklONLSXsa3";
    private final String authSecret = "d08VLmXB9jBCX0wcAb25BUVx3we2FEQiBaqHBKtW";

    protected Factual factual = new Factual(authKey, authSecret);
    private TextView resultText = null;

    private static final String TAG = "ScoreAPIActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_api);
        resultText = (TextView) findViewById(R.id.resultText);
        FactualRetrievalTask task = new FactualRetrievalTask();

        double latitude = 34.06018;
        double longitude = -118.41835;
        int meters = 5000;
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.GPS_PROVIDER;
        Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        Log.d(TAG, "LAT: " + latitude);
        Log.d(TAG, "LON: " + longitude);

        Query query = new Query()
                .within(new Circle(latitude, longitude, meters))
                .field("cuisine").equal("Italian")
                .sortAsc("$distance")
                .only("name", "address", "tel");

        task.execute(query);
    }

    protected class FactualRetrievalTask extends AsyncTask<Query, Integer, List<ReadResponse>> {
        @Override
        protected List<ReadResponse> doInBackground(Query... params) {
            List<ReadResponse> results = Lists.newArrayList();
            for (Query q : params) {
                results.add(factual.fetch("restaurants-us", q));
            }
            return results;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected void onPostExecute(List<ReadResponse> responses) {
            StringBuffer sb = new StringBuffer();
            for (ReadResponse response : responses) {
                for (Map<String, Object> restaurant : response.getData()) {
                    String name = (String) restaurant.get("name");
                    String address = (String) restaurant.get("address");
                    String phone = (String) restaurant.get("tel");
                    Number distance = (Number) restaurant.get("$distance");
                    sb.append(distance + " meters away: "+name+" @ " +address + ", call "+phone);
                    sb.append(System.getProperty("line.separator"));
                }
            }
            resultText.setText(sb.toString());
        }

    }
}