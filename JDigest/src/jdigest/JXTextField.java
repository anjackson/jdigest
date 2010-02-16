package jdigest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;

/**
 * It's rubbish but JTextField does not have an "on change" listener event.
 * Yes, you can use a Document Listener but that gets triggered *every* time
 * the user presses a key. Yes, you can use a combination of ActionListener and
 * FocusListener, but it's cumbersome. This class does the job for you.
 *
 */
public class JXTextField extends JTextField
{
	private static final long serialVersionUID = 1L;

	public interface TextListener
	{
		public void textChanged();
	}

	private final List<TextListener> listeners = new ArrayList<TextListener>();

	public JXTextField()
	{
		super();
		initialize();
	}

	public JXTextField(int columns)
	{
		super(columns);
		initialize();
	}

	public JXTextField(String text)
	{
		super(text);
		initialize();
	}

	public JXTextField(String text, int columns)
	{
		super(text, columns);
		initialize();
	}

	public JXTextField(Document doc, String text, int columns)
	{
		super(doc, text, columns);
		initialize();
	}

	private void initialize()
	{
		final Runnable fireTextChanged = new Runnable()
		{
			public void run()
			{
				fireTextChanged();
			}
		};
		addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				SwingUtilities.invokeLater(fireTextChanged);
			}
		});
		addFocusListener(new FocusListener()
		{
			private String oldText;

			public void focusLost(FocusEvent e)
			{
				if(!getText().equals(oldText))
					SwingUtilities.invokeLater(fireTextChanged);
			}
		
			public void focusGained(FocusEvent e)
			{
				oldText = getText();
			}
		});
	}

	public void addTextListener(TextListener listener)
	{
		listeners.add(listener);
	}

	public void removeTextListener(TextListener listener)
	{
		listeners.remove(listener);
	}

	private void fireTextChanged()
	{
		for(TextListener listener: listeners)
			listener.textChanged();
	}
}
