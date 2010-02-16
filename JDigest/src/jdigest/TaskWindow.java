package jdigest;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Formatter;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class TaskWindow extends JFrame
{
	private static final long serialVersionUID = 1L;

	protected Logger logger;
	private JLabel folderNameLabel;
	private JXProgressBar totalProgressBar;
	private long totalFileCount;
	private long totalSize;
	private long doneFileCount = 0;
	private long doneSize = 0;
	private JLabel totalProgressLabel;
	private JLabel fileNameLabel;
	private JXProgressBar fileProgressBar;
	private long fileSize;
	private JLabel fileProgressLabel;
	private JButton cancelButton;
	private boolean taskDone = false;
	private boolean closePending = false;

	private BasicTask task;

	private final String title;

	public TaskWindow(String title)
	{
		this.title = title;
		setTitle(title);

		logger = new Logger();

		folderNameLabel = new JLabel(" ");
		folderNameLabel.setAlignmentX(LEFT_ALIGNMENT);
		totalProgressBar = new JXProgressBar(1000*60*10, 0, 0);
		totalProgressBar.setAlignmentX(LEFT_ALIGNMENT);
		totalProgressLabel = new JLabel(" ");
		totalProgressLabel.setAlignmentX(LEFT_ALIGNMENT);
		fileNameLabel = new JLabel(" ");
		fileNameLabel.setAlignmentX(LEFT_ALIGNMENT);
		fileProgressBar = new JXProgressBar(1000*10, 0, 0);
		fileProgressBar.setAlignmentX(LEFT_ALIGNMENT);
		fileProgressLabel = new JLabel(" ");
		fileProgressLabel.setAlignmentX(LEFT_ALIGNMENT);
		cancelButton = new JButton("Cancel");

		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane(logger);
		logger.setFillsViewportHeight(true);
		logger.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//logger.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		scrollPane.setAlignmentX(LEFT_ALIGNMENT);
		add(totalProgressLabel);
		add(totalProgressBar);
		add(folderNameLabel);
		add(fileNameLabel);
		add(fileProgressBar);
		add(fileProgressLabel);
		add(scrollPane);
		cancelButton.setAlignmentX(LEFT_ALIGNMENT);
		add(cancelButton);

		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cancelOrClose();
			}
		});

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				closePending = true;
				cancelOrClose();
			}
		});

		pack();
		setSize(480, 320);
		setLocationRelativeTo(null);
	}

	protected void setTask(BasicTask task)
	{
		this.task = task;
		task.addPropertyChangeListener(new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent e)
			{
				if("fileProgress".equals(e.getPropertyName()))
				{
					long fileProgress = (Long)e.getNewValue(); 
					fileProgressBar.update(fileProgress);
					if(fileProgress == 0)
						fileProgressLabel.setText(new Formatter().format(
							"Current file: %s of %s",
							Util.getByteString(fileProgress),
							Util.getByteString(fileSize)).toString());
					else
						fileProgressLabel.setText(new Formatter().format(
							"Current file: %s - %s of %s (%s/sec)",
							fileProgressBar.getInstantETAAsString(),
							Util.getByteString(fileProgress),
							Util.getByteString(fileSize),
							Util.getByteString(
								fileProgressBar.getInstantSpeed()))
							.toString());
				}
				else if("doneSize".equals(e.getPropertyName()))
				{
					doneSize = (Long)e.getNewValue();
					totalProgressBar.update(doneSize);
					setTitle(new Formatter().format(
						"%.0f%% - %s",
						100* totalProgressBar.getPercentComplete(),
						title
					).toString());
					//setTitle("JDigest - " + title + " - " +
					//	totalProgressBar.getPercentComplete() + "%");
					updateTotalProgressLabel();
				}
				else if("fileOrFolderName".equals(e.getPropertyName()))
				{
					fileNameLabel.setText("Current file: " +
						(String)e.getNewValue());
				}
				else if("fileSize".equals(e.getPropertyName()))
				{
					fileSize = (Long)e.getNewValue();
					fileProgressBar.reset(0, fileSize);
				}
				else if("doneFileCount".equals(e.getPropertyName()))
				{
					doneFileCount = (Long)e.getNewValue();
					updateTotalProgressLabel();
				}
				else if("folderName".equals(e.getPropertyName()))
				{
					folderNameLabel.setText("Current folder: " +
						(String)e.getNewValue());
				}
				else if("totalFileCount".equals(e.getPropertyName()))
				{
					totalFileCount = (Long)e.getNewValue();
					totalProgressLabel.setText(new Formatter().format(
							"Analyzing... (%,d files found so far)",
							totalFileCount).toString());
				}
				else if("totalSize".equals(e.getPropertyName()))
				{
					totalSize = (Long)e.getNewValue();
					totalProgressBar.reset(0, totalSize);
				}
				else if("done".equals(e.getPropertyName()))
				{
					taskDone = (Boolean)e.getNewValue();
					if(taskDone)
					{
						if(closePending)
							dispose();
						else
						{
							cancelButton.setText("Close");
							cancelButton.setEnabled(true);
						}
					}
				}
			}
		});
		task.execute();
		setVisible(true);
	}

	private void updateTotalProgressLabel()
	{
		totalProgressLabel.setText(new Formatter().format(
			"Total: %s - %,d of %,d files / %s of %s (%s/sec)",
			totalProgressBar.getAverageETAAsString(),
			doneFileCount, totalFileCount,
			Util.getByteString(doneSize), Util.getByteString(totalSize),
			Util.getByteString(totalProgressBar.getAverageSpeed()))
			.toString());
	}

	private void cancelOrClose()
	{
		if(taskDone)
			dispose();
		else
		{
			task.cancel(false);
			cancelButton.setText("Cancelling...");
			cancelButton.setEnabled(false);
		}
	}
}
