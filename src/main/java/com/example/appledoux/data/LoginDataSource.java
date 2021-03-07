package com.example.appledoux.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.appledoux.R;
import com.example.appledoux.data.model.LoggedInUser;
import com.example.appledoux.ui.login.LoginActivity;
import com.example.appledoux.ui.login.LoginViewModel;

import java.io.IOException;
import java.net.URLConnection;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.lang.Object;
import java.security.spec.KeySpec;

import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.appledoux.R;
import com.example.appledoux.data.MaBaseSQLite;
import com.example.appledoux.data.Result;
/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    private LoginViewModel loginViewModel;
    private static final int VERSION_BDD = 1;
    private static final String NOM_BDD = "app.db3";
    private static final String TABLE_USER = "table_user";
    private static final String COL_ID = "ID";
    private static final String COL_SALT="SALT";
    private static final int NUM_COL_ID = 0;
    private MaBaseSQLite maBaseSQLite;
    private SQLiteDatabase bdd;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Result<LoggedInUser> login(String username, Context context) {

        try {
            // TODO: handle loggedInUser authentication
            LoggedInUser fakeUser =
                    new LoggedInUser(
                            java.util.UUID.randomUUID().toString(),
                            "");
            maBaseSQLite = new MaBaseSQLite(context,NOM_BDD,null,VERSION_BDD);
            open();
            Cursor c = bdd.query(TABLE_USER,new String[] {COL_ID,COL_SALT},null,null,null,null,null);
            if (c.getCount() == 0)
            {
                throw new Exception();
            }
            c.moveToFirst();
            byte[] salt = c.getBlob(c.getColumnIndex(COL_SALT));
            byte[] hash = c.getBlob(c.getColumnIndex(COL_ID));
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt);
            byte[] calculated = md.digest(username.getBytes(StandardCharsets.UTF_8));
            if (new String(hash).equals(new String(calculated)))
            {

            }
            else
            {
                throw new Exception();
            }
            c.close();

            return new Result.Success<>(fakeUser);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
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
}