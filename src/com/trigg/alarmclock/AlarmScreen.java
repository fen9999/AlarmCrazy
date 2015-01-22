package com.trigg.alarmclock;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AlarmScreen extends Activity {

	public final String TAG = this.getClass().getSimpleName();
	private static final int OPERATION_ADD = 0;
	private static final int OPERATION_SUB = 1;
	private static final int OPERATION_MULTIPLY = 2;
	private static final int OPERATION_DIVIDE = 3;

	private WakeLock mWakeLock;
	private MediaPlayer mPlayer;
	private String question = "1 + 1 = ? ";
	private String correctAnswer = "2";

	private static final int WAKELOCK_TIMEOUT = 60 * 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		generateQuestion();
		// Setup layout
		this.setContentView(R.layout.activity_alarm_screen);

		String name = getIntent().getStringExtra(AlarmManagerHelper.NAME);
		int timeHour = getIntent().getIntExtra(AlarmManagerHelper.TIME_HOUR, 0);
		int timeMinute = getIntent().getIntExtra(
				AlarmManagerHelper.TIME_MINUTE, 0);
		String tone = getIntent().getStringExtra(AlarmManagerHelper.TONE);

		TextView tvName = (TextView) findViewById(R.id.alarm_screen_name);
		tvName.setText(name);

		TextView tvTime = (TextView) findViewById(R.id.alarm_screen_time);
		tvTime.setText(String.format("%02d : %02d", timeHour, timeMinute));

		TextView tvQuestion = (TextView) findViewById(R.id.alarm_question);
		tvQuestion.setText(question);

		final EditText etAnswer = (EditText) findViewById(R.id.alarm_answer);

		Button dismissButton = (Button) findViewById(R.id.alarm_screen_button);
		dismissButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				String answer = etAnswer.getText().toString();
				if (answer.equals(correctAnswer)) {
					mPlayer.stop();
					finish();
				} else {
					Toast.makeText(getApplicationContext(),
							answer + " is wrong asnwer", Toast.LENGTH_SHORT)
							.show();
				}
			}
		});

		// Play alarm tone
		mPlayer = new MediaPlayer();
		// try {
		// if (tone != null && !tone.equals("")) {
		// Uri toneUri = Uri.parse(tone);
		// if (toneUri != null) {
		// mPlayer.setDataSource(this, toneUri);
		// mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
		// mPlayer.setLooping(true);
		// mPlayer.prepare();
		// mPlayer.start();
		// }
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		// Ensure wakelock release
		Runnable releaseWakelock = new Runnable() {

			@Override
			public void run() {
				getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
				getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
				getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

				if (mWakeLock != null && mWakeLock.isHeld()) {
					mWakeLock.release();
				}
			}
		};

		new Handler().postDelayed(releaseWakelock, WAKELOCK_TIMEOUT);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();

		// Set the window to keep screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		// Acquire wakelock
		PowerManager pm = (PowerManager) getApplicationContext()
				.getSystemService(Context.POWER_SERVICE);
		if (mWakeLock == null) {
			mWakeLock = pm
					.newWakeLock(
							(PowerManager.FULL_WAKE_LOCK
									| PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP),
							TAG);
		}

		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire();
			Log.i(TAG, "Wakelock aquired!!");
		}

	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mWakeLock != null && mWakeLock.isHeld()) {
			mWakeLock.release();
		}
	}

	private void generateQuestion() {
		Random random = new Random();
		int operation = random.nextInt(OPERATION_DIVIDE + 1);
		int a, b, c, max, min;
		switch (operation) {
		case OPERATION_ADD:
			max = 50;
			min = 10;
			a = random.nextInt((max - min) + 1) + min;
			b = random.nextInt((max - min) + 1) + min;
			question = a + " + " + b + " = ? ";
			correctAnswer = String.valueOf(a + b);
			break;
		case OPERATION_SUB:
			max = 100;
			min = 40;
			a = random.nextInt((max - min) + 1) + min;
			max = a - 1;
			min = 11;
			b = random.nextInt((max - min) + 1) + min;
			question = a + " - " + b + " = ? ";
			correctAnswer = String.valueOf(a - b);
			break;
		case OPERATION_MULTIPLY:
			max = 20;
			min = 11;
			a = random.nextInt((max - min) + 1) + min;
			max = 11;
			min = 3;
			b = random.nextInt((max - min) + 1) + min;
			question = a + " x " + b + " = ? ";
			correctAnswer = String.valueOf(a * b);
			break;
		case OPERATION_DIVIDE:
			max = 15;
			min = 8;
			c = random.nextInt((max - min) + 1) + min;
			max = 11;
			min = 5;
			b = random.nextInt((max - min) + 1) + min;
			a = c * b;
			question = a + " : " + b + " = ? ";
			correctAnswer = String.valueOf(c);
			break;

		default:
			break;
		}
	}
}
