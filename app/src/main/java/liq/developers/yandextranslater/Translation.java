package liq.developers.yandextranslater;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Michael on 16.03.2017.
 */

public class Translation {

    static String apiKey = "trnsl.1.1.20170315T155527Z.2f0d19260d937ff1.8ff0c419c00e818c7aca3a60f599d83716898fb4";

    /*
    собственно перевод
     */

    public static String translateText(String lang, String text) throws IOException, ParseException {

        String requestUrl = "https://translate.yandex.net/api/v1.5/tr.json/translate?key="
                + apiKey + "&lang=" + lang + "&text=" + text;

        URL url = new URL(requestUrl);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.connect();
        int rc = httpConnection.getResponseCode();

        if (rc == 200) {
            String line;
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            StringBuilder strBuilder = new StringBuilder();
            while ((line = buffReader.readLine()) != null) {
                strBuilder.append(line + '\n');
            }

            return getTextFromJson(strBuilder.toString());
        }
        return "Error: " + rc;
    }

    public static String getTextFromJson(String str) throws ParseException { //парсинг
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(str);
        StringBuilder sb = new StringBuilder();
        JSONArray array = (JSONArray) object.get("text");
        for (Object s : array) {
            sb.append(s.toString());
        }
        return sb.toString();
    }

    /*
    определение языка
     */

    public static String getLang(String text, String hint) throws IOException, ParseException    {

        String requestUrl = "https://translate.yandex.net/api/v1.5/tr.json/detect?key="
                + apiKey + "&text=" + text + "&hint=" + hint;

        URL url = new URL(requestUrl);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.connect();
        int rc = httpConnection.getResponseCode();

        if (rc == 200) {
            String line;
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
            StringBuilder strBuilder = new StringBuilder();
            while ((line = buffReader.readLine()) != null) {
                strBuilder.append(line + '\n');
            }

            return getLangFromJson(strBuilder.toString());
        }
        return "error " +  rc;
    }

    public static String getLangFromJson(String str) throws ParseException {

        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(str);
        String s = String.valueOf(object.get("lang"));
        return s;
    }

}