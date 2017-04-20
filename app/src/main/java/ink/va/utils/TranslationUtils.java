package ink.va.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.Nullable;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

/**
 * Created by PC-Comp on 4/20/2017.
 */

public class TranslationUtils {

    public static void Translate(final String sourceText, final String fromLanguage, final String toLanguage, @Nullable final TranslationCallback translationCallback) {
        Thread workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Translate translate = TranslateOptions.newBuilder().setApiKey(Constants.GOOGLE_API_KEY).build().getService();

                try {
                    final Translation translation = translate.translate(sourceText, Translate.TranslateOption.sourceLanguage(fromLanguage),
                            Translate.TranslateOption.targetLanguage(toLanguage));

                    if (translationCallback != null) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                translationCallback.onTranslationDone(translation);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (translationCallback != null) {
                        translationCallback.onTranslationFailed(e);
                    }
                }
            }
        });
        workerThread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        workerThread.start();
        workerThread = null;
    }

    public interface TranslationCallback<T> {
        void onTranslationDone(Translation result);

        void onTranslationFailed(Exception e);
    }

}
