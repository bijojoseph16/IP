import java.io.*;
import java.net.*;

public class sftpserver 
{
  public static void main(String[] args)
  {
    DatagramSocket socket = null;
    DatagramPacket packet = null;
    File f = null;
    FileWriter fw = null;
    BufferedWriter bw = null;
    byte[] buffer = new byte[1024];
    String s = null;

    try {
      socket = new DatagramSocket(7735);
    }catch (IOException e) {
      e.printStackTrace();
    }
    
    try {
      f = new File("HelloOut");
      fw = new FileWriter(f);
      bw = new BufferedWriter(fw);
      int i = 0;
      while(true){
      packet = new DatagramPacket(buffer, buffer.length);
      socket.receive(packet);
      s = new String(packet.getData(), 0, packet.getLength());
      bw.write(s);
      bw.flush();
      i += 1;
        if(i >=2) break; 
      }
     bw.close();
     socket.close();
    }catch(FileNotFoundException e) {
      e.printStackTrace();
    }catch(IOException e) {
      e.printStackTrace();
    }  
  }
}
