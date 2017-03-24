import java.io.*;
import java.net.*;

public class sftpserver 
{
  public static void main(String[] args)
  {
    int port = Integer.parseInt(args[0]);
    String file = args[1];
    File outputFile = new File(file);
    byte[] buffer = new byte[536]; //As we fix the size of the UDP packet sent
    FileOutputStream output = null;
    DatagramSocket socket = null;
    DatagramPacket packet = null;


    try {
      socket = new DatagramSocket(port);
    }catch (IOException e) {
      e.printStackTrace();
    }
    
    try {
      
     while(true){
      packet = new DatagramPacket(buffer, buffer.length);
      socket.receive(packet);
        try {
          output = new FileOutputStream(outputFile, true);
          buffer = packet.getData();
          output.write(buffer, 0, packet.getLength());
          output.close();
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
