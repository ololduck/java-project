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
	 * Extract the zip in the given folder WARNING : If the archive is not
	 * onetop-proof, the decompression may invade the folder.
	 * 
	 * @param path
	 *            the path to the zip file
	 * @param dst
	 *            the top father folder where the zip will be extracted
	 * @return the path to the extracted folder
	 */
	public static Path extract(Path path, Path dst) {
		if (!ArchiveChecker.isZipArchive(path)) {
			throw new IllegalArgumentException(path + " is not a correct zip file");
		}

		if (!dst.toFile().isDirectory()) {
			throw new IllegalArgumentException(dst + " is not a correct folder");
		}

		try (ZipInputStream inputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(path.toFile())))) {

			boolean firstFile = true;

			ZipEntry ze = inputStream.getNextEntry();
			if (ze == null) {
				throw new IllegalStateException("The archive is empty");
			}

			do {
				File f;

				if (!firstFile) {
					f = new File(dst.toFile(), ze.getName());
					f.getParentFile().mkdir();
				} else {
					f = new File(dst.toFile(), "./toto");
					firstFile = false;
				}

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
			} while ((ze = inputStream.getNextEntry()) != null);

		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Error on reading the source or writing on the destination");
		}

		return null;
	}
}
