import java.io.*;
import java.net.*;
import java.nio.*;
public class sftpserver 
{
  public static void main(String[] args)
  {
    int port = Integer.parseInt(args[0]);
    String file = args[1];
    File outputFile = new File(file);
    byte[] messageBuffer = new byte[1000];
    byte[] packetBuffer = new byte[1004]; //Size of packet is 1004
    byte[] ACKBuffer = new byte[4];
    ByteBuffer ackByteBuffer = ByteBuffer.allocate(4);
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
    
    try {
      address = InetAddress.getByName("localhost");  
    }catch(UnknownHostException e) {
       e.printStackTrace(); 
    }
    
    try {
      
     while(true){
       packet = new DatagramPacket(packetBuffer, packetBuffer.length);
       socket.receive(packet);
       if(Math.random() > 0.05) {
         try {
           output = new FileOutputStream(outputFile, true);
           packetBuffer = packet.getData();
           ByteBuffer packetByteBuffer = ByteBuffer.allocate(packetBuffer.length);
           packetByteBuffer.put(packetBuffer);
           seqNo = packetByteBuffer.getInt(0);
           packetByteBuffer.position(4);
           packetByteBuffer.get(messageBuffer);
           ACKNo = seqNo;
           ackByteBuffer.putInt(0,ACKNo);
           ACKBuffer = ackByteBuffer.array();
           ACKpacket = new DatagramPacket(ACKBuffer,0, 4, address, 7736);                  
           output.write(packetBuffer, 4, packet.getLength()-4);
           System.out.println("Packet Length: "+packet.getLength());
           System.out.println("Data Length: "+(packet.getLength()-4));
           System.out.println("Received Packet With sequence number: " + seqNo);
           output.close();
           ACKsocket.send(ACKpacket);
         }catch(FileNotFoundException e){
            e.printStackTrace();
         }       
       }
       else {
    	   System.out.println("Packet Loss");
       }
     }
   }catch(FileNotFoundException e) {
      e.printStackTrace();
    }catch(IOException e) {
      e.printStackTrace();
    }  
  }
}
