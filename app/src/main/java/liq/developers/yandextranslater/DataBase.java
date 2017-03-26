package liq.developers.yandextranslater;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


import static liq.developers.yandextranslater.fragment_favourites.mapFav;


/**
 * Created by Michael on 23.03.2017.
 */

class DataBase {  //SharedPreferences не давал сохранять неуникальные ключи, пришлось использовать SQL

     private ContentValues cv = null;
    private static SQLiteDatabase db = null;
    private static DBHelper dbHelper;
    private static String LOG_TAG = "myLogs";

    private LinkedHashMap<Integer, Map<String, String>> dbMap; //Int - ID элемента,
                                                                // Map - исходный и переведенный текста
    private String tableName;

    DataBase(Activity a, String tableName)
    {

        this.tableName = tableName;
        dbHelper = new DBHelper(a);
        dbMap = new LinkedHashMap<>();
    }


    void reload() {

        cv = new ContentValues(); // создание объект для данных
        db = dbHelper.getWritableDatabase(); // подключение к БД

        dbMap.clear();
        Log.d(LOG_TAG, "--- Rows in " + tableName + ": ---");
        //  запрос всех данных из таблицы, получаем Cursor
        Cursor c = db.query(tableName, null, null, null, null, null, null);

        // позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {

            // определяем номера столбцов по имени в выборке
            int idColIndex = c.getColumnIndex("id");
            int originalText = c.getColumnIndex("originalText");
            int translatedText = c.getColumnIndex("translatedText");

            do {
                // получаем значения по номерам столбцов и пишем все в лог
                Log.d(LOG_TAG,
                        "ID = " + c.getInt(idColIndex) +
                                ", originalText = " + c.getString(originalText) +
                                ", translatedText = " + c.getString(translatedText));
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла


                Map<String,String> tmp = new HashMap<>();

                tmp.put("originalText", c.getString(originalText));
                tmp.put("translatedText", c.getString(translatedText));
                dbMap.put(c.getInt(idColIndex), tmp);

            } while (c.moveToNext());
        } else
            Log.d(LOG_TAG, "0 rows");
        c.close();

        dbHelper.close();
    }

    public void add (String originalText, String translatedText){

        cv = new ContentValues(); // создание объекта для данных
        db = dbHelper.getWritableDatabase(); // подключение к БД


        Log.d(LOG_TAG, "--- Insert in "  + tableName + ": ---");
        // данные для вставки в виде пар: наименование столбца - значение
        cv.put("originalText", originalText);
        cv.put("translatedText", translatedText);
        // вставляем запись и получаем ее ID
        long rowID = db.insert(tableName, null, cv);
        Log.d(LOG_TAG, "row inserted, ID = " + rowID);

        dbHelper.close();
        reload();
    }

    void delete(String id){

        if (!id.equalsIgnoreCase("")) {

            cv = new ContentValues(); // создание объекта для данных
            db = dbHelper.getWritableDatabase(); // подключение к БД

            Log.d(LOG_TAG, "--- Delete from " + " " + tableName + ": ---");
            // удаляем по id
            int delCount = db.delete(tableName, "id = " + id, null);
            Log.d(LOG_TAG, "deleted rows count = " + delCount);
            mapFav.remove(id);

            dbHelper.close();
            reload();
        }

    }

    void clear()
    {
        cv = new ContentValues(); // создание объекта для данных
        db = dbHelper.getWritableDatabase(); // подключение к БД

        Log.d(LOG_TAG, "--- Clear " + tableName + ": ---");
        // удаляем все записи
        int clearCount = db.delete(tableName, null, null);
        Log.d(LOG_TAG, "deleted rows count = " + clearCount);
        dbMap.clear();

        dbHelper.close();

    }

    //Вызывается из фрагментов для обновления ListView
    LinkedHashMap<Integer, Map<String, String>> getDbMap(){
        return dbMap;
    }


}
