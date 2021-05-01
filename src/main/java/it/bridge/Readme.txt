To enable silent mode add following parameters as VM Options
-Dsilent.mode=true -Dsilent.mode.selenv=<property file name , except .properties extension>
eg: -Dsilent.mode=true -Dsilent.mode.selenv=sample

if you are running silent mode to deploy a adhoc release, you need to pass the following vm option as well.
-Dsilent.mode.release
eg: -Dsilent.mode.release=6.6.1.51

Also add following parameters to <env>.properties file

### SILENT DEPLOYMENT ###
#SILENT_OPERATION_MODE => 1-Deploy Release(s)
#SILENT_OPERATION_MODE => 2-Deploy Adhoc Release
SILENT_MODE_OPERATION_TYPE=1
SILENT_MODE_CONTINUE_WITH_BACKUP_ERRORS=true
### END - SILENT DEPLOYMENT ###

Sample startup parameters
-Doracle.net.tns_admin=C:\oracle\product\11.2.0\client_1\network\admin 
-Dsilent.mode=true 
-Dsilent.mode.selenv=sample 
-Dsilent.mode.release=6.6.1.52