package xyz.pengzhihui.esp32ipcam;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity implements View.OnClickListener
{

    private static final String TAG = "MainActivity::";

    private HandlerThread handlerThread;
    private Handler handler;
    private ImageView imageView;

    private final int DOWNDLOAD = 1;
    private final int REGISTER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.downloadFile).setOnClickListener(this);
        findViewById(R.id.register).setOnClickListener(this);
        imageView = findViewById(R.id.img);

        handlerThread = new HandlerThread("http");
        handlerThread.start();
        handler = new HttpHandler(handlerThread.getLooper());
    }


    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.downloadFile:
                handler.sendEmptyMessage(DOWNDLOAD);
                break;
            case R.id.register:
                handler.sendEmptyMessage(REGISTER);
                break;
            default:
                break;
        }
    }


    private class HttpHandler extends Handler
    {
        public HttpHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case DOWNDLOAD:
                    downloadFile();
                    break;
                case REGISTER:
                    registerUser();
                    break;
                default:
                    break;
            }
        }
    }


    private void registerUser()
    {

        String registerUrl = "http://169.254.230.253:8080/register";
        try
        {
            URL url = new URL(registerUrl);
            HttpURLConnection postConnection = (HttpURLConnection) url.openConnection();
            postConnection.setRequestMethod("POST");
            postConnection.setConnectTimeout(1000 * 5);
            postConnection.setReadTimeout(1000 * 5);
            postConnection.setDoInput(true);
            postConnection.setDoOutput(true);
            postConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            String postParms = "name=1702C2019&password=12345&verifyCode=8888";
            OutputStream outputStream = postConnection.getOutputStream();
            outputStream.write(postParms.getBytes());//把参数发送过去.
            outputStream.flush();
            final StringBuffer buffer = new StringBuffer();
            int code = postConnection.getResponseCode();
            if (code == 200)
            {
                InputStream inputStream = postConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                while ((line = bufferedReader.readLine()) != null)
                {
                    buffer.append(line);
                }
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(MainActivity.this, buffer.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void downloadFile()
    {
        String downloadUrl = "http://192.168.43.65:81/stream";
        String savePath = "/sdcard/pic.jpg";

        File file = new File(savePath);
        if (file.exists())
        {
            file.delete();
        }

        BufferedInputStream bufferedInputStream = null;
        FileOutputStream outputStream = null;
        try
        {
            URL url = new URL(downloadUrl);

            try
            {
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setConnectTimeout(1000 * 5);
                httpURLConnection.setReadTimeout(1000 * 5);
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                if (httpURLConnection.getResponseCode() == 200)
                {
                    InputStream in = httpURLConnection.getInputStream();

                    InputStreamReader isr = new InputStreamReader(in);
                    BufferedReader bufferedReader = new BufferedReader(isr);

                    String line;
                    StringBuffer stringBuffer = new StringBuffer();

                    int i = 0;

                    int len;
                    byte[] buffer;

                    while ((line = bufferedReader.readLine()) != null)
                    {
                        if (line.contains("Content-Type:"))
                        {
                            line = bufferedReader.readLine();

                            len = Integer.parseInt(line.split(":")[1].trim());

                            bufferedInputStream = new BufferedInputStream(in);
                            buffer = new byte[len];

                            int t = 0;
                            while (t < len)
                            {
                                t += bufferedInputStream.read(buffer, t, len - t);
                            }

                            bytesToImageFile(buffer, "0A.jpg");

                            final Bitmap bitmap = BitmapFactory.decodeFile("sdcard/0A.jpg");
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    imageView.setImageBitmap(bitmap);
                                }
                            });
                        }


                    }
                }

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (bufferedInputStream != null)
                {
                    bufferedInputStream.close();
                }
                if (outputStream != null)
                {
                    outputStream.close();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }

    private void bytesToImageFile(byte[] bytes, String fileName)
    {
        try
        {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes, 0, bytes.length);
            fos.flush();
            fos.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}