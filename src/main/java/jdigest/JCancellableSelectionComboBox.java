package jdigest;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

public class JCancellableSelectionComboBox extends JComboBox
{
	private static final long serialVersionUID = 1L;

	private Object last = null;
	private Object current  = null;

	public JCancellableSelectionComboBox(Object[] items)
	{
		super(items);
		initialize();
	}

	public JCancellableSelectionComboBox()
	{
		super();
		initialize();
	}

	private void initialize()
	{
		addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					last = current;
					current = e.getItem();
				}
			}
		});
	}

	public void undoSelectionLater()
	{
		selectLater(last);
	}

	public void selectLater(final Object selection)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				current = selection;
				setSelectedItem(selection);
			}
		});
	}
}
