package ia.vorobev.grep;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Grep function
 * usage: java -jar Grep.jar substringToFind file1 file2...
 * <p>
 * Created by ia.vorobev on 05.12.2016.
 */
public class Grep {

    // parameters of 'new line' constant
    private static final ByteBuffer NEW_LINE = ByteBuffer.wrap(System.lineSeparator().getBytes());
    private static final int NL_LENGTH = NEW_LINE.capacity();
    private static final byte NL_FIRST = NEW_LINE.get(0);
    private static final byte NL_LAST = NEW_LINE.get(NL_LENGTH - 1);

    // variables for B-M search
    private static int[] rightEntries;
    private static ByteBuffer bMask;
    private static int maskLength;

    public static void main(String[] args) {
        checkArgs(args);

        if (args.length > 2) {
            grep(args[0], args[1], Arrays.copyOfRange(args, 2, args.length));
        } else {
            grep(args[0], args[1]);
        }
    }

    /**
     * @return number of lines, that contains given substring
     */
    public static int grep(String substring, String mandatoryFile, String... optionalFiles) {
        prepareSearching(substring);

        List<String> pathsList = new ArrayList<>(1 + optionalFiles.length);
        pathsList.add(mandatoryFile);

        if (optionalFiles.length != 0) {
            pathsList.addAll(Arrays.asList(optionalFiles));
        }

        int cnt = 0;
        for (String file : pathsList) {

            try (FileChannel ch = new RandomAccessFile(new File(file), "r").getChannel()) {
                WritableByteChannel outChannel = Channels.newChannel(System.out);
                MappedByteBuffer buf = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
                int max = buf.capacity();

                ByteBuffer fileName = ByteBuffer.wrap((file + ": ").getBytes());

                int from;
                int leftBorder;
                int rightBorder = 0;
                while ((from = findSubstring(buf, rightBorder, max)) != -1) {
                    cnt++;
                    leftBorder = findLeftBorder(buf, from);
                    rightBorder = findRightBorder(buf, from + maskLength, max);

                    outChannel.write(fileName);
                    fileName.rewind();

                    ch.transferTo(leftBorder,
                            rightBorder - leftBorder,
                            outChannel);

                    newLine(outChannel);
                }

            } catch (IOException e) {
                System.err.println("grep: " + e.getMessage());
            }
        }

        return cnt;
    }

    private static void checkArgs(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: java -jar Grep.jar substring file1 file2 ...");
        }

        if (args[0].isEmpty()) {
            throw new IllegalArgumentException("Empty substring");
        }

        if (args[1].isEmpty()) {
            throw new IllegalArgumentException("Empty fileName");
        }
    }

    private static void newLine(WritableByteChannel outChannel) throws IOException {
        outChannel.write(NEW_LINE);
        NEW_LINE.rewind();
    }

    private static int findRightBorder(MappedByteBuffer buf, int from, int max) {
        for (int i = from; i < max; i++) {

            if (buf.get(i) != NL_FIRST) {
                while (++i <= max && buf.get(i) != NL_FIRST) ;
            }

            if (i <= max) {
                int j = i + 1;
                int end = j + NL_LENGTH - 1;
                for (int k = 1; j < end && buf.get(j) == NEW_LINE.get(k); j++, k++) ;

                if (j == end) {
                    return i;
                }
            }
        }
        return max;
    }

    private static int findLeftBorder(MappedByteBuffer buf, int from) {
        for (int i = from; i >= 0; i--) {
            if (buf.get(i) != NL_LAST) {
                while (--i >= 0 && buf.get(i) != NL_LAST) ;
            }

            if (i >= 0) {
                int j = i - 1;
                int end = j - NL_LENGTH + 1;
                for (int k = NL_LENGTH - 2; j > end && buf.get(j) == NEW_LINE.get(k); j--, k--) ;

                if (j == end) {
                    return i + 1;
                }
            }
        }
        return 0;
    }

    private static void prepareSearching(String mask) {
        bMask = ByteBuffer.wrap(mask.getBytes());
        maskLength = bMask.capacity();

        int dictPower = 256;
        rightEntries = new int[dictPower];

        for (int i = 0; i < dictPower; i++) {
            rightEntries[i] = -1;
        }
        int temp;
        for (int j = 0; j < maskLength; j++) {
            temp = bMask.get();
            rightEntries[temp < 0 ? 127 - temp : temp] = j;
        }
    }

    private static int findSubstring(ByteBuffer buf, int from, int max) {
        int skip;
        for (int i = from; i <= max - maskLength; i += skip) {
            skip = 0;

            for (int j = maskLength - 1; j >= 0; j--) {
                if (bMask.get(j) != buf.get(i + j)) {
                    int temp = buf.get(i + j);
                    skip = j - rightEntries[temp < 0 ? 127 - temp : temp];
                    if (skip < 1) {
                        skip = 1;
                    }
                    break;
                }
            }
            if (skip == 0) {
                return i;
            }
        }
        return -1;
    }

}