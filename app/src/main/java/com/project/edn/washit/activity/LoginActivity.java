package com.project.edn.washit.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.project.edn.washit.Config;
import com.project.edn.washit.R;
import com.project.edn.washit.api.ServiceHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    private  Button btnlogin;
    private EditText username;
    private EditText password;
    private TextView create;
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btnlogin=(Button)findViewById(R.id.email_sign_in_button);
        username=(EditText)findViewById(R.id.txt_email);
        username.addTextChangedListener(this);
        password=(EditText)findViewById(R.id.txt_password);
        password.addTextChangedListener(this);
        create=(TextView)findViewById(R.id.createacc);
        create.setOnClickListener(this);
        btnlogin.setOnClickListener(this);
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Loading..");
    }

    @Override
    public void onClick(View view) {

        Intent in;
        switch (view.getId()){
            case R.id.createacc:
                in=new Intent(this,RegistrasiActivity.class);
                startActivity(in);

                break;
            case R.id.email_sign_in_button:
                int usernameLenght=username.getText().toString().trim().length();
                int passwordLength=password.getText().toString().trim().length();
                if(usernameLenght==0 || passwordLength==0){
                    if (usernameLenght==0){
                        username.setError("This Field is Required");
                    }else{
                        if (!username.getText().toString().matches(("[A-Za-z~@#$%^&*:;<>.,/}{+ ]"))){
                            username.setError("Invalid Username");
                        }

                    }
                    if (passwordLength==0){
                        password.setError("This Field is Required");
                    }else{
                        if (!password.getText().toString().matches(("[A-Za-z~@#$%^&*:;<>.,/}{+ ]"))){
                            password.setError("Invalid Password");
                        }
                    }
                }else{
                    progressDialog.show();
                    RequestLogin(username.getText().toString().trim(), password.getText().toString().trim());
                }
                break;
            default:
                break;

        }
    }
    private void RequestLogin(final String username, final String password){
        //Buatkan Request Dalam bentuk String
        ServiceHelper.getInstance().login(username,password, FirebaseInstanceId.getInstance().getToken()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                progressDialog.dismiss();
                try {
                    String json=response.body().string();
                    if (LoginActivity.this.Success(json).equalsIgnoreCase("true")) {
                        logintomain(json);
                    }else {
                        Toast.makeText(LoginActivity.this, "Invalid Email Or Password", Toast.LENGTH_LONG).show();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

//
    }
    public void logintomain(String json) {
        progressDialog.dismiss();
        String token = "";
        String name ="";;
        String email ="";;
        String telp = "";;
        try {
            JSONObject json2 = ((JSONObject) new JSONTokener(json).nextValue()).getJSONObject("data");

            token = json2.getString(Config.TOKEN_SHARED_PREF);
            name = json2.getString("name");
            email = json2.getString("email");
            telp = json2.getString("phone");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean(Config.LOGGEDIN_SHARED_PREF, true);
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("telp", telp);

        editor.putString(Config.TOKEN_SHARED_PREF, token);
        editor.commit();
        startActivity(new Intent(this, MainActivity.class));
    }
    public String Success(String json){
        String succes="";
        try {
            String JSON_STRING="{\"response\":"+json+"}";
            JSONObject emp=(new JSONObject(JSON_STRING)).getJSONObject("response");
            succes=emp.getString("success");

        }catch (JSONException e) {
            e.printStackTrace();
        }
        return succes;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (username.getText().toString().trim().length()!=0){
            if (!username.getText().toString().matches("[A-Za-z~@#$%^&*:;<>.,/}{+ ]*")){
                username.setError(null);
            }
        }
        if (password.getText().toString().length()!=0){
            password.setError(null);
        }

    }
}
