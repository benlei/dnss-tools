/* This program is still a work in progress.
 * The purpose of this program is to be able to read and potentially
 * unpack the data from a .pak file and view its contents.
 *
 * In this context, a word is 4 bytes
 */
import java.io.*;
import java.nio.*;

class ResourcePak {
  /**
   * The file object that should be a .pak file.
   */
  private File file;

  /**
   * Creates a ResourcePak instance with the given
   * File object. The File object should not be null.
   * @param file  A file object
   */
  public ResourcePak(File file) {
    this.file = file;
  }

  /**
   * Checks whether the File object in this instance is
   * actually a .pak file solely based on the extension
   * of the file.
   *
   * @return true if the file ends with .pak
   *         false otherwise
   */
  public boolean valid() {
    return file == null || file.getName().endsWith(".pak");
  }

  /**
   * Attempts to go through everything in the .pak file and
   * extract it into the given output directory.
   *
   * @param output The directory file that the files in the .pak
   *               archive will be extracted to.
   * @throws IOException
   *         When something goes wrong with accessing the .pak archive.
   */
  public void extract(File output) throws IOException {
    RandomAccessFile raf = new RandomAccessFile(file, "r");

    // Checks if the file should be unpacked/read by thsi program
    String header = "EyedentityGames Packing File 0.1";
    byte[] head = new byte[32];
    if( raf.read(head) != -1) {
      String headstr = new String(head, "UTF-8");

      if(new String(head, "UTF-8").equals(header)) {
        System.out.println("File is a resource pak.");
      } else {
        System.out.println("File cannot be read.");
        return;
      }
    }

    byte[] word = new byte[4];

    /* 0x104 = 260 */
    raf.seek(260);
    raf.read(word); // read 4 bytes
    int fileCount = wordToInt(word);

    raf.read(word);
    int fileStartOffset = wordToInt(word);

    raf.seek(fileStartOffset);
    byte[] nameBytes = new byte[256];
    raf.read(nameBytes);
    String name = new String(nameBytes, "UTF-8");
    System.out.println("Name: " + name);

    System.out.println("Total Files: " + fileCount);
    System.out.println("First File Offset: " + fileStartOffset);


    raf.close();
  }


  /**
   * Converts an array of 4 bytes into an INT.
   * @param b the 4 byte array
   * @return the int version of the 4 bytes
   */
  public int wordToInt(byte[] b) {
    ByteBuffer buf = ByteBuffer.wrap(b);
    if(ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN)) {
      // not sure if needed... I use LE system
      buf = buf.order(ByteOrder.BIG_ENDIAN);
    } else { // is LE
      buf = buf.order(ByteOrder.LITTLE_ENDIAN);
    }

    return buf.getInt();
  }


  /** 
   * java ResourcePak output_dir pak_1 pak_2 ...
   * output_dir : The directory where the contents of pak files will be
   *              extracted to.
   * pak_n : The pak file path.
   */
  public static void main(String[] files) throws IOException {
    if(files.length < 2) {
      System.out.println("java ResourcePak output_dir pak_1 pak_2 ...");
      System.exit(1);
    }

    File output = new File(files[0]);
    if(! output.exists()) {
      if (! output.mkdirs()) {
        System.out.println("Cannot create output directory!");
        System.exit(1);
      }
    }

    for(int i = 1; i < files.length; i++) {
      File file = new File(files[i]);
      ResourcePak rp = new ResourcePak(file);
      if(! rp.valid()) {
        System.out.println(files[i] + " is not a valid pak file path.");
      } else {
        rp.extract(output);
      }
    }
  }
}