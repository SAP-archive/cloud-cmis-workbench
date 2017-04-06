#!/bin/bash

if [ -z "$JAVA_HOME" ]; then
  j=$(which java 2>/dev/null)
  if [ -z "$j" ]; then
 	echo "Unable to locate Java!"
    exit 1
  else
    JAVA="$j"
  fi
else
  JAVA="$JAVA_HOME/bin/java"
fi


SCRIPT_DIR=$(dirname "$0")

pushd "$SCRIPT_DIR" > /dev/null

cd "$SCRIPT_DIR/lib"

WCP="."
for i in *.jar; do
  WCP="$SCRIPT_DIR/lib/$i:${WCP}"
done

popd > /dev/null

# use variable CUSTOM_JAVA_OPTS to set additional JAVA options

# uncomment the following lines to configure HTTP proxy

# export http_proxy=http://<proxy>:<port>
# export https_proxy=https://<proxy>:<port>
# export no_proxy=localhost,127.0.0.0,.local


JAVA_PROXY_CONF=$($JAVA -classpath $WCP org.apache.chemistry.opencmis.workbench.ProxyDetector -j -s)
JAVA_OPTS="$JAVA_PROXY_CONF"

exec $JAVA $JAVA_OPTS $CUSTOM_JAVA_OPTS -classpath $WCP com.sap.sdc.tck.corprep.tests.ConsoleRunner "$@"
