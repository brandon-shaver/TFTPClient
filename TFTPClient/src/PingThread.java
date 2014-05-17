/**
 * Brandon Shaver and Cody Cartrette
 * CS 413 Advanced Networking
 * Dr. Cichanowski
 * 
 * TFTP Client for OSX 
 * 
 */
import java.net.InetAddress;


public class PingThread implements Runnable {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	String ipAddress;
		
		ipAddress = (MainWindow.server.getText());
		
		if (ipAddress.isEmpty()){
			
			ipAddress = "127.0.0.1";
		}
		
		
		MainWindow.transferLog.append("\nSending ping to " + ipAddress + "...");
		
		try {
			
			InetAddress ip = InetAddress.getByName(ipAddress);
			
			System.out.println(ip.toString());
			
			MainWindow.transferLog.append(ip.isReachable(3000) ? "\nHost is reachable!" : "\nHost is NOT reachable.");
			
		} catch (Exception ex){
			ex.getMessage();
		}
		
		MainWindow.transferLog.setCaretPosition(MainWindow.transferLog.getDocument().getLength());
		

	}

}
