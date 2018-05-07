package com.qson.runrest;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final int STATUS_READY = 0;
    private static final int STATUS_RUN = 1;
    private static final int STATUS_REST = 2;

    private static final long TIMER_INTERVAL = 1000L;
    private static final long TIMER_MAX = 10L * 60L * 60L * 1000L;

    private TextView mTotalRunText;
    private TextView mRunCountText;
    private TextView mRunCountDownText;
    private TextView mRestTimeText;
    private Button mActionButton;
    private View mRootView;

    private int mRunTimeSetting;
    private int mRestTimeSetting;

    private int mStatus;

    private int mTotalRunTime;
    private int mRunCountDownTime;
    private int mRestTime;
    private int mRunCount;

    private CountDownTimer mRunTimer;
    private CountDownTimer mRestTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mRootView = findViewById(android.R.id.content);
        mTotalRunText = findViewById(R.id.total_run_time);
        mRunCountText = findViewById(R.id.run_count);
        mRunCountDownText = findViewById(R.id.run_countdown);
        mRestTimeText = findViewById(R.id.rest_time);
        mActionButton = findViewById(R.id.action_button);
        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleActionClick();
            }
        });

        mRunTimeSetting = 120;
        mRestTimeSetting = 60;

        initRun(mRunTimeSetting, mRestTimeSetting);
        syncDisplay();
    }

    @Override
    protected void onDestroy() {
        mRunTimer.cancel();
        mRestTimer.cancel();
        super.onDestroy();
    }

    private void handleActionClick() {
        switch (mStatus) {
            case STATUS_READY:
                initRun(mRunTimeSetting, mRestTimeSetting);
                startRun();
                break;
            case STATUS_RUN:
                startRest();
                break;
            case STATUS_REST:
                startRun();
                break;
            default:
                break;
        }
    }

    private void initRun(int runTime, int restTime) {
        mStatus = STATUS_READY;
        mActionButton.setText(getResources().getString(R.string.button_start));

        mTotalRunTime = 0;
        mRunCountDownTime = runTime;
        mRestTime = 0;
        mRunCount = 0;
    }

    private void syncDisplay() {
        mTotalRunText.setText(TimeUtil.secToTimeString(mTotalRunTime));
        mRunCountText.setText(String.valueOf(mRunCount));
        mRunCountDownText.setText(TimeUtil.secToTimeString(mRunCountDownTime));
        mRestTimeText.setText(TimeUtil.secToTimeString(mRestTime));
    }

    private void startRun() {
        mStatus = STATUS_RUN;
        if (mRestTimer != null) {
            mRestTimer.cancel();
        }

        mRunCount++;
        mRootView.setBackgroundColor(getResources().getColor(R.color.runColor));
        mActionButton.setText(getResources().getString(R.string.button_rest));
        mRunCountDownTime = mRunTimeSetting;

        mRunTimer = new CountDownTimer(TIMER_MAX, TIMER_INTERVAL) {
            public void onTick(long millisUntilFinished) {
                int timeSec = (int) ((TIMER_MAX - millisUntilFinished) / 1000);

                if (timeSec > 0) {
                    mTotalRunTime++;
                }

                if (mRunCountDownTime > 0) {
                    mRunCountDownTime = mRunTimeSetting - timeSec;
                    if (mActionButton.isEnabled()) {
                        mActionButton.setEnabled(false);
                    }
                }

                if (mRunCountDownTime <= 0) {
                    mRunCountDownTime = 0;
                    if (!mActionButton.isEnabled()) {
                        mActionButton.setEnabled(true);
                    }
                }
                syncDisplay();
            }

            public void onFinish() {}
        };
        mRunTimer.start();

        syncDisplay();
    }

    private void startRest() {
        mStatus = STATUS_REST;
        if (mRunTimer != null) {
            mRunTimer.cancel();
        }

        mRootView.setBackgroundColor(getResources().getColor(R.color.restColor));
        mActionButton.setText(getResources().getString(R.string.button_run));
        mRunCountDownTime = mRunTimeSetting;

        mRestTimer = new CountDownTimer(TIMER_MAX, TIMER_INTERVAL) {
            public void onTick(long millisUntilFinished) {
                int timeSec = (int) ((TIMER_MAX - millisUntilFinished) / 1000);
                mRestTime = timeSec;
                syncDisplay();
            }

            public void onFinish() {}
        };
        mRestTimer.start();
    }
}