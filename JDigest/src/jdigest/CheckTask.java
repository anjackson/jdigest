package jdigest;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;

public class CheckTask extends BasicTask
{
	public CheckTask(Options options, Logger logger)
	{
		super(options, logger);
		this.options = options;
	}

	/**
	 * Analyzes a digest file
	 * 
	 * @param in The digest file reader
	 * @throws IOException If the digest file can not be read
	 */
	private void analyze(DigestFileReader in)
	throws IOException
	{
		boolean eof = false;
		DigestFileReader.FileDigest fileDigest;
		do
		{
			try
			{
				fileDigest = in.readDigest();
				if(fileDigest != null)
				{
					File file = new File(
						options.getAbsolutePath(new File(fileDigest.file)));
					setTotalSize(getTotalSize() + file.length());
					setTotalFileCount(getTotalFileCount() + 1);
				}
				else
					eof = true;
			}
			catch(DigestFileReader.MalformedLineException e)
			{
			}
		} while(!eof && !isCancelled());
	}

	private void check(DigestFileReader in)
	throws IOException
	{
		boolean eof = false;
		DigestFileReader.FileDigest fileDigest;
		do
		{
			try
			{
				fileDigest = in.readDigest();
				if(fileDigest != null)
				{
					File file = new File(
						options.getAbsolutePath(new File(fileDigest.file)));
					setFileOrFolderName(file.getParent());
					try
					{
						byte[] digest = getDigest(file);
						if(!isCancelled() && !fileDigest.digest.equals(
							digestToHexString(digest)))
						{
							setErrorCount(getErrorCount() + 1);
							publish(new Logger.Message(
								Logger.Message.Type.ERR,
								"Checksum mismatch: " + fileDigest.file));
						}
					}
					catch(Exception e)
					{
						setErrorCount(getErrorCount() + 1);
						publish(new Logger.Message(
								Logger.Message.Type.ERR, e));
					}
				}
				else
					eof = true;
			}
			catch(DigestFileReader.MalformedLineException e)
			{
				setErrorCount(getErrorCount() + 1);
				publish(new Logger.Message(
					Logger.Message.Type.ERR, e)
				);
			}
		} while(!eof && !isCancelled());
	}

	public Void doInBackground()
	{
		try
		{
			initialize();
			setStatus("Analyzing");
			publish(new Logger.Message(
				Logger.Message.Type.NFO, "Analyzing..."));
			DigestFileReader in = new DigestFileReader(
				options.getDigestFile(), options.getDigestFileEncoding().name);
			analyze(in);
			in.close();
			if(!isCancelled())
			{
				publish(new Logger.Message(
					Logger.Message.Type.NFO, new Formatter().format(
						"Found %,d files (%s)", getTotalFileCount(),
						Util.getByteString(getTotalSize()))));				
				setStatus("Checking");
				publish(new Logger.Message(
					Logger.Message.Type.NFO, "Checking..."));
				startMillis = Calendar.getInstance().getTimeInMillis();
				in = new DigestFileReader(options.getDigestFile(),
					options.getDigestFileEncoding().name);
				check(in);
				in.close();
			}
			if(isCancelled())
			{
				setStatus("Cancelled");
				publish(new Logger.Message(
					Logger.Message.Type.WRN, "Cancelled"));
			}
			else
			{
				long endMillis = Calendar.getInstance().getTimeInMillis();
				long deltaSecs = Math.max(1, (endMillis - startMillis) / 1000);
				long hours = deltaSecs / (60 * 60);
				long minutes = (deltaSecs % (60 * 60)) / 60;
				long seconds = deltaSecs % 60;
				publish(new Logger.Message(Logger.Message.Type.NFO,
					new Formatter().format("Checked %,d files (%s) in " +
						"%d:%02d:%02d (%s/sec)", getDoneFileCount(),
						Util.getByteString(getDoneSize()), hours,
						minutes, seconds,
						Util.getByteString(getDoneSize() / deltaSecs))));
				if(getErrorCount() > 0)
				{
					setStatus("Error");
					publish(new Logger.Message(Logger.Message.Type.ERR,
						"Some checksums did not match and/or some errors " +
						"occurred, please check the log"));
					publish(new Logger.Message(Logger.Message.Type.ERR,
					"Finished with errors"));
				}
				else
				{
					setStatus("Done");
					publish(new Logger.Message(
						Logger.Message.Type.NFO,
						"Finished successfully (all checksums matched)"));
				}
			}
		}
		catch(Exception e)
		{
			publish(new Logger.Message(Logger.Message.Type.ERR, e));
		}

		setDone(true);
		return null;
	}

	protected void process(List<Logger.Message> chunks)
	{
		for(Logger.Message message : chunks)
			getLogger().Append(message);
	}
}
