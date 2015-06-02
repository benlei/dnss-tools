package dnss.tools.pak;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class PakContent implements Runnable {
    private final PakParser parser;
    private final String pakPath;
    private final File destination;
    private final int fileSize;
    private final int compressedSize;
    private final int streamOffset;

    private final static Object directoryLock = new Object();

    // new PakContent(resolve(path), fileSize, compressedSize, streamOffset)
    public PakContent(PakParser parser, String pakPath, File destination, int fileSize, int compressedSize, int streamOffset) {
        this.parser = parser;
        this.pakPath = pakPath;
        this.destination = destination;
        this.fileSize = fileSize;
        this.compressedSize = compressedSize;
        this.streamOffset = streamOffset;
    }

    public PakParser getParser() {
        return parser;
    }

    public String getPakPath() {
        return pakPath;
    }

    public File getDestination() {
        return destination;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getCompressedSize() {
        return compressedSize;
    }

    public int getStreamOffset() {
        return streamOffset;
    }


    public void extract() throws IOException, DataFormatException {
        // Creating directory must be synchronized on all threads
        synchronized (directoryLock) {
            try {
                File dir = destination.getParentFile();
                if (! dir.exists()) {
                    dir.mkdirs();
                }
            } catch (SecurityException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
                parser.incrementSkippedFiles();
                return;
            }
        }

        // read compressed contents from pak
        RandomAccessFile inStream = new RandomAccessFile(parser.getFile(), "r");
        byte[] pakContents = new byte[getCompressedSize()];
        inStream.seek(getStreamOffset());
        inStream.readFully(pakContents);
        inStream.close();

        // read uncompressed contents
        int read;
        byte[] zData = new byte[8192];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Inflater inflater = new Inflater();
        inflater.setInput(pakContents);
        while ((read=inflater.inflate(zData)) != 0) {
            byteArrayOutputStream.write(zData, 0, read);
        }
        inflater.end();

        // Ensure that no two threads writes to the same location at same time
        synchronized (destination) {
            // if file exists already, then just rename it
            if (destination.exists() && ! Pak.isOverwrite()) {
                int i = 1;
                int extPos = pakPath.lastIndexOf('.'); // include the . as well for extension-less file case
                File outputFile;
                if (extPos == -1) {
                    extPos = pakPath.length();
                }

                String fileWithoutExt = pakPath.substring(0, extPos);
                String fileExt = pakPath.substring(extPos);
                do {
                    // file has format of ${destinationWithoutExtension}+${i}${ext}
                    outputFile = new File(Pak.getOutput(), fileWithoutExt + "+" + i + fileExt);
                    i++;
                } while (outputFile.exists());

                Files.move(destination.toPath(), outputFile.toPath());
                System.out.println("Moved " + destination.getAbsolutePath() + " to " + outputFile.getAbsolutePath());
            }

            FileOutputStream outStream = new FileOutputStream(destination);
            byteArrayOutputStream.writeTo(outStream);
            outStream.close();
        }

        if (Pak.isVerbose()) {
            System.out.println("[x] " + destination.getAbsolutePath() + " from " + parser.getFile().getAbsolutePath());
        }
        parser.incrementExtractedFiles();
    }

    @Override
    public void run() {
        try {
            extract();
        } catch (Exception e) {
            System.err.println("Could not extract to " + destination.getAbsolutePath() + ": " + e.getMessage());
            e.printStackTrace();
            parser.incrementSkippedFiles();
        }
    }
}
