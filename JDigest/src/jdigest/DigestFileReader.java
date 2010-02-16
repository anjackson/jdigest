package jdigest;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class DigestFileReader
{
	public class MalformedLineException extends Exception
	{
		private static final long serialVersionUID = 1L;

		public MalformedLineException(String file, long lineNumber, String line)
		{
			super(file + ":" + lineNumber + ": \"" + line + "\"");
		}
	}

	public class FileDigest
	{
		public String file;
		public String digest;

		public FileDigest(String file, String digest)
		{
			this.file = file;
			this.digest = digest;
		}
	}

	private String in;
	private BufferedReader reader;
	private long lineNumber;

	public DigestFileReader(String in, String encoding)
	throws UnsupportedEncodingException, FileNotFoundException
	{
		this.in = in;
		reader = new BufferedReader(new InputStreamReader(
			new FileInputStream(in), encoding));
		lineNumber = 0;
	}

	public FileDigest readDigest()
	throws IOException, MalformedLineException
	{
		String line = reader.readLine();
		if(line == null)
			return null;
		else
		{
			lineNumber += 1;
			int index = line.indexOf(' ');
			if(index == -1 || line.length() <= index + 3)
				throw new MalformedLineException(in, lineNumber, line);
			else
				return new FileDigest(line.substring(index + 2),
					line.substring(0, index));
		}
	}

	public void close()
	throws IOException
	{
		reader.close();
	}
}
