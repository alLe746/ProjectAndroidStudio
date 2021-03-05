package com.example.appledoux.ui.login;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;

import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.example.appledoux.R;
import com.example.appledoux.data.MaBaseSQLite;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.androidnetworking.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private static final int VERSION_BDD = 1;
    private static final String NOM_BDD = "app.db3";
    private static final String TABLE_USER = "table_user";
    private static final String TABLE_BANK = "table_bank";
    private static final String COL_ACCNAME = "account_name";
    private static final String COL_AMOUNT = "amount";
    private static final String COL_IBAN = "iban";
    private static final String COL_CURREN = "currency";
    private static final String COL_ID = "ID";
    private static final String COL_SALT="SALT";
    private static final int NUM_COL_ID = 0;
    private static String Iduser = "";
    private MaBaseSQLite maBaseSQLite;
    private SQLiteDatabase bdd;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidNetworking.initialize(getApplicationContext());
        //this.deleteDatabase("app.db3");
        maBaseSQLite = new MaBaseSQLite(this,NOM_BDD,null,VERSION_BDD);
        open();
        Cursor c = bdd.query(TABLE_USER,new String[] {COL_ID,COL_SALT},null,null,null,null,null);
        if (c.getCount() == 0)
        {
            setContentView(R.layout.initialisation);
            final EditText IdText = findViewById(R.id.usernameInit);
            final Button registerButton = findViewById(R.id.loginInit);
            loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                    .get(LoginViewModel.class);
            registerButton.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View v) {
                    registerButton.setVisibility(View.VISIBLE);
                    ContentValues values = new ContentValues();

                    SecureRandom random = new SecureRandom();
                    byte[] salt = new byte[16];
                    random.nextBytes(salt);
                    try {
                        MessageDigest md = MessageDigest.getInstance("SHA-512");
                        md.update(salt);
                        Iduser = IdText.getText().toString();
                        byte[] hash = md.digest(IdText.getText().toString().getBytes(StandardCharsets.UTF_8));
                        values.put(COL_SALT,salt);
                        values.put(COL_ID,hash);
                        if (bdd.insert(TABLE_USER,null,values)==-1)
                        {
                            throw new Exception();
                        }
                        affichage();
                    } catch (Exception e) {

                    }
                }
            });
        }
        else
        {
            setContentView(R.layout.activity_login);
            loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                    .get(LoginViewModel.class);

            final EditText usernameEditText = findViewById(R.id.username);
            final Button loginButton = findViewById(R.id.login);
            final ProgressBar loadingProgressBar = findViewById(R.id.loading);

            loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
                @Override
                public void onChanged(@Nullable LoginFormState loginFormState) {
                    if (loginFormState == null) {
                        return;
                    }
                    loginButton.setEnabled(loginFormState.isDataValid());
                    if (loginFormState.getUsernameError() != null) {
                        usernameEditText.setError(getString(loginFormState.getUsernameError()));
                    }
                }
            });

            loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
                @Override
                public void onChanged(@Nullable LoginResult loginResult) {
                    if (loginResult == null) {
                        return;
                    }
                    loadingProgressBar.setVisibility(View.GONE);
                    if (loginResult.getError() != null) {
                        showLoginFailed(loginResult.getError());
                    }
                    if (loginResult.getSuccess() != null) {
                        updateUiWithUser(loginResult.getSuccess());
                        Iduser = usernameEditText.getText().toString();
                        affichage();
                    }
                    setResult(Activity.RESULT_OK);
                    //Complete and destroy login activity once successful
                    //finish();
                }
            });
            TextWatcher afterTextChangedListener = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // ignore
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // ignore
                }

                @Override
                public void afterTextChanged(Editable s) {
                    loginViewModel.loginDataChanged(usernameEditText.getText().toString());
                }
            };
            Context context =this;
            loginButton.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View v) {
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    loginViewModel.login(usernameEditText.getText().toString(),context);
                }
            });
            Button resetButton = findViewById(R.id.button_reset);
            resetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reset(savedInstanceState);
                }
            });
        }
        c.close();
    }
    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        setContentView(R.layout.affichage);
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
    public void open(){
        //on ouvre la BDD en écriture
        bdd = maBaseSQLite.getWritableDatabase();
    }

    public void close(){
        //on ferme l'accès à la BDD
        bdd.close();
    }

    public SQLiteDatabase getBDD(){
        return bdd;
    }

    public void updateAPI()
    {
        //TODO:do it
        AndroidNetworking.get("https://60102f166c21e10017050128.mockapi.io/labbbank/accounts/")
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // do anything with response
                        bdd.execSQL("delete from "+ TABLE_BANK);
                        for (int i = 0; i < response.length(); i++)
                        {
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                if (obj.getString("id").equals(Iduser))
                                {
                                    ContentValues values = new ContentValues();
                                    values.put(COL_ACCNAME,obj.getString("accountName"));
                                    values.put(COL_AMOUNT,obj.getInt("amount"));
                                    values.put(COL_IBAN,obj.getString("iban"));
                                    values.put(COL_CURREN,obj.getString("currency"));
                                    if (bdd.insert(TABLE_BANK,null,values)==-1)
                                    {
                                        return;
                                    }
                                }
                            } catch (JSONException e) {
                                return;
                            }
                        }
                        updateListView();
                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error

                    }
                });

    }
    public void updateListView()
    {
        final TextView view = findViewById(R.id.Listaffichage);
        Cursor c = bdd.query(TABLE_BANK,new String[] {COL_ACCNAME,COL_AMOUNT,COL_CURREN,COL_IBAN},null,null,null,null,null);
        if (c.getCount()==0)
        {

            view.setText("Liste Vide, veuillez remplir en actualisant la liste");

        }
        else
        {
            String retour="";
            while(c.moveToNext())
            {
                int index;

                index = c.getColumnIndexOrThrow(COL_ACCNAME);
                String account_name = c.getString(index);

                index = c.getColumnIndexOrThrow(COL_AMOUNT);
                int amount = c.getInt(index);

                index = c.getColumnIndexOrThrow(COL_IBAN);
                String iban = c.getString(index);
                index = c.getColumnIndexOrThrow(COL_CURREN);
                String currency = c.getString(index);
                retour += account_name+ " (IBAN: " + iban + "): \r\n" + amount + " " + currency + "\r\n";
            }
            view.setText(retour);
        }
    }
    public void affichage()
    {
        setContentView(R.layout.affichage);

        //TODO:check with database
        updateListView();
        final Button updatebutton = findViewById(R.id.buttonupdate);
        updatebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAPI();
            }
        });
    }
    public void reset(Bundle savedInstanceState)
    {
        this.deleteDatabase("app.db3");
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }
    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
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
}