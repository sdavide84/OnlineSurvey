package project.it.onlinesurveyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class HomeActivity extends AppCompatActivity {

    private JSONObject auth = null;
    private String prefixURL = "http://";
    private String suffixURLAllSurvey = "/OnlineSurveyServices-0.0.1-SNAPSHOT/rest/servizi/allSurveys";
    private String suffixURLOneSurvey = "/OnlineSurveyServices-0.0.1-SNAPSHOT/rest/servizi/oneSurvey";
    private String separator = ":";
    private String URLAllSurvey = "";
    private String URLOneSurvey = "";
    private ListView listview = null;
    private StableArrayAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        /* IF Logout */
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.package.ACTION_LOGOUT");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("onReceive","Logout in progress");
                finish();
            }
        }, intentFilter);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        listview = (ListView) findViewById(R.id.listview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        listview.setAdapter(null);
        reloadSurvey();
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(HomeActivity context, int textViewResourceId, ArrayList<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

    //Menu in Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_logout:
                logOut();
                return true;
            case R.id.action_refresh:
                reloadSurvey();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadSurvey(String surveys) {

        if(surveys!=null) {
           /* Log.d("Dentro a HomeActivity - caricamento dei sondaggi per la home: ", surveys);*/

            ArrayList<String> surveyList = null;

            try {
                JSONArray surveyArray = new JSONArray(surveys);
                Log.d("Array di JSON", surveyArray.toString());

                surveyList = new ArrayList<String>();
                for (int i = 0; i < surveyArray.length(); i++) {
                    surveyList.add(surveyArray.getJSONObject(i).getString("id") + " - " + surveyArray.getJSONObject(i).getString("name"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, surveyList);
            listview.setAdapter((ListAdapter) adapter);

            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                    final String item = (String) parent.getItemAtPosition(position);

                    Log.d("Elemento Selezionato", item);

                    JSONObject requestSurvey = null;
                    final Map<String, String> surveyParams = new HashMap<>();
                    surveyParams.put("id", item.substring(0, item.indexOf('-') - 1));
                    surveyParams.put("name", null);
                    requestSurvey = new JSONObject(surveyParams);

                    CustomApplication app = (CustomApplication) getApplicationContext();
                    final Map<String, String> authParams = new HashMap<>();
                    authParams.put("email", app.getEmail());
                    authParams.put("password", app.getPassword());
                    auth = new JSONObject(authParams);

                    URLOneSurvey = prefixURL + app.getDomain() + separator + app.getPort() + suffixURLOneSurvey;

                    try {
                        auth.put("survey", requestSurvey);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d("Richiesta Sondaggio: ", auth.toString());
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URLOneSurvey, auth, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Intent goToSurveyPage = new Intent(HomeActivity.this, SurveyActivity.class);
                            Bundle b = new Bundle();

                            /*Map<String, byte[]> pics = extractImageFromJson(response);*/
                            Log.d("survey da completare", response.toString());
                            b.putString("survey", response.toString());

                            goToSurveyPage.putExtras(b);

                            Log.d("goToSurvey", "goToSurvey");
                            startActivity(goToSurveyPage);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(HomeActivity.this, "Errore richiesta sondaggio: " + volleyError, Toast.LENGTH_LONG).show();
                            Log.d("Errore richiesta sondaggio:", volleyError.getMessage());
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            CustomApplication app = (CustomApplication) getApplicationContext();
                            Map<String, String> parameters = new HashMap<String, String>();
                            parameters.put("email", app.getEmail());
                            parameters.put("password", app.getPassword());
                            return parameters;
                        }
                    };

                    RequestQueue rQueue = Volley.newRequestQueue(HomeActivity.this);
                    rQueue.add(request);

                }

            });
        }

    }

    private void reloadSurvey() {

        final Map<String,String> params = new HashMap<>();
        CustomApplication app = (CustomApplication) getApplicationContext();
        params.put("email", app.getEmail());
        params.put("password", app.getPassword());

        URLAllSurvey = prefixURL + app.getDomain() + separator + app.getPort() + suffixURLAllSurvey;

        JSONObject authRefresh = new JSONObject(params);
        CustomJsonArrayRequest request = new CustomJsonArrayRequest(Request.Method.POST, URLAllSurvey, authRefresh, new Response.Listener<JSONArray>(){
            @Override
            public void onResponse(JSONArray response) {

                Toast.makeText(HomeActivity.this, "Sondaggi ricaricati!", Toast.LENGTH_LONG).show();
                loadSurvey(response.toString());
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                        /*Toast.makeText(LoginActivity.this, "Some error occurred -> "+volleyError, Toast.LENGTH_LONG).show();*/
                errorDialog("Errore Refresh Sondaggi!");
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(HomeActivity.this);
        rQueue.add(request);
    }

    private void logOut() {
        CustomApplication app = (CustomApplication) getApplicationContext();
        app.setEmail("");
        app.setPassword("");

        /*broadcast to all Activity*/
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.package.ACTION_LOGOUT");
        sendBroadcast(broadcastIntent);

        Intent goToLoginPage = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(goToLoginPage);
    }

    public void errorDialog(String message) {
        DialogFragment dialog = new CustomDialogFragment();
        Bundle parameters = new Bundle();
        parameters.putString("message", message);
        dialog.setArguments(parameters);
        dialog.show(getSupportFragmentManager(), "WrongLogin");
    }

}