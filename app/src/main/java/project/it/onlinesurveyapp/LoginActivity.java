package project.it.onlinesurveyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText emailBox, passwordBox, domainBox, portBox;
    private Button loginButton;
    private TextView registerLink;

    private String URL = "";
    private String prefixURL = "http://";
    private String suffixURL = "/OnlineSurveyServices-0.0.1-SNAPSHOT/rest/servizi/allSurveys";
    private String domain = "miair12.ddns.net";
    private String separator = ":";
    private String port = "8080";
    private JSONObject auth = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        /*startDialog("immettere il dominio e la porta del server a cui ci si vuole connettere");*/

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

        emailBox = (EditText)findViewById(R.id.emailBox);
        passwordBox = (EditText)findViewById(R.id.passwordBox);
        domainBox = (EditText)findViewById(R.id.domain);
        portBox = (EditText)findViewById(R.id.port);
        loginButton = (Button)findViewById(R.id.loginButton);
        registerLink = (TextView)findViewById(R.id.registerLink);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Map<String,String> params = new HashMap<>();
                params.put("email", emailBox.getText().toString());
                params.put("password", passwordBox.getText().toString());
                auth = new JSONObject(params);

                Log.d("URL prima", URL);

                if( !domainBox.getText().toString().isEmpty() ) {
                    domain = domainBox.getText().toString();
                }
                if( !portBox.getText().toString().isEmpty() ) {
                    port = portBox.getText().toString();
                }

                CustomApplication app = (CustomApplication) getApplicationContext();
                app.setPort(port);
                app.setDomain(domain);
                URL = prefixURL + domain + separator + port + suffixURL;
                Log.d("URL dopo", URL);

                CustomJsonArrayRequest request = new CustomJsonArrayRequest(Request.Method.POST, URL, auth, new Response.Listener<JSONArray>(){

                    @Override
                    public void onResponse(JSONArray response) {

                        //save user credentials
                        CustomApplication app = (CustomApplication) getApplicationContext();
                        app.setEmail(emailBox.getText().toString());
                        app.setPassword(passwordBox.getText().toString());

                        Intent goToHome = new Intent(LoginActivity.this,HomeActivity.class);
                        Bundle b = new Bundle();
                        b.putString("surveys", response.toString());
                        goToHome.putExtras(b);
                        startActivity(goToHome);
                    }
                },new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        /*Toast.makeText(LoginActivity.this, "User o Password errati!", Toast.LENGTH_LONG).show();*/
                        errorDialog("Email o Password errati!");
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> parameters = new HashMap<String, String>();
                        parameters.put("email", emailBox.getText().toString());
                        parameters.put("password", passwordBox.getText().toString());
                        return parameters;
                    }
                };

                RequestQueue rQueue = Volley.newRequestQueue(LoginActivity.this);
                rQueue.add(request);
            }
        });

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    public void errorDialog(String message) {
        DialogFragment dialog = new CustomDialogFragment();
        Bundle parameters = new Bundle();
        parameters.putString("message", message);
        dialog.setArguments(parameters);
        dialog.show(getSupportFragmentManager(), "WrongLogin");
    }

    public void startDialog(String message) {
        DialogFragment dialog = new CustomDialogFragment();
        Bundle parameters = new Bundle();
        parameters.putString("message", message);
        dialog.setArguments(parameters);
        dialog.show(getSupportFragmentManager(), "WrongLogin");
    }
}