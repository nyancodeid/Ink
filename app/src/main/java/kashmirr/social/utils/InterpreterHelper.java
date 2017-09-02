package kashmirr.social.utils;

import android.content.Context;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * Created by USER on 2017-03-24.
 */

public class InterpreterHelper {
    private Context context;
    private Interpreter interpreterExecutor;
    public static final String DEFAULT_CODE_SAMPLE = "import ink.va.activities;" +
            "import android.content.Intent;" +
            "import android.content.*;" +
            "context.startActivity(new android.content.Intent(context, ink.va.activities.MyProfile.class));";

    public InterpreterHelper(Context context) {
        this.context = context;
    }

    public void evaluateCode(String method) {
        interpreterExecutor = new Interpreter();
        try {
            interpreterExecutor.set("context", context);
            interpreterExecutor.eval(method);
        } catch (EvalError evalError) {
            evalError.printStackTrace();
        }
    }
}
