package project.it.onlinesurveyapp;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SurveyActivity extends AppCompatActivity {

    private String URL = "";
    private String prefixURL = "http://";
    private String suffixURL = "/OnlineSurveyServices-0.0.1-SNAPSHOT/rest/servizi/sendSurveyAnswers";
    private String separator = ":";
    //JsonObject survey with a JsonArray of questions
    JSONObject surveyJsonObject = null;
    JSONArray answers = new JSONArray();
    Bundle b = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        /* IF Logout */
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.package.ACTION_LOGOUT");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("onReceive", "Logout in progress");
                finish();
            }
        }, intentFilter);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        //navigation Back
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        Log.d("SurveyActivity", "siamo nella survey activity!");

        //Stampa di prova
        this.b = getIntent().getExtras();
        String survey = b.getString("survey");
        Log.d("Survey richiesta: ", survey);

        try {
            this.surveyJsonObject = new JSONObject(survey);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("SurveyObject:", surveyJsonObject.toString());

        // Create a new Fragment to be placed in the activity layout
        //The first question
        newFragment(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.getItem(0).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_logout:
                logOut();
                return true;
           /* case R.id.help:
                showHelp();
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean("MyBoolean", true);
        savedInstanceState.putDouble("myDouble", 1.9);
        savedInstanceState.putInt("MyInt", 1);
        savedInstanceState.putString("MyString", "Welcome back to Android");
        // etc.
    }

    public void newFragment(int nQuestion) {

        Log.d("QUESTION NUMBER: ", String.format("%s", nQuestion));
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        JSONArray questionJsonArray = null;
        try {
            questionJsonArray = surveyJsonObject.getJSONArray("questions");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("questionJsonArray: ", questionJsonArray.toString());
        Log.d("questionJsonArraySize: ", String.format("%s", questionJsonArray.length()));

        //Question
        SurveyFragment fragment = new SurveyFragment();
        Bundle parameters = new Bundle();
        JSONObject question = null;

        try {
            question = questionJsonArray.getJSONObject(nQuestion - 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //put parameters for Fragment
        try {
            parameters.putString("surveyName", surveyJsonObject.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        parameters.putString("question", question.toString());
        //total number of questions
        parameters.putInt("nTotQuestions", questionJsonArray.length());
        //number of this question
        parameters.putInt("nQuestion", nQuestion);
        //list of answers

        fragment.setArguments(parameters);

        //add
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();

    }

    @SuppressLint("LongLogTag")
    public void sendSurvey() {

        //send survey to the server
        CustomApplication app = (CustomApplication) getApplicationContext();
        final Map<String, String> authParams = new HashMap<>();
        authParams.put("email", app.getEmail());
        authParams.put("password", app.getPassword());
        JSONObject auth = new JSONObject(authParams);

        try {
            auth.put("survey", surveyJsonObject);
            auth.put("answers", answers);
            Log.d("Survey Con risposta da inviare al server", auth.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        URL = prefixURL + app.getDomain() + separator + app.getPort() + suffixURL;

        CustomJsonArrayRequest request = new CustomJsonArrayRequest(Request.Method.POST, URL, auth, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {

                //Come back home with new surveys (because there isn't the survey already done
                Log.d("Dentro a SurveyActivity - caricamento dei sondaggi per la home: ", response.toString());
                /*Intent goToHomePage = new Intent(SurveyActivity.this, HomeActivity.class);*/
                /*Bundle b = new Bundle();
                b.putString("surveys", response.toString());
                goToHomePage.putExtras(b);*/
                /*Toast.makeText(SurveyActivity.this, "Survey inviata correttamente!", Toast.LENGTH_LONG).show();*/
                /*startActivity(goToHomePage);*/
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                        /*Toast.makeText(LoginActivity.this, "Some error occurred -> "+volleyError, Toast.LENGTH_LONG).show();*/
                Toast.makeText(SurveyActivity.this, "Errore invio Survey!", Toast.LENGTH_LONG).show();
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

        RequestQueue rQueue = Volley.newRequestQueue(SurveyActivity.this);
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

        Intent goToLoginPage = new Intent(SurveyActivity.this, LoginActivity.class);
        startActivity(goToLoginPage);
    }
}
