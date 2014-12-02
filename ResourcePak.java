/* This program is still a work in progress.
 * The purpose of this program is to be able to read and potentially
 * unpack the data from a .pak file and view its contents.
 *
 * In this context, a word is 4 bytes
 */
import java.io.*;
import java.util.zip.*;

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
  public void extract(File output, int fileOffsetStart)
    throws IOException, DataFormatException
  {
    RandomAccessFileLE ptr = new RandomAccessFileLE(file, "r");
    if(! validHeader(ptr)) {
      ptr.close();
      return;
    }

    /* 0x104 = 260 */
    ptr.seek(260);

    // read file count and start offset
    int count = ptr.readIntLE();
    long currOffset = ptr.readIntLE();
    int countLen = String.valueOf(count).length();

    System.out.println("Total Files: " + count);

    // creating required data types here
    String outputText = "%0" + countLen + "d of %d: %s";
    String path;
    int size;
    int zsize;
    int offset;
    Inflater inflater = new Inflater();

    currOffset += fileOffsetStart * (256+4+4+4+4+44);
    for(int i = fileOffsetStart, j = i + 1; i < count; i++, j++) {
      // reading each file header
      ptr.seek(currOffset);
      path = ptr.readString(256);
      ptr.skipBytes(4); // dummy data
      size = ptr.readIntLE();
      zsize = ptr.readIntLE();
      offset = ptr.readIntLE();
      ptr.skipBytes(44); // unknown+null padding

      // files are located elsewhere... remember where we were
      currOffset = ptr.getFilePointer();
      ptr.seek(offset);

      // like all strings, they end with \0. Also remove all whitespaces.
      path = path.substring(0, path.indexOf('\0')).trim();
      System.out.print(String.format(outputText, j, count,
                                    path.replace('\\','/')));
      byte[] data = new byte[size]; // actual data
      byte[] zdata = new byte[zsize]; // zipped data
      ptr.readFully(zdata);
      inflater.setInput(zdata);
      inflater.inflate(data);

      File zFile = new File(output, path), zDir = zFile.getParentFile();
      //System.out.println("\nFile path: " + zFile.getPath());
      if(! zDir.exists() && ! zFile.getParentFile().mkdirs()) {
        System.out.print(" FAILED");
        System.out.print("\n           ");
        System.out.println("Subdirectories could not be made.");
        continue;
      }

      FileOutputStream out = new FileOutputStream(zFile);
      out.write(data);
      out.close();

      System.out.println();
    }

    System.out.println("Extraction complete!");
    System.out.println("\n\nTotal Files extracted: " + (count - fileOffsetStart));

    ptr.close();
  }

  private boolean validHeader(RandomAccessFileLE ptr)
    throws IOException
  {
    String header = "EyedentityGames Packing File 0.1";
    return ptr.readString(header.length()).equals(header);
  }


  /** 
   * java ResourcePak output_dir pak_file [file_offset]
   * output_dir : The directory where the contents of pak files will be
   *              extracted to.
   * pak_file : The pak file path.
   * file_offset : [optional] which file to start extracting at.
   */
  public static void main(String[] args)
    throws IOException, DataFormatException
  {
    if(args.length < 2) {
      System.out.println("java ResourcePak output_dir pak_file [file_offset]");
      System.exit(1);
    }

    int fileStart = 1;
    if(args.length == 3 ) {
      fileStart = Integer.parseInt(args[2]);
      if(fileStart < 1) {
        System.out.println("file_offset parameter must be greater than 0.");
        System.exit(1);
      }
    }

    fileStart--;

    File output = new File(args[0]);
    if(! output.exists()) {
      if (! output.mkdirs()) {
        System.out.println("Cannot create output directory!");
        System.exit(1);
      }
    }


    File file = new File(args[1]);
    ResourcePak rp = new ResourcePak(file);
    if(! rp.valid()) {
      System.out.println(args[1] + " is not a valid pak file path.");
    } else {
      rp.extract(output, fileStart);
    }
  }
}