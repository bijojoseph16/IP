import java.io.*;
import java.net.*;

public class sftpclient
{
  public static void main(String[] args) 
  {
    DatagramSocket socket = null;
    File message = null;
    FileReader fr = null;
    BufferedReader br = null;
    byte[] buffer = null;
    DatagramPacket packet = null;
    InetAddress address = null;
    String line = null;

    try {
      socket = new DatagramSocket();
    }catch (IOException e) {
      e.printStackTrace();   
    }
    
    message = new File("HelloIn");
    buffer = new byte[1024];

    try {
        address = InetAddress.getByName("localhost");
    }catch(UnknownHostException e) {
      e.printStackTrace();
    }
    
    try {
      fr = new FileReader(message);
    }catch(FileNotFoundException e) {
      e.printStackTrace();
    }

    br = new BufferedReader(fr);

    try {

      line = br.readLine();
      
      while(line != null) {
       buffer = line.getBytes();
       packet = new DatagramPacket(buffer, buffer.length, address, 7735);
       socket.send(packet);
       line = br.readLine();
      }
   
      br.close();
      socket.close();

    }catch (FileNotFoundException e) {
      e.printStackTrace();
    }catch(IOException e) {
      e.printStackTrace();
    }
  }
}
