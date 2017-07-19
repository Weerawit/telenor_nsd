package com.nokia.cloudband.cbnd.nsdgenerator.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

public class ZipUtil {

	public ZipUtil() {
		// TODO Auto-generated constructor stub
	}

	public static void makeZip(File output, String manifest, String mainYamlFileName, String mainYamlContent) throws IOException {

		ZipOutputStream zipOut = null;
		try {
			zipOut = new ZipOutputStream(new FileOutputStream(output));
	
			ZipEntry manifestEntry = new ZipEntry("MANIFEST");
			zipOut.putNextEntry(manifestEntry);
	
			zipOut.write(manifest.getBytes());
			zipOut.closeEntry();
	
			ZipEntry yamlEntry = new ZipEntry(mainYamlFileName);
			zipOut.putNextEntry(yamlEntry);
	
			zipOut.write(mainYamlContent.getBytes());
			zipOut.closeEntry();
		} finally {
			IOUtils.closeQuietly(zipOut);
		}
	}

}
