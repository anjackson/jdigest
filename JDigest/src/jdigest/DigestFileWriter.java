package jdigest;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class DigestFileWriter
{
	private PrintStream writer;

	public DigestFileWriter(String out, String encoding)
	throws UnsupportedEncodingException, FileNotFoundException
	{
		this.writer = new PrintStream(
			new FileOutputStream(out), true, encoding);
	}

	public void writeDigest(String digest, String fileName)
	{
		writer.println(digest + " *" + fileName);
	}

	public void close()
	{
		writer.close();
	}
}
