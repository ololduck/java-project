package fr.upem.java_advanced.project.zip;

import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ArchiveChecker {

	/**
	 * Performs a check on a file
	 * 
	 * @param Path
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

}
