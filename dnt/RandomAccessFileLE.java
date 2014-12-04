import java.io.*;
import java.nio.*;

class RandomAccessFileLE extends RandomAccessFile {
	public RandomAccessFileLE(File file, String mode) 
    throws FileNotFoundException
  {
    super(file, mode);
  }

  public RandomAccessFileLE(String name, String mode)
    throws FileNotFoundException
  {
    super(name, mode);
  }

  public int readIntLE()
    throws IOException
  { 
    int ch1 = this.read();
    int ch2 = this.read();
    int ch3 = this.read();
    int ch4 = this.read();
    if ((ch1 | ch2 | ch3 | ch4) < 0)
      throw new EOFException();
    return ((ch4 << 24) | (ch3 << 16) | (ch2 << 8) | (ch1 << 0));
  }

  public short readShortLE()
    throws IOException
  {
    int ch1 = this.read();
    int ch2 = this.read();
    if ((ch1 | ch2) < 0)
      throw new EOFException();
    return (short)((ch2 << 8) + ch1);
  }

  public float readFloatLE()
    throws IOException
  {
    return Float.intBitsToFloat(readIntLE()); 
  }

  // A 32 bit single precision "double" is not a double.
/*  public double readDoubleLE()
    throws IOException
  {
    long ch1 = this.read();
    long ch2 = this.read();
    long ch3 = this.read();
    long ch4 = this.read();
    if ((ch1 | ch2 | ch3 | ch4) < 0)
      throw new EOFException();
    long val = ((ch4 << 56) | (ch3 << 48) | (ch2 << 40) | (ch1 << 32));
    return Double.longBitsToDouble(val);
  }*/

  public String readString(int len)
    throws IOException
  {
    byte[] b = new byte[len];
    super.readFully(b);
    return new String(b);
  }
}