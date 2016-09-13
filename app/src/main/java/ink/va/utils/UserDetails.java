package ink.va.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by PC-Comp on 8/4/2016.
 */
public class UserDetails {
    public static List<String> getUserAccountList(Context context) {
        System.gc();
        List<String> accountList = new ArrayList<>();
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = AccountManager.get(context).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                String possibleEmail = account.name;
                accountList.add(possibleEmail);
            }
        }
        return accountList;
    }
}
