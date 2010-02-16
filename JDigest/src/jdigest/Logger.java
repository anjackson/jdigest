package jdigest;
import java.awt.Component;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class Logger extends JTable
{
	private static final long serialVersionUID = 1L;

	public static class Message
	{
		enum Type {NFO, WRN, ERR}

		private GregorianCalendar timeStamp;
		private Type type;
		private Object details;

		public Message(Type type, Object details)
		{
			this.timeStamp = new GregorianCalendar();
			this.type = type;
			this.details = details;
		}

		public GregorianCalendar getTimeStamp()
		{
			return timeStamp;
		}

		public Type getType()
		{
			return type;
		}

		private ImageIcon getIcon()
		{
			return new ImageIcon(this.getClass().
				getResource("Icon" + getType() + ".png"));
		}

		public String getText()
		{
			return details.toString();
		}

		public Object getDetails()
		{
			return details;
		}
	}

	private static class LoggerTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 1L;

		// used for table column sizing purposes
		public final Message longMessage = new Message(Message.Type.NFO,
			"File C:\\a\\somewhat\\long\\path\\filename.ext could not be read" +
			"because of a very long error message with a stack trace");

		private String[] columnNames = {"", "Time", "Details"};
		private List<Message> data = new ArrayList<Message>();

		public int getColumnCount()
		{
			return columnNames.length;
		}

		public int getRowCount()
		{
			return data.size();
		}

		public String getColumnName(int col)
		{
			return columnNames[col];
		}

		public Class<?> getColumnClass(int col)
		{
			return new Class[] {
				ImageIcon.class,
				String.class,
				String.class
			}[col];
		}

		public Object[] getValues(Message message)
		{
			return new Object[] {
				message.getIcon(),
				DateFormat.getDateTimeInstance().format(
					message.getTimeStamp().getTime()),
				message.getDetails()
			};
		}

		public Object getValueAt(int row, int col)
		{
			Message message = data.get(row);
			return getValues(message)[col];
		}

		public void Append(Message message)
		{
			data.add(message);
			if(message.getDetails() instanceof Exception)
				((Exception)message.getDetails()).printStackTrace();
			fireTableRowsInserted(data.size() - 1, data.size() - 1);
		}

		public void clear()
		{
			int oldSize = data.size();
			data.clear();
			fireTableRowsDeleted(0, Math.max(0, oldSize - 1));
		}
	}

	public Logger()
	{
		super(new LoggerTableModel());
		initColumnSizes();
		setShowGrid(false);
	}

	private void initColumnSizes()
	{
		LoggerTableModel model = (LoggerTableModel)getModel();
		TableColumn column = null;
		Component comp = null;
		int headerWidth = 0;
		int cellWidth = 0;
		Object[] longValues = model.getValues(model.longMessage);
		TableCellRenderer headerRenderer =
			getTableHeader().getDefaultRenderer();

		for (int i = 0; i < getColumnCount(); i++)
		{
			column = getColumnModel().getColumn(i);
			comp = headerRenderer.getTableCellRendererComponent(
				null, column.getHeaderValue(), false, false, 0, 0);
			headerWidth = comp.getPreferredSize().width;

			comp = getDefaultRenderer(model.getColumnClass(i)).
			getTableCellRendererComponent(this, longValues[i],
				false, false, 0, i);
			cellWidth = comp.getPreferredSize().width + 5;

			column.setPreferredWidth(Math.max(headerWidth, cellWidth));
		}
	}

	public void Append(Message message)
	{
		((LoggerTableModel)getModel()).Append(message);
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				scrollRectToVisible(getCellRect(getRowCount() - 1, 0, true));
			}
		});
	}

	public void clear()
	{
		((LoggerTableModel)getModel()).clear();
	}
}
