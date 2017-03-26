package liq.developers.yandextranslater;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class YandexApiTests {
    @Test
    public void translation_isCorrect() throws Exception {
        assertEquals("привет", Translation.translateText("en-ru", "hi"));
    }

    @Test
    public void language_determination_isCorrect() throws Exception {
        assertEquals("en", Translation.getLang("Hello,+Yandex+Developper!", "en,ru"));

    }
}