package com.example.pizzasplit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;

import java.net.MalformedURLException;
import java.security.Provider;


public class NewPizzaActivity extends Activity {

    LocationManager locationManager;

    Location loc;

    public static final String SHAREDPREFFILE = "temp";
    public static final String USERIDPREF = "uid";
    public static final String TOKENPREF = "tkn";

    GoogleApiClient mGoogleApiClient;

    EditText brand;
    EditText type;
    EditText time;

    /**
     * Mobile Service Client reference
     */
    private MobileServiceClient mClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pizza);

        brand = (EditText)findViewById(R.id.brand);
        type = (EditText)findViewById(R.id.type);
        time = (EditText)findViewById(R.id.time);

        try {
            // Create the Mobile Service Client instance, using the provided
            // Mobile Service URL and key
            mClient = new MobileServiceClient(
                    "https://pizzasplit.azure-mobile.net/",
                    "ibHcRuGGDApQrcghIlDwwzzdmCYHXn63",
                    this);
            if(loadUserTokenCache(mClient)){
                Log.d("NewPizza", "Token load Success " + mClient.getCurrentUser().getUserId());
            }
            else {
                Log.d("NewPizza", "Token load fail");
                finish();
            }

        }
        catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        }

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);



        Criteria criteria =  new Criteria();
        String provider = LocationManager.NETWORK_PROVIDER;

        locationManager.requestSingleUpdate(provider, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                loc = location;
                if(loc != null){
                    Button submit = (Button)findViewById(R.id.submit);
                    submit.setText("Submit");
                    submit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            MobileServiceTable<PizzaItem> mClientTable = mClient.getTable("Listings", PizzaItem.class);

                            PizzaItem pizzaItem = new PizzaItem();
                            pizzaItem.brand = brand.getText().toString();
                            pizzaItem.type = type.getText().toString();
                            pizzaItem.time = Integer.valueOf(time.getText().toString());
                            pizzaItem.lat = loc.getLatitude();
                            pizzaItem.lon = loc.getLongitude();


                            final ProgressDialog dialog = new ProgressDialog(NewPizzaActivity.this);
                            dialog.setMessage("Listing pizza");
                            dialog.show();
                            mClientTable.insert(pizzaItem, new TableOperationCallback<PizzaItem>() {
                                @Override
                                public void onCompleted(PizzaItem pizzaItem, Exception e, ServiceFilterResponse serviceFilterResponse) {
                                    if(e == null){
                                        Log.d("NewPizza", "inserted successfully");
                                        Toast.makeText(getApplicationContext(), "Listed Successfully!", Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                    else {
                                        Log.d("NewPizza", e.getCause().toString());
                                        Toast.makeText(getApplicationContext(), "You can only have 1 listing at a time!", Toast.LENGTH_LONG).show();
                                    }

                                    dialog.dismiss();
                                }
                            });


                        }
                    });
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        }, null);




    }



    private boolean loadUserTokenCache(MobileServiceClient client)
    {
        SharedPreferences prefs = getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        String userId = prefs.getString(USERIDPREF, "undefined");
        if (userId == "undefined")
            return false;
        String token = prefs.getString(TOKENPREF, "undefined");
        if (token == "undefined")
            return false;

        MobileServiceUser user = new MobileServiceUser(userId);
        user.setAuthenticationToken(token);
        client.setCurrentUser(user);

        return true;
    }


    /**
     * Creates a dialog and shows it
     *
     * @param exception
     *            The exception to show in the dialog
     * @param title
     *            The dialog title
     */
    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if(exception.getCause() != null){
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    /**
     * Creates a dialog and shows it
     *
     * @param message
     *            The dialog message
     * @param title
     *            The dialog title
     */
    private void createAndShowDialog(String message, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_pizza, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
