package fr.upem.java_advanced.project.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Zip {

	/**
	 * Extract the zip in the given folder.
	 * 
	 * @param path
	 *            the path to the zip file, will be test with
	 *            ArchiveChecker.isZipArchive
	 * @param dst
	 *            the top father folder where the zip will be extracted, must
	 *            already exist
	 */
	public static void extract(Path path, Path dst) {
		if (!ArchiveChecker.isZipArchive(path)) {
			throw new IllegalArgumentException(path + " is not a correct zip file");
		}

		if (!dst.toFile().isDirectory()) {
			throw new IllegalArgumentException(dst.toAbsolutePath() + " is not a correct folder");
		}

		try (ZipInputStream inputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(path.toFile())))) {

			ZipEntry ze = inputStream.getNextEntry();
			if (ze == null) {
				throw new IllegalStateException("The archive is empty");
			}

			while ((ze = inputStream.getNextEntry()) != null) {

				File f = new File(dst.toFile(), ze.getName());

				f.getParentFile().mkdir();

				if (ze.isDirectory()) {
					f.mkdir();
					continue;
				}

				try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(f))) {

					final byte[] buf = new byte[1024];

					int bytesRead;
					while (-1 != (bytesRead = inputStream.read(buf))) {
						fos.write(buf, 0, bytesRead);
					}

				} catch (Exception e) {
					f.delete();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Error on reading the source or writing on the destination");
		}

	}
}
