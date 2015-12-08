package me.chrisvle.rechordly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.ChannelApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import me.chrisvle.rechordly.dummy.DummyContent;

public class PhoneListener extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks, ChannelApi.ChannelListener {

    private static final String PLAY = "/play";
    private static final String PAUSE = "/pause";
    private static final String SAVE = "/save";
    private static final String RETRY = "/retry";
    private static final String EDIT = "/edit";
    private static final String LYRIC = "/lyric";
    private static final String LYRIC_TXT = "/lyric_text";
    public static File file;
    public GoogleApiClient mApiClient;
    private BroadcastReceiver broadcastReceiver;


    @Override
    public void onCreate() {
        Log.d("PhoneListener", "OK");
        super.onCreate();
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks(this)
                .build();

        mApiClient.connect();

        IntentFilter filter = new IntentFilter();
        filter.addAction("/edit");
        filter.addAction("/lyric_add");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(EDIT)) {
                    String path = intent.getStringExtra("filePath");
                    file = new File(path);
                }
                else if (intent.getAction().equals(LYRIC)) {
                    String path = intent.getStringExtra("filePath");
                    file = new File(path);
                }
                else if (intent.getAction().equals(LYRIC_TXT)) {
                    String text = intent.getStringExtra("text");
                    // ADD TEXT TO file
                }
            }
        };
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("FIlename333", file.getAbsolutePath());
        if (messageEvent.getPath().equalsIgnoreCase(PLAY)) {
            Log.d("PhoneListener", "Play Request");
            Intent intent = new Intent("/play");
                intent.putExtra("path", file.getAbsolutePath());
                sendBroadcast(intent);
        } else if (messageEvent.getPath().equalsIgnoreCase(PAUSE)) {
            Log.d("PhoneListener", "Pause Request");
            Intent intent = new Intent("/pause");
            intent.putExtra("path", file.getAbsolutePath());
            sendBroadcast(intent);
        } else if (messageEvent.getPath().equalsIgnoreCase(RETRY)){
            Log.d("PhoneListener", "Retry Request");
            Intent intent = new Intent("/retry");
            file.delete();
        } else if (messageEvent.getPath().equalsIgnoreCase(SAVE)){
            Intent cropservice = new Intent(this, CropService.class);
            startService(cropservice);
            Intent gainservice = new Intent(this, GainService.class);
            startService(gainservice);
            Intent echoservice = new Intent(this, EchoService.class);
            startService(echoservice);

            Log.d("PhoneListener", "Save Request");
            String all = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            Log.d("Message", all);
            String[] edits = all.split("\\|");
            Log.d("EDIts0", edits[0]);

            Log.d("EDIts1", edits[1]);

            Log.d("EDIts2", edits[2]);

            // Handles all FILENAMIN
            Log.d("FIlename333", file.getName());
            if (!edits[0].equals("None")) {
                if (!edits[0].equals(file.getName())) {
                    File newfile = new File(Environment.getExternalStorageDirectory().getPath(), edits[0] + ".wav");
                    try {
                        copy(file, newfile);
                    } catch (IOException e) {
                        Log.d("COPY", "COULD NOT BE COPIED");
                    }
                    file.delete();
                    file = newfile;



                    Log.d("New filename after save", String.valueOf(this.getFilesDir()));
                    try {
                        copy(file, new_file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    file.delete();
                    file = new_file;
                    Log.d("New filename after save", String.valueOf(this.getFilesDir()));
                }
            }
            else {

            }

            MediaPlayer song = MediaPlayer.create(this, Uri.fromFile(file));
            long durationSong = song.getDuration();
            song.release();

            // Handles all GAIN
            double gain_val = 1;
            if (!edits[3].equals("None") && (!edits[3].equals("0"))) {
                gain_val = Double.parseDouble(edits[3]);
                Intent gain = new Intent("/gain");
                gain.putExtra("filePath", file.getAbsolutePath());
                gain.putExtra("volume", gain_val);
                sendBroadcast(gain);
            }

            // Handles all ECHO
            double echo_val = 1;
            if (!edits[4].equals("None") && (!edits[4].equals("0"))) {
                echo_val = Double.parseDouble(edits[4]);
                Intent echo = new Intent("/echo");
                echo.putExtra("filePath", file.getAbsolutePath());
                echo.putExtra("level", echo_val);
                sendBroadcast(echo);
            }

            // Handles all TRIM
            double left = 0;
            double right = durationSong/1000;
            Log.d("EDITS", edits[1]);

            Double leftValue = null;
            Double rightValue = null;

            if (!edits[1].equals("None")) {

                String[] time = edits[1].split(":");
                Double min = Double.parseDouble(time[0]) * 60;
                Double seconds = Double.parseDouble(time[1]);
                leftValue = min + seconds;

            }
            if (!edits[2].equals("None")) {
                String[] time = edits[2].split(":");
                Double min = Double.parseDouble(time[0]) * 60;
                Double seconds = Double.parseDouble(time[1]);
                rightValue = min + seconds;

//                right = Integer.parseInt(edits[2]);

            }

            if (leftValue == null) {
                leftValue = 0.0;
            }
            if (rightValue == null) {
                rightValue = right;
            }
            if ((rightValue == right && leftValue == 0.0)) {
                Log.d("TRIM", "NO need to trim!");
            } else {
                Log.d("FILENAME", file.getName());
                Intent trim = new Intent("/trim");
                trim.putExtra("file", file.getAbsolutePath());
                trim.putExtra("startTime", leftValue);
                trim.putExtra("endTime", rightValue);
                sendBroadcast(trim);
            }

            // Handles all TRANSCRIPTION
            if (!edits[5].equals("None")) {
                Intent transcription = new Intent("/transcription");
                sendBroadcast(transcription);
            }
            Log.d("SAVE", "Before Saving");
            Log.d("FILE", file.getName());
            Log.d("FILE", String.valueOf(file.length()));
            MediaPlayer mp = MediaPlayer.create(this, Uri.fromFile(file));
            long duration = mp.getDuration();
            mp.release();

            int minutes = (int) Math.floor(duration / 1000 / 60);
            int seconds = (int) ((duration / 1000) - (minutes * 60));
            String dur = minutes + ":" + String.format("%02d", seconds);

            Log.d("ASKLDJFLKAKLFJ", String.valueOf(seconds));

//            String dur = String.valueOf(duration);

            SavedDataList saves = SavedDataList.getInstance();
            saves.addSong(edits[0], String.valueOf(echo_val), String.valueOf(gain_val), dur, edits[5], Uri.fromFile(file).toString());
            saves.saveToDisk(getApplicationContext());
            DummyContent.addItem(new DummyContent.DummyItem(edits[0], dur, ""));

            Intent updateList = new Intent("/update_list");
            sendBroadcast(updateList);
            Log.d("SAVE", "After Saving");

         }
    }

    @Override
    public void onChannelOpened(Channel channel) {

        Log.d("PhoneListener", "Channel established");
        if (channel.getPath().equals("/new_recording")) {
            file = new File(Environment.getExternalStorageDirectory().getPath(), getTime() + ".wav");
            Log.d("this", String.valueOf(this.getFilesDir()));
            try {
                file.createNewFile();
                Log.d("FIlename", file.getName());
            } catch (IOException e) {
                Log.d("ERROR", "FILE COULD NOT BE MADR");
            }
            Log.d("PhoneListener", "Trying to receive file");

            channel.receiveFile(mApiClient, Uri.fromFile(file), false);
        }
        else if (channel.getPath().equals("/edit_recording")) {

        }
        else if (channel.getPath().equals("/playback")) {

        }

    }

    @Override
    public void onInputClosed(Channel channel, int int0, int int1) {
        Log.d("PhoneListener", "File Received!!");

    }

    @Override
    public void onChannelClosed(Channel channel, int i0, int i1) {
        Log.d("PhoneListener", "Channel Closed!");
        Log.d("FIlename", file.getName());

    }



    @Override
    public void onConnected(final Bundle connectionHint) {
        Wearable.ChannelApi.addListener(mApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i0) {
        Wearable.ChannelApi.removeListener(mApiClient, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
        unregisterReceiver(broadcastReceiver);
    }

    public String getTime() {
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        return ts;
    }

    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

}
