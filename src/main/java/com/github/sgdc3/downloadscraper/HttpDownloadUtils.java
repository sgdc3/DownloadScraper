package com.github.sgdc3.downloadscraper;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@UtilityClass
public class HttpDownloadUtils {

    private static final int BUFFER_SIZE = 4096;

    public static void downloadFile(final @NonNull String downloadUrl, final @NonNull File destination) throws IOException {
        final URL url = new URL(downloadUrl);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        final int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            connection.disconnect();
            throw new IOException("No file to download. Server replied HTTP code: " + responseCode);
        }

        final String disposition = connection.getHeaderField("Content-Disposition");

        String fileName = "";
        if (disposition != null) {
            int index = disposition.indexOf("filename=");
            if (index > 0) {
                fileName = disposition.substring(index + 10, disposition.length() - 1);
            }
        } else {
            fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1, downloadUrl.length());
        }

        try (InputStream inputStream = connection.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(new File(destination, fileName))) {
            int bytesRead;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        connection.disconnect();
    }
}
