/*
 * OsCollector - Collect system usage of client and servers in the cluster.
 *
 * Copyright (C) 2016, Denis Lussier
 * Copyright (C) 2016, Jan Wieck
 * Copyright (C) 2022, Huidong Zhang, Luyi Qu
 */

package edu.ecnu.dike.perf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.ecnu.dike.config.RuntimeProperty;

public class OsCollector {

	private final Logger log = Logger.getLogger(OsCollector.class);

	private volatile boolean endCollection = false;
	private ArrayList<Thread> threadList = new ArrayList<Thread>();

	public OsCollector(RuntimeProperty runtimeProperty, File outputDir) throws IOException {
		File sshFileDir;
		String script = runtimeProperty.getOsCollectorScript();
		String sshAddresses[] = runtimeProperty.getOsCollectorSSHAddr().split(",");
		String deviceNames[] = runtimeProperty.getOsCollectorDevices().split(",");
		for (String address : sshAddresses) {
			// each server machine owns sshAddress and several csv files
			sshFileDir = new File(outputDir, address);
			if (!sshFileDir.mkdir()) {
				throw new IOException("fail to create directory '" + sshFileDir.getPath() + "'");
			}

			// organize cmd line(need to copy ssh key to each server first)
			ArrayList<String> cmdLine = new ArrayList<String>();
			cmdLine.add("ssh");
			cmdLine.add(address);
			cmdLine.add("python");
			cmdLine.add(script);
			BufferedWriter[] resultCSVs = new BufferedWriter[deviceNames.length + 1];
			resultCSVs[0] = new BufferedWriter(new FileWriter(new File(sshFileDir, "sys_info.csv")));
			for (int i = 0; i < deviceNames.length; i++) {
				cmdLine.add(deviceNames[i]);
				resultCSVs[i + 1] = new BufferedWriter(new FileWriter(new File(sshFileDir, deviceNames[i] + ".csv")));
			}
			threadList.add(new Thread(new CmdRun(script, cmdLine, resultCSVs)));
		}
	}

	// client send start signal to oscollector
	public void start() {
		for (Thread thread : threadList) {
			thread.start();
		}
	}

	// client send end signal to oscollector
	public void stop() {
		endCollection = true;
		try {
			for (Thread thread : threadList) {
				thread.join();
			}
		} catch (InterruptedException ie) {
			log.error("get interruption while waiting for os collector workers finish " + ie.getMessage());
		}
	}

	// work threads
	private class CmdRun implements Runnable {
		private Process collProc;
		private String script;
		private ArrayList<String> cmdLine;
		private BufferedWriter[] resultCSVs;

		public CmdRun(String script, ArrayList<String> cmdLine, BufferedWriter[] resultCSVs) {
			this.script = script;
			this.cmdLine = cmdLine;
			this.resultCSVs = resultCSVs;
		}

		public void run() {
			// generate the system information from the server, start a process to avoid the
			// impact on result recording
			try {
				ProcessBuilder pb = new ProcessBuilder(cmdLine);
				pb.redirectError(ProcessBuilder.Redirect.INHERIT);
				collProc = pb.start();

				// send script file to ssh standard input
				BufferedReader scriptReader = new BufferedReader(new FileReader(script));
				BufferedWriter scriptWriter = new BufferedWriter(new OutputStreamWriter(collProc.getOutputStream()));
				String line;
				while ((line = scriptReader.readLine()) != null) {
					scriptWriter.write(line);
					scriptWriter.newLine();
				}
				scriptWriter.close();
				scriptReader.close();
			} catch (IOException ioe) {
				log.error("fail to start helper process, " + ioe.getMessage() + ", thread terminated");
				throw new RuntimeException("fail to start helper process");
			}

			// record the system information according to ssh standard output
			BufferedReader osData;
			String line;
			int resultIdx = 0;
			osData = new BufferedReader(new InputStreamReader(collProc.getInputStream()));

			// resultIdx not equal zero to ensure integrate results
			while (!endCollection || resultIdx != 0) {
				try {
					line = osData.readLine();
					if (line == null) {
						log.error("unexpected EOF while reading from external helper process");
						break;
					}
					resultCSVs[resultIdx].write(line);
					resultCSVs[resultIdx].newLine();
					resultCSVs[resultIdx].flush();
					if (++resultIdx >= resultCSVs.length) {
						resultIdx = 0;
					}
				} catch (IOException ioe) {
					log.error("fail to record data from helper process, " + ioe.getMessage());
					break;
				}
			}

			try {
				osData.close();
				for (int i = 0; i < resultCSVs.length; i++) {
					resultCSVs[i].close();
				}
			} catch (IOException ioe) {
				log.error("fail to close file reader or writer, " + ioe.getMessage());
			}
		}
	}
}