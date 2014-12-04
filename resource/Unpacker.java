/**
 * Data mining program.
 * The purpose of this program is to be able to read and
 * unpack the data from various .pak file.
 *
 * @author Benjamin Lei
 */
import java.io.*;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.util.zip.*;
import java.util.List;

class Unpacker {
  /**
   * The file object that should be a .pak file.
   */
  private File file;

  /**
   * Constant buffer size for data inflation
   */
  public final static int BUFSIZE = 1024 * 10; // 10 KB

  /**
   * List of paths that should be extracted
   */
  private List<String> whiteList;

  /**
   * Creates a Unpacker instance with the given
   * File object. The File object should not be null.
   * @param file  A file object
   */
  public Unpacker(File file) {
    this.file = file;
  }

  /**
   * Sets a whitelist of paths/files that we specifically want.
   * @param whiteList List of paths/files to only allow to extract.
   */
  public void setWhiteList(List<String> whiteList) {
    this.whiteList = whiteList;
  }

  /**
   * Checks if a given path is part of the whiteList paths,
   * and if it isn't then return false.
   * @param path the path from the resource pak considered to be extracted.
   * @return true if there is no whitelist or if path is part of a whitelist
   *              path, false otherwise.
   */
  public boolean canExtract(String path) {
    if(whiteList == null) {
      return true;
    }

    for(String wL : whiteList) {
      if(path.contains(wL)) {
        return true;
      }
    }

    return false;
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
   * @throws DataFormatException
   *         When something goes wrong when trying to inflate files in the .pak.
   */
  public void extract(File output)
    throws IOException, DataFormatException
  {
    RandomAccessFileLE ptr = new RandomAccessFileLE(file, "r");

    // check that this file is likely a .pak file
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

    for(int i = 0; i < count; i++, j++) {
      // set/reset current file pointer
      ptr.seek(currOffset);

      // like all strings, they end with \0. Also remove all whitespaces.
      path = ptr.readString(256);
      path = path.substring(0, path.indexOf('\0')).trim();
      ptr.skipBytes(4+4); // dummy data + useless size
      zsize = ptr.readIntLE();
      offset = ptr.readIntLE();
      ptr.skipBytes(44); // unknown+null padding

      // files are located elsewhere... remember where we were
      currOffset = ptr.getFilePointer();
      ptr.seek(offset);

      // whitelist file checking
      if(! canExtract(path)) {
        continue;
      }

      // output what is about to be extracted
      System.out.println(String.format(outputText, j, count, path));

      // make directory for files
      File zFile = new File(output, path);
      File zDir = zFile.getParentFile();
      if(! zDir.exists() && ! zDir.mkdirs()) {
        System.out.println("Extraction of "+path+" FAILED");
        continue;
      }

      // reset inflater
      inflater.reset();

      // get and store the compressed data and put it into the inflater
      byte[] buf = new byte[zsize];
      ptr.readFully(buf, 0, zsize);
      inflater.setInput(buf, 0, zsize);

      // uncompressed data
      FileOutputStream out = new FileOutputStream(zFile);
      while(! inflater.finished()) {
        byte[] inf = new byte[Unpacker.BUFSIZE];
        size = inflater.inflate(inf);
        out.write(inf, 0, size);
      }

      out.close();
      extracted++;
    }

    System.out.println("Extraction complete!");
    System.out.println("Total Files extracted in "+ file.getName()+":" + extracted);

    ptr.close();
  }


  /** 
   * java Unpacker pak_file output_dir whitelist
   * pak_file : The pak file path, in quotes; uses glob expressions
   * output_dir : The directory where the contents of pak files will be
   *              extracted to.
   * white_list : text file containing list of paths/files to allow to extract
   *
   * @param args List of arguments to the program.
   * @throws IOException
   *         @see extract
   * @throws DataFormatException
   *         @see extract
   */
  public static void main(String[] args)
    throws IOException, DataFormatException
  {
    if(args.length < 2) {
      System.out.println("java Unpacker pak_file output_dir [whitelist]");
      System.exit(1);
    }

    // glob file matching to unpack various files.
    File file = new File(args[0]);
    DirectoryStream<Path> dirStream = Files.newDirectoryStream(
                                              Paths.get(file.getParent()),
                                              file.getName());

    File output = new File(args[1]);
    for(Path path : dirStream) {
      Unpacker pak = new Unpacker(path.toFile());
      if(! pak.valid()) {
        System.out.println(args[1] + " is not a valid pak file path.");
      } else {
        if(args.length == 3) { // set whitelist
          pak.setWhiteList(Files.readAllLines(new File(args[2]).toPath(),
                                         Charset.defaultCharset()));
        }

        pak.extract(output);
      }
    }
  }
}