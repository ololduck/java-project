package fr.upem.java_advanced.project.zip;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

import com.martiansoftware.jsap.JSAPResult;

public class ArchiveChecker {
	private static final Logger	logger	= Logger.getLogger("fr.upem.java_advanced.project");

	/**
	 * Performs a check on a file
	 * 
	 * @param p
	 *            the path to the file to check
	 * @return true, if is a zip file
	 * @throws IOException
	 */
	public static boolean isZipArchive(Path p) {
		ZipFile zip = null;
		try {
			zip = new ZipFile(p.toFile());
		} catch (IOException e) {
			return false;
		} finally {
			if (zip != null) {
				try {
					zip.close();
				} catch (IOException e) {
					return false;
				}
				return true;
			}
		}
		return false;
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
		//first, check if there is only one directory at the root
		File onetopFolder=null;
		short directoryCount = 0;
		for (File f : root.listFiles()) {
			if (f.isDirectory()) {
				directoryCount++;
				onetopFolder = f;
			}
			if (directoryCount > 1)
				return false;
		}
		//then, we check if this directory has the right name
		if(onetopFolder != null && !onetopFolder.getName().equals(expectedName))
			return false;

		return directoryCount == 1;
	}

	/**
	 * Performs some various checks on a given archive file, according to the
	 * given CLI arguments.
	 * 
	 * This function unzips the file in a temporary directory.
	 * 
	 * @param p
	 *            The path to an archive file
	 * @param cliArgs
	 *            CLI arguments, defining the behaviour of this method.
	 * @throws IOException
	 */
	public static void checkArchive(Path p, JSAPResult cliArgs) throws IOException {
		if (!isZipArchive(p))
			throw new IllegalArgumentException("Not a zip file: " + p);
		Path tmp = Files.createTempDirectory("dm_checker");
		Zip.extract(p, tmp);

		StringBuilder mustMatchBuilder = new StringBuilder();
		Map<String, Boolean> seenMap = new HashMap<>();

		String mustMatch[] = cliArgs.getStringArray("existe");
		if (mustMatch.length != 0) {
			for (String word : mustMatch) {
				mustMatchBuilder.append(word + "|");
				seenMap.put(word, false);
			}
			if (mustMatchBuilder.length() > 0)
				mustMatchBuilder.deleteCharAt(mustMatchBuilder.length() - 1);
		}

		StringBuilder mustNotMatchBuilder = new StringBuilder();

		String mustNotBePresent[] = cliArgs.getStringArray("interdit");
		if (mustNotBePresent.length != 0) {
			String r = generateRegexFromArray(mustNotBePresent);
			mustNotMatchBuilder.append(r);
		}

		String mustNotBeginWith[] = cliArgs.getStringArray("beginsWith");
		if (mustNotBeginWith.length != 0) {
			if (mustNotMatchBuilder.length() > 0)
				mustNotMatchBuilder.append("|");
			mustNotMatchBuilder.append(generateRegexFromArray(mustNotBeginWith));
		}
		String mustEndWith[] = cliArgs.getStringArray("endsWith");
		if (mustNotBePresent.length != 0 && mustNotBeginWith.length == 0 && mustEndWith.length != 0)
			mustNotMatchBuilder.append("|");
		if (mustNotBeginWith.length != 0 && mustEndWith.length != 0)
			mustNotMatchBuilder.append(".*");

		if (mustEndWith.length != 0) {
			mustNotMatchBuilder.append(generateRegexFromArray(mustEndWith));
		}
		if (mustNotMatchBuilder.length() > 0)
			logger.info("file must not match " + mustNotMatchBuilder.toString());
		if (mustMatchBuilder.length() > 0)
			logger.info("file must match " + mustMatchBuilder.toString());
		recursivlyCheckFolders(tmp.toFile(), mustNotMatchBuilder.toString(), mustMatchBuilder.toString(), seenMap);
		// then check if all required files are here
		for(Entry<String, Boolean> me : seenMap.entrySet()) {
			if(me.getValue() == false) {
				logger.warning("required file " + me.getKey() + " has not been found in" + p);
			}
		}
	}

	private static String generateRegexFromArray(String array[]) {
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

	private static void recursivlyCheckFolders(File dir, String filesNotMustMatch, String filesMustMatch, Map<String, Boolean> seenMap) {
		Queue<File> toProcess = new LinkedList<>();
		toProcess.add(dir);
		Path rootPath = dir.toPath();
		while (!toProcess.isEmpty()) {
			File f = toProcess.poll();
			for (File child : f.listFiles()) {
				logger.info("handling file " + child.getName());
				if(seenMap.containsKey(rootPath.relativize(child.toPath()).toString())) {
					seenMap.put(rootPath.relativize(child.toPath()).toString(), true);
				}
				if (child.isDirectory()) {
					toProcess.add(child);
				}
				if (child.getName().matches(filesNotMustMatch) || !child.getName().matches(filesMustMatch)) {
					logger.warning("File " + rootPath.relativize(child.toPath()) + " matches forbidden filenames!");
				}
			}
		}
	}
}
