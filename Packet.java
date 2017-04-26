import java.nio.*;

public class Packet
{
  byte[] data = null;
  int offset;
  int dataSize;
  int seqNo;
  Packet(byte[] data, int offset, int size, int seqNo)
  {
    this.data = data;
    this.offset = offset;
    this.dataSize = size;
    this.seqNo = seqNo;
  }

  byte[] rdtsend()
  {
    ByteBuffer packet = ByteBuffer.allocate(dataSize + 4); //4 bytes is for the seqNo
    packet.putInt(0,seqNo);
    packet.position(4);
    packet.put(data, offset, dataSize);
    return packet.array();
  }

}
