/**
 * Brandon Shaver and Cody Cartrette
 * CS 413 Advanced Networking
 * Dr. Cichanowski
 * 
 * TFTP Client for OSX 
 * 
 */
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.Random;



public class SendThread implements Runnable {
	
	//static int tid;
	static int sourcePort;
	static int destinationPort;
	static int currentBlock;
	
	static String ipAddress;
	static String fileName;
	static String modeString;

	static DatagramSocket datagramSocket;
	static InetAddress iNetDest;
	static InetAddress iNetSource;
	
	static File file;
	static int fileIndex;
	static byte[] fileDataBytes;
	
	static boolean isLastPacket;
	static boolean hasError;
	
	final static int MAX_TIMEOUTS = 5;
	
	@Override
	public void run() {
		
		initTransfer();
		
		if (Action.isRead){
			System.out.println("Read Request is selected");

			createBlankFile();
			
			if (!hasError){
				sendReadRequest();
			}
			
			while (!isLastPacket && !hasError){
				listenForData();
			}
			
			System.out.println("Transfer Complete!");
			System.out.println("Errors occured: "+ hasError);
			//close sockets
			
		}else{
			System.out.println("Write data to server");
			
			//Write to TFTP Server [Upload]
			
			fileDataBytes = checkFile();
			
			if (!hasError){
				sendWriteRequest();
			}
			
			while (!isLastPacket && !hasError){				//conditions
				listenForAck();
			}
			
			System.out.println("Transfer Complete!");
			System.out.println("Errors occured: "+ hasError);
			
		}
		//close sockets
		datagramSocket.disconnect();
		datagramSocket.close();
		System.out.println("Data Socket safetly closed");
	}
	
	public void initTransfer(){
		
		ipAddress = MainWindow.server.getText();
		fileName = MainWindow.fileNameInput.getText();

		if (MainWindow.netascii.isSelected()){
			modeString = "netascii";
		}else if(MainWindow.octet.isSelected()){
			modeString = "octet";
		}else if(MainWindow.mail.isSelected()){
			modeString = "mail";
		}else{
			modeString = "Unknown";
		}
		
		System.out.println("Mode selected: " + modeString);
		
		sourcePort = genRandomTID(); // assign tid to source port 0 - 65535
		destinationPort = 69;
		currentBlock = 1;
		isLastPacket = false;
		hasError = false;
		fileIndex = 0;
		
		try{
			iNetDest = InetAddress.getByName(ipAddress);
			
			//tempVM = InetAddress.getByName("x.x.x.x");
			iNetSource = InetAddress.getLocalHost();
			datagramSocket = new DatagramSocket(sourcePort,iNetSource );
			
		}catch (Exception e){
			e.printStackTrace();
			System.out.println("Error creating datagram socket");
			hasError = true;
		}
		
	}
	
	
	public void listenForAck(){

		byte[] dataBuffer = new byte[4];
		
		try{
			//InetAddress inAddr = InetAddress.getLocalHost();
			
			DatagramPacket receivePacket = new DatagramPacket(dataBuffer, dataBuffer.length, iNetSource, sourcePort);
			
			datagramSocket.setSoTimeout(3000);
			datagramSocket.receive(receivePacket);
			
			byte[] udpData = receivePacket.getData();
			
			if(destinationPort == 69){
				destinationPort = receivePacket.getPort();
				
				datagramSocket.connect(iNetDest, destinationPort);
				System.out.println("New destination port assigned to: "+ destinationPort);
			}
			
			if (destinationPort == receivePacket.getPort()){
				System.out.println("Destination TID confirmed");
				
				if (udpData.length == 4){ //is correct size
					System.out.println("Packet is ack");
					
					byte[] tftpBlockNumber = Arrays.copyOfRange(udpData, 2, 4);
					byte[] tftpMessage = Arrays.copyOfRange(udpData, 4, udpData.length);
					
					if (udpData[0] == 0 && udpData[1] == 4){ //packet contains ack
						System.out.println("Data Packet has correct Opcode");
						
						if (getBlockNumber(tftpBlockNumber) == currentBlock){ 
							//block number is expected
							System.out.println("TFTP Block number matches: "+ currentBlock);
							System.out.println("UDP Data length: "+ receivePacket.getLength());
							System.out.println("UDP Data contents: "+ receivePacket.getData());
							System.out.println("TFTP Block number:" + getBlockNumber(tftpBlockNumber));
							
							if (!isLastPacket){
								byte[] tftpData = readNextData();
								tftpBlockNumber = getBlockNumberByte(++currentBlock);
							
								sendData(tftpBlockNumber, tftpData, receivePacket.getAddress(), receivePacket.getPort());
							}
							
						}else{
							System.out.println("TFTP Block Number doesn't match currentBlock: " + currentBlock);
							System.out.println("TFTP data block number:" + getBlockNumber(tftpBlockNumber));
						}
					
					}else if(udpData[0] == 0 && udpData[1] == 5){//error packet received
						readError(tftpMessage);
						isLastPacket = true; 
						hasError = true;
						
					}else{
						System.out.println("Data packet has incorrect Opcode");
						System.out.println("Invalid Data packet received.");
						sendError(2, receivePacket.getAddress(), receivePacket.getPort());
						hasError = true;
					}
				}else{
					System.out.println("TFTP Packet is Malformed or ack received.");
					System.out.println("Invalid Data packet recieved.");
					sendError(4, receivePacket.getAddress(), receivePacket.getPort());
					hasError = true;
				}
			}else{
				//send error
				System.out.println("Source port of received packet is incorrect");
				sendError(5, receivePacket.getAddress(), receivePacket.getPort());
			}
		}catch (Exception e){
				//timeout waiting for ack
			}
		
	}
	
	
	public void listenForData (){
		
		byte[] dataBuffer = new byte[516];
		
		//System.out.println("Listening for Data Packet...");
		
		try{
			DatagramPacket receivePacket = new DatagramPacket(dataBuffer, dataBuffer.length, iNetSource, sourcePort);
			
			datagramSocket.setSoTimeout(3000);
			datagramSocket.receive(receivePacket);
			
			byte[] udpData = receivePacket.getData();
			
			if(destinationPort == 69){
				destinationPort = receivePacket.getPort();
				
				datagramSocket.connect(iNetDest, destinationPort);
				System.out.println("New destination port assigned to: "+ destinationPort);
			}
			
			if (destinationPort == receivePacket.getPort()){
				System.out.println("Destination TID confirmed");
				
				if (udpData.length > 4){ //opcode is data packet
					System.out.println("Packet contains data");
					
					byte[] tftpBlockNumber = Arrays.copyOfRange(udpData, 2, 4);
					byte[] tftpData = Arrays.copyOfRange(udpData, 4, udpData.length);
					
					if (udpData[0] == 0 && udpData[1] == 3){ //packet contains data
						System.out.println("Data Packet has correct Opcode");
						
						if (getBlockNumber(tftpBlockNumber) == currentBlock){ //block number is expected
							System.out.println("TFTP Block number matches: "+ currentBlock);
							
							System.out.println("UDP Data length: "+ receivePacket.getLength());
							System.out.println("UDP Data contents: "+ receivePacket.getData());
							System.out.println("TFTP Data length: "+ tftpData.length);
							System.out.println("TFTP Block number:" + getBlockNumber(tftpBlockNumber));
							
							if (receivePacket.getLength() < 512){
								isLastPacket = true;
							}else{
								currentBlock++;
							}
							
							appendFile(tftpData, file);
							sendAck(tftpBlockNumber, receivePacket.getAddress(), receivePacket.getPort());
							
							
						}else{
							System.out.println("TFTP Block Number doesn't match currentBlock: " + currentBlock);
							System.out.println("TFTP data block number:" + getBlockNumber(tftpBlockNumber));
						}
					
					}else if(udpData[0] == 0 && udpData[1] == 5){//error packet received
						readError(tftpData);
						isLastPacket = true; 
						hasError = true;
						
					}else{
						System.out.println("Data packet has incorrect Opcode");
						System.out.println("Invalid Data packet received.");
						sendError(2, receivePacket.getAddress(), receivePacket.getPort());
						hasError = true;
					}
				}else{
					System.out.println("TFTP Packet is Malformed or ack received.");
					System.out.println("Invalid Data packet recieved.");
					sendError(4, receivePacket.getAddress(), receivePacket.getPort());
					hasError = true;
				}
			}else{
				//send error
				System.out.println("Source port of received packet is incorrect");
				sendError(5, receivePacket.getAddress(), receivePacket.getPort());
			}
			
		}catch (Exception e){
			//e.printStackTrace();
			System.out.println("Exception: Problem parsing received packet or timeout.");
			
			//resend packet
			if (currentBlock == 1){
				sendReadRequest();
			}else{
				
				byte[] prevBlock = getBlockNumberByte(currentBlock - 1);
				 
				sendAck(prevBlock, iNetDest, destinationPort);
				System.out.println("Did not receive block number: "+ currentBlock);
				System.out.println("Resent ack to : "+ (currentBlock - 1));
			}
			
		}
		

	}
	
	
	public void sendReadRequest(){
		
	
		byte[] readOpCode = {0,1};
		byte[] filename = fileName.getBytes();
		byte[] mode = modeString.getBytes();
		
		ByteArrayOutputStream tftpHead = new ByteArrayOutputStream();
		
		try { //try to get ip address from string
			
			tftpHead.write(readOpCode);
			tftpHead.write(filename);
			tftpHead.write(0);
			tftpHead.write(mode);
			tftpHead.write(0);
			
			byte[] data = tftpHead.toByteArray();
			
			DatagramPacket requestPacket = new DatagramPacket(data, data.length, iNetDest, destinationPort);
	        
			datagramSocket.send(requestPacket);

			System.out.println("Read Request sent from "+ datagramSocket.getLocalAddress() + " on port "+ datagramSocket.getLocalPort());
			System.out.println("Read Request sent to " + iNetDest.toString() + " on port " + destinationPort);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception trying to send READ request!");
			System.out.println("Possible Invalid IP");
		}
		
		
	}

	
	public void sendWriteRequest(){
		
		byte[] writeOpCode = {0,2};
		byte[] filename = fileName.getBytes();
		byte[] mode = modeString.getBytes();
		
		ByteArrayOutputStream tftpHead = new ByteArrayOutputStream();
		
		try { //try to get ip address from string
			
			tftpHead.write(writeOpCode);
			tftpHead.write(filename);
			tftpHead.write(0);
			tftpHead.write(mode);
			tftpHead.write(0);
			
			byte[] data = tftpHead.toByteArray();
			
			DatagramPacket requestPacket = new DatagramPacket(data, data.length, iNetDest, destinationPort);
	        
			datagramSocket.send(requestPacket);
			currentBlock = 0;
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception trying to send WRITE request!");
			System.out.println("Possible Invalid IP");
		}
		
	}
		

	public void sendData(byte[] block, byte[] data, InetAddress dest, int destPort){
		
		byte[] dataOpCode = {0,3};
		
		ByteArrayOutputStream tftpData = new ByteArrayOutputStream();
		
		try{
			
			tftpData.write(dataOpCode);
			tftpData.write(block);
			tftpData.write(data);

			byte[] tftpContents = tftpData.toByteArray();
			
			DatagramPacket dataPacket = new DatagramPacket(tftpContents, tftpContents.length, dest, destPort);
	        
			datagramSocket.send(dataPacket);
			
		}catch (Exception e){
			e.printStackTrace();
			System.out.println("Problem sending data packet");
		}
	}
	
	
	public void sendAck(byte[] block, InetAddress dest, int destPort){

		byte[] ackOpCode = {0,4};
		
		ByteArrayOutputStream tftpAck = new ByteArrayOutputStream();
		
		try{
			
			tftpAck.write(ackOpCode);
			tftpAck.write(block);

			byte[] data = tftpAck.toByteArray();
			
			DatagramPacket ackPacket = new DatagramPacket(data, data.length, dest, destPort);
	        
			datagramSocket.send(ackPacket);
			
			System.out.println("ack sent to ip: " + dest.toString());
			System.out.println("ack sent to port: " + destPort);
			System.out.println("Acknowledgement sent!");
			
		}catch (Exception e){
			e.printStackTrace();
			System.out.println("Exception trying to send ack from block:" + getBlockNumber(block));
		}
	}
	
	
	public byte[] checkFile(){
		
		try{
			file = new File(fileName);
			
			if (!file.exists()){
				System.out.println("File does not exist on local host");
				hasError = true;
				
			}else{
			
				if (modeString.equals("netascii")){
					
					FileReader fr = new FileReader(file);
					@SuppressWarnings("resource")
					BufferedReader br = new BufferedReader(fr);
					
					StringBuilder sb = new StringBuilder();
					String line = br.readLine();
					
					byte[] eol = new byte[2];
					eol[0] = 0x0D; //CR
					eol[1] = 0x0A; //LF
					
					while(line != null){
						sb.append(line);
						sb.append(new String(eol,"US-ASCII"));
						line = br.readLine();
					}
					
					String fileDataString = sb.toString();
					
					byte[] asciiBytes = fileDataString.getBytes("US-ASCII");
					
					return asciiBytes;
					
				}else if (modeString.equals("octet")){
					
					@SuppressWarnings("resource")
					FileInputStream fis = new FileInputStream(file);
					ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
					
					//String line = br.readLine();
					int currentChar = fis.read();
					
					while (currentChar != -1){
						dataStream.write(currentChar);
						currentChar = fis.read();
					}
					
					byte[] octetData = dataStream.toByteArray();
					
					return octetData;
					
				}else if (modeString.equals("mail")){
					
					System.out.println("Mail not currently supported.");
					hasError = true;
					
				}else{
					System.out.println("Mode string is unknown");
				}
				
				
			}
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Exception checking file for WRQ");
			hasError = true;
		}
		
		return null;
		
	}
	
	
	public byte[] readNextData(){
		
		byte[] fileData;
		
		if((fileIndex + 512) < fileDataBytes.length){
			fileData = new byte[512];
		}else{
			int size = fileDataBytes.length - fileIndex;
			fileData = new byte[size];
			isLastPacket = true;
		}
			
		for (int i = 0; i < fileData.length;i++){
			
			fileData[i] = fileDataBytes[fileIndex];
			fileIndex++;
		}
		
		return fileData;
	}
	
	
	public void createBlankFile(){
		
		try {
			
			file = new File(fileName);
			
			if(!file.exists()){
				file.createNewFile();
				System.out.println("File created: "+ fileName);
			}else{
				FileOutputStream fosWriter = new FileOutputStream(file);
				fosWriter.write((new String()).getBytes()); //clear file
				fosWriter.close();
			}
			
		} catch (Exception e){
			e.printStackTrace();
			System.out.println("Exception trying to create blank file from RRQ");
			hasError = true;
		}
		
	}
	
	
	public void appendFile(byte[] data, File filename){
		
		//Append byte array to existing file
		if (modeString.equals("netascii")){
			
			try{
				
				FileWriter fw = new FileWriter(file.getName(), true);
				BufferedWriter bw = new BufferedWriter(fw);
				
				if (isLastPacket){
					boolean possibleZeroEnding = true;
					int byteIndex = data.length - 1;
					
					while (possibleZeroEnding && byteIndex >= 0){
						
						if (data[byteIndex] != 0x00 && data[byteIndex] != 0x0A && data[byteIndex] != 0x0D){
							possibleZeroEnding = false;
							data = Arrays.copyOfRange(data, 0, byteIndex + 1);
						}else{
							byteIndex--;
						}
						
						System.out.println("byte Index: "+ byteIndex);
					}
				}
				
				for(byte b : data){
					
					//print normal byte
					byte[] asciiByte = new byte[1];
					asciiByte[0] = b;
						
					bw.write(new String(asciiByte, "US-ASCII"));
					
				}

				bw.close();
				
			}catch (Exception e){
				e.printStackTrace();
				System.out.println("Exception trying to append file: "+ filename.getName());
			}
			
		}else if (modeString.equals("octet")){
			
			try{
				FileOutputStream fos = new FileOutputStream(file.getName(), true);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				
				if (isLastPacket){
					boolean possibleZeroEnding = true;
					int byteIndex = data.length - 1;
					
					
					while (possibleZeroEnding && byteIndex >= 0){
						if (data[byteIndex] != 0x00){
							possibleZeroEnding = false;
							data = Arrays.copyOfRange(data, 0, byteIndex + 1);
						}else{
							byteIndex--;
						}
						//System.out.println("byte Index: "+ byteIndex);
					}
					
				}
				
				for (byte b : data){
					bos.write(b); //write as raw byte to bos
				}
				
				bos.close();
				
			}catch (Exception e){
				e.printStackTrace();
				System.out.println("Exception writing raw octet to file");
				hasError = true;
			}
			
		}else{
			System.out.println("Transfer Mode not supported");
			hasError = true;
		}
	}
	
	
	public int getBlockNumber(byte[] block){
		
		ByteBuffer bb = ByteBuffer.wrap(block);
		ShortBuffer sb = bb.asShortBuffer();
		
		//System.out.println("block Number bytes: " + block);
		//System.out.println("block length: "+ block.length);
		//System.out.println("Number of shorts in buffer: "+ sb.remaining());
		
		short s1 = sb.get(0);
		
		int value = s1 >= 0 ? s1 : 0x10000 + s1; //if negative add 65336 into big Endian notation
		
		return value;
	}
	
	
	public byte[] getBlockNumberByte(int blockNumber){
		
		short shortBlock = (short)blockNumber;
		
		byte[] blockBytes = new byte[2];
		
		blockBytes[0] = (byte)((shortBlock >> 8) & 0xff); //big endian
		blockBytes[1] = (byte)(shortBlock & 0xff);

		return blockBytes;
	}
	
	
	public void sendError(int error, InetAddress dest, int destPort){

	
		byte[] errorOpCode = {0,5};
		String errorString;

		if (error == 1){
			errorString = "File not found";
		}else if(error == 2){
			errorString = "Access violation";
		}else if(error == 3){
			errorString = "Disk full or allocation exceeded";
		}else if(error == 4){
			errorString = "Illegal TFTP Operation";
		}else if(error == 5){
			errorString = "Unknown transfer ID";
		}else if(error == 6){
			errorString = "File already exists";
		}else if (error == 7){
			errorString = "No Such user";
		}else{
			errorString = "Error creating error message";
		}

		byte[] errorCode = {0,(byte)error};
		byte[] errorMessage = errorString.getBytes();
		
		ByteArrayOutputStream tftpError = new ByteArrayOutputStream();
		
		try{
			
			tftpError.write(errorOpCode);
			tftpError.write(errorCode);
			tftpError.write(errorMessage);
			tftpError.write(0);
			
			byte[] data = tftpError.toByteArray();
			
			DatagramPacket errorPacket = new DatagramPacket(data, data.length, dest, destPort);
	        
			datagramSocket.send(errorPacket);
			
			System.out.println("Error sent to ip: " + dest.toString());
			System.out.println("Error sent to port: " + destPort);
			System.out.println("Error successfully sent!");
			
		}catch (Exception e){
			e.printStackTrace();
			System.out.println("Exception trying to send Error");
		}
		
		
	}
	
	
	public void readError(byte[] error){
		
		int errorCode = getBlockNumber(Arrays.copyOfRange(error, 2, 4));
		
		byte[] errorMessage = Arrays.copyOfRange(error, 4, error.length-1);
		
		String errorString = new String(errorMessage,0,errorMessage.length);
		
		System.out.println("Error Code: " + errorCode);
		System.out.println("Error Message: " + errorString);
	}
	
	
	public int genRandomTID(){
		
		Random r = new Random();
		
		int random = r.nextInt((65535 - 0) + 1) + 0;
		
		return random;
	}
}
