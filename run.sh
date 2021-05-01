#!/bin/bash
DEPLOYER_HOME=/home/regapi/UNIVERSAL_DEPLOYER/TBX_SRC/
export ORACLE_HOME=$DEPLOYER_HOME/conf/tools/instantclient-sqlplus-linux-11.2.0.4.0
export PATH=$ORACLE_HOME/bin/:$PATH
export LD_LIBRARY_PATH=$ORACLE_HOME/lib/:$LD_LIBRARY_PATH
export NLS_LANG=AMERICAN_AMERICA.UTF8
java -jar universal_deployer.jar -Ddeployer.home=$DEPLOYER_HOME
