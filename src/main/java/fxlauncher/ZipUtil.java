package fxlauncher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {
    public static byte[] getEntry(String fileName, InputStream is) {
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(is);
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                if (zipEntry.getName().equals(fileName)) {
                    return readStream(zis);
                }
                zipEntry = zis.getNextEntry();
            }
        } catch (Exception e) {
        } finally {
            if (zis != null) {
                try {
                    zis.closeEntry();
                    zis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static byte[] readStream(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }
}
