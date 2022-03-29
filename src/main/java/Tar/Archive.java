package Tar;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FilenameUtils;

class Archive {
    // Checks if the given filename is a valid filename in windows
    public static boolean checkFilename(String filename) {
        Set<Character> excludedChars = new HashSet<Character>(
                Arrays.asList('/', '\n', '\r', '\t', '\0',
                        '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'));
        return !filename.chars().mapToObj((i) -> (char) i).anyMatch(excludedChars::contains);
    }

    private String filename;
    private FileInputStream fileIS;
    // Files, contained in given archive
    // String - filename(duh)d
    // Integer - file size in bytes
    private List<Tuple<String, Integer>> files;

    // Header size, but with included first line
    // Used to set the offset when reading data blocks from archive
    int headerOffset;

    Archive(String filename) throws IOException {
        files = new ArrayList<Tuple<String, Integer>>();

        this.filename = filename;
        fileIS = new FileInputStream(filename);
        readHeader();
    }

    // Read header section of the file and save read data to files
    private void readHeader() throws IOException {
        // Just reading bytes until we encounter new line and converting them to chars
        StringBuilder firstLineBuild = new StringBuilder();
        do {
            firstLineBuild.append((Character)(char)(fileIS.read()));
        } while (firstLineBuild.charAt(firstLineBuild.length() - 1) != '\n');
        String[] headerData = firstLineBuild.toString().split(" ");

        // Assert if the first line is valid
        if (headerData.length != 2
                || headerData[1].length() < 3
                || !headerData[0].equals("header")
                || !headerData[1].startsWith("[") || headerData[1].endsWith("]")) {
            throw new IOException("Invalid header in file" + filename);
        }

        // Size of the header in bytes excluding the first line
        int headerSize;
        try {
            headerSize = Integer.parseInt(headerData[1].substring(1, headerData[1].length() - 2));
        } catch (NumberFormatException nfEx) {
            throw new IOException("Invalid header in file" + filename);
        }
        int firstLineLength = (headerData[0].length() + headerData[1].length());
        headerOffset = headerSize + firstLineLength;

        // Read the rest of the header
        byte[] buffer = new byte[headerSize];
        fileIS.read(buffer, 0, headerSize);

        String headerString = new String(buffer, "UTF-8");
        String[] headerLines = headerString.split("\n");

        for (String line : headerLines) {
            String[] lineData = line.split(" ");

            // Assert the read line is valid
            if (lineData.length != 2
                    || lineData[1].length() < 3
                    || lineData[0].length() == 0
                    || !lineData[1].startsWith("[") || headerData[1].endsWith("]")
                    || !checkFilename(lineData[0])) {
                throw new IOException("Invalid header in file" + filename);
            }

            int filesize;
            try {
                filesize = Integer.parseInt(lineData[1].substring(1, lineData[1].length() - 1));
            } catch (NumberFormatException nfEx) {
                throw new IOException("Invalid header in file" + filename);
            }

            files.add(new Tuple<String, Integer>(lineData[0], filesize));
        }
    }

    
    public void unzip() throws IOException {
        // How much bytes contained in 1 mb
        final int mb = 1_048_576;
        final int max_size = 10 * mb;
        byte[] buffer = new byte[max_size];

        String path = FilenameUtils.getPath(filename);

        for (Tuple<String, Integer> file : files) {
            FileOutputStream fOS = new FileOutputStream(path + file.first);

            // Read archive content and write it to file(max 10mb at a time)
            int toRead = file.second;
            do {
                fileIS.read(buffer, 0, Math.min(toRead, max_size));
                fOS.write(buffer, 0, Math.min(toRead, max_size));
                toRead -= max_size;
            } while (toRead > 0);
            fOS.close();
        }
        fileIS.close();
    }
}
