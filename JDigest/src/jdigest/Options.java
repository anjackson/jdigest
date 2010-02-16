package jdigest;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Options
{
	public class OptionsException extends java.lang.Exception
	{
		private static final long serialVersionUID = 1L;

		public OptionsException(String message)
		{
			super(message);
		}
	}

	public static abstract class PathRelativizer
	{
		/***
		 * Converts the given path to relative according to this object settings
		 * (which may be to just convert the path to Absolute). If path is a
		 * relative path already, it is first made absolute (@see
		 * java.io.File.getAbsolutePath), and only then converted to relative
		 * @param path the path to be made relative
		 * @return the relative representation of path
		 */
		public abstract String getRelativePath(File path);

		/***
		 * Converts the given path to absolute according to this object settings
		 * (which may be to just assume the path is absolute do nothing).
		 * @param path the path to be made absolute
		 * @return the absolute representation of path
		 */
		public abstract String getAbsolutePath(File path);

		public abstract String toString();

		/***
		 * Converts a path (assumed to be absolute) into a List of each of its
		 * elements. The first element in the returned list is the root: either a
		 * drive letter "L:\", a UNC path "\\computername" or the UNIX root "/".
		 * ".\"'s and "..\"'s are removed.  
		 * @param path
		 * @return a list of the path elements, the first element being the root
		 */
		private static List<String> getPathElements(File path)
		{
			List<String> pathElems = new ArrayList<String>();
			int nUp = 0;
			while(path != null)
			{
				File parent = path.getParentFile();
				String name = path.getName();
				if(parent != null)
				{
					// we still have not reached the root, add this element's name
					if(name.equals(".."))
						nUp++;
					else if(!name.equals("."))
						if(nUp == 0)
							pathElems.add(0, name);
						else
							nUp--;
				}
				else if(parent == null)
				{
					// we have reached the root
					if(path.getPath().equals("\\\\") && pathElems.size() > 0)
						// UNC path case, root = \\ + previous element
						pathElems.set(0, "\\\\" + pathElems.get(0) + "\\");
					else
						// non-UNC path case, root = path
						pathElems.add(0, path.getPath());
				}
				path = parent;
			}
			return pathElems;
		}

		/**
		 * Returns a representation of path relative to the directory represented by
		 * base, if possible (path and base share the same root); otherwise, the
		 * absolute representation of path is returned
		 * @param path
		 * @param base
		 * @return
		 */
		protected static String relativize(File path, File base)
		{
			if(!path.isAbsolute()) path = path.getAbsoluteFile();
			if(!base.isAbsolute()) base = base.getAbsoluteFile();

			List<String> pathElems = getPathElements(path);
			List<String> baseElems = getPathElements(base);

			if(!new File(pathElems.get(0)).equals(new File(baseElems.get(0))))
				return path.getPath();

			int i = 1;
			while(i < pathElems.size() && i < baseElems.size()
			&& new File(pathElems.get(i)).equals(new File(baseElems.get(i))))
				i++;

			String relativePathFwd = "";
			for(int j = i; j < pathElems.size(); j++)
				relativePathFwd += (relativePathFwd.length() == 0?
					"": File.separator) + pathElems.get(j);

			String relativePathBwd = "";
			for(int j = i; j < baseElems.size(); j++)
				relativePathBwd += ".." + File.separator;

			return relativePathBwd + relativePathFwd;
		}
	}

	public static class AbsolutePathRelativizer extends PathRelativizer
	{
		public AbsolutePathRelativizer()
		{
		}

		public String getRelativePath(File path)
		{
			return path.getAbsolutePath();
		}

		public String getAbsolutePath(File path)
		{
			return path.getPath();
		}

		public String toString()
		{
			return "Use full paths";
		}
	}

	public static class ToDigestFilePathRelativizer extends PathRelativizer
	{
		private Options options;

		public ToDigestFilePathRelativizer(Options options)
		{
			this.options = options;
		}

		private File getBase()
		{
			return new File(options.getDigestFile())
				.getAbsoluteFile().getParentFile();
		}

		public String getRelativePath(File path)
		{
			path = path.getAbsoluteFile();
			return relativize(path, getBase());
		}

		public String getAbsolutePath(File path)
		{
			return new File(getBase(), path.getPath()).getAbsolutePath();
		}

		public String toString()
		{
			return "Make relative to digest file";
		}
	}

	public static class ToCustomFilePathRelativizer extends PathRelativizer
	{
		private File base;

		public ToCustomFilePathRelativizer(String base)
		{
			setBase(base);
		}

		public String getBase()
		{
			return base.getPath();
		}

		public void setBase(String base)
		{
			setBase(new File(base));
		}

		public void setBase(File base)
		{
			this.base = base.getAbsoluteFile();
		}

		public String getRelativePath(File path)
		{
			path = path.getAbsoluteFile();
			return relativize(path, base);
		}

		public String getAbsolutePath(File path)
		{
			return new File(base, path.getPath()).getAbsolutePath();
		}

		public String toString()
		{
			return "Custom...";
		}
	}

	public static class Encoding
	{
		public static final Encoding defaultEncoding =
			new Encoding(Charset.defaultCharset().name());
		public static final Encoding utf8 = new Encoding("UTF-8");

		public final String name;
		public Encoding(String name)
		{
			this.name = name;
		}
		public String toString()
		{
			return this.name
				+ (equals(defaultEncoding)? " (System default)": ""); 
		}
		public boolean equals(Object o)
		{
			return (o instanceof Encoding && equals((Encoding)o));
		}
		public boolean equals(Encoding e)
		{
			return e.name.equals(this.name);
		}
		public int hashCode()
		{
			return name.hashCode();
		}
		public boolean isSupported()
		{
			return Charset.isSupported(name);
		}
		public static Encoding[] getBasicList(Encoding current)
		{
			if(!defaultEncoding.equals(current) && !utf8.equals(current))
				return new Encoding[] {defaultEncoding, utf8, current};
			else
				return new Encoding[] {defaultEncoding, utf8};
		}
		public static Encoding[] getFullList()
		{
			Charset[] available =
				Charset.availableCharsets().values().toArray(new Charset[0]);
			Encoding[] fullList = new Encoding[available.length];
			for(int i = 0; i < available.length; i++)
				fullList[i] = new Encoding(available[i].name());
			return fullList;
		}
	}

	public enum Action {digest, check, cancel};

	public enum Algorithm {
		md5("MD5", "md5", 128 / 8), sha1("SHA-1", "sha", 160 / 8)
		;
		public final String code;
		public final String extension;
		public final int digestLength;

		private Algorithm(String code, String extension, int digestLength)
		{
			this.code = code;
			this.extension = extension;
			this.digestLength = digestLength;
		}

		public String toString()
		{
			return code + " (" + (digestLength * 8) + " bit)";
		}

		public static Algorithm byCode(String code)
		{
			for(Algorithm alg: Algorithm.values())
				if(alg.code.equals(code))
					return alg;
			return null;
		}

		public static Algorithm byExtension(String fileName)
		{
			if(fileName != null)
				for(Algorithm alg: Algorithm.values())
					if(fileName.endsWith("." + alg.extension))
						return alg;
			return null;
		}
	}

	private boolean showOptionsWindow = false;
	private Encoding digestFileEncoding = Encoding.defaultEncoding;
	private PathRelativizer pathRelativizer =
		new ToDigestFilePathRelativizer(this);
	private Action action = null;
	private String digestFile = null;
	private String[] files = new String[0];
	private Algorithm algorithm = null;

	/**
	 * Creates a new Options object from the given command line parameters
	 * @param args
	 * @throws OptionsException
	 */
	public Options(String[] args)
	throws OptionsException
	{
		if(args.length == 0)
			return;

		int i = -1;
		while(++i < args.length)
		{
			if(args[i].equals("--check"))
			{
				action = Action.check;
				i++;
				if(i >= args.length)
					throw new OptionsException(
					"expected: {digestfile} for --check");
				else if(i < args.length - 1)
					throw new OptionsException(
					"only one file allowed with --check");
				digestFile = args[i];
				assert(i == args.length);
			}
			else if(args[i].equals("--digest"))
			{
				action = Action.digest;
				i++;
				if(i >= args.length - 1)
					throw new OptionsException(
					"expected: {file}... {digestfile} for --digest");
				files = new String[args.length - i - 1];
				for(int j = 0; j < files.length; j++)
					files[j] = args[i++];
				digestFile = args[i];
				assert(i == args.length);
			}
			else if(args[i].equals("--options"))
			{
				showOptionsWindow = true;
			}
			else if(args[i].equals("--encoding"))
			{
				i++;
				if(i >= args.length)
					throw new OptionsException(
					"expected: {enc} for --encoding");

				digestFileEncoding = new Encoding(args[i]);
				if(!digestFileEncoding.isSupported())
					throw new OptionsException("encoding " + args[i] +
					" is not supported by this Java VM");
			}
			else if(args[i].equals("--algorithm"))
			{
				i++;
				if(i >= args.length)
					throw new OptionsException(
					"expected: {alg} for --algorithm");

				algorithm = Algorithm.byCode(args[i].toUpperCase());
				if(algorithm == null)
					throw new OptionsException(
					"unrecognized algorithm: " + args[i]);
			}
			else if(args[i].equals("--paths"))
			{
				i++;
				if(i >= args.length)
					throw new OptionsException(
					"expected: {type} for --paths");

				if(args[i].equals("absolute"))
					pathRelativizer = new AbsolutePathRelativizer();
				else if(args[i].equals("relative-digest"))
					pathRelativizer = new ToDigestFilePathRelativizer(this);
				else if(args[i].equals("relative-custom"))
				{
					i++;
					if(i >= args.length)
						throw new OptionsException(
						"expected: [base] for --paths relative-custom");
					pathRelativizer = new ToCustomFilePathRelativizer(args[i]);
				}
				else
					throw new OptionsException(
					"unrecognized path processing mode: " + args[i]);
			}
			else
			{
				throw new OptionsException("unrecognized option: " + args[i]);
			}
		}

		if(action == null)
			throw new OptionsException("expected: --digest or --check");

		if(algorithm == null && !showOptionsWindow)
		{
			// deduce the algorithm from the digest file extension
			algorithm = Algorithm.byExtension(digestFile);
			if(algorithm == null)
				throw new OptionsException("expected: --algorithm {alg} or " +
				"digestfile with .md5 or .sha extension");
		}
	}

	/**
	 * Validates this Options object
	 * @throws OptionsException
	 */
	public void validate()
	throws OptionsException
	{
		if(action != Action.cancel)
		{
			if(action == Action.digest && files.length == 0)
				throw new OptionsException(
					"Please select one or more files / folders to digest");

			if(digestFile == null || digestFile.length() == 0)
				throw new OptionsException("Please select a digest file");

			if(algorithm == null)
				throw new OptionsException("Please select a digest algorithm");

			if(digestFileEncoding == null)
				throw new OptionsException(
					"Please select a digest file encoding");
		}
	}

	public Action getAction()
	{
		return action;
	}

	public void setAction(Action action)
	{
		this.action = action;
	}

	public String getDigestFile()
	{
		return digestFile;
	}

	public void setDigestFile(String digestFile)
	{
		this.digestFile = digestFile;
	}

	public int getFileCount()
	{
		return files.length;
	}

	public void setPathRelativizer(PathRelativizer pathRelativizer)
	{
		this.pathRelativizer = pathRelativizer;
	}

	public PathRelativizer getPathRelativizer()
	{
		return pathRelativizer;
	}

	public String getFile(int n)
	{
		return files[n];
	}

	public String[] getFiles()
	{
		return files;
	}

	public void setFiles(String[] files)
	{
		this.files = files;
	}

	public Encoding getDigestFileEncoding()
	{
		return digestFileEncoding;
	}

	public void setDigestFileEncoding(Encoding digestFileEncoding)
	{
		this.digestFileEncoding = digestFileEncoding;
	}

	public Algorithm getDigestAlgorithm()
	{
		return algorithm == null? Algorithm.byExtension(digestFile): algorithm;			
	}

	public void setDigestAlgorithm(Algorithm algorithm)
	{
		this.algorithm = algorithm;
	}

	public boolean getShowOptionsWindow()
	{
		return showOptionsWindow;
	}

	public void setShowOptionsWindow(boolean showOptionsWindow)
	{
		this.showOptionsWindow = showOptionsWindow;
	}

	public String getRelativePath(File path)
	{
		return getPathRelativizer().getRelativePath(path);
	}

	public String getAbsolutePath(File path)
	{
		return getPathRelativizer().getAbsolutePath(path);
	}

	public static String getUsageString()
	{
		return
"usage: JDigest [options] --digest {file} ... {digestfile}\n" +
"   or: JDigest [options] --check {digestfile}\n" +
"\n" +
"calculate or verify checksums\n" +
"\n" +
"options:\n" +
"\n" +
"   --options             show options window before starting\n" +
"   --algorithm {alg}     digest algorithm (MD5 or SHA-1); if ommitted, will try\n" +
"                         to deduce the algorithm from the digest file extension\n" +
"                         (.md5 or .sha)\n" +
"   --encoding {enc}      read/write digest file using given encoding; if\n" +
"                         omitted, the system default encoding is used\n" +
"   --paths {type} [base] write/read paths to/from the digest file as {type}:\n" +
"                         'absolute', 'relative-digest' (relative to the digest\n" +
"                         file location) or 'relative-custom' (relative to the\n" +
"                         given [base]\n";
	}
}
