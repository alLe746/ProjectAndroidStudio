package com.example.appledoux.data;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class MaBaseSQLite extends SQLiteOpenHelper {

    private static final String TABLE_USER = "table_user";
    private static final String COL_KEY ="Identifiant";
    private static final String COL_SALT ="SALT";
    private static final String COL_ID ="ID";
    private static final String TABLE_BANK = "table_bank";
    private static final String COL_ACCNAME = "account_name";
    private static final String COL_AMOUNT = "amount";
    private static final String COL_IBAN = "iban";
    private static final String COL_CURREN = "currency";

    private static final String CREATE_BDD = "CREATE TABLE " + TABLE_USER + " ("
            + COL_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT,"+ COL_SALT + " BLOB,"+ COL_ID + " BLOB);";
    private static final String CREATE_BDD_ACCOUNT = "CREATE TABLE " + TABLE_BANK + " ("
            + COL_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT,"+ COL_ACCNAME + " TEXT,"+ COL_AMOUNT + " INTEGER,"+COL_IBAN+" TEXT,"+COL_CURREN+" TEXT);";

    public MaBaseSQLite(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);

    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE_BDD);
        db.execSQL(CREATE_BDD_ACCOUNT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + TABLE_USER + ";");
        onCreate(db);
    }

}