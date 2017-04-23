import java.io.*;
import java.math.*;
import java.net.*;
import java.nio.*;
import java.util.*;
public class sftpserver 
{
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
    int ACKNo;


    try {
      socket = new DatagramSocket(port);
    }catch (IOException e) {
      e.printStackTrace();
    }
    
    try {
      ACKsocket = new DatagramSocket();
    }catch (IOException e) {
      e.printStackTrace();
    }
    
    /*try {
      address = InetAddress.getByName("localhost");  
    }catch(UnknownHostException e) {
       e.printStackTrace(); 
    }*/
    
    try {
      
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
         ACKNo = seqNo;
         if(Math.random() <= p) {
           System.out.println("Packet loss, sequence number = " + ACKNo);
          }
          
         else {
          ackByteBuffer.putInt(0,ACKNo);
          ackByteBuffer.position(4);
          ackByteBuffer.put(bACKP);
          ackByteBuffer.position(6);
          ackByteBuffer.put(bZeros);
          ACKBuffer = ackByteBuffer.array();
          System.out.println("ACK Buffer: "+Arrays.toString(ACKBuffer));
          ACKpacket = new DatagramPacket(ACKBuffer,0, 8, address, 7736);                  
          output.write(packetBuffer, 4, packet.getLength()-4);
          //System.out.println("Packet Length: "+packet.getLength());
          //System.out.println("Data Length: "+(packet.getLength()-4));
          //System.out.println("Received Packet With sequence number: " + seqNo);
          output.close();
          ACKsocket.send(ACKpacket);
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
