package com.example.pizzasplit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.ApiOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableDeleteCallback;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;
import com.microsoft.windowsazure.mobileservices.UserAuthenticationCallback;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;


public class PizzaListActivity extends ActionBarActivity {

    public static final String SHAREDPREFFILE = "temp";
    public static final String USERIDPREF = "uid";
    public static final String TOKENPREF = "tkn";

    LocationManager locationManager;
    String provider;
    ProgressDialog dialog;

    /**
     * Mobile Service Client reference
     */
    private MobileServiceClient mClient;

    private MobileServiceTable<PizzaItem> table;

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pizza_list);

        try {
            // Create the Mobile Service Client instance, using the provided
            // Mobile Service URL and key
            mClient = new MobileServiceClient(
                    "https://pizzasplit.azure-mobile.net/",
                    "ibHcRuGGDApQrcghIlDwwzzdmCYHXn63",
                    this);

            table = mClient.getTable("Listings", PizzaItem.class);




            listView = (ListView)findViewById(R.id.listings);


            TextView textView = new TextView(getApplicationContext());
            textView.setText("There is nothing here :(");
            listView.setEmptyView(textView);


            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    //TODO

                    final PizzaItem item = (PizzaItem)listView.getAdapter().getItem(i);




                    final EditText input = new EditText(getApplicationContext());
                    input.setBackgroundColor(Color.WHITE);
                    input.setTextColor(Color.BLACK);

                    input.setInputType(InputType.TYPE_CLASS_PHONE);

                    input.setHint("Your phone number");


                    AlertDialog.Builder builder = new AlertDialog.Builder(PizzaListActivity.this);
                    builder.setTitle("Send request to share "+ item.type + " at " + item.brand + " ?" );
                    builder.setView(input);

                    builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mClient.invokeApi("test", new SendPhone(item.userId, input.getText().toString()), Return.class, new ApiOperationCallback<Return>() {
                                @Override
                                public void onCompleted(Return aReturn, Exception e, ServiceFilterResponse serviceFilterResponse) {
                                    if(e == null){
                                        Toast.makeText(getApplicationContext(), "Successfully sent to user!", Toast.LENGTH_LONG).show();
                                        Log.d("PizzaList", item.userId + " " + input.getText().toString());
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    });


                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //do nothing
                        }
                    });

                    builder.create().show();


                }
            });



            if(loadUserTokenCache(mClient)){
                Log.d("PizzaList", "Token load Success " + mClient.getCurrentUser().getUserId());
            }
            else {
                Log.d("PizzaList", "Token load fail");
                finish();
            }


            //TODO add dialog
            dialog = new ProgressDialog(PizzaListActivity.this);
            dialog.setMessage("Loading records");
            dialog.show();

            locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
            Criteria criteria =  new Criteria();
            provider = LocationManager.NETWORK_PROVIDER;

            locationManager.requestSingleUpdate(provider, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    Log.d("PizzaList", location.getLatitude() + " " + location.getLongitude());

                    mClient.invokeApi("pizzaapi", new LatLong(location.getLatitude(), location.getLongitude()), PizzaItemArray.class, new ApiOperationCallback<PizzaItemArray>() {
                        @Override
                        public void onCompleted(PizzaItemArray pizzaItemArray, Exception e, ServiceFilterResponse serviceFilterResponse) {
                            Log.d("PizzaList", pizzaItemArray.list.size() + "");
                            listView.setAdapter(new PizzaItemAdapter(pizzaItemArray.list, getApplicationContext(), mClient.getCurrentUser().getUserId().toString()));
                            dialog.dismiss();
                        }

                    });
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







            //add listing
            Button add = (Button)findViewById(R.id.new_pizza);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent =  new Intent(getApplicationContext(), NewPizzaActivity.class);
                    startActivity(intent);

                }
            });


            //delete stuff
            final Button delete = (Button)findViewById(R.id.delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog = new ProgressDialog(PizzaListActivity.this);
                    dialog.setMessage("Deleting Listing");
                    dialog.show();

                    table.execute(new TableQueryCallback<PizzaItem>() {
                        @Override
                        public void onCompleted(List<PizzaItem> pizzaItems, int i, Exception e, ServiceFilterResponse serviceFilterResponse) {
                            if (e == null && pizzaItems.size() > 0) {
                                table.delete(pizzaItems.get(0).mId, new TableDeleteCallback() {
                                    @Override
                                    public void onCompleted(Exception e, ServiceFilterResponse serviceFilterResponse) {
                                        if (e == null) {
                                            Log.d("PizzaList", "Deleted!");
                                            Toast.makeText(getApplicationContext(), "Deleted!", Toast.LENGTH_LONG).show();
                                        } else {
                                            Log.d("PizzaList", e.getCause().toString());
                                            Toast.makeText(getApplicationContext(), "Error Deleting", Toast.LENGTH_LONG).show();
                                        }
                                        dialog.dismiss();
                                    }
                                });

                            } else {
                                Log.d("PizzaList", pizzaItems.size() + " size");
                                Toast.makeText(getApplicationContext(), "You have no open listings", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }
                        }
                    });
                }
            });

        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        }

    }

    public void refreshList(){

        locationManager.requestSingleUpdate(provider, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Log.d("PizzaList", location.getLatitude() + " " + location.getLongitude());

                mClient.invokeApi("pizzaapi", new LatLong(location.getLatitude(), location.getLongitude()), PizzaItemArray.class, new ApiOperationCallback<PizzaItemArray>() {
                    @Override
                    public void onCompleted(PizzaItemArray pizzaItemArray, Exception e, ServiceFilterResponse serviceFilterResponse) {
                        Log.d("PizzaList", pizzaItemArray.list.size() + "");
                        ((PizzaItemAdapter) listView.getAdapter()).update(pizzaItemArray.list);
                    }

                });
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


    @Override
    public void onPause(){
        super.onPause();

        if ((dialog != null) && dialog.isShowing())
            dialog.dismiss();
        dialog = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pizza_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            refreshList();
        }
        return super.onOptionsItemSelected(item);
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


}
