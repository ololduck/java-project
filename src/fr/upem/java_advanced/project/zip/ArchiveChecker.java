/**
 * ArchiveChecker
 * 
 * Performs a number of verifications on a given ZIP file.
 */

package fr.upem.java_advanced.project.zip;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.logging.Logger;

import fr.upem.java_advanced.project.Main;
import fr.upem.java_advanced.project.zip.Zip;

public class ArchiveChecker {
	private static final Logger					logger					= Logger.getLogger("fr.upem.java_advanced.project");
	private static final Map<String, Boolean>	enabledForceParameters	= new HashMap<>();

	private final List<Path>					forbiddenFilesFound		= new ArrayList<>();
	private final Map<String, Boolean>			seenRequiredFiles		= new HashMap<>();

	private final Path							archivePath;

	public ArchiveChecker(Path archivePath) {
		this.archivePath = archivePath;
		enabledForceParameters.put("forcebeginsWith", false);
		enabledForceParameters.put("forceendssWith", false);
		enabledForceParameters.put("forceinterdit", false);
		enabledForceParameters.put("forceonetop", false);
		enabledForceParameters.put("forceexiste", false);
		checkForForceParameters();
	}

	/**
	 * sets some bits to true if --force options are active (such as
	 * --forcebeginsWith,...). FIXME: rewrite that. its just plain ugly.
	 */
	private void checkForForceParameters() {
		if (Main.cliArgs.getStringArray("forcebeginsWith").length > 0)
			enabledForceParameters.put("forcebeginsWith", true);
		if (Main.cliArgs.getStringArray("forceendsWith").length > 0)
			enabledForceParameters.put("forceendsWith", true);
		if (Main.cliArgs.getStringArray("forceinterdit").length > 0)
			enabledForceParameters.put("forceinterdit", true);
		if (Main.cliArgs.getStringArray("forceonetop").length > 0)
			enabledForceParameters.put("forceonetop", true);
		if (Main.cliArgs.getStringArray("forceexiste").length > 0)
			enabledForceParameters.put("forceexiste", true);
	}

	/**
	 * Returns if the given zip file has only one directory at the root.
	 * 
	 * Since we can't have any information about the directory tree directly in
	 * the zip, we have to deflate the zip file to a tmp dir, and check the tree
	 * while we are deflating in order to be sure we only have ONE directory at
	 * the root.
	 * 
	 * GAH.
	 * 
	 * @param p
	 *            the path to a directory to check
	 * @return true, if the file only has one directory at the root.
	 * @throws IOException
	 */
	public static boolean isOnetop(Path p, String expectedName) throws IOException {
		File root = p.toFile();
		// first, check if there is only one directory at the root
		File onetopFolder = null;
		short directoryCount = 0;
		for (File f : root.listFiles()) {
			if (f.isDirectory()) {
				directoryCount++;
				onetopFolder = f;
			}
			if (directoryCount > 1)
				return false;
		}
		// then, we check if this directory has the right name
		if (onetopFolder != null && !onetopFolder.getName().equals(expectedName))
			return false;

		return directoryCount == 1;
	}
	private String buildSeenMap(String array[]) {
		Map<String, Boolean> m = new HashMap<>();
		String mustMatchRegex = null;
		if (array.length != 0) {
			mustMatchRegex = generateRegexFromArray(array);
		}
		return mustMatchRegex;
	}

	/**
	 * Performs some various checks on a given archive file, according to the
	 * given CLI arguments.
	 * 
	 * This function unzips the file in a temporary directory.
	 * 
	 * @return a Path to the temporary directory containing the unzipped file,
	 *         for later use.
	 * @throws IOException
	 */
	public Path checkArchive() throws IOException {
		if (!Zip.isZipArchive(this.archivePath))
			throw new IllegalArgumentException("Not a zip file: " + this.archivePath);
		Path tmp = Files.createTempDirectory("dm_checker");
		Zip.extract(this.archivePath, tmp);

		Map<String, Boolean> seenMap = new HashMap<>();

		final String mustMatch[] = Main.cliArgs.getStringArray("existe");
		String mustMatchRegex = "";
		if (mustMatch.length != 0) {
			for (String word : mustMatch)
				seenMap.put(word, false);
			mustMatchRegex = generateRegexFromArray(mustMatch);
		}

		String interditRegex = generateRegexFromArray(Main.cliArgs.getStringArray("interdit"));
		logger.fine("interdit: " + interditRegex);

		String beginsWithRegex = generateRegexFromArray(Main.cliArgs.getStringArray("beginsWith")) + ".*";
		logger.fine("begins with: " + beginsWithRegex); 
		String endsWithRegex = ".*" + generateRegexFromArray(Main.cliArgs.getStringArray("endsWith"));
		logger.fine("endsWith: " + endsWithRegex);
		String onetopFileName = Main.cliArgs.getString("onetop");

		boolean isFirstIteration = true;
		tmp.forEach((path) -> {
			logger.fine(path.toString());
			if (isFirstIteration) {
				try {
				ArchiveChecker.isOnetop(path, onetopFileName);
				//isFirstIteration = false;
				}catch(IOException e) {
					logger.warning("IOEXCEPTION!!!");
				}
			}
			// do this with every cli arg
			if (path.toString().matches(buildSeenMap(mustMatch))) {
				seenMap.put(path.toFile().getName(), true);
			}
			if (path.toString().matches(interditRegex)) {
				logger.warning("file " + path + " has a forbidden name");
			}
			if (path.toString().matches(beginsWithRegex)) {
				logger.warning("file " + path + " has a forbidden name");
			}
			if (path.toString().matches(endsWithRegex)) {
				logger.warning("file " + path + " has a forbidden name");
			}
		});

		// then check if all required files are here
		for (Entry<String, Boolean> me : seenMap.entrySet()) {
			if (me.getValue() == false) {
				logger.warning("required file " + me.getKey() + " has not been found in" + this.archivePath);
			}
		}
		return tmp;
	}

	private String generateRegexFromArray(String array[]) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (String end : array) {
			sb.append(end + "|");
		}
		sb.deleteCharAt(sb.length() - 1);
		if (sb.length() > 0) {
			sb.append(")");
		}
		return sb.toString();
	}

	private void recursivlyCheckFolders(File dir, String filesNotMustMatch, String filesMustMatch) {
		Queue<File> toProcess = new LinkedList<>();
		toProcess.add(dir);
		Path rootPath = dir.toPath();
		while (!toProcess.isEmpty()) {
			File f = toProcess.poll();
			for (File child : f.listFiles()) {
				Path childPath = rootPath.relativize(child.toPath());
				logger.info("handling file " + child.getName());
				if (seenRequiredFiles.containsKey(childPath.toString())) {
					seenRequiredFiles.put(childPath.toString(), true);
				}
				if (child.isDirectory()) {
					toProcess.add(child);
				}
				if (child.getName().matches(filesNotMustMatch) || !child.getName().matches(filesMustMatch)) {
					forbiddenFilesFound.add(childPath);
				}
			}
		}
	}
}
