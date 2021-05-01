package it.bridge;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.net.ssl.*;
import java.io.*;
import java.util.Date;
import java.util.Properties;

public class SendMail {

    static String username = "";
    static String password = "";
    static String from_addr = "";
    private boolean auth = false;
    private boolean debug = false;

    public SendMail() {
        initTrustManager();

        String DFlag = GetInfo.getConfig("mail.smtp.debug");

        if (DFlag.equals("-1")) {
            debug = false;
        } else {
            debug = Boolean.parseBoolean(DFlag);
        }

        String AFlag = GetInfo.getConfig("mail.smtp.auth");
        if (AFlag.equals("-1")) {
            auth = false;
        } else {
            auth = Boolean.parseBoolean(AFlag);
        }
    }


    public void SendM(String strAttachment, String Status, String strDBAttachment, StringBuilder mailBodyDBErr) {
        Properties props = new Properties();

        String PROTOCOL = GetInfo.getConfig("mail.smtp.protocol");
        if (PROTOCOL.equals("SSL")) {
            props.put("mail.smtp.ssl.enable", true);
        } else if (PROTOCOL.equals("TLS")) {
            props.put("mail.smtp.starttls.enable", true);
        }

        props.put("mail.smtp.ssl.trust", GetInfo.getConfig("mail.smtp.host"));
        props.put("mail.smtp.host", GetInfo.getConfig("mail.smtp.host"));
        props.put("mail.smtp.port", GetInfo.getConfig("mail.smtp.port"));
        props.put("mail.smtp.socketFactory.fallback", "true");

        username = GetInfo.getConfig("mail.smtp.user");
        password = GetInfo.getConfig("mail.smtp.password");
        from_addr = GetInfo.getConfig("mail.smtp.from");

        Authenticator authenticator = null;
        if (auth) {
            props.put("mail.smtp.auth", true);
            authenticator = new Authenticator() {
                private PasswordAuthentication pa = new PasswordAuthentication(username, password);

                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return pa;
                }
            };
        }


        Session session = Session.getInstance(props, authenticator);
        session.setDebug(debug);

        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(from_addr));

            message.addRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(GetInfo.getConfig("mail.smtp.recipients")));

            message.setSubject(GetInfo.getConfig("mail.smtp.subject"));
            message.setSentDate(new Date());

            Multipart mp = new MimeMultipart();
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(MailBody(mailBodyDBErr), "text/html");
            mp.addBodyPart(htmlPart);


            MimeBodyPart attachPart = new MimeBodyPart();
            String attachFile = strAttachment;
            attachPart.attachFile(attachFile);
            mp.addBodyPart(attachPart);

            if (strDBAttachment != null) {
                attachPart = new MimeBodyPart();
                attachPart.attachFile(strDBAttachment);
                mp.addBodyPart(attachPart);
            }

            message.setContent(mp);

            Transport.send(message);

        } catch (MessagingException ex) {
            loggers.addLogAndPrint("SERV", "Report mail dispatch FAILED | " + ex.getMessage(), false);
            loggers.addLogEx("SEVE", "MessagingException", ex);
        } catch (FileNotFoundException e) {
            loggers.addLogAndPrint("SERV", "Report mail dispatch FAILED | " + e.getMessage(), false);
            loggers.addLogEx("SEVE", "FileNotFoundException", e);
        } catch (IOException e) {
            loggers.addLogAndPrint("SERV", "Report mail dispatch FAILED | " + e.getMessage(), false);
            loggers.addLogEx("SEVE", "IOException", e);
        }
    }

    private String MailBody(StringBuilder mailBodyDBErr) throws FileNotFoundException, IOException {
        String RET = "";

        if (GetInfo.getConfig("mail.smtp.body.loadfromfile").equals("true")) {
            try {

                String strLine = null;

                FileInputStream fstream = new FileInputStream(GetInfo.getConfig("mail.smtp.body"));
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));

                StringBuffer buffer = new StringBuffer("");
                while ((strLine = br.readLine()) != null) {
                    buffer.append(strLine);
                }

                RET = buffer.toString();
                in.close();

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        } else {

            RET = GetInfo.getConfig("mail.smtp.body");

        }

        if (mailBodyDBErr != null) {
            if (mailBodyDBErr.toString() != "") {
                RET = RET + "\n" + mailBodyDBErr.toString();
            }
        }

        return RET;
    }

    public static void initTrustManager() {
        loggers.addLog("INFO", "Started the Trusted Manager ...");

        TrustManager[] tms = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                loggers.addLog("INFO", "****************************");
                loggers.addLog("INFO", "Checking SSL certificate ");

            }
        }};

        loggers.addLog("INFO", "Called the trusted manager ...");

        HostnameVerifier htv = new HostnameVerifier() {
            public boolean verify(String s, SSLSession sslSession) {
                loggers.addLog("INFO", "****************************");
                loggers.addLog("INFO", "Verify Hostname ...");
                return true;
            }
        };

        try {
            // Create context based on a TLS protocol and a SunJSSE provider.
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, tms, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

            HttpsURLConnection.setDefaultHostnameVerifier(htv);

            loggers.addLog("INFO", "SSL Context is set properly ...");

        } catch (Exception e) {
            loggers.addLogEx("SEVE", "Error in setting the SSL Context ...", e);
            System.out.println("Error in setting the SSL Context ...");
        }
    }

}

