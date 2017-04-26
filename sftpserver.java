import java.io.*;
import java.math.*;
import java.net.*;
import java.nio.*;
import java.util.*;
public class sftpserver 
{
  /*
   @param port -port the server listens on
   @param file - output file to write to
   @param outputFile - points to the @param file to write to
   @param p - loss probabibilty
   @param messageBuffer - To store data received from packet sent by client
   @param packetBuffer - To store packets recived from client
   @param ACKBuffer - Store the ACkNo
   @param ACKP - Denotes it is an ack packet
   @param bACKP - Store @param ACKP in a bytesArray
   @param zeros - Header portion of ACK packet
   @param bZeros - Store @param zeros in a bytesArray
   @param ackByteBuffer - To store the ack packet before sending
   @param output - to write to output file
   @param socket - to receive packets
   @param ACKsocket - to send ACKs
   @param packet - to get the packet send by clinet
   @param ACKpacket - to send ACK packet to client
   @param address - address of the client
   */
	  /*public static int calculateCheckSum(byte[] data) {
		  int length = data.length;
		  int i = 0;
		  int checksum = 0;
		  int sum;
		    while (length > 1) {
		      sum = (((data[i] << 8) & 0xFF00) | ((data[i + 1]) & 0xFF));
		      checksum += sum;
		 
		      if ((checksum & 0xFFFF0000) > 0) {
		        checksum = checksum & 0xFFFF;
		        checksum += 1;
		      }

		      i += 2;
		      length -= 2;
		    }

		     if (length > 0) {
		      checksum += (data[i] << 8 & 0xFF00);
		      if ((checksum & 0xFFFF0000) > 0) {
		        checksum = checksum & 0xFFFF;
		        checksum += 1;
		      }
		    }
		    checksum = ~checksum;
		    checksum = checksum & 0xFFFF;
		    return checksum;
		  }*/

  public static void main(String[] args)
  {
    int port = Integer.parseInt(args[0]);
    String file = args[1];
    File outputFile = new File(file);
    double p = Double.parseDouble(args[2]);
    byte[] messageBuffer = new byte[1000];
    byte[] packetBuffer = new byte[1004]; //Size of packet is 1004
    byte[] ACKBuffer = new byte[4];
    String ACKP = "1010101010101010";
    byte[] bACKP = new BigInteger(ACKP, 2).toByteArray();
    String zeros = "0000000000000000";
    byte[] bZeros = new BigInteger(zeros, 2).toByteArray();

    ByteBuffer ackByteBuffer = ByteBuffer.allocate(8);
    FileOutputStream output = null;
    DatagramSocket socket = null;
    DatagramSocket ACKsocket = null;
    DatagramPacket packet = null;
    DatagramPacket ACKpacket = null;
    InetAddress address = null;
    int seqNo;
    //int ACKNo;
    int expectedACK = 0;

    /*
     Socket to receive packets from client
     */
    try {
      socket = new DatagramSocket(port);
    }catch (IOException e) {
      e.printStackTrace();
    }
    
    /*
     Socket to send ACK to client
     */
    try {
      ACKsocket = new DatagramSocket();
    }catch (IOException e) {
      e.printStackTrace();
    }
       
    try {
     /*
      When the server receives a packet it drops it with a 
      the random number it generates it less than loss probability.
      If not it will write the contents to an output file and send an ACK
      to the client
      */ 
     while(true){
       packet = new DatagramPacket(packetBuffer, packetBuffer.length);
       socket.receive(packet);
       address = packet.getAddress();
       try {
         output = new FileOutputStream(outputFile, true);
         packetBuffer = packet.getData();
         ByteBuffer packetByteBuffer = ByteBuffer.allocate(packetBuffer.length);
         packetByteBuffer.put(packetBuffer);
         seqNo = packetByteBuffer.getInt(0);
         packetByteBuffer.position(4);
         packetByteBuffer.get(messageBuffer);
         //int checksum ;
         //checksum = calculateCheckSum(messageBuffer);
         //System.out.println("Check Sum for Packet "+seqNo+" is"+checksum);
         //ACKNo = seqNo;
         if(Math.random() <= p) {
           System.out.println("Packet loss, sequence number = " + seqNo);
          }
          
         else {
        	 if(expectedACK == seqNo) {
        	   //System.out.println("Receved ACK No"+expectedACK+"and SeqNo "+seqNo);
               ackByteBuffer.putInt(0,expectedACK);
               ackByteBuffer.position(4);
               ackByteBuffer.put(bACKP);
               ackByteBuffer.position(6);
               ackByteBuffer.put(bZeros);
               ACKBuffer = ackByteBuffer.array();
               ACKpacket = new DatagramPacket(ACKBuffer,0, 8, address, 7736);                  
               output.write(packetBuffer, 4, packet.getLength()-4);
               output.close();
               ACKsocket.send(ACKpacket);
               expectedACK += 1;
        	 }
        	 else{
	           output.close();
                   //System.out.println("ACK different from SeqNo");
          	   //System.out.println("Receved ACK No "+expectedACK+" and SeqNo "+seqNo);
                 /*ackByteBuffer.putInt(0,expectedACK);
                 ackByteBuffer.position(4);
                 ackByteBuffer.put(bACKP);
                 ackByteBuffer.position(6);
                 ackByteBuffer.put(bZeros);
                 ACKBuffer = ackByteBuffer.array();
                 ACKpacket = new DatagramPacket(ACKBuffer,0, 8, address, 7736);                  
                 output.write(packetBuffer, 4, packet.getLength()-4);
                 output.close();
                 ACKsocket.send(ACKpacket);*/
          	 }
        	 //System.out.println("Receved ACK No"+expectedACK+"and SeqNo "+seqNo);
          }
         }catch(FileNotFoundException e){
            e.printStackTrace();
         }       
     }
   }catch(FileNotFoundException e) {
      e.printStackTrace();
    }catch(IOException e) {
      e.printStackTrace();
    }  
  }
}
