package liq.developers.yandextranslater;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



/**
 * Created by Michael on 23.03.2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    private String tableName1 = "tableHistory"; //Таблицы для истории и избранного
    private String tableName2 = "tableFavourites";

    DBHelper(Context context) {
        // конструктор суперкласса
        super(context, "db", null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String LOG_TAG = "myLogs";

        Log.d(LOG_TAG, "--- onCreate database ---");
        // создаем таблицу с полями
        db.execSQL("create table if not exists " + tableName1 + " ("
                + "id integer primary key autoincrement,"
                + "originalText text,"
                + "translatedText text" + ");");

        db.execSQL("create table if not exists " + tableName2 + " ("
                + "id integer primary key autoincrement,"
                + "originalText text,"
                + "translatedText text" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
