/* This program is still a work in progress.
 * The purpose of this program is to be able to read and potentially
 * unpack the data from a .pak file and view its contents.
 *
 * In this context, a word is 4 bytes
 */
import java.io.*;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.util.zip.*;
import java.util.List;
import java.util.Collections;

class Unpacker {
  /**
   * The file object that should be a .pak file.
   */
  private File file;

  /**
   * Constant buffer size for data
   */
  public final static int BUFSIZE = 1024 * 10; // 10 KB

  /**
   * Creates a Unpacker instance with the given
   * File object. The File object should not be null.
   * @param file  A file object
   */
  public Unpacker(File file) {
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
  public void extract(File output, List<String> whiteList)
    throws IOException, DataFormatException
  {
    RandomAccessFileLE ptr = new RandomAccessFileLE(file, "r");
    String header = "EyedentityGames Packing File 0.1";
    if(! ptr.readString(header.length()).equals(header)) {
      ptr.close();
      return;
    }

    /* 0x104 = 260 */
    ptr.seek(260);

    // read file count and start offset
    int count = ptr.readIntLE();
    long currOffset = ptr.readIntLE();
    int countLen = String.valueOf(count).length();

    // creating required data types here
    String outputText = file.getName() + " : %0" + countLen + "d of %d : %s";
    String path;
    int zsize;
    int offset;
    Inflater inflater = new Inflater();
    int size;
    int j = 1;
    int extracted = 0;

    // skips file if needed
    for(int i = 0; i < count; i++, j++) {
      // reading each file header
      ptr.seek(currOffset);
      path = ptr.readString(256);
      ptr.skipBytes(4+4); // dummy data + useless size
      zsize = ptr.readIntLE();
      offset = ptr.readIntLE();
      ptr.skipBytes(44); // unknown+null padding

      // files are located elsewhere... remember where we were
      currOffset = ptr.getFilePointer();
      ptr.seek(offset);

      // like all strings, they end with \0. Also remove all whitespaces.
      path = path.substring(0, path.indexOf('\0')).trim();

      boolean allowed = true;
      for(String wL : whiteList) {
        if(path.contains(wL)) {
          allowed = true;
          break;
        }
        allowed = false;
      }

      if(! allowed) {
        continue;
      }

      // make directory for files
      File zFile = new File(output, path), zDir = zFile.getParentFile();
      if(! zDir.exists() && ! zFile.getParentFile().mkdirs()) {
        System.out.println("Extraction of "+path+" FAILED");
        continue;
      }

      System.out.println(String.format(outputText, j, count, path));

      // reset inflater and fill it with data
      inflater.reset();

      // the compressed data
      byte[] buf = new byte[zsize];
      ptr.readFully(buf, 0, zsize);
      inflater.setInput(buf, 0, zsize);

      FileOutputStream out = new FileOutputStream(zFile);
      while(! inflater.finished()) {
        byte[] inf = new byte[Unpacker.BUFSIZE];
        size = inflater.inflate(inf);
        out.write(inf, 0, size);
      }
      // write uncompressed blocks to output
      
      out.close();
      extracted++;
    }


    System.out.println("Extraction complete!");
    System.out.println("Total Files extracted in "+ file.getName()+":" + extracted);

    ptr.close();
  }


  /** 
   * java Unpacker output_dir pak_file whitelist
   * output_dir : The directory where the contents of pak files will be
   *              extracted to.
   * pak_file : The pak file path, in quotes; uses glob expressions
   * white_list : list of files to ignore
   */
  public static void main(String[] args)
    throws IOException, DataFormatException
  {
    if(args.length < 2) {
      System.out.println("java Unpacker output_dir pak_file [file_offset]");
      System.exit(1);
    }


    File output = new File(args[0]);
    if(! output.exists()) {
      if (! output.mkdirs()) {
        System.out.println("Cannot create output directory!");
        System.exit(1);
      }
    }

    File file = new File(args[1]);
    DirectoryStream<Path> dirStream = Files.newDirectoryStream(
                                              Paths.get(file.getParent()),
                                              file.getName());

    for(Path path : dirStream) {
      Unpacker pak = new Unpacker(path.toFile());
      if(! pak.valid()) {
        System.out.println(args[1] + " is not a valid pak file path.");
      } else {
        List<String> whiteList = Collections.emptyList();
        if(args.length == 3) {
          whiteList = Files.readAllLines(new File(args[2]).toPath(),
                                         Charset.defaultCharset());
        }
        pak.extract(output, whiteList);
      }
    }
  }
}