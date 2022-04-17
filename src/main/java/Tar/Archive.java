package Tar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

class Archive {
    // Checks if the given filename is a valid filename in Windows
    public static boolean checkFilename(String filename) {
        final Set<Character> excludedChars = new HashSet<>(
                Arrays.asList('/', '\n', '\r', '\t', '\0',
                        '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'));
        return filename.chars().mapToObj((i) -> (char) i).noneMatch(excludedChars::contains);
    }

    Archive(String filename) throws IOException {
        if (!(new File(filename).canRead())) {
            throw new IOException("Given archive cannot be read");
        }
        fileIS = new FileInputStream(filename);
        files = new ArrayList<>();
        this.filename = filename;

        readHeader();
    }

    // Read header section of the file and save read data to files variable
    private void readHeader() throws IOException {
        Tuple<String, Integer> headerData;
        String firstString;
        try (BufferedReader fileReader = new BufferedReader(new FileReader(filename, Constants.headerEncoding))) {
            firstString = fileReader.readLine();
            headerData = getFileData(firstString);
            if (!headerData.first.equals("header")) {
                throw new IOException("Invalid header in file" + filename);
            }
        }
        fileIS.skipNBytes(firstString.getBytes(Constants.headerEncoding).length + 1 /*account for \n char*/);

        // Read the rest of the header
        byte[] buffer = new byte[headerData.second];
        // Hom many bytes was read
        int readBytes = 0;
        do {
            readBytes += fileIS.read(buffer, 0, headerData.second - readBytes);
        } while (headerData.second < readBytes);

        String headerString = new String(buffer, Constants.headerEncoding);
        String[] headerLines = headerString.split("\n");

        for (String line : headerLines) {
            Tuple<String, Integer> fileData = getFileData(line);
            files.add(new Tuple<>(fileData.first, fileData.second));
        }
    }

    public void unzip() throws IOException {
        String path = FilenameUtils.getPath(filename);

        for (Tuple<String, Integer> file : files) {
            try (FileOutputStream fOS = new FileOutputStream(path + file.first)) {
                // Read archive content and write it to file
                int toRead = file.second;
                // How much bytes was read
                int readBytes;
                byte[] buffer = new byte[Constants.max_buffer_size];
                do {
                    readBytes = fileIS.read(buffer, 0, Math.min(toRead, Constants.max_buffer_size));
                    fOS.write(buffer, 0, readBytes);
                    toRead -= readBytes;
                } while (toRead > 0);
            }
        }
        fileIS.close();
    }

    // Get filename and filesize from one line in the header
    private Tuple<String, Integer> getFileData(String rawString) throws IOException {
        Pattern filenamePattern = Pattern.compile("^[\\s\\S.]+(?=\\s\\[)");
        Pattern filesizePattern = Pattern.compile("(?<=\\s\\[)\\d+(?=\\]$)");

        Matcher filenameMatcher = filenamePattern.matcher(rawString);
        Matcher filesizeMatcher = filesizePattern.matcher(rawString);

        if (!filenameMatcher.find() || !filesizeMatcher.find()) {
            throw new IOException("Invalid header in file" + filename);
        }

        int size;
        try {
            size = Integer.parseInt(rawString.substring(filesizeMatcher.start(), filesizeMatcher.end()));
        } catch (NumberFormatException nfEx) {
            throw new IOException("Invalid header in file" + filename);
        }
        Tuple<String, Integer> out = new Tuple<>(
                rawString.substring(filenameMatcher.start(), filenameMatcher.end()), size);

        if (!checkFilename(out.first)) {
            throw new IOException("Invalid header in file" + filename);
        }
        return out;
    }

    private final String filename;
    private final FileInputStream fileIS;
    // Files, contained in given archive
    // String - filename
    // Integer - file size in bytes
    private final List<Tuple<String, Integer>> files;
}
