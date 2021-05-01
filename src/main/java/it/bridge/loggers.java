package it.bridge;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


public class loggers {
    public static final String lineSep = System.getProperty("line.separator");


    static FileHandler fh;
    static Logger logger;
    static String LogFilePath = "";

    public void init(String SelEnv) {

        try {
            LogManager lm = LogManager.getLogManager();

            logger = Logger.getLogger("UniDep");

            lm.addLogger(logger);
            for (Handler handler : logger.getHandlers()) {
                handler.flush();
                handler.close();
                logger.removeHandler(handler);
            }
            logger.setUseParentHandlers(false);

            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
            Date date = new Date();

            LogFilePath = System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator + "LOGS" + File.separator + dateFormat.format(date) + "_" + SelEnv + ".txt";

            fh = new FileHandler(LogFilePath);
            fh.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {

                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    Date date = new Date();
                    Throwable thrown = record.getThrown();
                    if (thrown != null) {

                        StringWriter writer = new StringWriter();
                        record.getThrown().printStackTrace(new PrintWriter(writer));
                        return dateFormat.format(date) + " " + record.getLevel() + " " + record.getMessage() + lineSep + "EXCEPTION: " + writer.toString() + lineSep;

                    } else {

                        return dateFormat.format(date) + " " + record.getLevel() + " " + record.getMessage() + lineSep;

                    }
                }
            });


            logger.addHandler(fh);

        } catch (Exception e) {
            System.out.println("Exception thrown: " + e);
            e.printStackTrace();
        }
    }

    public static String GetLogName() {

        return LogFilePath;
    }

    public static void addLog(String level, String note) {

        Level lv = getLogLevel(level);
        logger.log(lv, note);

    }

    private static Level getLogLevel(String level) {

        Level lv = Level.INFO;
        if (level.equals("INFO")) {
            lv = Level.INFO;
        } else if (level.equals("WARN")) {
            lv = Level.WARNING;
        } else if (level.equals("SEVE")) {
            lv = Level.SEVERE;
        }
        return lv;
    }

    public static void addLogEx(String level, String msg, Exception e) {

        Level lv = getLogLevel(level);
        logger.log(lv,msg,e);

    }


    public static void addLogAndPrint(String level, String note, boolean lvOn) {

        Level lv = getLogLevel(level);
        logger.log(lv, note);

        if (lvOn == true) {
            System.out.println(lv.getName() + " : " + note);
        } else {
            System.out.println(note);
        }

    }


    public static void addLogAndPrintf(String level, String[] pf, boolean lvOn) {

        Level lv = getLogLevel(level);
        logger.log(lv, Arrays.toString(pf));

        if (lvOn == true) {
            System.out.println(lv.getName() + " : " + Arrays.toString(pf));
        } else {
            System.out.printf("%-5s  %-12s  %-15s %-40s  %-40s  %-40s  %-40s\n", pf[0], pf[1], pf[2], pf[3], pf[4], pf[5], pf[6]);
        }
    }


    public static void addLogPrintErrorReport(String level, String note1, String note2, boolean lvOn, String ERR_CODE, String ERR_CATEGORY) {

        Level lv = getLogLevel(level);
        logger.log(lv, note1 + note2);

        if (lvOn == true) {
            System.out.println(lv.getName() + " : " + note1 + note2);
        } else {
            System.out.println(note1 + note2);
        }

        GetInfo.recBackupDeployErr(ERR_CODE, note2, ERR_CATEGORY);

    }

    public static void addBlankLine() {
        logger.log(Level.OFF, "");
    }

    public static void fhClose() {
        fh.close();
    }
}
