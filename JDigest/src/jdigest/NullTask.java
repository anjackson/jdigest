package jdigest;

public class NullTask extends BasicTask
{
	
	public NullTask(Options options, Logger logger)
	{
		super(options, logger);
	}

	public Void doInBackground()
	{
		setStatus("Error");
		publish(new Logger.Message(
			Logger.Message.Type.ERR, "Invalid action: " + options.getAction()));
		setDone(true);
		return null;
	}
}
