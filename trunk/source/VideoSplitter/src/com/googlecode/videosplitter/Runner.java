/*
 *  Video Splitter
 * 
 *  Copyright (c) 2014 Ted Hive <tedhive@gmail.com>
 *  
 *  Developed from mp4splitter [ https://code.google.com/p/mp4splitter/ ]
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.googlecode.videosplitter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class Runner {

    private static Logger logger = Logger.getLogger(Runner.class);
    
	public static void main(String[] args) {
		initLog();
		logger.info("Start Video Splitter");
		try {
			new Window().open();
		} catch (Throwable e) {
			logger.error("", e);
		}
		logger.info("End Video Splitter");
		System.exit(0);
	}

	private static void initLog() {
		try {
			String configDir = new File(System.getProperty("user.dir"), "cfg").getAbsolutePath();
			String logConfigFile = new File(configDir, "log-conf.xml").getAbsolutePath();
			String logDir = new File(System.getProperty("user.dir"), "log").getAbsolutePath();
			String stdoutLogFile = new File(logDir, "stdout.log").getAbsolutePath();

			DOMConfigurator.configure(logConfigFile);
			System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(stdoutLogFile)), true));
			System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(stdoutLogFile)), true));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
