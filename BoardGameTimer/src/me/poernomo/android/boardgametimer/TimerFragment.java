package me.poernomo.android.boardgametimer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class TimerFragment extends Fragment {

	private TextView mTimeShow;
	private Button mStart;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getActivity().setTitle(R.string.timer_title);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.fragment_timer, parent, false);

		mTimeShow = (TextView) v.findViewById(R.id.time_remaining_textview);
		mStart = (Button) v.findViewById(R.id.start_button);

		return v;
	}

}
