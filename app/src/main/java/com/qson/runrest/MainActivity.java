package com.qson.runrest;

import android.media.AudioManager;
import android.media.ToneGenerator;
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
    private static final int STATUS_RUN_EXTRA = 2;
    private static final int STATUS_REST = 3;
    private static final int STATUS_REST_EXTRA = 4;

    private static final long TIMER_INTERVAL = 1000L;
    private static final long TIMER_MAX = 10L * 60L * 60L * 1000L;

    private static final int BEEP_LENGTH_SHORT = 200;
    private static final int BEEP_LENGTH_MID = 500;
    private static final int BEEP_LENGTH_LONG = 800;
    private static final int BEEP_REST_EXTRA_INTERVAL = 15;

    private TextView mRunCountText;
    private TextView mRunCountDownText;
    private TextView mRestTimeText;
    private TextView mTotalRunText;
    private TextView mTotalTimeText;

    private Button mActionButton;
    private Button mEndButton;
    private View mRootView;

    private int mRunTimeSetting;
    private int mRestTimeSetting;

    private int mStatus;

    private long mStartTimestamp;
    private int mTotalRunTime;
    private int mRunCountDownTime;
    private int mRestTime;
    private int mRunCount;

    ToneGenerator mTone;

    private CountDownTimer mRunTimer;
    private CountDownTimer mRestTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mRootView = findViewById(android.R.id.content);
        mRunCountText = findViewById(R.id.run_count);
        mRunCountDownText = findViewById(R.id.run_countdown);
        mRestTimeText = findViewById(R.id.rest_time);

        mTotalRunText = findViewById(R.id.total_run_time);
        mTotalTimeText = findViewById(R.id.total_time);
        mTotalTimeText.setVisibility(View.GONE);

        mActionButton = findViewById(R.id.action_button);
        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleActionClick();
            }
        });

        mEndButton = findViewById(R.id.end_button);
        mEndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalizeRun();
            }
        });

        mTone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

        mRunTimeSetting = 120;
        mRestTimeSetting = 60;

        initRun(mRunTimeSetting);
        syncDisplay();
    }

    @Override
    protected void onDestroy() {
        stopTimer();
        super.onDestroy();
    }

    private void stopTimer() {
        if (mRunTimer != null) {
            mRunTimer.cancel();
        }
        if (mRestTimer != null) {
            mRestTimer.cancel();
        }
    }

    private void handleActionClick() {
        switch (mStatus) {
            case STATUS_READY:
                initRun(mRunTimeSetting);
                startRun();
                break;
            case STATUS_RUN:
            case STATUS_RUN_EXTRA:
                startRest();
                break;
            case STATUS_REST:
            case STATUS_REST_EXTRA:
                startRun();
                break;
            default:
                break;
        }
    }

    private void initRun(int runTime) {
        mStatus = STATUS_READY;
        mActionButton.setText(getResources().getString(R.string.button_start));
        mTotalTimeText.setVisibility(View.GONE);

        mTotalRunTime = 0;
        mRunCountDownTime = runTime;
        mRestTime = 0;
        mRunCount = 0;
        mStartTimestamp = System.currentTimeMillis();
    }

    private void syncDisplay() {
        mRunCountText.setText(String.valueOf(mRunCount));
        mRunCountDownText.setText(TimeUtil.secToTimeString(mRunCountDownTime));
        mRestTimeText.setText(TimeUtil.secToTimeString(mRestTime));
        mTotalRunText.setText(TimeUtil.secToTimeString(mTotalRunTime));
    }

    private void startRun() {
        mStatus = STATUS_RUN;
        mTone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, BEEP_LENGTH_MID);
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

                if (mStatus == STATUS_RUN) {
                    if (mRunCountDownTime > 0) {
                        mRunCountDownTime = mRunTimeSetting - timeSec;
                        if (mActionButton.isEnabled()) {
                            mActionButton.setEnabled(false);
                        }
                    }
                    if (mRunCountDownTime <= 0) {
                        mStatus = STATUS_RUN_EXTRA;
                        mTone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, BEEP_LENGTH_SHORT);
                        mRunCountDownTime = 0;
                        mRootView.setBackgroundColor(
                                getResources().getColor(R.color.runExtraColor));
                        if (!mActionButton.isEnabled()) {
                            mActionButton.setEnabled(true);
                        }
                    }
                } else if (mStatus == STATUS_RUN_EXTRA) {
                    mRunCountDownTime++;
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
        mTone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, BEEP_LENGTH_MID);
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
                if (mStatus == STATUS_REST && mRestTime >= mRestTimeSetting) {
                    mStatus = STATUS_REST_EXTRA;
                    mTone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, BEEP_LENGTH_LONG);
                    mRootView.setBackgroundColor(getResources().getColor(R.color.restExtraColor));
                } else if (mStatus == STATUS_REST_EXTRA) {
                    if ((mRestTime - mRestTimeSetting) % BEEP_REST_EXTRA_INTERVAL == 0) {
                        mTone.startTone(
                                ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, BEEP_LENGTH_SHORT);
                    }
                }
                syncDisplay();
            }

            public void onFinish() {}
        };
        mRestTimer.start();
    }

    private void finalizeRun() {
        stopTimer();

        mRootView.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        int totalTime = 0;
        if (mStatus != STATUS_READY) {
            totalTime = (int)((System.currentTimeMillis() - mStartTimestamp) / 1000L);
        }
        mTotalTimeText.setText(TimeUtil.secToTimeString(totalTime));
        mTotalTimeText.setVisibility(View.VISIBLE);

        mTone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, BEEP_LENGTH_LONG);

        mStatus = STATUS_READY;
        mActionButton.setText(getResources().getString(R.string.button_run));
        mActionButton.setEnabled(true);
    }
}