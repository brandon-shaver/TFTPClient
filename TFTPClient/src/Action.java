/**
 * Brandon Shaver and Cody Cartrette
 * CS 413 Advanced Networking
 * Dr. Cichanowski
 * 
 * TFTP Client for OSX 
 * 
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class Action implements ActionListener {

	static boolean isRead;

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		if (e.getSource() == MainWindow.send){
			MainWindow.transferLog.append("\nsend button pressed.");
			
			if (MainWindow.read.isSelected()){
				
				MainWindow.transferLog.append("\nRead file is selected.");
				isRead = true;

			}else{
				
				MainWindow.transferLog.append("\nWrite file is selected.");
				isRead = false;
			}
			
			Thread t2 = new Thread(new SendThread());
			t2.start();
			
		}else if (e.getSource() == MainWindow.ping){
			
			System.out.println("ping thread started");
			Thread t1 = new Thread(new PingThread());
			t1.start();
	
		}else if(e.getSource() == MainWindow.clear){
			
			System.out.println("Cleared transfered Log.");
			MainWindow.transferLog.setText(null);;
			
		}else{
			
			MainWindow.transferLog.append("\nError: Unknown source of action listener.");
		}
		
		MainWindow.transferLog.setCaretPosition(MainWindow.transferLog.getDocument().getLength());
		MainWindow.transferLog.requestFocus();

	}

}
