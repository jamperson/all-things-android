package me.poernomo.android.boardgametimer;

import java.util.Locale;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TimerFragment extends Fragment {

	private CountDownTimer countDownTimer;
	private final long mRoundTimeLength = 45;
	private final long mInterimTimeLength = 15;
	private final long mInterval = 1 * 1000;
	private final static int MAX_ROUND = 6;
	private TextView mTimeShow;
	private TextView mRound;
	private TextView mStage;
	private Button mStart;
	private Button mReset;
	private Button mSkip;
	private Button mResetAge;
	private long mSecondsRemaining;
	private String mSecondsText;
	private String mMinuteText;
	private boolean timerStarted;
	private GameState gameState;

	private TextToSpeech myTTS;
	private int MY_DATA_CHECK_CODE = 0;
	private int roundCount = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().setTitle(R.string.timer_title);
	}

	@Override
	public void onDestroy() {
		myTTS.shutdown();
		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				myTTS = new TextToSpeech(getActivity(),
						new TextToSpeech.OnInitListener() {

							@Override
							public void onInit(int status) {
								if (status == TextToSpeech.SUCCESS) {
									myTTS.setLanguage(Locale.UK);
								} else if (status == TextToSpeech.ERROR) {
									Toast.makeText(getActivity(),
											"Text to speech failed",
											Toast.LENGTH_SHORT).show();
								}

							}
						});
//				myTTS.setSpeechRate((float) 0.9);
			} else {
				Intent installTTSIntent = new Intent();
				installTTSIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installTTSIntent);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_timer, parent, false);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActivity().getActionBar();
			actionBar.setTitle(R.string.app_name);
		}

		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

		mTimeShow = (TextView) v.findViewById(R.id.time_remaining_textview);
		mRound = (TextView) v.findViewById(R.id.round_textview);
		mStage = (TextView) v.findViewById(R.id.stage_textview);
		mStart = (Button) v.findViewById(R.id.start_button);
		mReset = (Button) v.findViewById(R.id.reset_button);
		mSkip = (Button) v.findViewById(R.id.skip_button);
		mResetAge = (Button) v.findViewById(R.id.reset_age_button);

		timerStarted = false;

		// TODO: get gameState from SavedInstance
		gameState = GameState.ROUND;

		// TODO: find value of remaining time? = startTime? get from
		// SavedInstance
		setTimerLength();

		mStart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!timerStarted)
					startTimer();
				else
					pauseTimer();
			}
		});

		mReset.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				resetTimer();
			}
		});

		mSkip.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (countDownTimer != null)
					countDownTimer.cancel();
				skipToNextStage();
			}

		});

		mResetAge.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				resetAge();
			}
		});

		return v;
	}

	protected void resetAge() {
		roundCount = 1;
		gameState = GameState.ROUND;
		resetTimer();
	}

	private void skipToNextStage() {
		if (gameState == GameState.ROUND) {
			gameState = GameState.INTERIM;
		} else {
			gameState = GameState.ROUND;
			roundCount++;
		}
		setTimerLength();
		startTimer();
		updateDisplay();
	}

	private void setTimerLength() {
		if (gameState == GameState.ROUND)
			mSecondsRemaining = mRoundTimeLength;
		else
			mSecondsRemaining = mInterimTimeLength;
	}

	private void startTimer() {
		countDownTimer = new MyCountdownTimer(mSecondsRemaining, mInterval);
		countDownTimer.start();
		mStart.setText(R.string.pause);
		timerStarted = true;
	}

	private void pauseTimer() {
		countDownTimer.cancel();
		mStart.setText(R.string.start);
		timerStarted = false;
	}

	private void resetTimer() {
		countDownTimer.cancel();
		if (gameState == GameState.ROUND)
			mSecondsRemaining = mRoundTimeLength;
		else
			mSecondsRemaining = mInterimTimeLength;
		mStart.setText(R.string.start);
		timerStarted = false;
		updateDisplay();
	}

	// TODO: inefficient. fix this.
	private void updateDisplay() {
		mRound.setText("Round " + roundCount);

		if (timerStarted) {
			if (gameState == GameState.ROUND)
				mStage.setText(R.string.stage_choose);
			else
				mStage.setText(R.string.stage_play);
		} else
			mStage.setText(R.string.stage_empty);

		String leadingZero = "0";
		long minuteRemaining = mSecondsRemaining / 60;
		if (minuteRemaining >= 10) {
			leadingZero = "";
		}
		mMinuteText = leadingZero + mSecondsRemaining / 60;

		long secondsRemaining = mSecondsRemaining % 60;
		leadingZero = "0";
		if (secondsRemaining >= 10) {
			leadingZero = "";
		}
		mSecondsText = leadingZero + mSecondsRemaining % 60;
		mTimeShow.setText(mMinuteText + ":" + mSecondsText);
	}

	private void speakWords(String speech) {
		myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);

	}

	public class MyCountdownTimer extends CountDownTimer {

		public MyCountdownTimer(long secondsInFuture, long countDownInterval) {
			super(secondsInFuture * 1000, countDownInterval);
		}

		@Override
		public void onFinish() {
			if (gameState == GameState.ROUND)
				speakWords("please play your card");
			if (roundCount < MAX_ROUND)
				skipToNextStage();
			else
				resetAge();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			mSecondsRemaining = millisUntilFinished / 1000;
			if (gameState == GameState.ROUND) {
				if (mSecondsRemaining == 10)
					speakWords("ten seconds remaining");
				else if (mSecondsRemaining <= 5)
					speakWords("" + mSecondsRemaining);
				else if (mSecondsRemaining == mRoundTimeLength - 1)
					speakWords("round" + roundCount + "has started");
			} else {
				if (mSecondsRemaining == 5)
					speakWords("starting round" + (roundCount + 1) + "inn");
				else if (mSecondsRemaining <= 3)
					speakWords("" + mSecondsRemaining);
			}
			updateDisplay();
		}
	}
}
