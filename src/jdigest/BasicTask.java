package jdigest;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;

import javax.swing.SwingWorker;

public abstract class BasicTask extends SwingWorker<Void, Logger.Message>
{
	protected Options options;
	private Logger logger;
	private String status;

	private byte[] buffer = new byte[128*1024];
	private MessageDigest messageDigest;

	private long totalFileCount = 0;
	private long doneFileCount = 0;
	private long totalSize = 0;
	private long doneSize = 0;

	private String fileOrFolderName = null;
	private long fileSize = 0;
	private long fileProgress = 0;

	private boolean done = false;

	private long errorCount = 0;
//	private long warningCount = 0;

	protected long startMillis;

	public BasicTask(Options options, Logger logger)
	{
		this.options = options;
		this.logger = logger;
		this.status = "Initializing";
	}

	protected void setStatus(String status)
	{
		String old = this.status;
		this.status = status;
		firePropertyChange("status", old, status);
	}

	public String getStatus()
	{
		return status;
	}

	protected void initialize()
	throws NoSuchAlgorithmException
	{
		messageDigest = MessageDigest.getInstance(
			options.getDigestAlgorithm().code);
	}

	protected void setTotalFileCount(long totalFileCount)
	{
		long old = this.totalFileCount;
		this.totalFileCount = totalFileCount;
		firePropertyChange("totalFileCount", old, totalFileCount);
	}

	protected long getTotalFileCount()
	{
		return totalFileCount;
	}

	private void setDoneFileCount(long doneFileCount)
	{
		long old = this.doneFileCount;
		this.doneFileCount = doneFileCount;
		firePropertyChange("doneFileCount", old, doneFileCount);
	}

	protected long getDoneFileCount()
	{
		return doneFileCount;
	}

	protected void setTotalSize(long totalSize)
	{
		long old = this.totalSize;
		this.totalSize = totalSize;
		firePropertyChange("totalSize", old, totalSize);
	}

	protected long getTotalSize()
	{
		return totalSize;
	}

	private void setDoneSize(long doneSize)
	{
		long old = this.doneSize;
		this.doneSize = doneSize;
		firePropertyChange("doneSize", old, doneSize);
	}	

	protected long getDoneSize()
	{
		return doneSize;
	}

	protected void setFileOrFolderName(String fileOrFolderName)
	{
		String old = this.fileOrFolderName;
		this.fileOrFolderName = fileOrFolderName;
		firePropertyChange("fileOrFolderName", old, fileOrFolderName);
	}

	private void setFileSize(long fileSize)
	{
		long old = this.fileSize;
		this.fileSize = fileSize;
		firePropertyChange("fileSize", old, fileSize);
	}

	private void setFileProgress(long fileProgress)
	{
		long old = this.fileProgress;
		this.fileProgress = fileProgress;
		firePropertyChange("fileProgress", old, fileProgress);
	}

	protected void setErrorCount(long errorCount)
	{
		long old = this.errorCount;
		this.errorCount = errorCount;
		firePropertyChange("errorCount", old, errorCount);
	}

	protected long getErrorCount()
	{
		return errorCount;
	}

	protected void setDone(boolean done)
	{
		boolean old = this.done;
		this.done = done;
		firePropertyChange("done", old, done);
	}

	protected Logger getLogger()
	{
		return logger;
	}

	/**
	 * Calculates a file's digest
	 * 
	 * @param file The file whose digest is to be calculated
	 * @return The digest represented as a byte array, or null if cancelled
	 * @throws FileNotFoundException if the file is not found
	 * @throws IOException if there is an IO exception while reading the file
	 */
	protected byte[] getDigest(File file)
	throws FileNotFoundException, IOException
	{
		messageDigest.reset();
		setFileOrFolderName(file.getPath());
		setFileSize(file.length());
		setFileProgress(0);
		BufferedInputStream in = new BufferedInputStream(
				new FileInputStream(file));

		int n = 0;
		int accum = 0;
		long millisA = Calendar.getInstance().getTimeInMillis();
		while(n >= 0 && !isCancelled())
		{
			n = in.read(buffer);
			if(n > 0)
			{
				messageDigest.update(buffer, 0, n);
				accum += n;
				long millisB = Calendar.getInstance().getTimeInMillis();
				if(millisB - millisA >= 250)
				{
					setFileProgress(fileProgress + accum);
					setDoneSize(doneSize + accum);
					accum = 0;
					millisA = millisB;
				}
			}
		}

		in.close();

		if(isCancelled())
			return null;
		else
		{
			setFileProgress(fileProgress + accum);
			setDoneSize(doneSize + accum);
			setDoneFileCount(doneFileCount + 1);
			return messageDigest.digest();
		}
	}

	/**
	 * Converts a digest represented by a byte array to a String of
	 * hexadecimal digits
	 * 
	 * @param digest The digest to be converted to an hexadecimal string
	 * @return The hexadecimal string representation of the digest
	 */
	public String digestToHexString(byte[] digest)
	{
		Formatter f = new Formatter();
		for (byte c : digest)
			f.format("%02x", c);
		return f.toString();
	}

	public abstract Void doInBackground();

	protected void process(List<Logger.Message> chunks)
	{
		for(Logger.Message message : chunks)
			logger.Append(message);
	}
}
