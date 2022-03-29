package Tar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Archiver {
    private List<Tuple<String, Long>> files;
    private String outputFilename;

    public Archiver(List<String> in, String out) {
        files = new ArrayList<Tuple<String, Long>>();

        outputFilename = out;
        for (String file : in) {
            File nF = new File(file);
            if (!nF.exists() || nF.isDirectory()) {
                throw new IllegalArgumentException(
                        "File " + file + " does not exist");
            }
            files.add(new Tuple<String, Long>(nF.getName(), nF.length()));
        }
    }

    public void archive() throws IOException {
        File out = new File(outputFilename);

        if (!out.createNewFile())
            throw new IllegalArgumentException("File " + outputFilename + " already exists");

        FileOutputStream writer = new FileOutputStream(out);

        // Compute and write header
        StringBuilder toWrite = new StringBuilder();
        for (Tuple<String, Long> file : files) {
            toWrite.append(file.first);
            toWrite.append(" [");
            toWrite.append(file.second.toString());
            toWrite.append("]\n");
        }
        // Write header size at the start
        writer.write(("header [" + String.valueOf(toWrite.length()) + "]\n").getBytes());
        writer.write(toWrite.toString().getBytes());

        final int mb = 1_048_576;
        final int max_size = 10 * mb;
        byte[] buffer = new byte[max_size];
        // Write file contents
        for (Tuple<String, Long> file : files) {
            FileInputStream fileIS = new FileInputStream(file.first);
            // Read archive content and write it to file(max 10mb at a time)
            long toRead = file.second;
            do {
                fileIS.read(buffer, 0, (int)Math.min(toRead, max_size));
                writer.write(buffer, 0, (int)Math.min(toRead, max_size));
                toRead -= max_size;
            } while (toRead > 0);
            fileIS.close();
        }
        writer.close();
    }

}
