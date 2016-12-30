package service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;

import quanye.qmuiscplayer.R;
import quanye.qmuiscplayer.activity.MainActivity;

/**
 * @author 陈权业
 * 播放音乐的Service，可后台
 */
public class MusicPlayerService extends Service {

    private final static String MUSIC_PATH = "quanye.qmusicplayer.MusicPlayerService.music_filepath";
    private final static String CTRL_BCR = "quanye.qmusicplayer.MusicPlayerService.ControlBroadcastReceiver";
    private final static String UPDATE_CMD = "update_command";
    private MediaPlayer player;
    /**
     * 发送音乐路径通知广播
     * @param context context
     * @param path 音乐路径
     */
    public static void sendBroadcast(Context context, String path, String cmd) {
        Intent intent = new Intent(CTRL_BCR);
        intent.putExtra(MUSIC_PATH, path);
        intent.putExtra(UPDATE_CMD, cmd);
        context.sendBroadcast(intent);
    }

    /**
     * 控制广播接收器
     */
    public class ControlBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String musicPath = intent.getStringExtra(MUSIC_PATH);
            String cmd = intent.getStringExtra(UPDATE_CMD);
            switch (cmd) {
                case MainActivity.RESUME:
                    if (player != null && !player.isPlaying()) {
                        resume();
                    }
                    break;
                case MainActivity.START:
                    playmusic(musicPath);
                    break;
                case MainActivity.STATE_PAUSED:
                    pause();
                    break;
                case MainActivity.STATE_STOPED:
                    stop();
                    break;
                case MainActivity.NEXT:
                    // 发送广播让MainActivity类处理
                    break;
            }
            MainActivity.sendBroadcast(MusicPlayerService.this, cmd);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // show hello toast
        Toast.makeText(this, "Enjoy!", Toast.LENGTH_SHORT).show();
        // init system Meidaplayer
        player = new MediaPlayer();
        // 播放完成当前歌曲后，继续播下一曲
        player.setOnCompletionListener((MediaPlayer mp) -> {
            MainActivity.sendBroadcast(MusicPlayerService.this, MainActivity.NEXT);
        });
        // register broadcastreceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(CTRL_BCR);
        registerReceiver(new ControlBroadcastReceiver(), filter);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 开始播放音乐
     * @param path 音乐文件路径
     */
    private void playmusic(String path) {
        // 若列表没有音乐，则不启动播放
        if (path.equals(MainActivity.NO_MUSIC)) {
            return;
        }
        // 重置音乐
        player.reset();
        try {
            player.setDataSource(path);
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.can_not_played_music), Toast.LENGTH_SHORT).show();
        }
        // 开始播放
        player.start();
        // 给activity发送“已经正在播放”状态的广播
        MainActivity.sendBroadcast(MusicPlayerService.this, MainActivity.STATE_PLAYING);
    }

    /**
     * 暂停播放
     */
    private void pause() {
        player.pause();
        // 给activity发送“已经停止播放”状态的广播
        MainActivity.sendBroadcast(MusicPlayerService.this, MainActivity.STATE_PAUSED);
    }

    /**
     * 继续播放
     */
    private void resume() {
        player.start();
    }

    /**
     * 停止播放
     */
    private void stop() {
        player.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
        player = null;
    }
}
