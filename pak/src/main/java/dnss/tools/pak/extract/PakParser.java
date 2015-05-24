package dnss.tools.pak.extract;

import dnss.tools.pak.Pak;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class PakParser implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(PakContent.class);
    public static final String HEADER = "EyedentityGames Packing File 0.1";
    public static final long START_POS = 260;
    private static Map<String, File> map = new ConcurrentHashMap<String, File>();

    private File file;
    private boolean done;

    private int totalFiles;
    private AtomicInteger ignoredFiles = new AtomicInteger(0);
    private AtomicInteger skippedFiles = new AtomicInteger(0);
    private AtomicInteger extractedFiles = new AtomicInteger(0);

    public PakParser(File file) {
        this.file = file;
    }

    public void parse() throws IOException {
        RandomAccessFile stream = new RandomAccessFile(file, "r");
        FileChannel channel = stream.getChannel();

        // List of buffers/bytes needed
        byte[] headerBytes = new byte[HEADER.length()];
        byte[] pathBytes = new byte[256];
        ByteBuffer header = ByteBuffer.wrap(headerBytes);
        ByteBuffer words = ByteBuffer.allocate(8);
        ByteBuffer buf = ByteBuffer.allocateDirect((256 + 4 + 4 + 4 + 4 + 44) * 64); // 19.75 KB

        // configure the buffers
        words.order(LITTLE_ENDIAN);
        buf.order(LITTLE_ENDIAN);

        // Check if the header is okay
        try {
            channel.read(header);
            if (!Arrays.equals(headerBytes, HEADER.getBytes())) {
                log.error("Invalid dnt file header, aborting parsing " + file.getAbsolutePath());
                return;
            }
        } catch (IOException e) {
            log.error("Could not read file, aborting parsing " + file.getAbsolutePath());
            return;
        }

        // Sets where we start
        channel.position(START_POS);
        channel.read(words);

        // gets # of files and start offset
        words.flip(); // read it
        totalFiles = words.getInt();
        channel.position(words.getInt());

        Queue<Runnable> queue = Pak.getQueue();
        int parsed = 0;
        while (parsed < totalFiles) {
            channel.read(buf);
            buf.flip(); // make it readable up to its limit
            while (buf.hasRemaining()) {
                String path;
                int fileSize;
                int compressedSize;
                int streamOffset;

                buf.get(pathBytes);
                buf.position(buf.position() + 4); // skip 4 bytes
                fileSize = buf.getInt();
                compressedSize = buf.getInt();
                streamOffset = buf.getInt();
                buf.position(buf.position() + 44); // 44 padding bytes

                // fix the path
                path = new String(pathBytes);
                path = path.substring(0, path.indexOf('\0')).trim();

                if (isExtractable(path)) {
                    PakContent content = new PakContent(this, path, resolve(path), fileSize, compressedSize, streamOffset);
                    queue.add(content);
                } else {
                    incrementIgnoredFiles();
                }
                parsed++;
            }
            buf.clear(); // need to clear buffer for reading in more data
        }

        channel.close();
        stream.close();
    }

    public boolean isDone() {
        return done;
    }

    public String getPakName() {
        return file.getName();
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public int getIgnoredFiles() {
        return ignoredFiles.get();
    }

    public void incrementIgnoredFiles() {
        ignoredFiles.incrementAndGet();
    }

    public int getSkippeddFiles() {
        return skippedFiles.get();
    }

    public void incrementSkippedFiles() {
        skippedFiles.incrementAndGet();
    }

    public int getExtractedFiles() {
        return extractedFiles.get();
    }

    public void incrementExtractedFiles() {
        extractedFiles.incrementAndGet();
    }

    public File getFile() {
        return file;
    }

    private File resolve(String destination) {
        File dir = new File(Pak.getDestination(), destination);
        if (! map.containsKey(dir.getAbsolutePath())) {
            map.put(dir.getAbsolutePath(), dir);
        }

        return map.get(dir.getAbsolutePath());
    }

    private boolean isExtractable(String path) {
        boolean allowed = false, ignored = false;

        ArrayList<Pattern> allowPatterns = Pak.getWhiteList();
        if (allowPatterns.size() != 0) {
            for (Pattern pattern : allowPatterns) {
                allowed |= pattern.matcher(path).find();
            }
        } else {
            allowed = true;
        }

        ArrayList<Pattern> ignorePatterns = Pak.getBlackList();
        if (ignorePatterns.size() != 0) {
            for (Pattern pattern : ignorePatterns) {
                ignored |= pattern.matcher(path).find();
            }
        } else {
            ignored = false;
        }

        return allowed && ! ignored;
    }

    public void run() {
        try {
            parse();
        } catch (IOException e) {
            log.error("Could not parse " + file.getAbsolutePath(), e);
        } finally {
            done = true;
        }
    }
}