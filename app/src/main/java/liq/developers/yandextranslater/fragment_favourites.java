package liq.developers.yandextranslater;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Created by Michael on 23.03.2017.
 */

public class fragment_favourites extends Fragment {
    public static fragment_favourites newInstance() {
        return new fragment_favourites();
    }

    public fragment_favourites() {
    }


    static SimpleAdapter adapter; //адаптер ListView
    Activity a; //чтобы передать адаптеру
    static LinkedHashMap<Integer, Map<String, String>> mapFav; //для заполнения списка
    static ArrayList<Map<String, String>> arrListFav; //список для заполнения ListView



    Button  clearFavsBtn; //Очистить избранное

    static TextView emptyFavTv; //Вывод сообщения, если список пустой

    static DataBase f; // экземляр класса дб для хранения favourites. Инициализируется
                        // в MainActivity, т.к. должен срабатывать раньше чем местный onCreateView
    static ListView lvFavourites;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_favourites, container, false);
        lvFavourites = (ListView) rootView.findViewById(R.id.lvFavourites);

        emptyFavTv = (TextView) rootView.findViewById(R.id.emptyFavTv);
        a = getActivity();

        arrListFav = new ArrayList<>();

        clearFavsBtn = (Button) rootView.findViewById(R.id.clearFavsBtn);
        clearFavsBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(
                        getActivity());
                alert.setTitle(R.string.confirmDeletionTitle);
                alert.setMessage(R.string.confirmDeletionFavouritesMessage);
                alert.setPositiveButton(R.string.confirmDeletionAnswerYes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        clearAsync clearAsync = new clearAsync();
                        clearAsync.execute();
                        dialog.dismiss();
                    }
                });
                alert.setNegativeButton(R.string.confirmDeletionAnswerNo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }

        });

        lvFavourites.setLongClickable(true);  //Удаление одного элемента по долгому нажатию на него
        lvFavourites.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(final AdapterView<?> parent, final View v, final int position, long id) {
                AlertDialog.Builder alert = new AlertDialog.Builder(
                        getActivity());
                alert.setTitle(R.string.confirmDeletionTitle);
                alert.setMessage(R.string.confirmDeletionRecordMessage);
                alert.setPositiveButton(R.string.confirmDeletionAnswerYes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        TwoLineListItem tlli = (TwoLineListItem)v;
                        TextView topTv = (TextView) tlli.getChildAt(0);
                        final String topText = topTv.getText().toString();

                        TextView botTv = (TextView) tlli.getChildAt(1);
                        final String botText = botTv.getText().toString();


                        deleteAsync deleteAsync = new deleteAsync();
                        deleteAsync.execute(getListItemId(new HashMap<String, String>() {
                            {
                                put("originalText",topText);
                                put("translatedText",botText);
                            }}));

                        dialog.dismiss();

                    }
                });
                alert.setNegativeButton(R.string.confirmDeletionAnswerNo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
                return true;
            }
        });

        reloadAsync reloadAsync = new reloadAsync();
        reloadAsync.execute();

        return rootView;
    }

    public static void updateAdapter(Activity a){ // NotifyDataSetChanged не сработал ¯\_(ツ)_/¯

        lvFavourites.setAdapter(null);
        arrListFav = null;
        arrListFav = new ArrayList<>();

        mapFav = f.getDbMap();

        for (Integer s : mapFav.keySet()) {
            arrListFav.add(mapFav.get(s));
        }
        Collections.reverse(arrListFav); // Чтобы сохранялась хронология

        adapter = new SimpleAdapter(a, arrListFav, android.R.layout.simple_list_item_2,
                new String[]{"originalText", "translatedText"},
                new int[]{android.R.id.text1, android.R.id.text2});

        lvFavourites.setAdapter(adapter);

        if (adapter.isEmpty()) {
            emptyFavTv.setVisibility(View.VISIBLE);
        }
        else {
            emptyFavTv.setVisibility(View.INVISIBLE);
        }



    }

    public String getListItemId(Map<String, String> listitem){  //Получение ид элемента для удаления

        mapFav = f.getDbMap();
        for(Map.Entry<Integer, Map<String, String>> entry : mapFav.entrySet())
        {
            if (entry.getValue().equals(listitem))
                return entry.getKey().toString();
        }
        return "0";
    }


    //Классы для работы с БД в новом потоке

    //Класс addAsync находится в fragment_translate,
    //т.к. добавление происходит отттуда
    private class deleteAsync extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String[] params) {
            try {
                f.delete(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            updateAdapter(a);
        }
    }

    private class clearAsync extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String[] params) {
            try {
                f.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            updateAdapter(a);
        }
    }

    private class reloadAsync extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String[] params) {
            try {
                f.reload();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            updateAdapter(a);
        }
    }





}
