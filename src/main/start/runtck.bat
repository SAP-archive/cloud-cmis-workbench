@echo off

rem use variable CUSTOM_JAVA_OPTS to set additional JAVA options

rem uncomment the following lines to configure HTTP proxy

rem set http_proxy=http://<proxy>:<port>
rem set https_proxy=https://<proxy>:<port>
rem set no_proxy=localhost,127.0.0.0,.local


for /F "delims=/" %%x in ('"java -classpath .;%~dp0\lib\* org.apache.chemistry.opencmis.workbench.ProxyDetector -j -s"') do set "JAVA_PROXY_CONF=%%x"
set JAVA_OPTS=%JAVA_PROXY_CONF%

java %JAVA_OPTS% %CUSTOM_JAVA_OPTS% -classpath ".;%~dp0\lib\*" com.sap.sdc.tck.corprep.tests.ConsoleRunner %*