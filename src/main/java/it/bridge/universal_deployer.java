package it.bridge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.net.InetAddress;
import java.net.UnknownHostException;

import it.bridge.objects.HostAuthentication;
import it.bridge.objects.ParamDBInvalid;
import it.bridge.objects.ParamDepErrors;
import it.bridge.objects.ParamFinalDBReport;
import org.apache.commons.io.FilenameUtils;
import static it.bridge.GetInfo.getConfig;
import static it.bridge.GetInfo.getCommonConfig;

public class universal_deployer {

    static String PROFILE_FILE_NAME = "";
    static String APP_IPS = "";
    static String APP_PORT = "";
    static String APP_USER = "";
    static String APP_PASSWORD = "";
    static String ENV_TYPE = "";
    static String ENV_MODE = "";
    static String REL_VERSION = "";
    static String LAST_UPDATE = "";
    static ArrayList<Integer> DEP_REL_ARR;
    static String selecEnv;

    public static void setSelecEnv(String selecEnv) {
        universal_deployer.selecEnv = selecEnv;
        System.setProperty("selecEnv", selecEnv);
    }

    static String SEL_RELEASE;
    static boolean BACKUP_ONLY_FLAG = false;
    static boolean REL_VERIFICATION = false;
    final static String DEPLOYER_REL_VER = "1.0.2";
    final static String DEPLOYER_REL_DATE = "2021";
    static HashMap<String, HostAuthentication> MAP_USER_PASS = new HashMap<String, HostAuthentication>();
    static boolean DATABASE_FLAG = false, DB_SUCCESS_FLAG = false;
    static String DB_VERSION, DB_LU_DATE;
    static HashMap<String, String> HOLD_VERSIONS = new HashMap<String, String>();
    public static ArrayList<ParamDepErrors> ARR_ALL_DEP_ERRORS;


    public static void main(String[] args) throws Exception {

        Boolean CONTINUE_FLAG = true;
        ParamFinalDBReport DB_SCRIPT_REPORT = null;

        String deployer_home = System.getProperty("user.dir");

        if (System.getProperty("deployer.home") != null) {
            deployer_home = System.getProperty("deployer.home");
        }

        //Capture logged user
        Process p = Runtime.getRuntime().exec("whoami");
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            System.setProperty("LOGGED_USER", s);
        }

        String BASE_DIR = deployer_home;

        ARR_ALL_DEP_ERRORS = new ArrayList<ParamDepErrors>();
        loggers lognow = new loggers();

        System.setProperty("BASE_DIR", BASE_DIR);
        System.setProperty("ADHOC_DEPLOYMENT_FLAG", "false");

        InetAddress ip = InetAddress.getLocalHost();
        String hostname = ip.getHostName();
        System.setProperty("hostname", hostname);

        // Silent Mode Verification
        String SILENT_MODE_SELENV = "";

        if (System.getProperty("silent.mode") == null) {

            System.setProperty("silent.mode", "false");

        } else {

            if (System.getProperty("silent.mode").equals("true")) {

                SILENT_MODE_SELENV = System.getProperty("silent.mode.selenv");
                setSelecEnv(SILENT_MODE_SELENV);
                lognow.init(selecEnv);
                loggers.addLogAndPrint("INFO", "SILENT MODE : ON", false);
            }
        }

        if (System.getProperty("silent.mode").equals("true")) {

            String SILENT_MODE_OPERATION_TYPE = getConfig("SILENT_MODE_OPERATION_TYPE");

            if (SILENT_MODE_OPERATION_TYPE.equals("-1")) {

                loggers.addLogAndPrint("INFO", "Configuration SILENT_MODE_OPERATION_TYPE not found", false);
                System.exit(0);

            } else {
                loggers.addLog("INFO", "SILENT_MODE_OPERATION_TYPE : " + SILENT_MODE_OPERATION_TYPE);
                System.setProperty("SILENT_MODE_OPERATION_TYPE", SILENT_MODE_OPERATION_TYPE);
            }

            if (System.getProperty("SILENT_MODE_OPERATION_TYPE").equals("2")) {
                if (System.getProperty("silent.mode.release") == null) {
                    loggers.addLogAndPrint("INFO", "Runtime VM Option \"silent.mode.release\" configuration not found",
                            false);
                    System.exit(0);
                }
            }

            if (System.getProperty("silent.mode.release") != null) {
                loggers.addLog("INFO", "SILENT MODE SELECTED RELEASE : " + System.getProperty("silent.mode.release"));
            }
        }
        // End of Silent Mode Verification

        label: while (true) {
            String selec = "";

            if (System.getProperty("silent.mode").equals("true")) {
                selec = System.getProperty("SILENT_MODE_OPERATION_TYPE");
            } else {
                selec = Selection();
            }

            String BlockItems=null;
            if (!(BlockItems=getCommonConfig("BLOCK_MENU_ITEMS")).equalsIgnoreCase("-1")){
                String[] BLOCK = BlockItems.split(",");

                for (int d=0; d<BLOCK.length;d++){

                    if (BLOCK[d].equals(selec)){
                        if (!getCommonConfig("BLOCK_MENU_ITEMS_CUSTOM_MESSAGE").equals("-1") && !getCommonConfig("BLOCK_MENU_ITEMS_CUSTOM_MESSAGE").equals("")){
                            System.out.println(getCommonConfig("BLOCK_MENU_ITEMS_CUSTOM_MESSAGE"));
                            continue label;
                        }else{
                            System.out.println("You are not authorized to access this menu !!!");
                            continue label;
                        }
                    }
                }
            }

            if (selec.equals("1") || selec.equals("3")) {
                if (selec.equals("3")) {
                    BACKUP_ONLY_FLAG = true;
                }

                selecEnv = profile_selection();
                setSelecEnv(selecEnv);

                if (selecEnv.equalsIgnoreCase("Back")) {
                    continue;
                } else if (!selecEnv.equalsIgnoreCase("Back")) {
                    if (!System.getProperty("silent.mode").equals("true")) {
                        lognow.init(selecEnv);
                    }
                    loggers.addLog("INFO", "SELECTED PROFILE : " + System.getProperty("selecEnv"));
                    loggers.addLog("INFO", "DEPLOYER VERSION - " + DEPLOYER_REL_VER);
                    loggers.addLog("INFO", "DEPLOYER LAST UPDATED DATE - " + DEPLOYER_REL_DATE);

                    if (BACKUP_ONLY_FLAG == true) {
                        loggers.addLog("INFO", "SELECTED MODE - BACKUP ONLY");
                    } else {
                        loggers.addLog("INFO", "SELECTED MODE - RELEASE DEPLOYMENT");
                    }

                    if (load_profile(selecEnv) == true) {

                        String connStatus = check_connectivity();
                        if (connStatus.equalsIgnoreCase("B")) {
                            continue;
                        } else if (connStatus.equalsIgnoreCase("N")) {
                            System.out.println("Bye");
                            System.exit(0);
                        } else if (connStatus.equalsIgnoreCase("Y")) {
                            CONTINUE_FLAG = true;
                            break;
                        } else {
                            System.out.println("Info!! Invalid Selection");
                            CONTINUE_FLAG = false;
                            continue;
                        }
                    }

                }

            } else if (selec.equals("2")) {

                selecEnv = profile_selection();
                setSelecEnv(selecEnv);
                System.setProperty("ADHOC_DEPLOYMENT_FLAG", "true");

                if (selecEnv.equals("Back")) {
                    continue;
                } else if (!selecEnv.equals("Back")) {
                    lognow.init(System.getProperty("selecEnv") + "_adhoc");
                    loggers.addLog("INFO", "SELECTED PROFILE : " + System.getProperty("selecEnv"));
                    loggers.addLog("INFO", "SELECTED MODE - ADHOC RELEASE DEPLOYMENT");
                    loggers.addLog("INFO", "DEPLOYER VERSION - " + DEPLOYER_REL_VER);
                    loggers.addLog("INFO", "DEPLOYER LAST UPDATED DATE - " + DEPLOYER_REL_DATE);

                    GetInfo.adhocDirCheck();

                    if (load_profile(selecEnv) == true) {

                        String connStatus = check_connectivity();
                        if (connStatus.equalsIgnoreCase("B")) {
                            continue;
                        } else if (connStatus.equalsIgnoreCase("N")) {
                            System.out.println("Bye");
                            System.exit(0);
                        } else if (connStatus.equalsIgnoreCase("Y")) {
                            CONTINUE_FLAG = true;
                            break;
                        } else {
                            System.out.println("Info!! Invalid Selection");
                            CONTINUE_FLAG = false;
                            continue;
                        }
                    }
                }
            } else if (selec.equals("4")) {
                // Database Scripts Executor
                lognow.init(selecEnv + "_DBOnly");
                DB_SCRIPT_REPORT = DBPackageRunner(true);
                CONTINUE_FLAG = false;
                break;

            } else if (selec.equals("5")) {

                selecEnv = profile_selection();
                setSelecEnv(selecEnv);
                lognow.init(System.getProperty("selecEnv") + "_Dashboard");

                if (load_profile(selecEnv) == true) {
                    UpdateDashboard ud = new UpdateDashboard();
                    ud.update_dashboards(REL_VERSION, LAST_UPDATE, DB_VERSION, DB_LU_DATE);
                }

                System.exit(0);

            } else if (selec.equals("6")) {
                System.out.println("Bye");
                System.exit(0);
            } else {
                System.out.println("Invalid selection");
                continue;
            }
        }

        String PROCEED_CONFIRMATION = "-1";
        if (CONTINUE_FLAG == true) {

            while (true) {

                SEL_RELEASE = available_releases("0");

                loggers.addLog("INFO", "SELECTED RELEASE : " + SEL_RELEASE);

                PROCEED_CONFIRMATION = show_release_summary(SEL_RELEASE.trim(), false);
                if (PROCEED_CONFIRMATION.equals("-1")) {
                    if (System.getProperty("silent.mode").equals("true")) {
                        if (System.getProperty("SILENT_MODE_OPERATION_TYPE").equals("2")) {
                            loggers.addLogAndPrint("INFO", "SILENT MODE : No valid release found", false);
                            System.exit(0);
                        }
                    } else {
                        continue;
                    }
                } else if (PROCEED_CONFIRMATION.equalsIgnoreCase("Y")) {
                    CONTINUE_FLAG = true;
                    break;
                } else if (PROCEED_CONFIRMATION.equalsIgnoreCase("B")) {
                    CONTINUE_FLAG = false;
                    continue;
                } else if (PROCEED_CONFIRMATION.equalsIgnoreCase("S")) {
                    CONTINUE_FLAG = true;
                    break;
                } else if (PROCEED_CONFIRMATION.equalsIgnoreCase("N")) {
                    System.out.println("Bye");
                    System.exit(0);
                } else {
                    System.out.println("Invalid selection");
                    CONTINUE_FLAG = false;
                    continue;
                }
            }

        }

        // Call release bundling process
        String TEMP_BUNDLE_DIR = null;
        if (CONTINUE_FLAG == true) {

            System.out.println("Release bundling process started \nPlease wait...");

            BundleReleases br = new BundleReleases();
            TEMP_BUNDLE_DIR = br.BuildSingleRelease(DEP_REL_ARR);

            if (TEMP_BUNDLE_DIR.equals("-1")) {

                loggers.addLogAndPrint("SERE", "Release bundling process failed", true);
                System.out.println("Bye");
                CONTINUE_FLAG = false;
                System.exit(0);

            } else {

                loggers.addLogAndPrint("INFO", "Release bundling process completed", false);

            }

        }

        // Call Rule Validator Process
        System.out.println("");
        loggers.addLogAndPrint("INFO", "Executing backup rule validator...", false);
        boolean val_backup_status = true, val_deploy_status = true;
        RelBackup rbVal = new RelBackup();
        val_backup_status = rbVal.backup_artifacts_validator(TEMP_BUNDLE_DIR);
        if (val_backup_status == true) {

            loggers.addLogAndPrint("INFO", "Backup rule validator | Success", false);
        }

        System.out.println("");
        loggers.addLogAndPrint("INFO", "Executing deploy rule validator...", false);
        RelDeploy rdVal = new RelDeploy();
        val_deploy_status = rdVal.deploy_artifacts_validator(TEMP_BUNDLE_DIR);
        if (val_deploy_status == true) {

            loggers.addLogAndPrint("INFO", "Deploy rule validator | Success", false);
        }

        if (val_backup_status == false || val_deploy_status == false) {
            loggers.addLogAndPrint("INFO", "Rule validator fail, Please check the property file and try again \nBye",
                    false);
            CONTINUE_FLAG = false;
            System.exit(0);
        }

        // Release Backup Process
        if (CONTINUE_FLAG == true) {

            if (PROCEED_CONFIRMATION.toUpperCase().equals("S")) {

                loggers.addLogAndPrint("INFO", "Skipping release backup process", false);

            } else {

                loggers.addLog("INFO", "========== STARTING BACKUP PROCESS ==========");

                boolean backup_status = true;
                RelBackup rb = new RelBackup();

                backup_status = rb.backup_artifacts(TEMP_BUNDLE_DIR);

                System.out.println("");

                if (backup_status == false) {
                    loggers.addLogAndPrint("WARN", "Backup process endup with some error(s)", true);

                    if (System.getProperty("silent.mode").equals("true")) {

                        String tmpSilentIgnoreErrors = getConfig("SILENT_MODE_CONTINUE_WITH_BACKUP_ERRORS");

                        if (!tmpSilentIgnoreErrors.equalsIgnoreCase("true")) {

                            loggers.addLog("INFO", "SILENT MODE : SILENT_MODE_CONTINUE_WITH_BACKUP_ERRORS = true");
                            loggers.addLogAndPrint("INFO",
                                    "SILENT MODE : Backup process not successful, Please check the logs \nSystem Exit",
                                    false);
                            System.exit(0);
                        }
                    }

                } else {
                    loggers.addLogAndPrint("INFO", "Backup process completed", false);
                }
            }
        }

        // Release deployment process
        if (CONTINUE_FLAG == true) {

            if (BACKUP_ONLY_FLAG == false) {

                if (PROCEED_CONFIRMATION.equals("S") || PROCEED_CONFIRMATION.equals("s")) {
                    System.out.println("\nBackup process skipped & system is ready to start deployment");
                } else {
                    System.out.println("\nBackup process completed & system is ready to start deployment");
                }

            } else {

                System.out.println("\nEnd of backup process \nBye");
                System.exit(0);
            }

            String strConfirmation = "-1";

            while (true) {

                System.out.println("Press (Y)Continue (N)Disconnect -> (Release Deployment)");

                BufferedReader br = null;

                if (System.getProperty("silent.mode").equals("true")) {
                    loggers.addLogAndPrint("INFO", "SILENT MODE : Continue Release Deployment Process : Y", false);
                    Reader inFromUser = new StringReader("Y");
                    br = new BufferedReader(inFromUser);
                } else {
                    br = new BufferedReader(new InputStreamReader(System.in));
                }

                strConfirmation = br.readLine();

                if (strConfirmation.equals("-1")) {

                    System.out.println("INFO!! Invalid Selection");
                    continue;

                } else if (strConfirmation.equalsIgnoreCase("Y")) {

                    CONTINUE_FLAG = true;
                    break;

                } else if (strConfirmation.equalsIgnoreCase("N")) {

                    CONTINUE_FLAG = false;
                    System.out.println("Bye");
                    System.exit(0);

                } else {

                    System.out.println("Invalid selection");
                    CONTINUE_FLAG = false;
                    continue;
                }
            }

            loggers.addLog("INFO", "========== STARTING DEPLOYMENT PROCESS ==========");

            boolean deploy_status = true;
            RelDeploy rd = new RelDeploy();
            deploy_status = rd.deploy_artifacts(TEMP_BUNDLE_DIR);

            System.out.println("");

            if (deploy_status == false) {
                loggers.addLogAndPrint("WARN", "Deployment process end up with some error(s)", true);
            } else {
                loggers.addLogAndPrint("INFO", "Deployment process completed", false);
            }

            // Apply DB Script
            if (DATABASE_FLAG == false) {
                if (DB_VERSION.equals(REL_VERSION)) {
                    DB_SCRIPT_REPORT = DBPackageRunner(false);
                    CONTINUE_FLAG = true;
                } else {
                    loggers.addLog("INFO", "========== DB SCRIPT APPLIER STOPPED ==========");
                    loggers.addLog("INFO",
                            "Version mismatch found DB_VERSION - " + DB_VERSION + " APP_VERSION - " + REL_VERSION);
                }
            }

            // Release verification
            try {
                if (REL_VERIFICATION == true) {
                    System.out.println("\nRelease verification process started ...\n");
                    RelVerify.LoadRelMD5(TEMP_BUNDLE_DIR);
                    RelVerify.CompareRelMD5();
                    RelVerify.PrintMD5ForDeployed();
                    System.out.println("End of release verification process");
                }
            } catch (Exception e) {
                loggers.addLogAndPrint("INFO", "Error in release verification process", false);
            }
        }

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date dateobj = new Date();

        // Update deployed version
        if (System.getProperty("ADHOC_DEPLOYMENT_FLAG").equals("false")) {

            UpdateDashboard updb = new UpdateDashboard();

            if (DATABASE_FLAG == true) {

                GetInfo.setConfigVersion("DB_LAST_UPDATE", df.format(dateobj));
                GetInfo.setConfigVersion("DB_REL_VERSION", SEL_RELEASE);

                HOLD_VERSIONS.put("DB_VER_NEW", SEL_RELEASE);
                HOLD_VERSIONS.put("DB_LU_NEW", df.format(dateobj));

                updb.update_dashboards_via_deployment("#NA", "#NA", SEL_RELEASE, df.format(dateobj));

            } else {

                GetInfo.setConfigVersion("LAST_UPDATE", df.format(dateobj));
                GetInfo.setConfigVersion("REL_VERSION", SEL_RELEASE);

                HOLD_VERSIONS.put("REL_VER_NEW", SEL_RELEASE);
                HOLD_VERSIONS.put("REL_LU_NEW", df.format(dateobj));

                if (DB_SUCCESS_FLAG == true) {

                    GetInfo.setConfigVersion("DB_LAST_UPDATE", df.format(dateobj));
                    GetInfo.setConfigVersion("DB_REL_VERSION", SEL_RELEASE);

                    HOLD_VERSIONS.put("DB_VER_NEW", SEL_RELEASE);
                    HOLD_VERSIONS.put("DB_LU_NEW", df.format(dateobj));

                    updb.update_dashboards_via_deployment(SEL_RELEASE, df.format(dateobj), SEL_RELEASE, df.format(dateobj));
                } else {
                    updb.update_dashboards_via_deployment(SEL_RELEASE, df.format(dateobj), "#NA", "#NA");
                }
            }

        } else {

            HOLD_VERSIONS.put("ADHOC", "TRUE");
        }

        if (DATABASE_FLAG == true) {
            HOLD_VERSIONS.put("DB_ONLY_FLAG", "TRUE");
        }

        if (DB_SUCCESS_FLAG == true) {
            HOLD_VERSIONS.put("DB_SUCCESS_FLAG", "TRUE");
        } else {
            HOLD_VERSIONS.put("DB_SUCCESS_FLAG", "FALSE");
        }

        HOLD_VERSIONS.put("SEL_RELEASE", SEL_RELEASE);
        HOLD_VERSIONS.put("SEL_RELEASE_DT", df.format(dateobj));

        if (DB_SCRIPT_REPORT == null) {
            DB_SCRIPT_REPORT = new ParamFinalDBReport(null, false, null);
        }

        DB_SCRIPT_REPORT.setHOLD_VERSIONS(HOLD_VERSIONS);

        ShowInfo.DispatchNotification("SUCCESS", DATABASE_FLAG, DB_SCRIPT_REPORT);

        // Cleaning temporary files
        System.out.println("\nCleaning temporary files");
        File delDir = new File(System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator + "TEMP"
                + File.separator + TEMP_BUNDLE_DIR);
        boolean delStatus = ShowInfo.cleaningTempDir(delDir);
        if (delStatus == false) {
            loggers.addLogAndPrint("WARN", "Cleaning temporary files | FAILED", false);
        }

        if (DB_SCRIPT_REPORT != null) {
            if (DB_SCRIPT_REPORT.getZipFile() != "") {
                delDir = new File(System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator
                        + "TEMP" + File.separator + DB_SCRIPT_REPORT.getZipFile());
                delStatus = ShowInfo.cleaningTempFile(delDir);
                if (delStatus == false) {
                    loggers.addLogAndPrint("WARN", "Cleaning temporary database files | FAILED", false);
                }
            }
        }

        System.out.println("");
        loggers.addLogAndPrint("INFO", "========== END OF DEPLOYMENT ==========", false);

        loggers.fhClose();

        System.exit(0);

    }

    private static ParamFinalDBReport DBPackageRunner(boolean DBOnlyFlag) {

        loggers.addLog("INFO", "========== DB SCRIPT APPLIER STARTED ==========");

        ParamFinalDBReport dbReport = null;

        boolean CONTINUE = true;
        DBExecute dbrun = new DBExecute();

        if (DBOnlyFlag == false) {

            if (!dbrun.checkScriptAvailability(SEL_RELEASE)) {

                loggers.addLog("INFO", "No database scripts available for the selected releases");

                CONTINUE = false;

                if (DBOnlyFlag == true) {
                    DATABASE_FLAG = true;
                }

                DB_SUCCESS_FLAG = true;
            }
        }

        if (CONTINUE == true) {
            if (dbrun.setParams(DBOnlyFlag)) {

                if (dbrun.scriptValidator()) {

                    loggers.addLogAndPrint("INFO", "DB - Script validator - Success",false);

                    if (!dbrun. checkDBConnectivity()) {
                        loggers.addLogAndPrint("WARN", "Error in DB Connectivity", true);
                        if (DBOnlyFlag == true) {
                            System.out.println("Bye");
                            System.exit(0);
                        }
                    }

                    ArrayList<ParamDBInvalid> rs_before = null;
                    if (System.getProperty("disable_invalid_objects_check").equalsIgnoreCase("false")) {
                        rs_before = dbrun.checkInvaldObjects();
                    }

                    dbReport = dbrun.scriptApplier();

                    if (DBOnlyFlag == true) {
                        DATABASE_FLAG = true;
                    }

                    if (dbReport.getStatus()) {

                        DB_SUCCESS_FLAG = true;
                        if (System.getProperty("disable_invalid_objects_check").equalsIgnoreCase("false")) {
                            dbReport.setInvalidBefore(rs_before);
                            dbReport.setInvalidAfter(dbrun.checkInvaldObjects());
                            dbReport.setRs_disabled_obj_after_rel(dbrun.checkDisableObjects());
                        } else {
                            dbReport.setInvalidBefore(null);
                            dbReport.setInvalidAfter(null);
                            dbReport.setRs_disabled_obj_after_rel(null);
                        }
                    }

                } else {

                    loggers.addLogAndPrint("SERV", "DB Script validation failed", true);
                    if (DBOnlyFlag == true) {
                        System.out.println("Bye");
                        System.exit(0);
                    }
                }

            } else {

                loggers.addLogAndPrint("WARN", "Error in loading db parameters or DB script applier skipped ", true);
                if (DBOnlyFlag == true) {
                    System.out.println("Bye");
                    System.exit(0);
                }
            }
        }

        return dbReport;
    }

    public static String Selection() throws IOException {
        System.out.println("=================================================================================");
        System.out.println("           	   Welcome To The Deployer - Ver " + "1.0.2" + " ("
                + "2021" + ")");
        System.out.println("=================================================================================");
        System.out.println("(1) Deploy Release(s)");
        System.out.println("(2) Adhoc Release Deployment");
        System.out.println("(3) Take Backup(s) - Under Construction");
        System.out.println("(4) Apply Database Package(s) - Under Construction");
        System.out.println("(5) Update Dashboards");
        System.out.println("(6) Quit");
        System.out.println("");
        System.out.println("Enter Your Choice [ 1 - 6 ] :");

        String select = "-1";
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        select = br.readLine();

        return select;
    }

    public static String check_connectivity() throws Exception {

        String CONNECT_STATUS = null, selection, TEMP_CONFIG;
        boolean CONN_FAIL_FLAG = false;
        ArrayList<String> IPS_VERIFY = new ArrayList<String>();

        System.out.println("Verify connectivity between servers");

        // Check connectivity for backup rules
        String tmpS = getConfig("BACKUP_KRULE_PREFIXES");

        if (!tmpS.equals("-1")) {

            List<String> BKP_KRULE_PREFIX = new ArrayList<String>(Arrays.asList(tmpS.split(",")));

            for (int i = 0; i < BKP_KRULE_PREFIX.size(); i++) {

                TEMP_CONFIG = getConfig(BKP_KRULE_PREFIX.get(i) + "_HOSTNAME");

                if (!TEMP_CONFIG.equals("-1")) {
                    if (check_duplicate_hostname(IPS_VERIFY, TEMP_CONFIG) == false) {

                        IPS_VERIFY.add(TEMP_CONFIG);
                        MAP_USER_PASS.put(TEMP_CONFIG,
                                GetInfo.getHostAuthentication(BKP_KRULE_PREFIX.get(i), "backup", 0));
                    }

                } else {

                    loggers.addLogAndPrint("WARN",
                            "Value not found for config " + BKP_KRULE_PREFIX.get(i) + "_HOSTNAME \nBye", false);
                    System.exit(0);
                }
            }
        }

        // Agent.xml rules
        String TEMP_ADD = getConfig("TBX_AGENT_XML_HOSTNAME");
        if (check_duplicate_hostname(IPS_VERIFY, TEMP_ADD) == false) {

            IPS_VERIFY.add(TEMP_ADD);
            MAP_USER_PASS.put(TEMP_ADD, GetInfo.getHostAuthentication("", "agentxml", 0));

        }

        if (BACKUP_ONLY_FLAG == false) {

            // Check connectivity for deployment rules
            tmpS = getConfig("DEPLOY_KRULE_PREFIXES");

            if (!tmpS.equals("-1")) {

                List<String> DEP_KRULE_PREFIX = new ArrayList<String>(Arrays.asList(tmpS.split(",")));

                if (DEP_KRULE_PREFIX.size() > 0) {

                    for (int k = 0; k < DEP_KRULE_PREFIX.size(); k++) {

                        TEMP_CONFIG = getConfig(DEP_KRULE_PREFIX.get(k) + "_HOSTNAMES");
                        List<String> DEP_HOST = new ArrayList<String>(Arrays.asList(TEMP_CONFIG.split(",")));

                        if (!TEMP_CONFIG.equals("-1")) {
                            for (int l = 0; l < DEP_HOST.size(); l++) {

                                if (check_duplicate_hostname(IPS_VERIFY, DEP_HOST.get(l)) == false) {

                                    IPS_VERIFY.add(DEP_HOST.get(l));
                                    MAP_USER_PASS.put(DEP_HOST.get(l),
                                            GetInfo.getHostAuthentication(DEP_KRULE_PREFIX.get(k), "deploy", l));

                                }
                            }
                        } else {

                            loggers.addLogAndPrint("WARN",
                                    "Value(s) not found for config " + DEP_KRULE_PREFIX.get(k) + "_HOSTNAMES \nBye",
                                    false);
                            System.exit(0);
                        }
                    }
                }
            }

            // Check connectivity for agent.xml upload in multiple destinations
            String TEMP_AGT_DESTINATIONS = getConfig("TBX_AGENT_XML_MULTIPLE_HOSTNAMES");
            if (!TEMP_AGT_DESTINATIONS.equals("-1")) {

                List<String> DEP_MULTIPE_AGENT = new ArrayList<String>(Arrays.asList(TEMP_AGT_DESTINATIONS.split(",")));
                for (int k = 0; k < DEP_MULTIPE_AGENT.size(); k++) {

                    if (check_duplicate_hostname(IPS_VERIFY, DEP_MULTIPE_AGENT.get(k)) == false) {

                        IPS_VERIFY.add(DEP_MULTIPE_AGENT.get(k));
                        MAP_USER_PASS.put(DEP_MULTIPE_AGENT.get(k),
                                GetInfo.getHostAuthentication(DEP_MULTIPE_AGENT.get(k), "deploy", k));

                    }
                }
            }

        }

        if (IPS_VERIFY.size() > 0) {

            for (int i = 0; i < IPS_VERIFY.size(); i++) {

                FileTransferHandler FH = new FileTransferHandler();

                HostAuthentication HostAuth = MAP_USER_PASS.get(IPS_VERIFY.get(i));
                FH.SFTPHandler(HostAuth);

                try {
                    FH.connect();
                } catch (Exception ex) {
                    CONN_FAIL_FLAG = true;
                }

                if (FH.isConnected()) {
                    CONNECT_STATUS = "Connected";
                    FH.disconnect();
                } else {
                    CONNECT_STATUS = "Connection Failed";
                    CONN_FAIL_FLAG = true;
                }

                loggers.addLogAndPrint("INFO", IPS_VERIFY.get(i) + " " + CONNECT_STATUS, true);
            }

        } else {

            loggers.addLogAndPrint("INFO", "No servers found to check the connectivity", true);
        }

        if (CONN_FAIL_FLAG == true) {
            loggers.addLogAndPrint("WARN", "All servers not connected properly", true);
            System.out.println("Bye");
            System.exit(0);
        }

        System.out.println("\nPress (Y)Continue (B)Back (N)Disconnect");

        while (true) {

            BufferedReader br = null;

            if (System.getProperty("silent.mode").equals("true")) {
                loggers.addLogAndPrint("INFO", "SILENT MODE : Continue after connectivity verification process : Y",
                        false);
                Reader inFromUser = new StringReader("Y");
                br = new BufferedReader(inFromUser);
            } else {
                br = new BufferedReader(new InputStreamReader(System.in));
            }

            selection = "-1";
            selection = br.readLine();

            if (!selection.equals("")) {
                break;
            }
        }
        return selection;

    }

    private static boolean check_duplicate_hostname(ArrayList<String> ListIPS, String IP) {
        boolean RET_STATUS = false;

        for (int u = 0; u < ListIPS.size(); u++) {
            if (ListIPS.get(u).equals(IP)) {
                RET_STATUS = true;
                break;
            }
        }

        return RET_STATUS;

    }

    public static boolean load_profile(String PROF_NAME) {

        try {

            clearConsole();

            Properties props = new Properties();
            String proPath = System.getProperty("BASE_DIR") + File.separator + "conf" + File.separator
                    + System.getProperty("hostname") + File.separator + PROF_NAME + ".properties";
            props.load(new FileInputStream(proPath));

            APP_PORT = props.getProperty("APP_PORT");

            APP_USER = props.getProperty("DEFAULT_APP_USER");
            if (APP_USER == null) {
                loggers.addLogAndPrint("INFO", "Configuration DEFAULT_APP_USER not found", false);
                System.exit(0);
            }

            APP_PASSWORD = props.getProperty("DEFAULT_APP_PASSWORD");
            if (APP_PASSWORD == null) {
                loggers.addLogAndPrint("INFO", "Configuration DEFAULT_APP_PASSWORD not found", false);
                System.exit(0);
            }

            if (props.getProperty("DEFAULT_SSH_PORT") == null) {
                loggers.addLogAndPrint("INFO", "Configuration DEFAULT_SSH_PORT not found", false);
                System.exit(0);
            }

            ENV_TYPE = props.getProperty("ENV_TYPE");
            if (ENV_TYPE == null) {
                loggers.addLogAndPrint("INFO", "Configuration ENV_TYPE not found", false);
                System.exit(0);
            }

            ENV_MODE = props.getProperty("ENV_MODE");
            if (ENV_MODE == null) {
                loggers.addLogAndPrint("INFO", "Configuration ENV_MODE not found", false);
                System.exit(0);
            }

            String temp_rel_veriification = getConfig("REL_VERIFICATION").toUpperCase();
            if (temp_rel_veriification.equals("-1")) {
                REL_VERIFICATION = false;
            } else {
                if (temp_rel_veriification.equals("TRUE")) {
                    REL_VERIFICATION = true;
                } else if (temp_rel_veriification.equals("FALSE")) {
                    REL_VERIFICATION = false;
                } else {
                    loggers.addLogAndPrint("INFO", "Configuration REL_VERIFICATION invalid value", false);
                    System.exit(0);
                }
            }

            REL_VERSION = GetInfo.getConfigVersion("REL_VERSION");
            if (REL_VERSION.equals("-1")) {
                loggers.addLogAndPrint("INFO", "Configuration REL_VERSION not found", false);
                System.exit(0);
            }

            LAST_UPDATE = GetInfo.getConfigVersion("LAST_UPDATE");
            if (LAST_UPDATE.equals("-1")) {
                loggers.addLogAndPrint("INFO", "Configuration LAST_UPDATE not found", false);
                System.exit(0);
            }

            DB_VERSION = GetInfo.getConfigVersion("DB_REL_VERSION");
            if (DB_VERSION.equals("-1")) {
                loggers.addLogAndPrint("INFO", "Configuration DB_REL_VERSION not found", false);
                System.exit(0);
            }

            DB_LU_DATE = GetInfo.getConfigVersion("DB_LAST_UPDATE");
            if (DB_LU_DATE.equals("-1")) {
                loggers.addLogAndPrint("INFO", "Configuration DB_LAST_UPDATE not found", false);
                System.exit(0);
            }

            if (props.getProperty("DEFAULT_AUTH_TYPE") == null) {
                loggers.addLogAndPrint("INFO", "Configuration DEFAULT_AUTH_TYPE not found", false);
                System.exit(0);
            }

            if (props.getProperty("TBX_AGENT_XML_CATEGORY") == null) {
                loggers.addLogAndPrint("INFO", "Configuration TBX_AGENT_XML_CATEGORY not found", false);
                System.exit(0);
            }

            if (props.getProperty("TBX_AGENT_XML_HOSTNAME") == null) {
                loggers.addLogAndPrint("INFO", "Configuration TBX_AGENT_XML_HOSTNAME not found", false);
                System.exit(0);
            }

            if (props.getProperty("DEPLOY_KRULE_PREFIXES") == null) {
                loggers.addLogAndPrint("INFO", "Configuration DEPLOY_KRULE_PREFIXES not found", false);
                System.exit(0);
            }

            if (props.getProperty("BACKUP_KRULE_PREFIXES") == null) {
                loggers.addLogAndPrint("INFO", "Configuration BACKUP_KRULE_PREFIXES not found", false);
                System.exit(0);
            }

            // Enable tnsname.ora |Add configuration DB_ENABLE_TNS=true
            // Further please pass "-Doracle.net.tns_admin=<path to tnsnames.ora>"
            // Eg : -Doracle.net.tns_admin=C:\oracle\product\11.2.0\client_2\network\admin
            if (props.getProperty("DB_ENABLE_TNS") != null) {
                if (props.getProperty("DB_ENABLE_TNS").equalsIgnoreCase("true")) {
                    System.setProperty("enable.tns", "true");
                } else {
                    System.setProperty("enable.tns", "false");
                }
            } else {
                System.setProperty("enable.tns", "false");
            }

            if (props.getProperty("DB_DISABLE_INVALID_OBJECTS_CHECK") != null) {
                if (props.getProperty("DB_DISABLE_INVALID_OBJECTS_CHECK").equalsIgnoreCase("true")) {
                    System.setProperty("disable_invalid_objects_check", "true");
                } else {
                    System.setProperty("disable_invalid_objects_check", "false");
                }
            } else {
                System.setProperty("disable_invalid_objects_check", "false");
            }

            if (props.getProperty("DB_SCRIPT_RUN_WITH_LOCAL_AUTHENTICATION") != null) {
                if (props.getProperty("DB_SCRIPT_RUN_WITH_LOCAL_AUTHENTICATION").equalsIgnoreCase("true")) {
                    System.setProperty("db_script_run_with_local_authentication", "true");
                } else {
                    System.setProperty("db_script_run_with_local_authentication", "false");
                }
            } else {
                System.setProperty("db_script_run_with_local_authentication", "false");
            }

            // Backup Sub rule validation
            List<String> BKP_KRULE_PREFIX = new ArrayList<String>(
                    Arrays.asList(getConfig("BACKUP_KRULE_PREFIXES").split(",")));
            for (int j = 0; j < BKP_KRULE_PREFIX.size(); j++) {

                String tempConfigCat, tempConfigAuth = "";

                if ((tempConfigCat = props.getProperty(BKP_KRULE_PREFIX.get(j) + "_CATEGORY")) == null) {
                    loggers.addLogAndPrint("INFO", "Configuration " + BKP_KRULE_PREFIX.get(j) + "_CATEGORY not found",
                            false);
                    System.exit(0);
                }

                if (props.getProperty(BKP_KRULE_PREFIX.get(j) + "_HOSTNAME") == null) {
                    loggers.addLogAndPrint("INFO", "Configuration " + BKP_KRULE_PREFIX.get(j) + "_HOSTNAME not found",
                            false);
                    System.exit(0);
                }

                if (props.getProperty(BKP_KRULE_PREFIX.get(j) + "_AUTH_TYPE") == null) {
                    if (props.getProperty("DEFAULT_AUTH_TYPE") == null) {
                        loggers.addLogAndPrint("INFO",
                                "Configuration " + BKP_KRULE_PREFIX.get(j) + "_AUTH_TYPE not found", false);
                        System.exit(0);
                    } else {
                        tempConfigAuth = props.getProperty("DEFAULT_AUTH_TYPE");
                    }
                } else {
                    tempConfigAuth = props.getProperty(BKP_KRULE_PREFIX.get(j) + "_AUTH_TYPE");
                }

                if (tempConfigAuth.equalsIgnoreCase("userpass")) {
                    if (props.getProperty(BKP_KRULE_PREFIX.get(j) + "_USERNAME") == null) {
                        if (props.getProperty("DEFAULT_APP_USER") == null) {
                            loggers.addLogAndPrint("INFO",
                                    "Configuration " + BKP_KRULE_PREFIX.get(j) + "_USERNAME not found", false);
                            System.exit(0);
                        }
                    }

                    if (props.getProperty(BKP_KRULE_PREFIX.get(j) + "_PASSWORD") == null) {
                        if (props.getProperty("DEFAULT_APP_PASSWORD") == null) {
                            loggers.addLogAndPrint("INFO",
                                    "Configuration " + BKP_KRULE_PREFIX.get(j) + "_PASSWORD not found", false);
                            System.exit(0);
                        }
                    }

                } else if (tempConfigAuth.equalsIgnoreCase("keyfile")) {
                    if (props.getProperty(BKP_KRULE_PREFIX.get(j) + "_KEYFILE") == null) {
                        if (props.getProperty("DEFAULT_KEYFILE") == null) {
                            loggers.addLogAndPrint("INFO", "Configuration " + BKP_KRULE_PREFIX.get(j)
                                    + "_KEYFILE or DEFAULT_KEYFILE not found", false);
                            System.exit(0);
                        }
                    }
                }

                // BKP : Check whether backup items are loading from file(s) or direct
                if (props.getProperty(BKP_KRULE_PREFIX.get(j) + "_ITEMS_LOAD_FROM_FILE") != null) {

                    List<String> tempFil = Arrays
                            .asList(props.getProperty(BKP_KRULE_PREFIX.get(j) + "_ITEMS_LOAD_FROM_FILE").split(","));

                    if (GetInfo.getFileAvailability(tempFil) == false) {
                        loggers.addLogAndPrint("INFO",
                                "Configuration " + BKP_KRULE_PREFIX.get(j) + "_ITEMS_LOAD_FROM_FILE | FAILED", false);
                        System.exit(0);
                    }

                } else {

                    if (props.getProperty(BKP_KRULE_PREFIX.get(j) + "_ITEMS") == null) {
                        loggers.addLogAndPrint("INFO", "Configuration " + BKP_KRULE_PREFIX.get(j) + "_ITEMS not found",
                                false);
                        System.exit(0);
                    }
                }

                if (props.getProperty(BKP_KRULE_PREFIX.get(j) + "_PATH") == null) {
                    if (props.getProperty("DEFAULT_" + tempConfigCat + "_PATH") == null) {
                        loggers.addLogAndPrint("INFO", "Configuration " + BKP_KRULE_PREFIX.get(j) + "_PATH or DEFAULT_"
                                + tempConfigCat + "_PATH not found", false);
                        System.exit(0);
                    }
                }

                // BKP : Check whether backup EXCLUDE items are loading from file(s) or direct
                if (props.getProperty(BKP_KRULE_PREFIX.get(j) + "_EXCLUDE_ITEMS_LOAD_FROM_FILE") != null) {

                    List<String> tempFil = Arrays.asList(
                            props.getProperty(BKP_KRULE_PREFIX.get(j) + "_EXCLUDE_ITEMS_LOAD_FROM_FILE").split(","));

                    if (GetInfo.getFileAvailability(tempFil) == false) {
                        loggers.addLogAndPrint("INFO",
                                "Configuration " + BKP_KRULE_PREFIX.get(j) + "_EXCLUDE_ITEMS_LOAD_FROM_FILE | FAILED",
                                false);
                        System.exit(0);
                    }
                }

            }

            // Deploy Sub rule validation
            List<String> DEP_KRULE_PREFIX = new ArrayList<String>(
                    Arrays.asList(getConfig("DEPLOY_KRULE_PREFIXES").split(",")));
            for (int i = 0; i < DEP_KRULE_PREFIX.size(); i++) {

                String tempConfigCat, tempConfigAuth = "", tempConfigHostnames;

                if ((tempConfigCat = props.getProperty(DEP_KRULE_PREFIX.get(i) + "_CATEGORY")) == null) {
                    loggers.addLogAndPrint("INFO", "Configuration " + DEP_KRULE_PREFIX.get(i) + "_CATEGORY not found",
                            false);
                    System.exit(0);
                }

                if ((tempConfigHostnames = props.getProperty(DEP_KRULE_PREFIX.get(i) + "_HOSTNAMES")) == null) {
                    loggers.addLogAndPrint("INFO", "Configuration " + DEP_KRULE_PREFIX.get(i) + "_HOSTNAMES not found",
                            false);
                    System.exit(0);
                }

                // DEP : Check whether deploy items are loading from file(s) or direct
                if (props.getProperty(DEP_KRULE_PREFIX.get(i) + "_ITEMS_LOAD_FROM_FILE") != null) {

                    List<String> tempFil = Arrays
                            .asList(props.getProperty(DEP_KRULE_PREFIX.get(i) + "_ITEMS_LOAD_FROM_FILE").split(","));

                    if (GetInfo.getFileAvailability(tempFil) == false) {
                        loggers.addLogAndPrint("INFO",
                                "Configuration " + DEP_KRULE_PREFIX.get(i) + "_ITEMS_LOAD_FROM_FILE | FAILED", false);
                        System.exit(0);
                    }

                } else {

                    if (props.getProperty(DEP_KRULE_PREFIX.get(i) + "_ITEMS") == null) {
                        loggers.addLogAndPrint("INFO", "Configuration " + DEP_KRULE_PREFIX.get(i) + "_ITEMS not found",
                                false);
                        System.exit(0);
                    }
                }

                // DEP : Check whether deploy EXCLUDE items are loading from file(s) or direct
                if (props.getProperty(DEP_KRULE_PREFIX.get(i) + "_EXCLUDE_ITEMS_LOAD_FROM_FILE") != null) {

                    List<String> tempFil = Arrays.asList(
                            props.getProperty(DEP_KRULE_PREFIX.get(i) + "_EXCLUDE_ITEMS_LOAD_FROM_FILE").split(","));

                    if (GetInfo.getFileAvailability(tempFil) == false) {
                        loggers.addLogAndPrint("INFO",
                                "Configuration " + DEP_KRULE_PREFIX.get(i) + "_EXCLUDE_ITEMS_LOAD_FROM_FILE | FAILED",
                                false);
                        System.exit(0);
                    }
                }

                List<String> TEMP_HOST_LIST = new ArrayList<String>(Arrays.asList(tempConfigHostnames.split(",")));
                for (int l = 0; l < TEMP_HOST_LIST.size(); l++) {

                    if (props.getProperty(DEP_KRULE_PREFIX.get(i) + "_AUTH_TYPE_" + (l + 1)) == null) {

                        if (props.getProperty("DEFAULT_AUTH_TYPE") == null) {
                            loggers.addLogAndPrint("INFO",
                                    "Configuration " + DEP_KRULE_PREFIX.get(i) + "_AUTH_TYPE_" + (l + 1) + " not found",
                                    false);
                            System.exit(0);
                        } else {
                            tempConfigAuth = props.getProperty("DEFAULT_AUTH_TYPE");
                        }

                    } else {
                        tempConfigAuth = props.getProperty(DEP_KRULE_PREFIX.get(i) + "_AUTH_TYPE_" + (l + 1));
                    }

                    if (tempConfigAuth.equalsIgnoreCase("userpass")) {

                        if (props.getProperty(DEP_KRULE_PREFIX.get(i) + "_USERNAME_" + (l + 1)) == null) {
                            if (props.getProperty("DEFAULT_APP_USER") == null) {
                                loggers.addLogAndPrint("INFO", "Configuration " + DEP_KRULE_PREFIX.get(i) + "_USERNAME_"
                                        + (l + 1) + " not found", false);
                                System.exit(0);
                            }
                        }

                        if (props.getProperty(DEP_KRULE_PREFIX.get(i) + "_PASSWORD_" + (l + 1)) == null) {

                            if (props.getProperty("DEFAULT_APP_PASSWORD") == null) {
                                loggers.addLogAndPrint("INFO", "Configuration " + DEP_KRULE_PREFIX.get(i) + "_PASSWORD_"
                                        + (l + 1) + " not found", false);
                                System.exit(0);
                            }
                        }

                    } else if (tempConfigAuth.equalsIgnoreCase("keyfile")) {

                        if (props.getProperty(DEP_KRULE_PREFIX.get(i) + "_KEYFILE_" + (l + 1)) == null) {
                            if (props.getProperty("DEFAULT_KEYFILE") == null) {
                                loggers.addLogAndPrint("INFO", "Configuration " + DEP_KRULE_PREFIX.get(i) + "_KEYFILE_"
                                        + (l + 1) + " or DEFAULT_KEYFILE not found", false);
                                System.exit(0);
                            }
                        }
                    }

                    if (props.getProperty(DEP_KRULE_PREFIX.get(i) + "_PATH_" + (l + 1)) == null) {

                        if (props.getProperty("DEFAULT_" + tempConfigCat + "_PATH") == null) {
                            loggers.addLogAndPrint("INFO", "Configuration " + DEP_KRULE_PREFIX.get(i) + "_PATH_"
                                    + (l + 1) + " or DEFAULT_" + tempConfigCat + "_PATH not found", false);
                            System.exit(0);
                        }
                    }
                }
            }

            // Check whether any category is having multiple ANY condition under ITEM LIST
            boolean bool_multiple = false;
            ArrayList<String> SOURCE_DIRS = GetInfo.getSourceDirectories();
            for (int i = 0; i < SOURCE_DIRS.size(); i++) {

                bool_multiple = false;
                ArrayList<String> CAT_PREFIXES_LIST = GetInfo.getCategoryPrefixes(SOURCE_DIRS.get(i),
                        "BACKUP_KRULE_PREFIXES");
                for (int k = 0; k < CAT_PREFIXES_LIST.size(); k++) {
                    String[] tmpList = GetInfo.getItemsList(CAT_PREFIXES_LIST.get(k), "ITEMS");
                    if (tmpList.length == 1) {
                        if (tmpList[0].equalsIgnoreCase("any")) {

                            if (bool_multiple == true) {
                                loggers.addLogAndPrint("INFO", "BACKUP : Category " + SOURCE_DIRS.get(i)
                                        + " is having multiple ANY condition under <RULE PREFIX>_ITEMS", false);
                                System.exit(0);
                            } else {
                                bool_multiple = true;
                            }
                        }
                    }
                }

            }

            System.out
                    .println("--------------------------------------------------------------------------------------");
            System.out.println("         You have selected the " + PROF_NAME);
            System.out
                    .println("--------------------------------------------------------------------------------------");
            System.out.println("Application Port 		: " + APP_PORT);
            System.out.println("Application User 		: " + APP_USER);
            System.out.println("Environment Type		: " + ENV_TYPE + " | " + ENV_MODE);
            System.out.println("Current Release Version		: " + REL_VERSION);
            System.out.println("Last Updated On			: " + LAST_UPDATE);
            System.out.println("Current DB Version		: " + DB_VERSION);
            System.out.println("Last DB Updated On		: " + DB_LU_DATE);
            System.out
                    .println("--------------------------------------------------------------------------------------");

            loggers.addLog("INFO", "Existing release : " + REL_VERSION + "  Deployed on : " + LAST_UPDATE);
            loggers.addLog("INFO", "Existing release (DB) : " + DB_VERSION + "  Deployed on : " + DB_LU_DATE);

            universal_deployer.HOLD_VERSIONS.put("REL_VER", REL_VERSION);
            universal_deployer.HOLD_VERSIONS.put("REL_LU", LAST_UPDATE);
            universal_deployer.HOLD_VERSIONS.put("DB_VER", DB_VERSION);
            universal_deployer.HOLD_VERSIONS.put("DB_LU", DB_LU_DATE);

        } catch (IOException e) {

            loggers.addLogEx("SEVE", e.getMessage(), e);
            return false;

        } catch (Exception ex) {

            loggers.addLogEx("SEVE", ex.getMessage(), ex);
            return false;

        }

        return true;

    }

    public static String profile_selection() {
        String select = null;
        boolean SEL_MATCH_FLAG = false;

        clearConsole();

        try {

            FilenameFilter filter = new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".properties");
                }
            };

            File f = new File(System.getProperty("BASE_DIR") + File.separator + "conf" + File.separator
                    + System.getProperty("hostname"));
            File[] files = f.listFiles(filter);

            System.out
                    .println("--------------------------------------------------------------------------------------");
            System.out.println("           	   Environment profiles for " + System.getProperty("hostname"));
            System.out
                    .println("--------------------------------------------------------------------------------------");

            for (int i = 0; i < files.length; i++) {

                File file = files[i];

                if (file.isDirectory()) {
                    System.out.print("directory:");
                } else {
                    String fileNameWithOutExt = FilenameUtils.removeExtension(file.getName());
                    int index = i + 1;
                    System.out.println("(" + index + ") " + fileNameWithOutExt);

                }
            }

            System.out.println("(#) Back");
            System.out
                    .println("--------------------------------------------------------------------------------------");
            System.out.println("\nSelect the environment name  : ");

            while (SEL_MATCH_FLAG == false) {

                BufferedReader br = null;

                if (System.getProperty("silent.mode").equals("true")) {

                    loggers.addLogAndPrint("INFO",
                            "SILENT MODE : Environment Selection : " + System.getProperty("silent.mode.selenv"), false);
                    Reader inFromUser = new StringReader(System.getProperty("silent.mode.selenv"));
                    br = new BufferedReader(inFromUser);

                } else {

                    br = new BufferedReader(new InputStreamReader(System.in));
                }

                select = "-1";
                select = br.readLine();

                if (!select.equals("")) {
                    for (int i = 0; i < files.length; i++) {

                        File file = files[i];

                        if (file.isDirectory()) {
                            System.out.print("directory:");
                        } else {

                            String fileNameWithOutExt = FilenameUtils.removeExtension(file.getName());

                            if (select.equals(fileNameWithOutExt) || select.equals("Back")) {
                                SEL_MATCH_FLAG = true;
                                break;
                            }
                        }
                    }
                    if (SEL_MATCH_FLAG == false) {
                        System.out.println("Info!! Invalid selection");
                    }
                }

            }

        } catch (UnknownHostException e) {

            loggers.addLogAndPrint("SEVE", e.getMessage(), false);
            loggers.addLogEx("SEVE", e.getMessage(), e);

        } catch (IOException ex) {

            loggers.addLogAndPrint("SEVE", ex.getMessage(), false);
            loggers.addLogEx("SEVE", ex.getMessage(), ex);
        } catch (Exception exx) {

            loggers.addLogAndPrint("SEVE", exx.getMessage(), false);
            loggers.addLogEx("SEVE", exx.getMessage(), exx);
        }

        return select;
    }

    public final static void clearConsole() {
        try {
            final String os = System.getProperty("os.name");

            if (os.contains("Windows")) {
                Runtime.getRuntime().exec("cls");
            } else {
                Runtime.getRuntime().exec("clear");
            }
        } catch (final Exception e) {
            // Handle any exceptions.
        }
    }

    public static String available_releases(String mode) {

        // If mode - 0 -> General release
        // If mode - 1 -> DB Release

        HashMap<String, String> VALID_RELEASES = new HashMap<String, String>();
        String selectRel = null, UPLOADED_ON, SILENTMODE_MAX_VERSION = "";
        int REL_VER, CURRENT_VER = 0, SILENTMODE_MAX = 0;
        boolean FOUND_MATCHED_REL = false;

        if (mode == "0") {
            CURRENT_VER = Integer.parseInt(REL_VERSION.replaceAll("\\.", ""));
        } else if (mode == "1") {
            CURRENT_VER = Integer.parseInt(DB_VERSION.replaceAll("\\.", ""));
        }

        try {

            System.out
                    .println("--------------------------------------------------------------------------------------");
            System.out.println("                    ALL AVAILABLE RELEASES");
            System.out
                    .println("--------------------------------------------------------------------------------------");
            System.out.printf("#  %-30.30s  %-20.20s%n", "RELEASE ID", "UPLOADED DATE");
            System.out
                    .println("--------------------------------------------------------------------------------------");

            File f = new File(System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator
                    + GetInfo.getReleaseSource());
            File[] files = f.listFiles();
            Arrays.sort(files);

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {

                        REL_VER = Integer.parseInt(file.getName().split("_")[1].replaceAll("\\.", ""));
                        UPLOADED_ON = file.getName().split("_")[0];

                        if (System.getProperty("ADHOC_DEPLOYMENT_FLAG").equals("false")) {
                            if (REL_VER > CURRENT_VER) {
                                System.out.printf("#  %-30.30s  %-20.20s%n", file.getName().split("_")[1], UPLOADED_ON);
                                FOUND_MATCHED_REL = true;

                                if (REL_VER > SILENTMODE_MAX) {
                                    SILENTMODE_MAX_VERSION = file.getName().split("_")[1];
                                    SILENTMODE_MAX = REL_VER;
                                }


                            }
                        } else {
                            System.out.printf("#  %-30.30s  %-20.20s%n", file.getName().split("_")[1], UPLOADED_ON);
                            FOUND_MATCHED_REL = true;
                        }

                        VALID_RELEASES.put(file.getName().split("_")[1], String.valueOf(REL_VER));
                    }
                }
            }

            if (FOUND_MATCHED_REL == false) {
                System.out.println("\nMatched releases not found !!! \nBye");
                System.exit(0);
            } else {
                System.out.println("\nEnter Release For Deployment :");
            }

            while (true) {

                BufferedReader br = null;

                if (System.getProperty("silent.mode").equalsIgnoreCase("true")) {
                    if (System.getProperty("SILENT_MODE_OPERATION_TYPE").equals("1")) {
                        if (System.getProperty("silent.mode.release") != null
                                && System.getProperty("silent.mode.release") != "") {
                            SILENTMODE_MAX_VERSION = System.getProperty("silent.mode.release");
                            loggers.addLogAndPrint("INFO",
                                    "SILENT MODE : Release Selection (User Version) : " + SILENTMODE_MAX_VERSION,
                                    false);
                        } else {
                            loggers.addLogAndPrint("INFO",
                                    "SILENT MODE : Release Selection (Max Version) : " + SILENTMODE_MAX_VERSION, false);
                        }
                    } else if (System.getProperty("SILENT_MODE_OPERATION_TYPE").equals("2")) {
                        SILENTMODE_MAX_VERSION = System.getProperty("silent.mode.release");
                        loggers.addLogAndPrint("INFO",
                                "SILENT MODE : Release Selection (Adhoc Version) : " + SILENTMODE_MAX_VERSION, false);
                    }
                    Reader inFromUser = new StringReader(SILENTMODE_MAX_VERSION);
                    br = new BufferedReader(inFromUser);

                } else {

                    br = new BufferedReader(new InputStreamReader(System.in));
                }

                selectRel = "-1";
                selectRel = br.readLine();

                if (VALID_RELEASES.get(selectRel) == null) {
                    if (System.getProperty("silent.mode").equalsIgnoreCase("true")) {
                        loggers.addLogAndPrint("INFO",
                                "SILENT MODE : Invalid release selection.\nBye", false);
                        System.exit(0);

                    }
                    System.out.println("Info!! Invalid selection. Please try again");
                    continue;
                } else {
                    break;
                }
            }
        } catch (Exception e) {

            loggers.addLogEx("SERV", e.getMessage(), e);

        }

        return selectRel;

    }


    public static String show_release_summary(String SELECTED_REL, boolean DB_FLAG) throws IOException {

        boolean NO_RELEASES_FLAG = true;
        String REL_DIRECTORY, CONFIRMATION = null;

        if (DB_FLAG == true) {
            System.out.println("");
        }

        System.out.println("--------------------------------------------------------------------------------------");

        if (DB_FLAG == false) {
            System.out.println("                    COMPLETE RELEASE SUMMARY");
        } else {
            System.out.println("                    DATABASE - SCRIPT SUMMARY");
        }

        System.out.println("--------------------------------------------------------------------------------------");

        if (DB_FLAG == true) {

            System.out.printf("%-13s %1s %1s%n", "DB HOSTNAME", ":", getConfig("DB_HOSTNAME"));
            System.out.printf("%-13s %1s %1s%n", "DB PORT", ":", getConfig("DB_PORT"));

            if (System.getProperty("enable.tns").equals("true")) {
                System.out.printf("%-13s %1s %1s%n", "DB TNSNAME", ":", getConfig("DB_SID"));
            } else {
                System.out.printf("%-13s %1s %1s%n", "DB SID", ":", getConfig("DB_SID"));
            }

        }

        File f = new File(System.getProperty("BASE_DIR") + File.separator + "METERIALS" + File.separator
                + GetInfo.getReleaseSource());
        File[] files = f.listFiles();

        DEP_REL_ARR = new ArrayList<Integer>();

        try {

            if (System.getProperty("ADHOC_DEPLOYMENT_FLAG").equals("false")) {
                for (int i = 0; i < files.length; i++) {

                    File file = files[i];
                    int REL_VER;

                    if (file.isDirectory()) {
                        REL_VER = Integer.parseInt(file.getName().split("_")[1].replaceAll("\\.", ""));

                        if (DB_FLAG == false) {

                            if (REL_VER > Integer.parseInt(REL_VERSION.replaceAll("\\.", ""))
                                    && REL_VER <= Integer.parseInt(SELECTED_REL.replaceAll("\\.", ""))) {
                                DEP_REL_ARR.add(REL_VER);
                            }
                        } else {

                            if (REL_VER > Integer.parseInt(DB_VERSION.replaceAll("\\.", ""))
                                    && REL_VER <= Integer.parseInt(SELECTED_REL.replaceAll("\\.", ""))) {
                                DEP_REL_ARR.add(REL_VER);
                            }
                        }
                    }
                }

            } else {

                for (int i = 0; i < files.length; i++) {

                    File file = files[i];
                    int ADHOC_REL_VER;

                    if (file.isDirectory()) {

                        ADHOC_REL_VER = Integer.parseInt(file.getName().split("_")[1].replaceAll("\\.", ""));
                        if (ADHOC_REL_VER == Integer.parseInt(SELECTED_REL.replaceAll("\\.", ""))) {
                            DEP_REL_ARR.add(ADHOC_REL_VER);
                        }
                    }
                }
            }

            Collections.sort(DEP_REL_ARR);

            for (int i = 0; i < DEP_REL_ARR.size(); i++) {

                NO_RELEASES_FLAG = false;

                REL_DIRECTORY = GetInfo.getReleaseDirectory(DEP_REL_ARR.get(i));

                System.out.println(
                        "--------------------------------------------------------------------------------------");
                System.out.println("Release Summary :" + REL_DIRECTORY.split("_")[1]);
                System.out.println(
                        "--------------------------------------------------------------------------------------");

                ShowInfo si = new ShowInfo();

                if (DB_FLAG == false) {
                    si.dispaly_release_content(REL_DIRECTORY);
                } else {
                    si.dispaly_release_content_db(REL_DIRECTORY, DEP_REL_ARR.get(i));
                }
            }

        } catch (Exception e) {

            NO_RELEASES_FLAG = true;
        }

        if (NO_RELEASES_FLAG == true) {

            if (System.getProperty("silent.mode").equalsIgnoreCase("true")) {
                loggers.addLogAndPrint("INFO",
                        "SILENT MODE : No valid releases found.\nBye", false);
                System.exit(0);

            } else {
                System.out.println("Info!! No valid releases found");
                CONFIRMATION = "-1";
            }

        } else {

            if (DB_FLAG == false) {

                System.out.println("\nPress (Y)Continue (B)Back (N)Disconnect (S) Skip -> (Backup Process)  : ");

                while (true) {

                    BufferedReader br = null;

                    if (System.getProperty("silent.mode").equals("true")) {

                        loggers.addLogAndPrint("INFO", "SILENT MODE : Continue Backup Process : Y", false);
                        Reader inFromUser = new StringReader("Y");
                        br = new BufferedReader(inFromUser);

                    } else {

                        br = new BufferedReader(new InputStreamReader(System.in));
                    }

                    CONFIRMATION = br.readLine();
                    if (!CONFIRMATION.equals("")) {
                        break;
                    }
                }

            } else {

                System.out.println("\nPress (Y)Continue (B)Back (S)Skip (DB Script Apply)  : ");

                while (true) {

                    BufferedReader br = null;

                    if (System.getProperty("silent.mode").equals("true")) {

                        loggers.addLogAndPrint("INFO", "SILENT MODE : Continue DB Script Apply Process : Y", false);
                        Reader inFromUser = new StringReader("Y");
                        br = new BufferedReader(inFromUser);

                    } else {

                        br = new BufferedReader(new InputStreamReader(System.in));
                    }

                    CONFIRMATION = br.readLine();
                    if (!CONFIRMATION.equals("")) {
                        break;
                    }
                }
            }
        }

        return CONFIRMATION;
    }

}
