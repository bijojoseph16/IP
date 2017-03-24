import java.io.*;
import java.net.*;

public class sftpclient
{
  public static void main(String[] args) 
  {
    String hostname = args[0];
    int port = Integer.parseInt(args[1]);
    String filename = args[2];
    File message = new File(filename);
    FileInputStream input = null;
    BufferedInputStream bufferedinput = null;
    byte[] buffer = new byte[(int)message.length()]; 
    DatagramSocket socket = null;
    DatagramPacket packet = null;
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
      address = InetAddress.getByName(hostname);  
    }catch(UnknownHostException e) {
       e.printStackTrace(); 
    }
    
    bufferedinput = new BufferedInputStream(input);
    
    try {
      bufferedinput.read(buffer, 0, buffer.length);
      int i = 0;
      int size; 
      while (i < buffer.length) {
        if (buffer.length - i > 536) {
          size = 536; //Taking into account an MTU of 576, UDP max packet size = 576 -20 -20 = 536
          packet = new DatagramPacket(buffer, i, size, address, port);
          try {
            Thread.sleep(100); 
          }catch (InterruptedException e) {
             e.printStackTrace();  
          }
          socket.send(packet);
          i = i + 536;
        }
        else {
          size = buffer.length - i;
          packet = new DatagramPacket(buffer, i, size, address, port);
          try {
            Thread.sleep(100); 
          }catch (InterruptedException e) {
             e.printStackTrace();  
          }
          socket.send(packet);
          i = i + 536; //Just for conveneince
        }
      }
      System.out.println("MessageSent");
      input.close();
      socket.close();
    }catch (IOException e) {
      e.printStackTrace();
    }
  }
}
