package Tar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Archiver {
    public Archiver(List<String> in, String out) {
        files = new ArrayList<>();

        outputFilename = out;
        for (String file : in) {
            File nF = new File(file);
            if (!nF.exists() || nF.isDirectory()) {
                throw new IllegalArgumentException(
                        "File " + file + " does not exist");
            }
            files.add(new Tuple<>(nF.toString(), nF.length()));
        }
    }

    public void archive() throws IOException {
        File out = new File(outputFilename);
        if (!out.createNewFile())
            throw new IllegalArgumentException("File " + outputFilename + " already exists");

        try (FileOutputStream writer = new FileOutputStream(out)) {
            // Construct and write the header
            StringBuilder toWrite = new StringBuilder();
            for (Tuple<String, Long> file : files) {
                // Separate the name of the file
                toWrite.append(file.first.substring(file.first.lastIndexOf("\\") + 1));
                toWrite.append(" [");
                toWrite.append(file.second.toString());
                toWrite.append("]\n");
            }
            // Write header size at the start
            writer.write(
                    ("header [" + (toWrite.toString().getBytes(Constants.headerEncoding).length) + "]\n")
                            .getBytes(Constants.headerEncoding));
            writer.write(toWrite.toString().getBytes(Constants.headerEncoding));

            // Write file contents
            for (Tuple<String, Long> file : files) {
                try (FileInputStream fileIS = new FileInputStream(file.first)) {
                    // Read archive content and write it to file
                    long toRead = file.second;
                    int readBytes;
                    byte[] buffer = new byte[Constants.max_buffer_size];
                    do {
                        readBytes = fileIS.read(buffer, 0, (int) Math.min(toRead, Constants.max_buffer_size));
                        writer.write(buffer, 0, readBytes);
                        toRead -= readBytes;
                    } while (toRead > 0);
                }
            }
        }
    }

    private List<Tuple<String, Long>> files;
    private String outputFilename;
}
