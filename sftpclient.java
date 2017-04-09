import java.io.*;
import java.net.*;
import java.nio.*;
public class sftpclient
{
  public static void main(String[] args) 
  {
    String hostname = args[0];
    int port = Integer.parseInt(args[1]);
    String filename = args[2];
    File message = new File(filename);
    int seqNo = 0;
    int maxSeqNo = 0;
    int ACKNo = 0;
    int oldACKNo =-1;
    int offset = 0;
    int MSS = 1000;
    int windowSize = 5;
    int dataSize;
    int packetHeaderSize = 4;
    int fileSize = (int)message.length();
    int packetsBufferSize = (int)(fileSize + (packetHeaderSize * Math.ceil((float)fileSize/MSS)));
    int beginWindow = 0;
    int endWindow = windowSize * (MSS + packetHeaderSize);
    int initialSeqNo = 0;
    System.out.println(packetsBufferSize);
    FileInputStream input = null;
    byte[] messageBuffer = new byte[(int)message.length()];
    System.out.println(fileSize);
    byte[] packetBuffer = null;
    byte[] ACKBuffer = new byte[4];
    byte[] packetsBuffer;
    ByteBuffer packetsByteBuffer = ByteBuffer.allocate(packetsBufferSize);
    ByteBuffer ACKByteBuffer = ByteBuffer.allocate(4);
    DatagramSocket socket = null;
    DatagramPacket packet = null;
    DatagramSocket ACKsocket = null;
    DatagramPacket ACKpacket = new DatagramPacket(ACKBuffer, ACKBuffer.length);
    InetAddress address = null;
    
    
    try {
      input = new FileInputStream(message);
    }catch(FileNotFoundException e) {
      e.printStackTrace();  
    }
    
    try {
      socket = new DatagramSocket();
    } catch(IOException e){
      e.printStackTrace();
    }

    try {
      ACKsocket = new DatagramSocket(7736);
    } catch(IOException e){
      e.printStackTrace();
    }
    
    try {
      address = InetAddress.getByName(hostname);  
    }catch(UnknownHostException e) {
       e.printStackTrace(); 
    }
    
    try {
      while(input.read(messageBuffer) > 0) {
        while(offset < messageBuffer.length) {
          if (messageBuffer.length - offset > MSS) {
              dataSize = MSS;
              Packet p = new Packet(messageBuffer, offset, dataSize, seqNo);
              System.out.println("Offset: " + offset);
              System.out.println("Packet Size: " + dataSize);     
              packetBuffer = p.rdtsend();
              packetsByteBuffer.put(packetBuffer);
              seqNo += 1;
              offset = offset + dataSize;
                                  
          }
          else {
              System.out.println("Offset " +offset);
              System.out.println("Messg buffer length "+messageBuffer.length);
              dataSize = messageBuffer.length - offset;
              System.out.println("Packet Size "+dataSize);
              Packet p = new Packet(messageBuffer, offset, dataSize, seqNo);
              packetBuffer = p.rdtsend();
              System.out.println("packet buffer length"+packetBuffer.length);
              packetsByteBuffer.put(packetBuffer);
              offset = offset + dataSize; 
              maxSeqNo = seqNo;
             
          } 
        }
        input.close();
      }
      //System.out.println("Finished Transmission");
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
    System.out.println("Maximum Sequence number" + maxSeqNo);
    try {
      while (ACKNo < maxSeqNo) {
    	try {
    	 while(bytesSent < endWindow) {
    	   System.out.println("Begin Window:"+beginWindow);
    	   System.out.println("End Window:"+endWindow);
    	   System.out.println("BytesSent"+bytesSent);
           if(packetsBuffer.length - bytesSent > MSS + packetHeaderSize) {
    		 packet = new DatagramPacket(packetsBuffer, bytesSent, MSS + packetHeaderSize, address, port);
             socket.send(packet);
             System.out.println("Sent packet with sequence number " + seqNo);
             seqNo++;
             bytesSent += MSS + packetHeaderSize;
            }
    	    else {
    		  packet = new DatagramPacket(packetsBuffer, bytesSent, packetsBuffer.length - bytesSent, address, port);
              socket.send(packet);
              System.out.println("Sent packet with sequence number " + seqNo);
              seqNo++; 
              bytesSent += MSS + packetHeaderSize;
            }
         }
    	 ACKsocket.setSoTimeout(100);
         ACKsocket.receive(ACKpacket);
         System.out.println("Received ACK");
         ACKBuffer = ACKpacket.getData();
         ACKByteBuffer.put(ACKBuffer);
         ACKByteBuffer.rewind();
         ACKNo = ACKByteBuffer.getInt(0);
         System.out.println("ACK number is "+ACKNo);
         
                  
         if (ACKNo <= seqNo) {
        	 beginWindow = beginWindow + (ACKNo - oldACKNo) * (MSS + packetHeaderSize);
        	 if(endWindow < packetsBuffer.length) {
        	 endWindow = endWindow + (ACKNo - oldACKNo) * (MSS + packetHeaderSize);
        	 }
        	 oldACKNo = ACKNo;
        	 //initialSeqNo = ACKNo;
        	 //ACKsocket = new DatagramSocket(7736);
         }

    	}catch(SocketTimeoutException e) {
            ACKsocket.close();
    	 	System.out.println("Packet Loss occurred at "+(ACKNo+1));
    	 	seqNo = ACKNo + 1;
    	 	bytesSent = seqNo * (MSS + packetHeaderSize);
    	 	//beginWindow = seqNo*(MSS + packetHeaderSize);
    	 	//endWindow = (initialSeqNo + 1)*windowSize*(MSS + packetHeaderSize);
    		ACKsocket = new DatagramSocket(7736);
    		continue;
    	}
      }
      socket.close();
    }catch(IOException e) {
    	e.printStackTrace();
    }	
  }
}	
