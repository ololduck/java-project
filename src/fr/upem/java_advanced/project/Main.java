package fr.upem.java_advanced.project;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

public class Main {

	private static final String	LOGFILE_NAME	= "DMChecker.log";
	public static JSAPResult	cliArgs;

	public static void main(String[] args) {

		Logger logger = Logger.getLogger("fr.upem.java_advanced.project");
		try {
			FileHandler fh = new FileHandler(LOGFILE_NAME);
			fh.setLevel(Level.ALL);
			fh.setFormatter(new SimpleFormatter());
		} catch (SecurityException | IOException e1) {
			logger.warning("Could not open log file \"" + LOGFILE_NAME + "\": " + e1.getMessage());
		}

		JSAP jsap = new JSAP();
		Switch suddenDeath = new Switch("CheckArchives").setShortFlag('1').setLongFlag("sudden-death");
		UnflaggedOption archives = new UnflaggedOption("archives").setStringParser(JSAP.STRING_PARSER).setRequired(true).setGreedy(true);
		FlaggedOption onetop = new FlaggedOption("onetop").setShortFlag('o').setLongFlag("onetop").setStringParser(JSAP.STRING_PARSER).setAllowMultipleDeclarations(false).setRequired(false);
		onetop.setHelp("Un seul sous répertoire de nom <onetop> dans le répertoire racine de l'archive sans compter les répertoires et fichiers ignorés comme : './ '");
		FlaggedOption endsWith = new FlaggedOption("endsWith").setShortFlag('e').setLongFlag("endsWith").setStringParser(JSAP.STRING_PARSER).setAllowMultipleDeclarations(true).setRequired(false);
		endsWith.setHelp("les fichiers dont le nom se termine par la <endswith>  sont interdits par exemple: -e ~ pour les noms de fichiers terminant par ~ ou -e __MACOSX ces fichiers/répertoires seront ignorés à la décompression");
		FlaggedOption beginsWith = new FlaggedOption("beginsWith").setShortFlag('b').setLongFlag("beginsWith").setStringParser(JSAP.STRING_PARSER).setAllowMultipleDeclarations(true).setRequired(false);
		beginsWith.setHelp("les fichiers/répertoires commençant par <startswith> sont interdits. Par exemple --beginsWith f_ pour les noms de  fichiers commençant par f_");
		FlaggedOption existe = new FlaggedOption("existe").setShortFlag('x').setLongFlag("existe").setStringParser(JSAP.STRING_PARSER).setAllowMultipleDeclarations(true).setRequired(false);
		existe.setHelp("Vérifie la présence du fichier ou répertoire (regex). Le répertoire top level est ignoré dans la comparaison. Par exemple:-x index.html");
		FlaggedOption interdit = new FlaggedOption("interdit").setShortFlag('i').setLongFlag("interdit").setStringParser(JSAP.STRING_PARSER).setAllowMultipleDeclarations(true).setRequired(false);
		interdit.setHelp("Vérifie l'absence du fichier ou répertoire de regex <interdit> le repertoire top level est ignoré dans la comparaison. Par exemple : --interdit __MACOSX");
		try {
			jsap.registerParameter(suddenDeath);
			jsap.registerParameter(archives);
			jsap.registerParameter(onetop);
			jsap.registerParameter(endsWith);
			jsap.registerParameter(beginsWith);
			jsap.registerParameter(existe);
			jsap.registerParameter(interdit);
		} catch (JSAPException e) {
			logger.severe("Could not register a cli parameter: " + e.getMessage());
		}

		cliArgs = jsap.parse(args);
		if (!cliArgs.success()) {
			System.err.println(jsap.getHelp());
		}

		/* do the function calls, etc... */
	}

}
