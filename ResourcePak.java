/* This program is still a work in progress.
 * The purpose of this program is to be able to read and potentially
 * unpack the data from a .pak file and view its contents.
 *
 * IDEA: EXTRACT FILES USING THREADS?
 */
import java.io.*;
import java.nio.*;

class ResourcePak {
  public static void main(String[] args) throws IOException {
    File f = new File("C:/Nexon/DragonNest/Resource00.pak");
    RandomAccessFile raf = new RandomAccessFile(f, "r");

    // Checks if the file should be unpacked/read by thsi program
    String header = "EyedentityGames Packing File 0.1";
    byte[] head = new byte[32];
    if( raf.read(head) != -1) {
      String headstr = new String(head, "UTF-8");

      if(new String(head, "UTF-8").equals(header)) {
        System.out.println("File is a resource pak.");
      } else {
        System.out.println("File cannot be read.");
        System.exit(-1);
      }
    }

    byte[] word = new byte[4];

    /* 0x104 = 260 */
    raf.seek(260);
    raf.read(word); // read 4 bytes
    int fileCount = bytesToInt(word);

    raf.read(word);
    int fileStartOffset = bytesToInt(word);

    raf.seek(fileStartOffset);
    byte[] nameBytes = new byte[256];
    raf.read(nameBytes);
    String name = new String(nameBytes, "UTF-8");
    System.out.println("Name: " + name);


    /* note to self: intel architecture is little endian. */
    //long fileCount = ByteBuffer.wrap(word).order(ByteOrder.LITTLE_ENDIAN).getInt();

    /* at the moment still researching what this file offset variable really is */
    //long fileOffset = ByteBuffer.wrap(word).order(ByteOrder.LITTLE_ENDIAN).getInt();

    System.out.println("Total Files: " + fileCount);
    System.out.println("First File Offset: " + fileStartOffset);


    raf.close();
  }


  public static int bytesToInt(byte[] b) {
    return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();

  }
}
