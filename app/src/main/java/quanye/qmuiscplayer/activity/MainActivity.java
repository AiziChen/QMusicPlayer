package quanye.qmuiscplayer.activity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import quanye.qmuiscplayer.R;
import quanye.qmuiscplayer.Tools;
import service.MusicPlayerService;

/**
 * @author 陈权业
 * 主Activity，列表Activity
 */
public class MainActivity extends AppCompatActivity {

    public final static String NO_MUSIC = "nomusics";
    public final static String NO_COMMAND = "nocommand";
    public final static String UPDATE_BCR = "quanye.qmusicplayer.MusicPlayerService.UpdateBroadcastReceiver";
    public final static String CTRL_CMD = "control_command";
    public final static String NEXT = "next";
    public final static String PREVIOUS = "previous";
    public final static String START = "start";
    public final static String RESUME = "resume";
    public final static String STATE_PLAYING = "playing";
    public final static String STATE_PAUSED = "paused";
    public final static String STATE_STOPED = "stoped";
    private Toolbar ngBar;
    private ListView musicListView;
    private ArrayAdapter<String> adapter;
    private TextView songName;
    private ImageView playImg;
    private ImageView nextImg;

    public static int sIndex = 0;
    public static String state = STATE_STOPED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ngBar = (Toolbar) findViewById(R.id.ng_bar);
        ngBar.setOnClickListener((View v)->
                Toast.makeText(this, Tools.randomHello(), Toast.LENGTH_SHORT).show());
        musicListView = (ListView) findViewById(R.id.musicListView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1
                , getSongItems());
        musicListView.setAdapter(adapter);
        musicListView.setOnItemClickListener(new Listener());

        songName = (TextView) findViewById(R.id.song_name);
        playImg = (ImageView) findViewById(R.id.img_play);
        playImg.setOnClickListener((View view) -> {
            switch (state) {
                case STATE_STOPED:
                    play(getSongItems()[sIndex], START);
                    break;
                case STATE_PAUSED:
                    play(getSongItems()[sIndex], RESUME);
                    break;
                case STATE_PLAYING:
                    play(NO_MUSIC, STATE_PAUSED);
                    break;
            }
        });

        nextImg = (ImageView) findViewById(R.id.img_next);
        nextImg.setOnClickListener((View view) -> {
            MainActivity.sendBroadcast(this, NEXT);
        });

        // 注册 broadcastreceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_BCR);
        registerReceiver(new UpdateBroadcastReceiver(), filter);
        // 保证MusicPlayerService只启动一次
        if (!isServiceRunning(this, MusicPlayerService.class.getName())) {
            startService(new Intent(this, MusicPlayerService.class));
        } else {
            //MusicPlayerService.sendBroadcast(this, NO_MUSIC, NO_COMMAND);
        }

        // 恢复列表位置
        if (getSongItems().length >= 0) {
            restoreLvPos();
            songName.setText(getSongItems()[sIndex]);
        }
        // 设置播放状态图标
        setSongStateImage();
    }

    /**
     * 根据音乐的播放状态设置播放状态图标
     */
    private void setSongStateImage() {
        switch (state) {
            case STATE_PAUSED:
                playImg.setImageResource(R.mipmap.play);
                break;
            case STATE_PLAYING:
                playImg.setImageResource(R.mipmap.pause);
                break;
            case STATE_STOPED:
                playImg.setImageResource(R.mipmap.stop);
                break;
        }
    }

    /**
     * 获取音乐列表
     * @return
     */
    public static String[] getSongItems() {
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return new String[]{NO_MUSIC};
        }
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        List<String> list = new ArrayList<>();
        if (list.size() <= 0) {
            return new String[]{NO_MUSIC};
        }
        for (File name : file.listFiles()) {
            // 滤过非.mp3文件
            if (!name.isDirectory() && name.getName().endsWith(".mp3")) {
                list.add(name.getName());
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 点击音乐列表时的事件监听器
     */
    private class Listener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long li) {
            sIndex = i;
            // set song name
            String item = getSongItems()[i];
            // play music
            play(item, START);
        }
    }

    /**
     * 发送广播让服务者播放音乐
     * @param name 音乐全名
     */
    private void play(String name, String cmd) {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        String filePath = file.getAbsolutePath() + "/" + name;
        if (new File(filePath).exists()) {
            MusicPlayerService.sendBroadcast(this, filePath, cmd);
            // 改变图标
            playImg.setImageResource(R.mipmap.pause);
            // 改变工具栏文字
            String title = name.substring(0, name.lastIndexOf("."));
            songName.setText(title);
        } else {
            Toast.makeText(this, getString(R.string.can_not_played_music), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 给MusicPlayerService发送广播
     * @param context context
     * @param cmd 播放指令
     */
    public static void sendBroadcast(Context context, String cmd) {
        Intent intent = new Intent(UPDATE_BCR);
        intent.putExtra(CTRL_CMD, cmd);
        context.sendBroadcast(intent);
    }

    private void playNextSong() {
        int len = getSongItems().length;
        sIndex ++;
        if (sIndex >= len) {
            sIndex = 0;
        }
        String item = getSongItems()[sIndex];
        play(item, START);
    }

    /**
     * 更新当前activity状态的广播接收器
     */
    public class UpdateBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String cmd = intent.getStringExtra(CTRL_CMD);
            switch (cmd) {
                case NEXT:
                    playNextSong();
                    break;
                case PREVIOUS:
                    // 不实现
                    break;
                case START:
                    state = STATE_PLAYING;
                    break;
                case RESUME:
                    state = STATE_PLAYING;
                    break;
                case STATE_PLAYING:
                    state = STATE_PLAYING;
                    break;
                case STATE_PAUSED:
                    state = STATE_PAUSED;
                    break;
                case STATE_STOPED:
                    break;
            }
            // 改变图标
            setSongStateImage();
        }
    }

    /**
     *   恢复listView 的位置
     * @return 顶部列的位置
     */
    private int restoreLvPos() {
        SharedPreferences sharedPreferences = getSharedPreferences("listViewPos", MODE_PRIVATE);
        int pos = sharedPreferences.getInt("pos", 0);
        musicListView.setSelection(pos);
        return pos;
    }

    /**
     * 指定的Serivce是否在运行
     * @param mContext context
     * @param className 指定的Serivce名字
     * @return 返回运行状态
     */
    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList
                = activityManager.getRunningServices(30);
        if (!(serviceList.size()>0)) {
            return false;
        }
        for (int i=0; i<serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*保存当前列表的顶项位置*/
        SharedPreferences sharedPreferences = getSharedPreferences("listViewPos", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("pos", musicListView.getFirstVisiblePosition());
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
