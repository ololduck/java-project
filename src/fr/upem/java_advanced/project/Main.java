package fr.upem.java_advanced.project;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;


public class Main {
	
	private static final String LOGFILE_NAME = "DMChecker.log";

	public static void main(String[] args) {
		
		Logger logger = Logger.getLogger("fr.upem.java_advanced.project");
		FileHandler fh;
		try {
			fh = new FileHandler(LOGFILE_NAME);
			fh.setLevel(Level.ALL);
			fh.setFormatter(new SimpleFormatter());
		} catch (SecurityException | IOException e1) {
			logger.warning("Could not open log file \"" + LOGFILE_NAME + "\": " + e1.getMessage());
		}
		
		JSAP jsap = new JSAP();
		Switch suddenDeath = new Switch("CheckArchives").setShortFlag('1').setLongFlag("sudden-death");
        UnflaggedOption archives = new UnflaggedOption("archives").setStringParser(JSAP.STRING_PARSER).setRequired(true).setGreedy(true);
		
		try {
			jsap.registerParameter(suddenDeath);
			jsap.registerParameter(archives);
		} catch (JSAPException e) {
			logger.severe("Could not register a cli parameter: " +e.getMessage());
		}
		
		JSAPResult config = jsap.parse(args);
		
		/* do the function calls, etc...*/
	}
	
	
}
