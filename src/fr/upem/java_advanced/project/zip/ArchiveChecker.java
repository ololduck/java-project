package fr.upem.java_advanced.project.zip;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

	/**
	 * Returns if the given zip file has only one directory at the root.
	 * 
	 * @param p
	 *            the Path of a zip file
	 * @return true, if the file only has one directory at the root.
	 */
	public static boolean isOnetopZipArchive(Path p) {
		if (!isZipArchive(p)) {
			throw new IllegalArgumentException("given file is no zipfile :/");
		}
		try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(p.toFile())))) {
			
			ZipEntry ze = zis.getNextEntry();
			logger.fine(ze.getName());
			System.out.println(ze.getName());
			if (!ze.isDirectory()) {
				return false;
			}
			
			ze = zis.getNextEntry();
			
			while((ze = zis.getNextEntry()) != null) {

				System.out.println(ze.getName());
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
