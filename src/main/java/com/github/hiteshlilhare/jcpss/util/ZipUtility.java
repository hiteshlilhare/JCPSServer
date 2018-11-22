package com.github.hiteshlilhare.jcpss.util;

import com.github.hiteshlilhare.jcpss.JCPSSCOnstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;

/**
 * Inspired by the code at below mentioned link
 * https://github.com/eugenp/tutorials/blob/master/core-java-io
 *
 * @author Hitesh
 */
public class ZipUtility {

    public static void main(final String[] args) throws IOException {
        final String sourceFile = JCPSSCOnstants.JCPS_SRV_DIR + "/"
                + JCPSSCOnstants.JCPS_REMOTE_REPO + "/lilharesudha/OpenPGPApplet/v0.2";
        final String destFile = JCPSSCOnstants.JCPS_SRV_DIR + "/"
                + JCPSSCOnstants.JCPS_SRV_TEMP_DIR + "/lilharesudha.OpenPGPApplet.v0.2.zip";
        zip(sourceFile, destFile);

        /*final String sourceFile = JCPSSCOnstants.JCPS_SRV_DIR+"/"
                +JCPSSCOnstants.JCPS_REMOTE_REPO+"/lilharesudha/OpenPGPApplet/v0.2";
        final FileOutputStream fos = new FileOutputStream(JCPSSCOnstants.JCPS_SRV_DIR+"/"
                +JCPSSCOnstants.JCPS_SRV_TEMP_DIR+"/lilharesudha.OpenPGPApplet.v0.2.zip");
        final ZipOutputStream zipOut = new ZipOutputStream(fos);
        final File fileToZip = new File(sourceFile);

        zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();*/
    }

    public static void zip(final String srcFile,
            final String destFile) throws IOException {
        File destFileObj = new File(destFile);
        if (destFileObj.exists()) {
            FileUtils.forceDelete(new File(destFile));
        }
        final String sourceFile = srcFile;
        final FileOutputStream fos = new FileOutputStream(destFile);
        final ZipOutputStream zipOut = new ZipOutputStream(fos);
        final File fileToZip = new File(sourceFile);

        zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();
    }

    private static void zipFile(final File fileToZip, final String fileName, final ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            final File[] children = fileToZip.listFiles();
            for (final File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        final FileInputStream fis = new FileInputStream(fileToZip);
        final ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        final byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }
}
