package liq.developers.yandextranslater;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;

import static liq.developers.yandextranslater.Translation.getLang;
import static liq.developers.yandextranslater.Translation.translateText;

import static liq.developers.yandextranslater.fragment_favourites.f;
import static liq.developers.yandextranslater.fragment_history.h;

/**
 * Created by Michael on 16.03.2017.
 */

public class fragment_translate extends android.support.v4.app.Fragment {
    public static fragment_translate newInstance() {
        return new fragment_translate();
    }
    public fragment_translate() {}

    String originalTextToSave;
    String translatedTextToSave;
    String langFrom, langInto; // Коды языков для формирования запроса серверу
    String originalText = "";

    Spinner langChooseFrom; //Выбор исходного языка
    Spinner langChooseInto; //Выбор конечного языка
    Integer langChooseFromCurrentPosition; // Нужны для смены текущих языков местами
    Integer langChooseIntoCurrentPosition;

    Button reverseBtn; //Сменить языки местами
    Button btnClear; //Очистить окно ввода
    FloatingActionButton fab; //Красивая кнопочка для добавление в изьранное

    EditText inputText; //Окно ввода
    TextView outputText; //Окно вывода

    boolean initialized = false; // из-за onItemSelect в Spinner при запуске выдавало
                                 // ошибку 400 (в API она не указана)
    View rootView;


    // для вывода тостов используется getActivity(), т.к. фрагменты не являются потомками контекста
    // и getApplicationContext() не сработает


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_translate, container, false);
        reverseBtn = (Button) rootView.findViewById(R.id.reverseBtn);
        btnClear = (Button) rootView.findViewById(R.id.btn_clear);
        inputText = (EditText) rootView.findViewById(R.id.inputText);
        outputText = (TextView) rootView.findViewById(R.id.outputTextView);
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                inputText.setText("");
            }
        });

        reverseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer tmp = langChooseIntoCurrentPosition;
                langChooseInto.setSelection(langChooseFromCurrentPosition);
                langChooseFrom.setSelection(tmp);

            }
        });

        fab.setOnClickListener(new View.OnClickListener() { //Кнопка исчезает через 2с после клика

            Handler handler = new Handler(Looper.getMainLooper() /*UI thread*/);
            Runnable workRunnable;

            @Override
            public void onClick(View view) {

                fab.setImageResource(R.drawable.faviconstaron);
                Toast.makeText(getActivity(), R.string.addedToFavourites, Toast.LENGTH_SHORT).show();
                fab.setClickable(false);

                String from = " (" + langFrom + ")";
                String into = " (" + langInto + ")";
                addFavAsync addFavAsync = new addFavAsync();
                addFavAsync.execute(originalTextToSave.concat(from), translatedTextToSave.concat(into));

                handler.removeCallbacks(workRunnable);
                workRunnable = new Runnable() {

                    @Override
                    public void run() {

                        fab.setVisibility(View.INVISIBLE);
                        fab.setClickable(true);
                        fab.setImageResource(R.drawable.faviconstaroff);

                    }
                };
                handler.postDelayed(workRunnable, 2000 /*delay*/);

               ;
            }
        });

        /*
        С КАКОГО ЯЗЫКА ПЕРЕВОДИТЬ
         */

         langChooseFrom = (Spinner) rootView.findViewById(R.id.langChooseFrom_spinner);

        ArrayAdapter<CharSequence> langChooseFromAdapter = ArrayAdapter
                .createFromResource(getActivity(), R.array.langs_array,
                        android.R.layout.simple_spinner_item);

        langChooseFromAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        langChooseFrom.setSelection(0); // Дефолт - русский
        langChooseFrom.setAdapter(langChooseFromAdapter);

        langChooseFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                langChooseFromCurrentPosition = position;
                langFrom = GetLangCode(parent.getItemAtPosition(position).toString());

                if (!originalText.isEmpty())
                    translate(originalText);

                initialized = true;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {    }
        });


        /*
        НА КАКОЙ ЯЗЫК ПЕРЕВОДИТЬ
         */


        langChooseInto = (Spinner) rootView.findViewById(R.id.langChooseInto_spinner);

        ArrayAdapter<CharSequence> langChooseIntoAdapter = ArrayAdapter
                .createFromResource(getActivity(), R.array.langs_array,
                        android.R.layout.simple_spinner_item);

        langChooseIntoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        langChooseInto.setAdapter(langChooseIntoAdapter);
        langChooseInto.setSelection(1); // Дефолт - английский

        langChooseInto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                langChooseIntoCurrentPosition = position;
                langInto = GetLangCode(parent.getItemAtPosition(position).toString());

                if (!originalText.isEmpty())
                    translate(originalText);
                initialized = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {   }
        });


        TextView t = (TextView) rootView.findViewById(R.id.copyrightsTv); // Ссылка на Янгдекс.Переводчик
        t.setMovementMethod(LinkMovementMethod.getInstance());

        inputText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                originalText = inputText.getText().toString().replaceAll(" ", "+");;
                translate(originalText);
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {    }
            @Override
            public void afterTextChanged(Editable editable) {    }
        });

        // Добавление в историю, а также возможность добавить в избранное происходит через 3 сек,
        // чтобы не добалялся прямо уж каждый символ
        outputText.addTextChangedListener(
                new TextWatcher() {
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    Handler handler = new Handler(Looper.getMainLooper() /*UI thread*/);
                    Runnable workRunnable;

                    @Override public void afterTextChanged(Editable s) {

                       fab.setVisibility(View.INVISIBLE);

                        handler.removeCallbacks(workRunnable);
                        workRunnable = new Runnable() {
                            @Override
                            public void run() {
                                originalTextToSave = inputText.getText().toString();
                                translatedTextToSave = outputText.getText().toString();

                                // условие inputText.hasFocus() чтобы предотвратить лишнюю запись в историю
                                // при срабатывании onTextChanged, когда пользователь в другом фрагменте
                                if (!Objects.equals(originalTextToSave, "") && !Objects.equals(translatedTextToSave, "") && inputText.hasFocus()) {

                                    fab.setVisibility(View.VISIBLE);

                                    String from = " (" + langFrom + ")";
                                    String into = " (" + langInto + ")";
                                    addHistAsync addHistAsync = new addHistAsync();
                                    addHistAsync.execute(originalTextToSave.concat(from), translatedTextToSave.concat(into));
                                    //Toast.makeText(getActivity(), "UPDATED", Toast.LENGTH_SHORT).show();
                                }
                            }
                        };
                        handler.postDelayed(workRunnable, 3000 /*delay*/);
                    }
                }
        );



        // Запрет переноса строки. Яндекс.Переводчик с этим не дружит, как выяснилось
        inputText.setOnKeyListener(new View.OnKeyListener()   {

            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                return (event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER);
            }
        }
        );
        return rootView;
    }

    private void translate(String text) // Собственно перевод (в новом потоке)
    {
        if (!(text == null) && initialized) {
        String from_into = langFrom + "-" + langInto;
        TranslateAsync tr = new TranslateAsync();
        tr.execute(from_into, text);
        }
    }

    private String GetLangCode (String lang)  // Можно было и все запилить, но их так много,
                                              // а главное то общий функционал создать
    {
        switch (lang) {
            case "Английский": return "en";
            case "Русский": return "ru";
            case "Немецкий": return "de";
            case "Китайский": return "zh";
//            case "Определить язык":          //Эта штука работает, но немного коряво, так что в релиз не пойдет
//                GetLangAsync gl = new GetLangAsync();
//                return gl.execute(originalText, "en,ru").toString();
            default: return "ru";
        }
    }

    private class TranslateAsync extends AsyncTask<String, Void, String> { // Новый поток для работы с сетью

        String translatedText;
        @Override
        protected String doInBackground(String[] params) {
            try {
                translatedText = translateText(params[0], params[1]);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
            return translatedText;
        }

        @Override
        protected void onPostExecute(String message) {
            //Toast.makeText(getActivity(), determinedLang, Toast.LENGTH_SHORT).show();
            outputText.setText(translatedText);
        }
    }

    //Не пригодился. Ну и фиг с ним
    private class GetLangAsync extends AsyncTask<String, Void, String> {

        String determinedLang;
        @Override
        protected String doInBackground(String[] params) {
            try {
                if (!params[0].isEmpty())
                determinedLang = getLang(params[0], params[1]);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
            return determinedLang;
        }

        @Override
        protected void onPostExecute(String message) {
            Toast.makeText(getActivity(), determinedLang, Toast.LENGTH_SHORT).show();
            langFrom = determinedLang;
        }
    }


    //Добавление в избранное
    private class addFavAsync extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String[] params) {
            try {
                f.add(params[0], params[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String message) {
           // fragment_favourites.updateAdapter(getActivity());
        }
    }

    //Добавление в историю
    private class addHistAsync extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String[] params) {
            try {
                h.add(params[0], params[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String message) {
             fragment_history.updateAdapter(getActivity());
        }
    }
}
