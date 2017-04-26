import java.io.*;
import java.net.*;
import java.nio.*;
public class sftpclient
{
  /*
   @param hostname - name of the host to connect to 
   @param port - port number to connect to the server
   @param filename - name of the file to send
   @param input - used to read contents of @param filename to put into a buffer
   @param message - File that points to filename
   @param seqno - sequence number of the packets being sent
   @param maxSeqNo - total number of packets sent
   @param ACKNo - Acknowledgement number received from the server
   @param oldACKNo - Used to keep tack of the last ACKNo to increase the window size
   @param offset - Used to grab bytes from the desired position
   @param windowSize - Set window size
   @param MSS - Maximum segment size
   @param dataSize - Keeps size of data not equal to MSS for last packet
   @param packetHeaderSize - packet Header size
   @param fileSize - Size of the file being read
   @param packetBufferSize - Buffer to keep all the packets as they are being read from the file, header is also added
   @param beginWindow - Start of window
   @param endWindow - End of window
   @param messageBuffer - used to store data to send
   @param packetBuffer - store the resulting packet to be sent
   @param ACKBuffer - store tha ACK packet received
   @param packetsBuffer - store all the packets
   @param packetsByteBuffer - stores the packets as recived from packetsBuffer, packets are then sent to server
                              from packetsByteBuffer
   @param ACKByteBuffer - receives the ACK packets from the server are store here
   @param socket - to send packets to server
   @param packet - packet to send to server
   @param ACKsocket - to receive ACKs from server
   @param ACKpacket - ACK packet received from server
   @param address - address of the server
   */
  public static void main(String[] args) 
  {
    String hostname = args[0];
    int port = Integer.parseInt(args[1]);
    String filename = args[2];
    File message = new File(filename);
    FileInputStream input = null;
    int seqNo = 0;
    int maxSeqNo = 0;
    int ACKNo = 0;
    int oldACKNo =-1;
    int offset = 0;
    int windowSize = Integer.parseInt(args[3]);
    int MSS = Integer.parseInt(args[4]);
    int dataSize;
    int packetHeaderSize = 4;
    int fileSize = (int)message.length();
    int packetsBufferSize = (int)(fileSize + (packetHeaderSize * Math.ceil((float)fileSize/MSS)));
    int beginWindow = 0;
    int endWindow = windowSize * (MSS + packetHeaderSize);
    byte[] messageBuffer = new byte[(int)message.length()];
    byte[] packetBuffer = null;
    byte[] ACKBuffer = new byte[8];
    byte[] packetsBuffer;
    ByteBuffer packetsByteBuffer = ByteBuffer.allocate(packetsBufferSize);
    ByteBuffer ACKByteBuffer = ByteBuffer.allocate(8);
    DatagramSocket socket = null;
    DatagramPacket packet = null;
    DatagramSocket ACKsocket = null;
    DatagramPacket ACKpacket = new DatagramPacket(ACKBuffer, ACKBuffer.length);
    InetAddress address = null;
    
    /*
     To read the contents of @param message create an input stream 
     */
    try {
      input = new FileInputStream(message);
    }catch(FileNotFoundException e) {
      e.printStackTrace();  
    }
    
    /*
     Create a socket to send packets
     */
    try {
      socket = new DatagramSocket();
    } catch(IOException e){
      e.printStackTrace();
    }
   
    /*
     Create a scocket to receive packets
     */
    try {
      ACKsocket = new DatagramSocket(7736);

    } catch(IOException e){
      e.printStackTrace();
    }
    
    /*
     Get the address of the server
     */
    try {
      address = InetAddress.getByName(hostname);  
    }catch(UnknownHostException e) {
       e.printStackTrace(); 
    }
    
    try {
      /*
       Read the contents of the file to send and used rdtsend() to make a packet
       all the packets to send will be stored in @param packetsByteBuffer.All packets 
       except the last packet will have data size MSS.
       */
      while(input.read(messageBuffer) > 0) {
        while(offset < messageBuffer.length) {
          if (messageBuffer.length - offset > MSS) {
              dataSize = MSS;
              Packet p = new Packet(messageBuffer, offset, dataSize, seqNo);
              packetBuffer = p.rdtsend();
              packetsByteBuffer.put(packetBuffer);
              seqNo += 1;
              offset = offset + dataSize;
                                  
          }
          else {
              dataSize = messageBuffer.length - offset;
              Packet p = new Packet(messageBuffer, offset, dataSize, seqNo);
              packetBuffer = p.rdtsend();
              packetsByteBuffer.put(packetBuffer);
              offset = offset + dataSize; 
              maxSeqNo = seqNo;
             
          } 
        }
        
      }
      //input.close();
      //socket.close();
      //ACKsocket.close();
      }catch(FileNotFoundException e) {
        e.printStackTrace();
      }catch(IOException e) {
        e.printStackTrace();
      }
    
    seqNo = 0;
    packetsBuffer = packetsByteBuffer.array();
    int bytesSent = 0;
    
    try {
      /*
       Packets are sent as long as window size permits.If window permit packets to be sent all the 
       packets will be sent, then no packets can be sent until an ACK arrive which will move the window.
       A timer is set for the packets sent if no ACK is received and timer expires then all the packets starting
       after the last ACK'd packet are sent again.     
       */
      while (ACKNo < maxSeqNo) {
    	try {
    	 while(bytesSent < endWindow) {
           if(packetsBuffer.length - bytesSent > MSS + packetHeaderSize) {
             packet = new DatagramPacket(packetsBuffer, bytesSent, MSS + packetHeaderSize, address, port);
             socket.send(packet);
             seqNo++;
             bytesSent += MSS + packetHeaderSize;
            }
    	    else {
    	      //System.out.println("Last packet");
    		  packet = new DatagramPacket(packetsBuffer, bytesSent, packetsBuffer.length - bytesSent, address, port);
              socket.send(packet);
              seqNo++; 
              bytesSent += MSS + packetHeaderSize;
            }
         }
    	 ACKsocket.setSoTimeout(20);
         ACKsocket.receive(ACKpacket);
         ACKBuffer = ACKpacket.getData();
         ACKByteBuffer.put(ACKBuffer);
         ACKByteBuffer.rewind();
         ACKNo = ACKByteBuffer.getInt(0);
                           
         //Moves the window on the basis of ACKs received
         if (ACKNo <= seqNo) {
        	 beginWindow = beginWindow + (ACKNo - oldACKNo) * (MSS + packetHeaderSize);
        	 if(endWindow < packetsBuffer.length) {
        	 endWindow = endWindow + (ACKNo - oldACKNo) * (MSS + packetHeaderSize);
        	 }
        	 if(endWindow > packetsBuffer.length) {
        		 endWindow = packetsBuffer.length;
        	 }
        	 oldACKNo = ACKNo;
        	 
         }

    	}catch(SocketTimeoutException e) {
            ACKsocket.close();
    	 	seqNo = ACKNo + 1;
    	 	System.out.println("Timeout, sequence number = "+seqNo);
    	 	//@param bytesSent will be reduced to include all packets that have been ACK'd
                //when timeoutoccurs
                bytesSent = seqNo * (MSS + packetHeaderSize);
    		ACKsocket = new DatagramSocket(7736);
    		continue;
    	}
      }
      socket.close();
      input.close();
    }catch(IOException e) {
    	e.printStackTrace();
    }	
  }
}	
