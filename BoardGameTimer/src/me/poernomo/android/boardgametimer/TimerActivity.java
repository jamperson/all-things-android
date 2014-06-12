package me.poernomo.android.boardgametimer;

import android.support.v4.app.Fragment;

public class TimerActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment()
	{
		return new TimerFragment();
	}

}
