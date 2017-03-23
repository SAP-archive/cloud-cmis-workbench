/**
 * Copyright 2013-2017, SAP SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sap.sdc.tck.corprep.tests;

import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.tck.CmisTest;
import org.apache.chemistry.opencmis.tck.CmisTestGroup;
import org.apache.chemistry.opencmis.tck.CmisTestProgressMonitor;
import org.apache.chemistry.opencmis.tck.CmisTestReport;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.CmisTestResultStatus;
import org.apache.chemistry.opencmis.tck.report.HtmlReport;
import org.apache.chemistry.opencmis.tck.report.JsonReport;
import org.apache.chemistry.opencmis.tck.report.TextReport;
import org.apache.chemistry.opencmis.tck.report.XmlReport;
import org.apache.chemistry.opencmis.tck.runner.AbstractRunner;

import com.sap.sdc.tck.corprep.tests.readonly.ReadOnlyTestGroup;
import com.sap.sdc.tck.corprep.tests.readwrite.ReadWriteTestGroup;

/**
 * Console Runner.
 */
public class ConsoleRunner extends AbstractRunner {

	public static final String GROUP_READ_ONLY = "read-only";
	public static final String GROUP_READ_WRITE = "read-write";
	public static final String GROUP_ALL = "all";
	public static final String GROUP_OPENCMIS = "opencmis";

	public static final String FORMAT_TEXT = "text";
	public static final String FORMAT_HTML = "html";
	public static final String FORMAT_XML = "XML";
	public static final String FORMAT_JSON = "json";

	public ConsoleRunner(String[] args) throws Exception {
		System.out.println("SAP Document Center TCK\n");

		try {
			// check arguments
			int parameterParametersFile = 0;
			int parameterTestGroup = parameterParametersFile + 1;
			boolean readUser = false;
			boolean readPassword = false;
			String reportFile = "-";
			String reportFormat = "text";

			if (args.length > 0) {
				int i = 0;
				while (args[i].startsWith("-")) {
					if (args[i].equals("-p")) {
						readPassword = true;
					} else if (args[i].equals("-u")) {
						readUser = true;
					} else if (args[i].startsWith("-report=")) {
						reportFile = args[i].substring(8);
					} else if (args[i].startsWith("-report-format=")) {
						reportFormat = args[i].substring(15);
					} else {
						System.err.println("Unknown argument '" + args[i] + "'.");
						return;
					}

					parameterParametersFile++;
					parameterTestGroup++;
					i++;
				}
			}

			if (args.length < parameterParametersFile + 1) {
				printUsage();
				return;
			}

			// check parameter file
			File parametersFile = new File(args[parameterParametersFile]);
			if (!parametersFile.exists()) {
				System.err.println("Session parameters file does not exist: " + parametersFile.getAbsolutePath());
				return;
			}
			if (!parametersFile.isFile()) {
				System.err.println("Session parameters file is not a file: " + parametersFile.getAbsolutePath());
				return;
			}

			// load session parameters
			loadParameters(new File(args[parameterParametersFile]));

			// test groups
			if (args.length < parameterTestGroup + 1) {
				addGroup(new ReadOnlyTestGroup());
			} else {
				String testGroupStr = args[parameterTestGroup];
				if (GROUP_READ_ONLY.equalsIgnoreCase(testGroupStr)) {
					addGroup(new ReadOnlyTestGroup());
				} else if (GROUP_READ_WRITE.equalsIgnoreCase(testGroupStr)) {
					addGroup(new ReadWriteTestGroup());
				} else if (GROUP_ALL.equalsIgnoreCase(testGroupStr)) {
					addGroup(new ReadOnlyTestGroup());
					addGroup(new ReadWriteTestGroup());
				} else if (GROUP_OPENCMIS.equalsIgnoreCase(testGroupStr)) {
					loadDefaultTckGroups();
				} else {
					System.err.println("Unknown test group!");
					System.exit(3);
				}
			}

			// report preparation
			CmisTestReport report = null;
			if (FORMAT_TEXT.equalsIgnoreCase(reportFormat)) {
				report = new TextReport();
			} else if (FORMAT_HTML.equalsIgnoreCase(reportFormat)) {
				report = new HtmlReport();
			} else if (FORMAT_XML.equalsIgnoreCase(reportFormat)) {
				report = new XmlReport();
			} else if (FORMAT_JSON.equalsIgnoreCase(reportFormat)) {
				report = new JsonReport();
			} else {
				System.err.println("Unknown report format!");
				System.exit(3);
			}

			// read user
			String username = null;
			if (readUser) {
				username = readLine();
				if (username == null || username.isEmpty()) {
					System.err.println("Please enter a username!");
					System.exit(2);
				}

				getParameters().put(SessionParameter.USER, username);
			}

			// read password
			char[] password = null;
			if (readPassword) {
				password = readPassword();
				if (password == null || password.length == 0) {
					System.err.println("Please enter a password!");
					System.exit(2);
				}

				getParameters().put(SessionParameter.PASSWORD, new String(password));
			}

			// run tests
			run(new ConsoleProgressMonitor());

			// create report
			Writer writer = null;
			try {
				if ("-".equals(reportFile) || reportFile.isEmpty()) {
					writer = new PrintWriter(System.out);
				} else {
					File file = new File(reportFile);
					System.out.println("Writing report to: " + file.getCanonicalPath());
					writer = new FileWriter(file);
				}

				Map<String, String> reportParameters = new TreeMap<String, String>(getParameters());
				if (reportParameters.containsKey(SessionParameter.PASSWORD)) {
					reportParameters.put(SessionParameter.PASSWORD, "*****");
				}

				report.createReport(reportParameters, getGroups(), writer);
			} catch (Exception ioe) {
				System.err.println("Could not create report: " + ioe.toString());
				System.exit(1);
			} finally {
				IOUtils.closeQuietly(writer);
			}

			System.exit(0);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.toString());
			System.exit(1);
		}
	}

	private void printUsage() {
		System.out.println("Usage: runtck"
				+ " [-p] [-v] [-report=<file>] [-report-format=<format>] <path-to-session-parameters-file> [test-group]\n");
		System.out.println("Parameters:\n");
		System.out.println(" -u                                 ask for username");
		System.out.println(" -p                                 ask for password");
		System.out.println(" -report=<file>                     report file name (default is stdout)");
		System.out.println(" -report-format=<format>            report file format (default is text)");
		System.out.println("                                       text  - plain text report");
		System.out.println("                                       html  - HTML report");
		System.out.println("                                       xml   - XML report");
		System.out.println("                                       json  - JSON report");
		;
		System.out.println(" <path-to-session-parameters-file>  "
				+ "path of the properties file that contains the session parameters");
		System.out.println(" [test-group]                       TCK test group");
		System.out.println("                                       read-only  - read-only tests (default)");
		System.out.println("                                       read-write - read-write tests");
		System.out.println("                                       all        - read-only and read-write tests");
		System.out.println("                                       opencmis   - all OpenCMIS TCK tests");
	}

	/**
	 * Reads a line from the console.
	 */
	private String readLine() {
		Console console = System.console();
		if (console == null) {
			try {
				System.out.print("Username: ");

				char[] line = readFromSystemIn();
				if (line == null) {
					return null;
				}

				return new String(line);
			} catch (IOException ioe) {
				return null;
			}
		} else {
			return console.readLine("Username: ");
		}
	}

	/**
	 * Reads a password from the console.
	 */
	private char[] readPassword() {
		Console console = System.console();
		if (console == null) {
			try {
				System.out.println("WARNING: Password will be echoed on screen!");
				System.out.print("Password: ");

				return readFromSystemIn();
			} catch (IOException ioe) {
				return null;
			}
		} else {
			return console.readPassword("Password: ");
		}
	}

	private char[] readFromSystemIn() throws IOException {
		InputStreamReader reader = new InputStreamReader(System.in);
		char[] buffer = new char[128];

		int l = reader.read(buffer) - 1;
		if (l > 0) {
			char[] result = new char[l];
			System.arraycopy(buffer, 0, result, 0, l);
			return result;
		} else {
			return null;
		}
	}

	/**
	 * Test progress monitor.
	 */
	private static class ConsoleProgressMonitor implements CmisTestProgressMonitor {
		@Override
		public void startGroup(CmisTestGroup group) {
			System.out.println();
			System.out.println(group.getName() + " (" + group.getTests().size() + " tests)");
		}

		@Override
		public void endGroup(CmisTestGroup group) {
			System.out.println();
		}

		@Override
		public void startTest(CmisTest test) {
			System.out.print("  " + test.getName());
		}

		@Override
		public void endTest(CmisTest test) {
			System.out.print(" (" + test.getTime() + "ms): ");
			System.out.println(getWorst(test.getResults()));
		}

		@Override
		public void message(String msg) {
			System.out.println(msg);
		}

		private CmisTestResultStatus getWorst(List<CmisTestResult> results) {
			if (results == null || results.isEmpty()) {
				return CmisTestResultStatus.OK;
			}

			int max = 0;

			for (CmisTestResult result : results) {
				if (max < result.getStatus().getLevel()) {
					max = result.getStatus().getLevel();
				}
			}

			return CmisTestResultStatus.fromLevel(max);
		}
	}

	public static void main(String[] args) throws Exception {
		new ConsoleRunner(args);
	}
}
