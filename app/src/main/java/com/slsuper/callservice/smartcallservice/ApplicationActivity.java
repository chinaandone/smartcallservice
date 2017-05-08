package com.slsuper.callservice.smartcallservice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.slsuper.callservice.eventbus.EventMessageArrive;
import com.slsuper.callservice.message.RabbitMQClient;
import com.slsuper.callservice.utils.ConstValue;
import com.slsuper.callservice.utils.SettingConfig;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ApplicationActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private TextView mCallServiceContent,mMsgLeftContents,mSetting;
    private FrameLayout frameLayout;
    private Map<Long,String> msgHashMap;
    private Queue<String> msgQueue;
    private Vibrator vibrator;
    private SettingConfig settingConfig;
    private RabbitMQClient rabbitRunnable;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_application);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        mCallServiceContent = (TextView)findViewById(R.id.fullscreen_content);
        mMsgLeftContents = (TextView)findViewById(R.id.msg_left);
        frameLayout = (FrameLayout)findViewById(R.id.backgroud);
        mSetting = (TextView)findViewById(R.id.setting);
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        settingConfig = new SettingConfig(this);
        // Set up the user interaction to manually show or hide the system UI.
//        mContentView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                toggle();
////                frameLayout.setBackgroundResource(R.drawable.backgroudidle);
//            }
//        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mContentView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    mCallServiceContent.setText("滑动删除");
                }
            });
        }
        mContentView.setOnTouchListener(new View.OnTouchListener() {
            private boolean isMove = false;
            private int mLastMontionX,mLastMontionY;
            private static final int TOUCH_SLOP = 100;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x,y;
                x=(int)event.getX();
                y=(int)event.getY();
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        isMove = false;
                        mLastMontionX = x;
                        mLastMontionY = y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(isMove){
                            break;
                        }
                        else{
//                            mCallServiceContent.setText("滑动删除");
                            if(mLastMontionX-x>=TOUCH_SLOP || mLastMontionY-y>=TOUCH_SLOP) {
                                if (!msgQueue.isEmpty()) {
                                    String[] tmpstr = msgQueue.poll().split("\\|");
                                    mCallServiceContent.setText(tmpstr[2]+"\n"+tmpstr[3]);
                                    mMsgLeftContents.setText(msgQueue.size() + "");
                                } else {
                                    mCallServiceContent.setText("");
//                                mContentView.setVisibility(View.INVISIBLE);
                                    mMsgLeftContents.setVisibility(View.INVISIBLE);
                                    frameLayout.setBackgroundResource(R.drawable.backgroudidle);

                                }
                                isMove = true;
                            }
                        }
                }
                return true;
            }
        });

        mMsgLeftContents.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                TextView tv = (TextView) view;
                tv.setText("");
                msgQueue.clear();
                tv.setVisibility(View.INVISIBLE);

            }
        });

        mSetting.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //启动设置界面
                Intent intent = new Intent(ApplicationActivity.this,SettingActivity.class);
//                startActivity(intent);
                startActivityForResult(intent,0x01);
                return false;

            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        //需要考虑延时启动MQ线程
        String serverHost = settingConfig.getServerHost();
        String watchID = settingConfig.getWatchID();
        if(serverHost!=null && watchID!=null) {
            rabbitRunnable = new RabbitMQClient(serverHost,watchID);
            new Thread(rabbitRunnable).start();
        }else{
            mHideHandler.postDelayed(new Runnable(){
                @Override
                public void run() {
                    mCallServiceContent.setText("未配置");
                }
            },1000);
        }
        EventBus.getDefault().register(this);
        msgHashMap = new HashMap<Long,String>();
        msgQueue = new LinkedList<String>();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vibrator.cancel();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==0x02) {
            if(rabbitRunnable!=null) {
                if (!rabbitRunnable.isStop()) {
                    rabbitRunnable.stopThread();
                }
                rabbitRunnable.init(data.getStringExtra(ConstValue.SERVERHOSTKEY), 0, data.getStringExtra(ConstValue.WATCHIDKEY));
            }else{
                rabbitRunnable = new RabbitMQClient(data.getStringExtra(ConstValue.SERVERHOSTKEY), 0, data.getStringExtra(ConstValue.WATCHIDKEY));
            }

            new Thread(rabbitRunnable).start();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

//    @Subscribe
//    public void onMessageArrive(EventMessageArrive eventMessageArrive){
//        if(mContentView.getVisibility()==View.INVISIBLE) {
//            frameLayout.setBackgroundResource(R.drawable.back);
//            mCallServiceContent.setText(eventMessageArrive.getMsgContenet().split("|")[1]);
//            mContentView.setVisibility(View.VISIBLE);
//        }else{
////            msgHashMap.put(Calendar.getInstance().getTime().getTime(),eventMessageArrive.getMsgContenet());
//            msgQueue.offer(eventMessageArrive.getMsgContenet());
//            mMsgLeftContents.setVisibility(View.VISIBLE);
//            mMsgLeftContents.setText(msgQueue.size());
//        }
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessageArrive eventMessageArrive){

        long [] pattern = {100,1000,100,1000};   // 停止 开启 停止 开启
        vibrator.vibrate(pattern,-1);           //重复两次上面的pattern 如果只想震动一次，index设为-1

        if(mCallServiceContent.getText().equals("")) {
            frameLayout.setBackgroundResource(R.drawable.back);
            mCallServiceContent.setText(eventMessageArrive.getMsgContenet().split("\\|")[2]+"\n"+eventMessageArrive.getMsgContenet().split("\\|")[3]);
            mContentView.setVisibility(View.VISIBLE);
        }else{
//            msgHashMap.put(Calendar.getInstance().getTime().getTime(),eventMessageArrive.getMsgContenet());
            msgQueue.offer(eventMessageArrive.getMsgContenet());
            mMsgLeftContents.setVisibility(View.VISIBLE);
            mMsgLeftContents.setText(msgQueue.size()+"");
        }
        Log.i("main thread","Thread"+Thread.currentThread());
    }

}
