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

    public static void Translate(final Object sourceText, final Object fromLanguage, final String toLanguage, @Nullable final TranslationCallback translationCallback) {
        Thread workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Translate translate = TranslateOptions.newBuilder().setApiKey(Constants.GOOGLE_API_KEY).build().getService();

                try {
                    final Translation translation = translate.translate(sourceText.toString(), Translate.TranslateOption.sourceLanguage(fromLanguage.toString()),
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
                } catch (final Exception e) {
                    e.printStackTrace();
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (translationCallback != null) {
                                translationCallback.onTranslationFailed(e);
                            }
                        }
                    });

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
