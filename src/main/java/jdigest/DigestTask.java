package jdigest;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;

public class DigestTask extends BasicTask
{
	public DigestTask(Options options, Logger logger)
	{
		super(options, logger);
		this.options = options;
	}

	/**
	 * Analyzes a file or all the files in a directory
	 *
	 * @param file The file or directory to be analyzed
	 */
	private void analyzeRecursive(File file)
	{
		try
		{
			if(Util.isSymlink(file))
				return;
		}
		catch(IOException e)
		{
			return;
		}

		if(file.isDirectory())
		{
			setFileOrFolderName(file.getPath());
			File[] subFiles = file.listFiles();
			if(subFiles != null)
				for(int i = 0; i < subFiles.length && !isCancelled(); i++)
				{
					File subFile = subFiles[i];
					analyzeRecursive(subFile);
				}
		}
		else
		{
			setTotalFileCount(getTotalFileCount() + 1);
			setTotalSize(getTotalSize() + file.length());
		}
	}

	/**
	 * Calculates the digest of a file or of all the files in a directory.
	 * The calculated digest(s) is/are written to the given PrintStream in
	 * standard digest file format (one line per file: digest + " *" +
	 * file name)
	 *  
	 * @param file The file or directory whose digest(s) is/are to be calculated
	 * @param out The DigestFileWriter to which the calculated digest(s) will be
	 * written
	 */
	private void getDigestRecursive(File file, DigestFileWriter out)
	{
		try
		{
			if(Util.isSymlink(file))
			{
				publish(new Logger.Message(Logger.Message.Type.NFO,
					"Ignored symbolic link: " + file.toString()));
				return;
			}
		}
		catch(IOException e)
		{
			publish(new Logger.Message(Logger.Message.Type.ERR, e));
			return;
		}

		if(file.isDirectory())
		{
			File[] subFiles = file.listFiles();
			if(subFiles == null)
			{
				setErrorCount(getErrorCount() + 1);
				publish(new Logger.Message(
						Logger.Message.Type.ERR,
						new IOException(file.toString())
				));
			}
			else
				for(int i = 0; i < subFiles.length && !isCancelled(); i++)
				{
					File subFile = subFiles[i];
					getDigestRecursive(subFile, out);
				}
		}
		else
		{
			try
			{
				byte[] digest = getDigest(file);
				if(!isCancelled())
				{
					out.writeDigest(digestToHexString(digest),
						options.getRelativePath(file));
				}
			}
			catch(Exception e)
			{
				setErrorCount(getErrorCount() + 1);
				publish(new Logger.Message(
						Logger.Message.Type.ERR, e));
			}
		}
	}

	public Void doInBackground()
	{
		try
		{
			initialize();
			DigestFileWriter out = new DigestFileWriter(
				options.getDigestFile(), options.getDigestFileEncoding().name);

			setStatus("Analyzing");
			publish(new Logger.Message(
				Logger.Message.Type.NFO, "Analyzing..."));
			for(int i = 0; i < options.getFileCount() && !isCancelled(); i++)
				analyzeRecursive(new File(options.getFile(i)));
			if(!isCancelled())
			{
				publish(new Logger.Message(
					Logger.Message.Type.NFO, new Formatter().format(
						"Found %,d files (%s)", getTotalFileCount(),
						Util.getByteString(getTotalSize()))));				
				setStatus("Digesting");
				publish(new Logger.Message(
					Logger.Message.Type.NFO, "Digesting..."));
				startMillis = Calendar.getInstance().getTimeInMillis();
				for(int i = 0; i < options.getFileCount() && !isCancelled();
				i++)
					getDigestRecursive(new File(options.getFile(i)), out);
			}
			out.close();
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
					new Formatter().format("Digested %,d files (%s) in " +
						"%d:%02d:%02d (%s/sec)", getDoneFileCount(),
						Util.getByteString(getDoneSize()), hours,
						minutes, seconds,
						Util.getByteString(getDoneSize() / deltaSecs))));
				if(getErrorCount() > 0)
				{
					setStatus("Error");
					publish(new Logger.Message(Logger.Message.Type.WRN,
						"Some files/folders were skipped" +
						", please check the log"));
					publish(new Logger.Message(Logger.Message.Type.WRN,
					"Finished with warnings"));
				}
				else
				{
					setStatus("Done");
					publish(new Logger.Message(
						Logger.Message.Type.NFO, "Finished successfully"));
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
