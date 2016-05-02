package com.zerogerc.photoframe.util;

import com.yandex.disk.client.DownloadListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class inherits yandex {@link DownloadListener}. You could get all loaded data using method {@link #getData()}.
 */
public class ByteDownloader extends DownloadListener {
    private ByteArrayOutputStream stream;

    @Override
    public OutputStream getOutputStream(boolean append) throws IOException {
        stream = new ByteArrayOutputStream();
        return stream;
    }

    /**
     * Get all currently loaded data.
     * @return currently loaded data
     */
    public byte[] getData() {
        return stream.toByteArray();
    }
}
