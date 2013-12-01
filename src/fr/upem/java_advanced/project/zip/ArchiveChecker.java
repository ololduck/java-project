package fr.upem.java_advanced.project.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

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
	 *            the path of a zip file
	 * @return true, if the file only has one directory at the root.
	 * @throws IOException 
	 */
	public static boolean isOnetopZipArchive(Path p) throws IOException {
		if (!isZipArchive(p)) {
			throw new IllegalArgumentException("given file is no zipfile :/");
		}

		Path tmp = Files.createTempDirectory("dm_checker");
		Zip.extract(p, tmp);
		File root = tmp.toFile();
		short directoryCount = 0;
		for(File f: root.listFiles()) {
			if(f.isDirectory())
				directoryCount++;
			if(directoryCount > 1)
				return false;
		}
		
		return directoryCount == 1;
	}
}
