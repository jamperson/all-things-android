package me.poernomo.android.boardgametimer;

import java.util.Locale;

import android.content.Intent;
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
	private final long mStartTime = 15 * 1000;
	private final long mInterval = 1 * 1000;
	private TextView mTimeShow;
	private Button mStart;
	private Button mReset;
	private long mSecondsRemaining;
	private String mSecondsText;
	private String mMinuteText;
	private boolean timerStarted;

	private TextToSpeech myTTS;
	private int MY_DATA_CHECK_CODE = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().setTitle(R.string.timer_title);
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
									myTTS.setLanguage(Locale.US);
								} else if (status == TextToSpeech.ERROR) {
									Toast.makeText(getActivity(),
											"Text to speech failed",
											Toast.LENGTH_SHORT).show();
								}

							}
						});
				myTTS.setSpeechRate((float) 0.8);
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

		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

		timerStarted = false;

		// TODO: find value of remaining time? = startTime? get from
		// SavedInstance
		mSecondsRemaining = mStartTime / 1000;

		mTimeShow = (TextView) v.findViewById(R.id.time_remaining_textview);
		mStart = (Button) v.findViewById(R.id.start_button);
		mStart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!timerStarted) {
					countDownTimer = new RoundTimer(mSecondsRemaining * 1000,
							mInterval);
					countDownTimer.start();
					mStart.setText(R.string.pause);
					timerStarted = true;
				} else {
					countDownTimer.cancel();
					mStart.setText(R.string.start);
					timerStarted = false;
				}
			}

		});
		mReset = (Button) v.findViewById(R.id.reset_button);
		mReset.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				countDownTimer.cancel();
				resetTimer();
			}
		});

		return v;
	}

	// TODO: highly inefficient. fix this.
	private void updateDisplay() {
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

	private void resetTimer() {
		mSecondsRemaining = mStartTime / 1000;
		mStart.setText(R.string.start);
		timerStarted = false;
		updateDisplay();
	}

	private void speakWords(String speech) {
		myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);

	}

	public class RoundTimer extends CountDownTimer {

		public RoundTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			speakWords("times up");
			resetTimer();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			mSecondsRemaining = millisUntilFinished / 1000;
			if (mSecondsRemaining == 7)
				speakWords("this round is ending in");
			else if (mSecondsRemaining <= 5)
				speakWords("" + mSecondsRemaining);
			else if (mSecondsRemaining == (mStartTime / 1000) - 1)
				speakWords("starting new round");
			updateDisplay();
		}
	}

	public class InterimTimer extends CountDownTimer {

		public InterimTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub

		}
	}
}
