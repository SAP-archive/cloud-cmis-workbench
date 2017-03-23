SAP Document Center TCK for Corporate Repositories
==================================================

The SAP Document Center (SDC) TCK checks, whether a CMIS repository that is connected to SDC as a corporate repository may work with SDC clients or not.
The TCK performs some basic CMIS operations and some operations that are specific to SDC.
If a repository passes the TCK it is very likely (but not guaranteed) that SDC clients can work with it. The TCK will be extended over time to cover more areas.

The tests are split into two groups; a *read-only group* and a *read-write group*.

A repository must pass the read-only tests and, if it supports write operations, must also pass the read-write tests.
Apart from those tests, it is highly recommended that the repository also passes all OpenCMIS TCK tests.


TCK Reports
-----------

A TCK run produces a report as text, HTML, XML, or JSON. The messages in a report are of the following types:

* **INFO**: This only an additional information, for example whether an uncritical feature is supported or not. 
* **SKIPPED**:  The repository doesn't support the feature and the test was skipped. 
* **OK**: The test has been passed.
* **WARNING**: The test has been passed, but something was not as expected. Some warnings can be ignored, some warnings can cause inconveniences for the end user. You should analyze the cause of the warning and decide if you can ignore it.
* **FAILURE**: The test failed. It is very likely that SDC clients have issues with this repository. That should be fixed.
* **UNEXPECTED_EXCEPTION**: The TCK received an unexpected exception. There is
probably a bug in the repository implementation.


Running the SDC TCK
-------------------

The TCK can be started in multiple ways. The easiest ways are the CMIS Workbench and the command line.


### Running the SDC TCK with the CMIS Workbench ###

1. Start the CMIS Workbench with `workbench.bat` or `workbench.sh`.
1. Select the login tab "SAP Document Center".
   1. Either select the landscape and enter your account ID into the Consumer field or select "Custom" from landscape drop-down box and enter the SDC URL.
   1. Enter username and password.
   1. Click "Load Repositories".
   1. Select the repository you want to test.
   1. Click "Login".
1.	Press the "TCK" button on the toolbar. The TCK window should open.
1.	In the test list, right click and select "Deselect all".
1.	Select the test group or single tests you would like to run.
1.	If you want to run read-write tests, make sure that the "Test folder path" field contains a path that is writeable.
1.	Click "Run TCK"
1.	When the tests finished, the Test Report window should open.
1.	Select the report type at the bottom and click "go". The HTML report contains the most details.


### Running the SDC TCK with on the Command Line ###

1. Copy the file "sample-session.parameters" from the CMIS Workbench directory to "tck.parameters" and open the copied file in a text editor.
1. Enter at least the URL and the repository ID. If you want to run read-write tests, make sure that the "testFolderParent" parameter points to a path that is writeable.
1. Open a terminal and change to the CMIS Workbench directory.
1. Run `runtck -u -p tck.parameters`. The read-only tests should run and a text report should be written to the console.
   1. Use `runtck -u -p -report=report.html -report-format=html tck.parameters` to generate and save a HTML report.
   1. Run just `runtck` to see more options and tests.
