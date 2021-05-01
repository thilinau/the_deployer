package it.bridge;

import static it.bridge.GetInfo.getConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;;

public class UpdateDashboard {

	public String dashboad_update_selection() {

		Set<Integer> option_set = new HashSet<Integer>();
		option_set.add(1);
		option_set.add(2);
		option_set.add(3);
		option_set.add(4);

		System.out.println("(1) Update Application Version Only");
		System.out.println("(2) Update Database Version Only");
		System.out.println("(3) Update both Application and Database Versions");
		System.out.println("(4) Quit");
		System.out.println("");
		System.out.println("Enter Your Choice [ 1 - 4 ] :");

		String select = "-1";

		while (true) {

			BufferedReader br = null;
			br = new BufferedReader(new InputStreamReader(System.in));

			select = "-1";
			try {
				select = br.readLine();
			} catch (IOException e) {
				loggers.addLogAndPrint("INFO",
						"Error in prompting dashboard selection menu. Please check log for more info.", false);
				loggers.addLogEx("INFO", e.getMessage(), e);
				System.exit(0);
			}

			if (!GetInfo.isNumeric(select)) {
				System.out.println("Invalid selection please try again !!!");
			} else {

				if (!option_set.contains(Integer.parseInt(select))) {
					System.out.println("Invalid selection please try again !!!");
				} else {
					break;
				}
			}
		}

		return select;
	}

	public boolean validate_dashboard_prams() {

		boolean status = true;

		if (getConfig("DASHBOARD_ENABLE").equals("-1")) {
			loggers.addLogAndPrint("INFO", "Dashboard Configuration DASHBOARD_ENABLE not found", false);
			status = false;
		}

		if (getConfig("DASHBOARD_BASE_URL").equals("-1")) {
			loggers.addLogAndPrint("INFO", "Dashboard Configuration DASHBOARD_BASE_URL not found", false);
			status = false;
		}

		if (getConfig("DASHBOARD_APP_VERSION_WIDGET").equals("-1")) {
			loggers.addLogAndPrint("INFO", "Dashboard Configuration DASHBOARD_APP_VERSION_WIDGET not found", false);
			status = false;
		}

		if (getConfig("DASHBOARD_APP_DATE_WIDGET").equals("-1")) {
			loggers.addLogAndPrint("INFO", "Dashboard Configuration DASHBOARD_APP_DATE_WIDGET not found", false);
			status = false;
		}

		if (getConfig("DASHBOARD_DB_VERSION_WIDGET").equals("-1")) {
			loggers.addLogAndPrint("INFO", "Dashboard Configuration DASHBOARD_DB_VERSION_WIDGET not found", false);
			status = false;
		}

		if (getConfig("DASHBOARD_DB_DATE_WIDGET").equals("-1")) {
			loggers.addLogAndPrint("INFO", "Dashboard Configuration DASHBOARD_DB_DATE_WIDGET not found", false);
			status = false;
		}

		return status;
	}

	public void update_dashboards(String ap_v, String ap_date, String db_v, String db_date) {

		if (validate_dashboard_prams()) {

			if (getConfig("DASHBOARD_ENABLE").equalsIgnoreCase("true")) {

				String user_selection = dashboad_update_selection();

				if (user_selection.equals("4")) {
					System.out.println("Bye");
					System.exit(0);
				}

				String data = "";

				// Set Dashboard Application Version
				if (!ap_v.equals("#NA") && (user_selection.equals("1") || user_selection.equals("3"))) {

					String dash_app_ver_wid_type = "";
					if ((dash_app_ver_wid_type = getConfig("DASHBOARD_APP_VERSION_WIDGET_TYPE")).equals("-1")) {
						dash_app_ver_wid_type = "text1";
					}
					data = "{\"auth_token\":\"YOUR_AUTH_TOKEN\",\"" + dash_app_ver_wid_type + "\":\"" + ap_v + "\"}";
					set_dashboard_values(data, getConfig("DASHBOARD_APP_VERSION_WIDGET"));
				}

				// Set Dashboard Application Updated Date
				if (!ap_date.equals("#NA") && (user_selection.equals("1") || user_selection.equals("3"))) {
					String dash_app_date_wid_type = "";
					if ((dash_app_date_wid_type = getConfig("DASHBOARD_APP_DATE_WIDGET_TYPE")).equals("-1")) {
						dash_app_date_wid_type = "text1";
					}
					data = "{\"auth_token\":\"YOUR_AUTH_TOKEN\",\"" + dash_app_date_wid_type + "\":\"" + ap_date
							+ "\"}";
					set_dashboard_values(data, getConfig("DASHBOARD_APP_DATE_WIDGET"));
				}

				// Set Dashboard Database Version
				if (!db_v.equals("#NA") && (user_selection.equals("2") || user_selection.equals("3"))) {
					String dash_db_ver_wid_type = "";
					if ((dash_db_ver_wid_type = getConfig("DASHBOARD_DB_VERSION_WIDGET_TYPE")).equals("-1")) {
						dash_db_ver_wid_type = "text1";
					}
					data = "{\"auth_token\":\"YOUR_AUTH_TOKEN\",\"" + dash_db_ver_wid_type + "\":\"" + db_v + "\"}";
					set_dashboard_values(data, getConfig("DASHBOARD_DB_VERSION_WIDGET"));
				}

				// Set Dashboard Database Updated Date
				if (!db_date.equals("#NA") && (user_selection.equals("2") || user_selection.equals("3"))) {
					String dash_db_date_wid_type = "";
					if ((dash_db_date_wid_type = getConfig("DASHBOARD_DB_DATE_WIDGET_TYPE")).equals("-1")) {
						dash_db_date_wid_type = "text1";
					}
					data = "{\"auth_token\":\"YOUR_AUTH_TOKEN\",\"" + dash_db_date_wid_type + "\":\"" + db_date + "\"}";
					set_dashboard_values(data, getConfig("DASHBOARD_DB_DATE_WIDGET"));
				}

				loggers.addLogAndPrint("INFO", "Dashboard Successfully Updated", false);
			} else {
				loggers.addLogAndPrint("INFO", "Dashboard feature is not switched on", false);
			}
		} else {
			loggers.addLogAndPrint("INFO", "Dashboard update | FAILED", false);
		}
	}

	public void update_dashboards_via_deployment(String ap_v, String ap_date, String db_v, String db_date) {
		
		if (getConfig("DASHBOARD_ENABLE").equalsIgnoreCase("true")) {

			loggers.addLog("INFO", "DASHBOARD | APP_VER:" + ap_v + " | APP_DATE:" + ap_date + " | DB_VER:" + db_v + " | DB_DATE:" + db_date );
	
			if (validate_dashboard_prams()) {

				String data = "";

				// Set Dashboard Application Version
				if (!ap_v.equals("#NA")) {

					String dash_app_ver_wid_type = "";
					if ((dash_app_ver_wid_type = getConfig("DASHBOARD_APP_VERSION_WIDGET_TYPE")).equals("-1")) {
						dash_app_ver_wid_type = "text1";
					}
					data = "{\"auth_token\":\"YOUR_AUTH_TOKEN\",\"" + dash_app_ver_wid_type + "\":\"" + ap_v + "\"}";
					set_dashboard_values(data, getConfig("DASHBOARD_APP_VERSION_WIDGET"));
					loggers.addLogAndPrint("INFO", "Dashboard | DASHBOARD_APP_VERSION_WIDGET : " + ap_v + " | SET", false);
				}

				// Set Dashboard Application Updated Date
				if (!ap_date.equals("#NA")) {
					String dash_app_date_wid_type = "";
					if ((dash_app_date_wid_type = getConfig("DASHBOARD_APP_DATE_WIDGET_TYPE")).equals("-1")) {
						dash_app_date_wid_type = "text1";
					}
					data = "{\"auth_token\":\"YOUR_AUTH_TOKEN\",\"" + dash_app_date_wid_type + "\":\"" + ap_date
							+ "\"}";
					set_dashboard_values(data, getConfig("DASHBOARD_APP_DATE_WIDGET"));
					loggers.addLogAndPrint("INFO", "Dashboard | DASHBOARD_APP_DATE_WIDGET : " + ap_date + " | SET", false);
				}

				// Set Dashboard Database Version
				if (!db_v.equals("#NA")) {
					String dash_db_ver_wid_type = "";
					if ((dash_db_ver_wid_type = getConfig("DASHBOARD_DB_VERSION_WIDGET_TYPE")).equals("-1")) {
						dash_db_ver_wid_type = "text1";
					}
					data = "{\"auth_token\":\"YOUR_AUTH_TOKEN\",\"" + dash_db_ver_wid_type + "\":\"" + db_v + "\"}";
					set_dashboard_values(data, getConfig("DASHBOARD_DB_VERSION_WIDGET"));
					loggers.addLogAndPrint("INFO", "Dashboard | DASHBOARD_DB_VERSION_WIDGET : " + db_v + " | SET", false);
				}

				// Set Dashboard Database Updated Date
				if (!db_date.equals("#NA")) {
					String dash_db_date_wid_type = "";
					if ((dash_db_date_wid_type = getConfig("DASHBOARD_DB_DATE_WIDGET_TYPE")).equals("-1")) {
						dash_db_date_wid_type = "text1";
					}
					data = "{\"auth_token\":\"YOUR_AUTH_TOKEN\",\"" + dash_db_date_wid_type + "\":\"" + db_date + "\"}";
					set_dashboard_values(data, getConfig("DASHBOARD_DB_DATE_WIDGET"));
					loggers.addLogAndPrint("INFO", "Dashboard | DASHBOARD_DB_DATE_WIDGET : " + db_date + " | SET", false);
				}

				loggers.addLogAndPrint("INFO", "Dashboard Successfully Updated", false);
				
			} else {
				
				loggers.addLogAndPrint("INFO", "Dashboard update | FAILED", false);
			}
		} else {
			loggers.addLogAndPrint("INFO", "Dashboard feature is not switched on", false);
		}
	}

	private void set_dashboard_values(String data, String widget) {

		HttpURLConnection dconn = null;

		try {

			String url = getConfig("DASHBOARD_BASE_URL") + widget;
			URL obj = new URL(url);
			dconn = (HttpURLConnection) obj.openConnection();

			dconn.setRequestProperty("Content-Type", "application/json");
			dconn.setDoOutput(true);

			dconn.setRequestMethod("GET");

			OutputStreamWriter out = new OutputStreamWriter(dconn.getOutputStream());
			out.write(data);
			out.close();

			new InputStreamReader(dconn.getInputStream());

		} catch (Exception ex) {
			loggers.addLogAndPrint("INFO", "Dashboard update : set_dashboard_values | FAILED", false);
			loggers.addLogEx("INFO", ex.getMessage(), ex);
		} finally {
			if (dconn != null) {
				dconn.disconnect();
			}
		}
	}

}
