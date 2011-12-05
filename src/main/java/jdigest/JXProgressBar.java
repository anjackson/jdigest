package jdigest;
import java.util.Calendar;

import javax.swing.JProgressBar;


public class JXProgressBar extends JProgressBar
{
	private static final long serialVersionUID = 1L;

	// JProgressBar can't handle long values, so we use an arbitrary scale
	private static final int PROGRESSBAR_MAX = 1024*1024;

	private long millisImmediate;
	private long min;
	private long max;
	private long current;
	private long minMillis;
	private long currentMillis;
	private long accum;
	private long accumMillis;

	public JXProgressBar(long millisImmediate, long min, long max)
	{
		super(0, PROGRESSBAR_MAX);
		this.millisImmediate = millisImmediate;
		reset(min, max);
	}

	public long now()
	{
		return Calendar.getInstance().getTimeInMillis();
	}

	public void reset(long min, long max)
	{
		this.min = min;
		this.max = max;
		this.current = min;
		this.minMillis = now();
		this.currentMillis = minMillis;
		this.accum = 0;
		this.accumMillis = 0;
		setValue(0);
	}

	public void update(long current)
	{
		long delta = current - this.current;
		long deltaMillis = now() - currentMillis;

		accum += delta;
		accumMillis += deltaMillis;
		if(accumMillis > millisImmediate)
		{
			accum -= (accum / accumMillis)
				* (accumMillis - millisImmediate);
			accumMillis = millisImmediate;
		}

		this.current = current;
		this.currentMillis += deltaMillis;

		if(max - min == 0)
			setValue(0);
		else
			setValue((int)((this.current) * PROGRESSBAR_MAX / (max - min)));
	}

	public long getInstantSpeed()
	{
		if(accumMillis == 0)
			return 0;
		else
			return (accum * 1000)/ accumMillis;
	}

	public long getAverageSpeed()
	{
		if(currentMillis - minMillis == 0)
			return 0;
		else
			return ((current - min) * 1000) / (currentMillis - minMillis);
	}

	public long getAverageETA()
	{
		return getETA(getAverageSpeed());
	}

	public long getInstantETA()
	{
		return getETA(getInstantSpeed());
	}

	private long getETA(long averageSpeed)
	{
		if(averageSpeed == 0)
			return -1;
		else
			return (max - current) / averageSpeed;
	}

	public String getAverageETAAsString()
	{
		return getETAAsString(getAverageETA());
	}

	public String getInstantETAAsString()
	{
		return getETAAsString(getInstantETA());
	}

	private String getETAAsString(long ETA)
	{
		if(ETA == -1)
			return "remaining time unknown";
		else if(ETA > 2 * 60 * 60)
			return "" + (ETA / 60 / 60) + " hours remaining";
		else if(ETA > 2 * 60)
			return "" + (ETA / 60) + " minutes remaining";
		else if(ETA > 60)
			return "1 minute remaining";
		else
			return "" + ETA + " seconds remaining";
	}
}
