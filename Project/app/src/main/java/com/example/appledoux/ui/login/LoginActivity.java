package com.example.appledoux.ui.login;

import android.app.Activity;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;

import com.example.appledoux.R;
import com.example.appledoux.data.MaBaseSQLite;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

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
    private MaBaseSQLite maBaseSQLite;
    private SQLiteDatabase bdd;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }
    public void affichage()
    {
        setContentView(R.layout.affichage);
        final RecyclerView view = findViewById(R.id.recyclerViewAccount);
        //TODO:check with database
        Cursor c = bdd.query(TABLE_BANK,new String[] {COL_ACCNAME,COL_AMOUNT,COL_CURREN,COL_IBAN},null,null,null,null,null);
        if (c.getCount()==0)
        {

        }
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
}