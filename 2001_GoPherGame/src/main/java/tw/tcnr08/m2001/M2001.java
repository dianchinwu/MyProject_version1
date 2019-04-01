package tw.tcnr08.m2001;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class M2001 extends AppCompatActivity {

    private ImageView[] imageViewList;
    private TextView textView;
    private Button btnplay;
    private int[] gopher;
    private Handler handler;
    private boolean play;
    private int score;
    private GopherSprite[] glist;
    SoundPool mSoundPool;
    int mSoundId;
    private SoundPool soundPool;
    private int touchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m2001);

        setupViewComponent();
    }

    private void setupViewComponent() {

        imageViewList = new ImageView[]{
                (ImageView) findViewById(R.id.imageView),
                (ImageView) findViewById(R.id.imageView2),
                (ImageView) findViewById(R.id.imageView3),
                (ImageView) findViewById(R.id.imageView4),
                (ImageView) findViewById(R.id.imageView5),
                (ImageView) findViewById(R.id.imageView6),
                (ImageView) findViewById(R.id.imageView7),
                (ImageView) findViewById(R.id.imageView8),
                (ImageView) findViewById(R.id.imageView9)
        };

        textView = (TextView) findViewById(R.id.tscore);
        btnplay=(Button)findViewById(R.id.btn_play);

        gopher = new int[]{R.drawable.hole,
                R.drawable.mole1, R.drawable.mole2, R.drawable.mole3,
                R.drawable.mole3, R.drawable.mole3, R.drawable.mole3,
                R.drawable.mole4, R.drawable.mole4, R.drawable.mole4,
                R.drawable.mole1, R.drawable.mole1, R.drawable.mole2,
                R.drawable.mole1, R.drawable.mole2, R.drawable.mole1,
                R.drawable.hole, R.drawable.hole, R.drawable.hole};
        handler = new Handler();

        // 建立音效池
        buildSoundPool();
        // 建立地鼠遊戲物件與註冊 onTouch 監聽器
        glist = new GopherSprite[9]; //9 hole

        for (int i = 0; i < glist.length; i++) {
            glist[i] = new GopherSprite(imageViewList[i]);
            imageViewList[i].setOnTouchListener(new GopherOnTouchListener(glist[i]));
        }



    } //END setupviewcomponent

    // 建立音效池
    private void buildSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attr = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(10)
                    .setAudioAttributes(attr)
                    .build();
        } else {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }
        touchId = soundPool.load(this, R.raw.touch, 1);

    }

    // Button元件的事件處理
    public void btn_play_Click(View view) {
        play = true;
        score = 0;
        textView.setText("0");

        new CountDownTimer(45000, 1000) {
            @Override
            public void onFinish() {
                play = false;
                setTitle("剩餘時間：0");
            }

            @Override
            public void onTick(long millisUntilFinished) {
                setTitle("剩餘時間：" + millisUntilFinished / 1000);
            }
        }.start();
        for (GopherSprite g : glist) {
            handler.post(g);
        }
    }

    //------------------------------------------
    private class GopherSprite implements Runnable {
        ImageView imageView;
        int idx;
        boolean hit;

        GopherSprite(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        public void run() {
            draw();
        }

        private void draw() {
            if (!play) {
                return;
            }
            if (hit) {
                imageView.setImageResource(R.drawable.mole5);
                hit = false;
                idx = 0;
                handler.postDelayed(this, 1000);
            } else {
                idx = idx % gopher.length;
                imageView.setImageResource(gopher[idx]);
                int n = (int) (Math.random() * 1000) % 3 + 1;
                handler.postDelayed(this, (n * 100));
                idx = ++idx % gopher.length;
            }
        }
    }


    private class GopherOnTouchListener implements View.OnTouchListener {
        GopherSprite g;
        public GopherOnTouchListener(GopherSprite g) {
            this.g = g;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (play && event.getAction() == MotionEvent.ACTION_DOWN) {
                if (gopher[g.idx] == R.drawable.mole2 ||
                        gopher[g.idx] == R.drawable.mole3||
                        gopher[g.idx] == R.drawable.mole4) {
                    g.hit = true;
                    soundPool.play(touchId, 1.0F, 1.0F, 0, 0, 1.0F);
                    score=score+5;
                    textView.setText(String.valueOf(score));

                } else {
                    textView.setText(String.valueOf(--score));
                }
            }
            return false;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);

    }
}
