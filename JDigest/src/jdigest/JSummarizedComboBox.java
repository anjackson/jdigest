package jdigest;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class JSummarizedComboBox extends JCancellableSelectionComboBox
{
	private static final long serialVersionUID = 1L;

	private class AdvancedItemSelectDialog extends JDialog
	{
		private static final long serialVersionUID = 1L;

		private JList list;
		private JTextField filterTextField;
		private Object selected;

		private class FilteringListModel extends AbstractListModel
		{
			private static final long serialVersionUID = 1L;

			private List<Object> list = new ArrayList<Object>();
			private List<Object> filteredList = new ArrayList<Object>();
			private String filter = null;

			public int getSize()
			{
				return filteredList.size();
			}

			public boolean filterMatches(Object element)
			{
				return filter == null? true:
					element.toString().toLowerCase().contains(
						filter.toLowerCase());
			}

			public void setFilter(String filter)
			{
				this.filter = filter;
				filteredList.clear();
				for(Object element: list)
					if(filterMatches(element))
						filteredList.add(element);
				fireContentsChanged(this, 0, getSize());
			}

			public Object getElementAt(int index)
			{
				return filteredList.get(index);
			}
		
			public void addElement(Object element)
			{
				list.add(element);
				if(filterMatches(element))
				{
					filteredList.add(element);
					fireIntervalAdded(this, getSize() - 1, getSize() - 1);
				}
			}
		}

		public AdvancedItemSelectDialog(Window owner)
		{
			super(owner);
			JLabel filterLabel = new JLabel("Filter:");
			filterTextField = new JTextField("");
			JButton showAllButton = new JButton("Show all");

			list = new JList();
			list.setModel(new FilteringListModel());
			JScrollPane scrollPane = new JScrollPane(list);

			JButton okButton = new JButton("Ok");
			JButton cancelButton = new JButton("Cancel");

			JPanel panel = new JPanel();
			GroupLayout layout = new GroupLayout(panel);
			panel.setLayout(layout);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
					.addComponent(filterLabel)
					.addComponent(filterTextField)
					.addComponent(showAllButton)
				)
				.addComponent(scrollPane)
				.addGroup(layout.createSequentialGroup()
					.addComponent(okButton)
					.addComponent(cancelButton)
				)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(filterLabel)
					.addComponent(filterTextField)
					.addComponent(showAllButton)
				)
				.addComponent(scrollPane)
				.addGroup(layout.createParallelGroup()
					.addComponent(okButton)
					.addComponent(cancelButton)
				)
			);

			setContentPane(panel);
			pack();
			setModal(true);

			filterTextField.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					applyFilter();
				}
			});
			showAllButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					filterTextField.setText("");
					applyFilter();
				}
			});
			okButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					System.out.println("ok!");
					selected = list.getSelectedValue();
					setVisible(false);
				}
			});
			cancelButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setVisible(false);
				}
			});
		}

		private void applyFilter()
		{
			FilteringListModel model = (FilteringListModel)list.getModel();
			model.setFilter(filterTextField.getText());
			if(model.getSize() > 0)
				list.setSelectedIndex(0);
		}

		public void addItem(Object item)
		{
			((FilteringListModel)list.getModel()).addElement(item);
		}

		public Object select()
		{
			selected = null;
			setLocationRelativeTo(JSummarizedComboBox.this);
			setVisible(true);
			return selected;
		}
	}

	private AdvancedItemSelectDialog dialog;

	private static class More
	{
		public String toString()
		{
			return "(more...)";
		}
	}

	public static final More ITEM_MORE = new More();

	private Set<Object> basicItems = new HashSet<Object>();

	public JSummarizedComboBox(Window owner)
	{
		super();
		dialog = new AdvancedItemSelectDialog(owner);
		addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Object selected = getSelectedItem();
				if(selected == ITEM_MORE)
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							final Object newSelection = dialog.select();
							if(newSelection == null)
								JSummarizedComboBox.this.undoSelectionLater();
							else
							{
								if(!basicItems.contains(newSelection))
									addBasicItem(newSelection);
								JSummarizedComboBox.this.selectLater(newSelection);
							}
						}
					});
				}
			}
		});
	}

	public void addItems(Object[] items)
	{
		for(Object item: items)
			addItem(item);
	}

	public void addItem(Object item)
	{
		dialog.addItem(item);
	}

	public void addBasicItems(Object[] basicItems)
	{
		for(Object basicItem: basicItems)
			addBasicItem(basicItem);
	}

	public void addBasicItem(Object basicItem)
	{
		super.removeItem(ITEM_MORE);
		basicItems.add(basicItem);
		super.addItem(basicItem);
		super.addItem(ITEM_MORE);
	}

//	public static void main(String[] args)
//	{
//		JFrame test = new JFrame();
//		final JSummarizedComboBox combo = new JSummarizedComboBox(test);
//		combo.addItems(new Object[] {"ANSI", "UTF-8", "SHIFT-JIS", "CP1526"});
//		combo.addBasicItems(new Object[] {"ANSI", "UTF-8"});
//		combo.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent e)
//			{
//				System.out.println("selected: " +
//					combo.getSelectedItem().toString());
//			}
//		});
//		test.add(combo);
//		test.pack();
//		test.setLocationRelativeTo(null);
//		test.setVisible(true);
//		test.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//	}
}
