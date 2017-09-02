package kashmirr.social.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * Created by USER on 2016-08-06.
 */
public class GMailAuthenticator extends Authenticator {
    String user;
    String pw;

    public GMailAuthenticator(String username, String password) {
        super();
        this.user = username;
        this.pw = password;
    }

    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, pw);
    }
}