package jdigest;

public class TaskFactory
{
	public static BasicTask createTask(Options options, Logger logger)
	{
		if(options.getAction() == Options.Action.check)
			return new CheckTask(options, logger);
		else if(options.getAction() == Options.Action.digest)
			return new DigestTask(options, logger);
		else
			return new NullTask(options, logger);
	}
}
