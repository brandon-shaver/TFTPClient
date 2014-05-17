/**
 * Brandon Shaver and Cody Cartrette
 * CS 413 Advanced Networking
 * Dr. Cichanowski
 * 
 * TFTP Client for OSX 
 * 
 */
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.*;

@SuppressWarnings("serial")
public class MainWindow extends JFrame implements WindowListener{
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new MainWindow();
	}
	
	static JTextArea transferLog;
	
	static JLabel fileName;
	static JTextField fileNameInput;
	
	static JLabel serverName;
	static JTextField server;
	
	static JScrollPane logScroll;
	
	static JButton send;
	static JButton ping;
	static JButton cancel;
	static JButton clear;
	
	static ButtonGroup requestGroup;
	static JRadioButton read;
	static JRadioButton write;
	
	static ButtonGroup modeGroup;
	static JRadioButton netascii;
	static JRadioButton octet;
	static JRadioButton mail;
	
	MainWindow(){
		//Set fonts for UI
		Font font = new Font("Courier", Font.PLAIN, 11);
		Font font2 = new Font("Courier", Font.PLAIN, 12);
		
		JPanel requestPanel = new JPanel();
		JPanel modePanel = new JPanel();
		
		read = new JRadioButton("Read");
		write = new JRadioButton("Write");
		
		read.setBackground(Color.lightGray);
		write.setBackground(Color.lightGray);
		
		read.setFocusable(false);
		write.setFocusable(false);
		
		requestPanel.add(read);
		requestPanel.add(write);
		requestPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Request"));
		requestPanel.setBackground(Color.lightGray);
		requestPanel.setLayout(new BoxLayout(requestPanel, BoxLayout.Y_AXIS));
		
		requestGroup = new ButtonGroup();
		requestGroup.add(read);
		requestGroup.add(write);
		
		netascii = new JRadioButton("netascii");
		octet = new JRadioButton("octet");
		mail = new JRadioButton("mail");
		
		netascii.setBackground(Color.lightGray);
		octet.setBackground(Color.lightGray);
		mail.setBackground(Color.lightGray);
		
		netascii.setFocusable(false);
		octet.setFocusable(false);
		mail.setFocusable(false);
		
		modePanel.add(netascii);
		modePanel.add(octet);
		modePanel.add(mail);
		modePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Mode"));
		modePanel.setBackground(Color.lightGray);
		modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.Y_AXIS));
		
		modeGroup = new ButtonGroup();
		modeGroup.add(netascii);
		modeGroup.add(octet);
		modeGroup.add(mail);
		
		JPanel serverInfo = new JPanel();
		
		fileName = new JLabel("Filename:");
		fileNameInput = new JTextField();
		
		serverName = new JLabel("TFTP Server:");
		server = new JTextField();
		
		serverInfo.add(fileName);
		serverInfo.add(fileNameInput);
		serverInfo.add(serverName);
		serverInfo.add(server);
		serverInfo.setBackground(Color.lightGray);
		
		serverInfo.setLayout(new BoxLayout(serverInfo, BoxLayout.Y_AXIS));
		//serverInfo.setAlignmentX(RIGHT_ALIGNMENT);
		//serverInfo.setSize(20, 200);
		
		send = new JButton("Send");
		cancel = new JButton("Cancel");
		ping = new JButton("Ping");
		clear = new JButton("Clear Log");
		
		transferLog = new JTextArea("***Welcome to TFTP Client v1.0b***");
		logScroll = new JScrollPane(transferLog);

		logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		logScroll.setVisible(true);
		logScroll.scrollRectToVisible(transferLog.getBounds());
		
		transferLog.setColumns(50);
		transferLog.setRows(9);
		transferLog.setCaretColor(Color.black);
		transferLog.setFont(font);
		transferLog.setLineWrap(true);
		transferLog.setEditable(false);
		
		Container c1 = getContentPane();

		c1.setBackground(Color.lightGray);
		c1.setLayout(new FlowLayout(FlowLayout.LEFT));
		c1.setSize(500, 500);
		
		c1.add(requestPanel);
		c1.add(modePanel);
		c1.add(serverInfo);
		c1.add(send);
		c1.add(cancel);
		c1.add(ping);
		c1.add(clear);
		c1.add(logScroll);
		
		fileNameInput.setColumns(15);
		fileNameInput.setBackground(Color.darkGray);
		fileNameInput.setForeground(Color.LIGHT_GRAY);
		fileNameInput.setFont(font2);
		fileNameInput.setCaretColor(Color.white);
		
		server.setColumns(15);
		server.setBackground(Color.darkGray);
		server.setForeground(Color.LIGHT_GRAY);
		server.setFont(font2);
		server.setCaretColor(Color.white);
		
		//JFrame properties
		this.setSize(380, 300);
		this.setLocation(500, 300);
		this.setTitle("CS413 TFTP Java Client v1.0 bc");
		this.setResizable(false);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Action action = new Action();
		
		read.setSelected(true);
		netascii.setSelected(true);
		
		send.addActionListener(action);
		cancel.addActionListener(action);
		ping.addActionListener(action);
		clear.addActionListener(action);
		
	}

	@Override
	public void windowActivated(WindowEvent arg0) {	}

	@Override
	public void windowClosed(WindowEvent arg0) {}

	@Override
	public void windowClosing(WindowEvent arg0) {

		dispose();
		System.exit(0);
		
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {}

	@Override
	public void windowDeiconified(WindowEvent arg0) {}

	@Override
	public void windowIconified(WindowEvent arg0) {}

	@Override
	public void windowOpened(WindowEvent arg0) {}

}
