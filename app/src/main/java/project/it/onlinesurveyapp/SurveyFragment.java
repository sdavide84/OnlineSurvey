package project.it.onlinesurveyapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SurveyFragment extends Fragment implements View.OnClickListener {

    private String prefixURL = "http://";
    private String separator = ":";
    private int nQuestion = 0;
    private int nTotQuestions = 0;

    private RadioGroup radioGroup;
    private View rootView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_survey, container, false);
        Log.d("rootView: ", rootView.toString() );

        String surveyName = null;
        JSONObject question = null;

        if (getArguments() != null) {
            surveyName = getArguments().getString("surveyName");
            nQuestion = getArguments().getInt("nQuestion");
            nTotQuestions = getArguments().getInt("nTotQuestions");
            try {
                question = new JSONObject(getArguments().getString("question"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Survey Description "name"
        try {
            Log.d("surveyName: ", surveyName);
            setSurveyTitle( surveyName , rootView);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        //Argument
        String argumentValue = null;
        try {
            argumentValue = question.getJSONObject("argument").getString("value");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Argument Description "value"
        try {
            setArgumentTitle( argumentValue , rootView);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        //Question Description "value"
        try {
            setQuestionTitle( question.getString("value") , rootView);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Pic
        try {
            setPic( question.getString("pic") , rootView);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray answers = new JSONArray();
        try {
            answers = question.getJSONArray("answers");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /*Log.d("answers to load on Radio Buttons: ", answers.toString());*/
        //Answers Radio Button "value"
        try {
            setRadioGroupAnswer( answers, rootView);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        Button button = (Button) rootView.findViewById(R.id.GoAhead);
        if(nQuestion < nTotQuestions) {
            button.setText("Avanti");
        } else {
            button.setText("Fine");
        }

        button.setOnClickListener(this);

        return rootView;
    }

    public void setSurveyTitle(String text, View view){
        TextView textView = (TextView) view.findViewById(R.id.survey_title);
        textView.setText(text);
    }

    public void setArgumentTitle(String text, View view){
        TextView textView = (TextView) view.findViewById(R.id.argument_title);
        textView.setText(text);
    }

    public void setQuestionTitle(String text, View view){
        TextView textView = (TextView) view.findViewById(R.id.question_title);
        textView.setText(text);
    }

    public void setPic( String picUrl, View view) {
        ImageView imageView = (ImageView) view.findViewById(R.id.pic);

         if(!picUrl.equalsIgnoreCase("null")) {
             Log.d("Pic url", picUrl);

             CustomApplication app = (CustomApplication) getActivity().getApplicationContext();
             String username = app.getEmail();
             String password = app.getPassword();

             String URL = prefixURL + app.getDomain() + separator + app.getPort() + picUrl;

                     LazyHeaders auth = new LazyHeaders.Builder() // can be cached in a field and reused
                     .addHeader("Authorization", new BasicAuthorization(username, password))
                     .build();
             GlideApp.with(this).load(new GlideUrl(URL, auth)).into(imageView);

        } else {
             imageView.setVisibility(View.GONE);
        }
    }

    public void setRadioGroupAnswer(JSONArray answers, View view){

        radioGroup = (RadioGroup) view.findViewById(R.id.radio_answers);

        for(int j = 0; j<answers.length(); j++) {

            RadioButton rb = new RadioButton(getActivity());

            String answer = null;
            String answer_id = null;
            try {
                answer = answers.getJSONObject(j).getString("value");
                answer_id = answers.getJSONObject(j).getString("id");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            rb.setText(answer);
            rb.setTag(Integer.parseInt(answer_id));

            Log.d("Answer id", answer_id);
            Log.d("Set Tag Radio button", new String() + rb.getTag());

            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            radioGroup.addView(rb, params);
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onClick(View v)
    {
        //TODO save radiobutton
        int selectedId = -100;
        boolean error = false;
        // get selected radio button from radioGroup
        if(radioGroup!= null) {
            selectedId = radioGroup.getCheckedRadioButtonId();
            // find the radiobutton by returned id
            RadioButton radioButton = (RadioButton) rootView.findViewById(selectedId);

            if(radioButton != null ) {

                Log.d("RISPOSTA SELEZIONATA", radioButton.getTag() + " - " + radioButton.getText().toString());
                JSONObject answer = new JSONObject();

                try {
                    answer.put("id", new String() + radioButton.getTag());
                    answer.put("value", radioButton.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ((SurveyActivity) getActivity()).answers.put(answer);
            } else {
                Log.d("RISPOSTA NON SELEZIONATA", "Risposta non selezionata, viene riproposta la stessa domanda!");
                errorDialog("Per favore selezionare una risposta!");
                error = true;
                ((SurveyActivity) getActivity()).newFragment(nQuestion);
            }
        } else {
            Log.d("RADIO GROUP: " , "null");
        }

        if(!error) {
            if (nQuestion < nTotQuestions) {
                Log.d("Pulsante premuto!", "GESTIONE PULSANTI SURVEY");
                ((SurveyActivity) getActivity()).newFragment(nQuestion + 1);
            } else {
                Log.d("Pulsante premuto!", "FINE SURVEY");
                ((SurveyActivity) getActivity()).sendSurvey();
            }
        }
    }

    public void errorDialog(String message) {
        DialogFragment dialog = new CustomDialogFragment();
        Bundle parameters = new Bundle();
        parameters.putString("message", message);
        dialog.setArguments(parameters);
        dialog.show(getFragmentManager(), "WrongAnswer");
    }
}
