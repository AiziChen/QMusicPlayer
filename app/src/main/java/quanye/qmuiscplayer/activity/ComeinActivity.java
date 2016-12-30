package quanye.qmuiscplayer.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ImageView;

import quanye.qmuiscplayer.R;

/**
 * @author 陈权业
 */

public class ComeinActivity extends AppCompatActivity {
    private static boolean state = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (state) {
            goMainActivity();
            return;
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_comin);
        ImageView imageView = (ImageView) findViewById(R.id.sayhello);
        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                goMainActivity();
                state = true;
            }
        }, 1*1000L);

    }

    private void goMainActivity() {
        startActivity(new Intent(ComeinActivity.this, MainActivity.class));
        finish();
    }
}
