package com.zerogerc.photoframe;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.DownloadListener;
import com.yandex.disk.client.ListItem;
import com.yandex.disk.client.TransportClient;
import com.yandex.disk.client.exceptions.WebdavException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TestActivity extends AppCompatActivity  {
    public static final String ITEM_KEY = "item";
    public static final String CREDENTIALS_KEY = "credentials";

    private ListItem item;
    private Credentials credentials;
    private ImageView image;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);

        credentials = getIntent().getParcelableExtra(CREDENTIALS_KEY);
        item = getIntent().getParcelableExtra(ITEM_KEY);

        image = ((ImageView) findViewById(R.id.test_image));

        handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                TransportClient client = null;
                try {
                    final Downloader downloader = new Downloader();
                    client = TransportClient.getInstance(getApplicationContext(), credentials);
                    client.download(item.getFullPath(), downloader);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(getApplicationContext()).load(downloader.getData()).into(image);
                        }
                    });
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (WebdavException ex) {
                    ex.printStackTrace();
                } finally {
                    if (client != null) {
                        client.shutdown();
                    }
                }
            }
        }).start();

    }

    private class Downloader extends DownloadListener {
        private ByteArrayOutputStream stream;

        @Override
        public OutputStream getOutputStream(boolean append) throws IOException {
            stream = new ByteArrayOutputStream();
            return stream;
        }

        public byte[] getData() {
            return stream.toByteArray();
        }
    }
}
