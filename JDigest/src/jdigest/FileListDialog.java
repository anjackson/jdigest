package jdigest;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.TransferHandler;

public class FileListDialog extends JDialog
{
	private static final long serialVersionUID = 1L;

	private JList list;
	private String[] files;

	public FileListDialog(String[] files)
	{
		setTitle("Select files - JDigest");

		this.files = files;

		JLabel hintLabel = new JLabel("Use the Add button or Drop the files " +
			"to be digested into the list below");

		list = new JList();
		DefaultListModel model = new DefaultListModel();
		list.setModel(model);
		for(String file: this.files)
			model.addElement(file);

		list.setDropMode(DropMode.INSERT);
		list.setTransferHandler(new TransferHandler()
		{
			/* We use two data flavors:
			 * - javaFileListFlavor if possible
			 * - uri-list if above not available (Linux) */

			private static final long serialVersionUID = 1L;

			private DataFlavor getURIListDataFlavor()
			{
				try
				{
					return new DataFlavor(
						"text/uri-list;class=java.lang.String");
				}
				catch(Exception e)
				{
					return null;
				}
			}

			public boolean canImport(TransferHandler.TransferSupport info)
			{
				DataFlavor uriListDataFlavor = getURIListDataFlavor();
				boolean uriListDataFlavorSupported =
					uriListDataFlavor != null
					&& info.isDataFlavorSupported(uriListDataFlavor);

				return info.isDataFlavorSupported(
					DataFlavor.javaFileListFlavor)
					|| uriListDataFlavorSupported;
			}

			@SuppressWarnings("unchecked")
			public boolean importData(TransferHandler.TransferSupport info)
			{
				DataFlavor uriListDataFlavor = getURIListDataFlavor();

				if(!info.isDrop())
					return false;

				JList list = (JList)info.getComponent();
				DefaultListModel listModel = (DefaultListModel)list.getModel();
				JList.DropLocation dl =
					(JList.DropLocation)info.getDropLocation();
				int index = dl.getIndex();
				try
				{
					if(info.isDataFlavorSupported(
						DataFlavor.javaFileListFlavor))
					{
						List<File> files = (List<File>)info.getTransferable()
							.getTransferData(DataFlavor.javaFileListFlavor);
						for(File file: files)
							listModel.add(index++, file.getAbsolutePath());
						return true;
					}
					else if(uriListDataFlavor != null
						&& info.isDataFlavorSupported(uriListDataFlavor))
					{
						String transferData = (String)info.getTransferable()
							.getTransferData(uriListDataFlavor);
						StringTokenizer tokenizer = new StringTokenizer(
							transferData, "\r\n");
						while(tokenizer.hasMoreTokens())
						{
							String line = tokenizer.nextToken().trim();
							if(!line.startsWith("#"))	// ignore comments
							{
								try
								{
									File file = new File(new URI(line));
									listModel.add(index++,
										file.getAbsolutePath());
								}
								catch(Exception e)
								{
									e.printStackTrace();
								}
							}
						}
						return true;
					}
					else
					{
						return false;
					}
				}
				catch(UnsupportedFlavorException e)
				{
					e.printStackTrace();
					return false;
				}
				catch(IOException e)
				{
					e.printStackTrace();
					return false;
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(list);

		JButton addButton = new JButton("Add...");
		JButton removeButton = new JButton("Remove");

		int maxButtonWidth = Math.max(addButton.getPreferredSize().width,
			removeButton.getPreferredSize().width);
		JPanel topPanel = new JPanel();
		GroupLayout layout = new GroupLayout(topPanel);
		topPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(hintLabel)
			.addGroup(layout.createSequentialGroup()
				.addComponent(scrollPane)
				.addGroup(layout.createParallelGroup()
					.addComponent(addButton, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, maxButtonWidth)
					.addComponent(removeButton, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, maxButtonWidth)
				)
			)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(hintLabel)
			.addGroup(layout.createParallelGroup()
				.addComponent(scrollPane)
				.addGroup(layout.createSequentialGroup()
					.addComponent(addButton)
					.addComponent(removeButton)
				)
			)
		);
		add(topPanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS));

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		JButton okButton = new JButton("Ok");
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(okButton);
		buttonPanel.add(Util.createHorizontalRigidArea(
			okButton, cancelButton, buttonPanel));
		buttonPanel.add(cancelButton);
		buttonPanel.setBorder(Util.createEmptyBorder(
			cancelButton, buttonPanel));

		bottomPanel.add(new JSeparator());
		bottomPanel.add(buttonPanel);

		add(bottomPanel, BorderLayout.SOUTH);

		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				DefaultListModel model = (DefaultListModel)list.getModel();
				FileListDialog.this.files = new String[model.size()];
				model.copyInto(FileListDialog.this.files);
				dispose();
			}
		});
		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		addButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				DefaultListModel listModel = (DefaultListModel)list.getModel();
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(
					JFileChooser.FILES_AND_DIRECTORIES);
				fileChooser.setMultiSelectionEnabled(true);
				int retval = fileChooser.showOpenDialog(FileListDialog.this);
				if(retval == JFileChooser.APPROVE_OPTION)
				{
					File[] files = fileChooser.getSelectedFiles();
					for(File file: files)
						listModel.addElement(file.getAbsolutePath());
				}
			}
		});
		removeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				DefaultListModel listModel = (DefaultListModel)list.getModel();
				int[] selected = list.getSelectedIndices();
				for(int i = selected.length - 1; i >=0; i--)
					listModel.remove(selected[i]);
			}
		});

		pack();
		setModal(true);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	public String[] getFiles()
	{
		return files;
	}
}
