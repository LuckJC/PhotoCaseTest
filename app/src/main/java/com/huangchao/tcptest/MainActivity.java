package com.huangchao.tcptest;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "TcpTest";
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    protected static final int CHOOSE_APK_FILE_RESULT_CODE = 21;
    public String host_ip;
    public EditText editText;
    public EditText indexEt;
    public EditText timeEt;
    public int week = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        indexEt = findViewById(R.id.etIndex);
        timeEt = findViewById(R.id.etTime);

        /*new FileServerAsyncTask(MainActivity.this)
                .execute("TIME?date=20190808184500\n");*/
        new FileServerAsyncTask(MainActivity.this)
                .execute("VERSION\n");

        findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FileServerAsyncTask(MainActivity.this)
                        .execute("TIME?date=" + timeEt.getText().toString() + "\n");
            }
        });

        findViewById(R.id.button).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Allow user to pick an image from Gallery or other
                        // registered apps
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                    }
                });

        findViewById(R.id.button2).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        new FileServerAsyncTask(MainActivity.this)
                                .execute("RESET?week=" + week + "\n");
                    }
                });

        findViewById(R.id.button3).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        new FileServerAsyncTask(MainActivity.this)
                                .execute("SYNC\n");
                    }
                });

        findViewById(R.id.button4).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        new FileServerAsyncTask(MainActivity.this)
                                .execute("SHOW\n");
                    }
                });

        ((RadioGroup) findViewById(R.id.rgWeek)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_0:
                        week = 1;
                        break;
                    case R.id.rb_1:
                        week = 2;
                        break;
                    case R.id.rb_2:
                        week = 3;
                        break;
                    case R.id.rb_3:
                        week = 4;
                        break;
                    case R.id.rb_4:
                        week = 5;
                        break;
                    case R.id.rb_5:
                        week = 6;
                        break;
                    case R.id.rb_6:
                        week = 7;
                        break;
                }
            }
        });

        findViewById(R.id.button6).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Allow user to pick an image from Gallery or other
                        // registered apps
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("application/vnd.android.package-archive");
                        startActivityForResult(intent, CHOOSE_APK_FILE_RESULT_CODE);
                    }
                });

        findViewById(R.id.button7).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        new FileServerAsyncTask(MainActivity.this)
                                .execute("VERSION\n");
                    }
                });
    }

    public class FileServerAsyncTask extends AsyncTask<String, Void, String> {

        private Context context;
        private TextView statusText;
        int len = 0;
        byte[] buffer = new byte[1024];

        public FileServerAsyncTask(Context context) {
            //this.context = context;
            statusText = (TextView) findViewById(R.id.status_text);
        }

        @Override
        protected String doInBackground(String... params) {
            Socket socket = new Socket();
            String cmd = params[0];

            try {
                Log.d(MainActivity.TAG, "Opening client socket - ");
                socket.bind(null);
                host_ip = editText.getText().toString();
                socket.connect((new InetSocketAddress(host_ip, 8988)), 5000);
                Log.d(MainActivity.TAG, "Client socket - " + socket.isConnected());
                OutputStream stream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();
                stream.write(cmd.getBytes());
                len = inputStream.read(buffer);
                if (len > 0) Log.d(MainActivity.TAG, "buffer: " + new String(buffer).trim());
                socket.shutdownOutput();
                Log.d(MainActivity.TAG, "Client: Data written");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(MainActivity.TAG, e.getMessage());
            } finally {
                if (len > 0)
                    return new String(buffer).trim();
                else
                    return "ok";
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                //Intent intent = new Intent();
                //intent.setAction(android.content.Intent.ACTION_VIEW);
                //intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                statusText.setText(result);
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CHOOSE_FILE_RESULT_CODE) {
            // User has picked an image. Transfer it to group owner i.e peer using
            // FileTransferService.
            Uri uri = data.getData();
            TextView statusText = (TextView) findViewById(R.id.status_text);
            statusText.setText("Sending: " + uri);
            Log.d(TAG, "Intent----------- " + uri);
            Intent serviceIntent = new Intent(this, FileTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                    editText.getText().toString());
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);

            int picture_index = Integer.parseInt(indexEt.getText().toString());
            //Random r = new Random(1);
            int mon = 0;//r.nextInt(100);
            int tue = 0;//r.nextInt(100);
            int wed = 0;//r.nextInt(100);
            int thu = 0;//r.nextInt(100);
            int fri = 0;//r.nextInt(100);
            int sat = 0;//r.nextInt(100);
            int sun = 0;//r.nextInt(100);
            switch (week) {
                case 1:
                    sun = picture_index;
                    break;
                case 2:
                    mon = picture_index;
                    break;
                case 3:
                    tue = picture_index;
                    break;
                case 4:
                    wed = picture_index;
                    break;
                case 5:
                    thu = picture_index;
                    break;
                case 6:
                    fri = picture_index;
                    break;
                case 7:
                    sat = picture_index;
                    break;
            }
            //Log.d(TAG, "sendFile: " + fileUri);
            String head = "DATA?filename=" + new File(FileUtils.getFilePathByUri(this, uri)).getName() +
                    "&mon=" + mon +
                    "&tue=" + tue +
                    "&wed=" + wed +
                    "&thu=" + thu +
                    "&fri=" + fri +
                    "&sat=" + sat +
                    "&sun=" + sun +
                    "\n";
            Log.d(TAG, "sendFile: head : " + head);
            serviceIntent.putExtra("head", head);
            startService(serviceIntent);
        } else if (requestCode == CHOOSE_APK_FILE_RESULT_CODE) {
            // User has picked an image. Transfer it to group owner i.e peer using
            // FileTransferService.
            Uri uri = data.getData();
            TextView statusText = (TextView) findViewById(R.id.status_text);
            statusText.setText("Sending: " + uri);
            Log.d(TAG, "Intent----------- " + uri);
            Intent serviceIntent = new Intent(this, FileTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                    editText.getText().toString());
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);

            //Log.d(TAG, "sendFile: " + fileUri);
            String head = "UPGRADE\n";
            Log.d(TAG, "sendFile: head : " + head);
            serviceIntent.putExtra("head", head);
            startService(serviceIntent);
        }
    }

}
