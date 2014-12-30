/*
 * Copyright © 2014 Daimler TSS. All Rights Reserved.
 *
 * Reproduction or transmission in whole or in part, in any form or by any
 * means, is prohibited without the prior written consent of the copyright
 * owner.
 * 
 * Created on: 20.08.2014
 * Created by: prestej
 * Last modified on: $Date: 2008/10/01 09:06:27MESZ $
 * Last modified by: $Author: Hardt, Dennis (dhardt7) prestej $
 */
package gradle.migration.tool.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GradleCaller {
	String workingDirectory;
	String argument;

	public GradleCaller(String workingDirectory, String argument) {
		this.workingDirectory = workingDirectory;
		this.argument = argument;
	}

	public void execGradleCommand() {
		ProcessBuilder builtProcess = buildProcess();
		executeProcess(builtProcess);
	}

	private ProcessBuilder buildProcess() {
		ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/C", "gradle", argument);
		pb.directory(new File(workingDirectory));
		pb.redirectErrorStream(true);
		return pb;
	}

	private void executeProcess(ProcessBuilder pb) {
		try {
			Process process = pb.start();
			waitForProcessToFinish(process);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void waitForProcessToFinish(Process process) {
		try {
			clearOutputStream(process);
			process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void clearOutputStream(Process process) throws InterruptedException {
		BufferedReader buffRead = createBufferedReader(process);
		try {
			String line = buffRead.readLine();
			while (line != null) {
				System.out.println(line);
				line = buffRead.readLine();
			}
			buffRead.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private BufferedReader createBufferedReader(Process process) {
		InputStream inStream = process.getInputStream();
		InputStreamReader inStreamRead = new InputStreamReader(inStream);
		BufferedReader buffRead = new BufferedReader(inStreamRead);
		return buffRead;
	}
}
