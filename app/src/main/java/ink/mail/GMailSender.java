package ink.mail;

import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import ink.callbacks.GeneralCallback;

/**
 * Created by USER on 2016-08-06.
 */
public class GMailSender extends javax.mail.Authenticator {
    private static final String TAG = GMailSender.class.getSimpleName();
    private String mailHost = "smtp.gmail.com";
    private static final String AUTH_EMAIL = "vaentertaiment@gmail.com";
    private static final String AUTH_PASSWORD = "5369615737425";
    private Session session;
    private Thread mNetworkThread;

    static {
        Security.addProvider(new JSSEProvider());
    }

    public GMailSender() {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailHost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getInstance(props, new GMailAuthenticator(AUTH_EMAIL, AUTH_PASSWORD));
    }

    public synchronized void sendMail(final String subject, final String body,
                                      final String sender, final String recipients,
                                      @Nullable final GeneralCallback<String> generalCallback) {
        if (mNetworkThread != null) {
            mNetworkThread = null;
        }
        mNetworkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                try {
                    MimeMessage message = new MimeMessage(session);
                    DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
                    message.setSender(new InternetAddress(sender));
                    message.setSubject(subject);
                    message.setDataHandler(handler);
                    if (recipients.indexOf(',') > 0)
                        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
                    else
                        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
                    javax.mail.Transport.send(message);
                    generalCallback.onSuccess("success");
                } catch (Exception e) {
                    if (generalCallback != null) {
                        generalCallback.onFailure(e.toString());
                    }
                    Log.d(TAG, "sendMail: " + e.toString());
                }
            }
        });
        mNetworkThread.start();
    }

    public class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            if (type == null)
                return "application/octet-stream";
            else
                return type;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
}