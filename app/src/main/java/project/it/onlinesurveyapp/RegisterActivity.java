package project.it.onlinesurveyapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private AwesomeValidation awesomeValidation;
    EditText nameBox, surnameBox, emailBox, passwordBox, confirmPasswordBox, domainBox, portBox;
    Button registerButton;
    private String URL = "";
    private String prefixURL = "http://";
    private String suffixURL = "/OnlineSurveyServices-0.0.1-SNAPSHOT/rest/servizi/registerUser";
    private String domain = "miair12.ddns.net";
    private String separator = ":";
    private String port = "8080";
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";
    private static final String NAME_PATTERN = "^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{1,}$";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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

        nameBox = (EditText) findViewById(R.id.name);
        surnameBox = (EditText) findViewById(R.id.surname);
        emailBox = (EditText) findViewById(R.id.emailBox);
        passwordBox = (EditText) findViewById(R.id.passwordBox);
        domainBox = (EditText) findViewById(R.id.domain);
        portBox = (EditText) findViewById(R.id.port);
        confirmPasswordBox = (EditText) findViewById(R.id.confirmPasswordBox);
        registerButton = (Button) findViewById(R.id.registerButton);

        awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);

        awesomeValidation.addValidation(this, R.id.name, NAME_PATTERN, R.string.nameerror);
        awesomeValidation.addValidation(this, R.id.surname, NAME_PATTERN, R.string.nameerror);
        awesomeValidation.addValidation(this, R.id.emailBox, Patterns.EMAIL_ADDRESS, R.string.emailerror);
        awesomeValidation.addValidation(this, R.id.passwordBox, PASSWORD_PATTERN, R.string.passworderror);

        registerButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v == registerButton) {

            if (awesomeValidation.validate()) {

                if (!passwordBox.getText().toString().equals(confirmPasswordBox.getText().toString())) {
                    /*Toast.makeText(RegisterActivity.this, "Errore nella fase di Registrazione - Le due password non coincidono", Toast.LENGTH_LONG).show();*/
                    alertDialog("Errore nella fase di Registrazione - Le due password non coincidono");
                } else {
                    final Map<String, String> userParams = new HashMap<>();
                    userParams.put("name", nameBox.getText().toString());
                    userParams.put("surname", surnameBox.getText().toString());
                    userParams.put("email", emailBox.getText().toString());
                    userParams.put("password", passwordBox.getText().toString());
                    JSONObject user = new JSONObject(userParams);

                    if (!domainBox.getText().toString().isEmpty()) {
                        domain = domainBox.getText().toString();
                    }
                    if (!portBox.getText().toString().isEmpty()) {
                        port = portBox.getText().toString();
                    }

                    URL = prefixURL + domain + separator + port + suffixURL;

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, user, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d("register response", response.toString());
                            //200 ok registrato, 404 errore
                            int esito = 0;
                            try {
                                esito = response.getInt("status_code");
                                Log.d("Esiteo registrazione", new String() + esito);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (esito == 200) {
                                Toast.makeText(RegisterActivity.this, "Registrazione avvenuta con Successo", Toast.LENGTH_LONG).show();
                                /*alertDialog("Registrazione avvenuta con Successo");*/

                                nameBox.setText("");
                                surnameBox.setText("");
                                emailBox.setText("");
                                passwordBox.setText("");
                                confirmPasswordBox.setText("");

                                finish();
                            } else {
                                alertDialog("Errore nella fase di Registrazione");
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            alertDialog("Errore Registrazione. La mail utilizzata è già presente!");

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

                    RequestQueue rQueue = Volley.newRequestQueue(RegisterActivity.this);
                    rQueue.add(request);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.getItem(0).setVisible(false);
        menu.getItem(1).setVisible(false);

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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void alertDialog(String message) {
        DialogFragment dialog = new CustomDialogFragment();
        Bundle parameters = new Bundle();
        parameters.putString("message", message);
        dialog.setArguments(parameters);
        dialog.show(getSupportFragmentManager(), "WrongLogin");
    }
}