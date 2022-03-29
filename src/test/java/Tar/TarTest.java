package Tar;

import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.io.FileUtils;

public class TarTest {
    private final List<String> testFN = Arrays.asList(
            "test-random-file-1.txt",
            "test-random-file-2.txt",
            "test-random-file-3.txt",
            "test-random-file-4.txt");
    private final String outDirName = "outDir";
    private final String[] unzipArgs = "-u outDir/test-archive.txt".split(" ");

    @Before
    public void generateRandomTestFiles() {
        try {
            // Create directory for output files
            new File(outDirName).mkdirs();

            for (String filename : testFN) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

                // Just some constants
                final int maxLines = 100;
                final int minLines = 10;
                final int maxLineLen = 100;
                final int minLineLen = 10;
    
                final Random rand = new Random();
                final int lineLen = minLines + (Math.abs(rand.nextInt()) % maxLines);

                for (int i = 0; i < lineLen; i++) {
                    writer.write(RandomStringUtils.randomAscii(minLineLen, maxLineLen));
                    writer.newLine();
                }
                writer.close();
            }

        } catch (IOException ioEx) {
            Tar.Main.printError("Failed to crate file");
            fail(ioEx.getMessage());
        }
    }

    private boolean compareFileContence(String filename1, String filename2) {
        if (!(new File(filename1).exists() && new File(filename2).exists()))
            return false;

        if (new File(filename1).length() != new File(filename2).length())
            return false;

        try {
            BufferedReader reader1 = new BufferedReader(new FileReader(filename1));
            BufferedReader reader2 = new BufferedReader(new FileReader(filename2));

            final int mb = 1_048_576;
            final int max_size = mb / 2;
            char[] buffer1 = new char[max_size];
            char[] buffer2 = new char[max_size];

            // A this point we know, that filesizes are identical
            long toRead = new File(filename1).length();
            do {
                reader1.read(buffer1, 0, (int) Math.min(toRead, max_size));
                reader2.read(buffer2, 0, (int) Math.min(toRead, max_size));

                if (!Arrays.equals(buffer1, buffer2)) {
                    reader1.close();
                    reader2.close();
                    return false;
                }
                toRead -= max_size;
            } while (toRead > 0);
            reader1.close();
            reader2.close();
            return true;
        } catch (IOException ioEx) {
            fail(ioEx.getMessage());

            // Just to make compiler happy
            return false;
        }
    }

    @Test
    public void randomFiles() {
        String[] testArgs1 = String.format("-out outDir/test-archive.txt %s %s",
                testFN.get(0), testFN.get(0)).split(" ");

        String[] testArgs2 = String.format("-out outDir/test-archive.txt %s %s %s",
                testFN.get(0), testFN.get(1), testFN.get(2)).split(" ");

        String[] testArgs3 = String.format("-out outDir/test-archive.txt %s %s %s %s",
                testFN.get(0), testFN.get(1), testFN.get(2), testFN.get(3)).split(" ");

        Tar.Main.main(testArgs1);
        Assert.assertTrue(new File("outDir/test-archive.txt").exists());
        Tar.Main.main(unzipArgs);
        Assert.assertTrue(compareFileContence(testFN.get(0), "outDir/" + testFN.get(0)));
        try {
            FileUtils.cleanDirectory(new File(outDirName));
        } catch (IOException ioEx) {
            fail(ioEx.getMessage());
        }

        Tar.Main.main(testArgs2);
        Assert.assertTrue(new File("outDir/test-archive.txt").exists());
        Tar.Main.main(unzipArgs);
        Assert.assertTrue(compareFileContence(testFN.get(0), "outDir/" + testFN.get(0)));
        Assert.assertTrue(compareFileContence(testFN.get(1), "outDir/" + testFN.get(1)));
        Assert.assertTrue(compareFileContence(testFN.get(2), "outDir/" + testFN.get(2)));
        try {
            FileUtils.cleanDirectory(new File(outDirName));
        } catch (IOException ioEx) {
            fail(ioEx.getMessage());
        }

        Tar.Main.main(testArgs3);
        Assert.assertTrue(new File("outDir/test-archive.txt").exists());
        Tar.Main.main(unzipArgs);
        Assert.assertTrue(compareFileContence(testFN.get(0), "outDir/" + testFN.get(0)));
        Assert.assertTrue(compareFileContence(testFN.get(1), "outDir/" + testFN.get(1)));
        Assert.assertTrue(compareFileContence(testFN.get(2), "outDir/" + testFN.get(2)));
        Assert.assertTrue(compareFileContence(testFN.get(3), "outDir/" + testFN.get(3)));
        try {
            FileUtils.cleanDirectory(new File(outDirName));
        } catch (IOException ioEx) {
            fail(ioEx.getMessage());
        }
    }

    @Test
    public void emptyFile() {
        try {
            new File("empty-file.txt").createNewFile();
        } catch (IOException ioEx) {
            fail(ioEx.getMessage());
        }

        String[] testArgs1 = String.format("-out outDir/test-archive.txt %s %s %s",
                "empty-file.txt", testFN.get(1), testFN.get(2)).split(" ");

        String[] testArgs2 = String.format("-out outDir/test-archive.txt %s %s %s",
                testFN.get(0), "empty-file.txt", testFN.get(2)).split(" ");

        String[] testArgs3 = String.format("-out outDir/test-archive.txt %s %s %s",
                testFN.get(0), testFN.get(1), "empty-file.txt").split(" ");

        Tar.Main.main(testArgs1);
        Assert.assertTrue(new File("outDir/test-archive.txt").exists());
        Tar.Main.main(unzipArgs);
        Assert.assertTrue(compareFileContence("empty-file.txt", "outDir/" + "empty-file.txt"));
        Assert.assertTrue(compareFileContence(testFN.get(1), "outDir/" + testFN.get(1)));
        Assert.assertTrue(compareFileContence(testFN.get(2), "outDir/" + testFN.get(2)));
        try {
            FileUtils.cleanDirectory(new File(outDirName));
        } catch (IOException ioEx) {
            fail(ioEx.getMessage());
        }

        Tar.Main.main(testArgs2);
        Assert.assertTrue(new File("outDir/test-archive.txt").exists());
        Tar.Main.main(unzipArgs);
        Assert.assertTrue(compareFileContence(testFN.get(0), "outDir/" + testFN.get(0)));
        Assert.assertTrue(compareFileContence("empty-file.txt", "outDir/" + "empty-file.txt"));
        Assert.assertTrue(compareFileContence(testFN.get(2), "outDir/" + testFN.get(2)));
        try {
            FileUtils.cleanDirectory(new File(outDirName));
        } catch (IOException ioEx) {
            fail(ioEx.getMessage());
        }

        Tar.Main.main(testArgs3);
        Assert.assertTrue(new File("outDir/test-archive.txt").exists());
        Tar.Main.main(unzipArgs);
        Assert.assertTrue(compareFileContence(testFN.get(0), "outDir/" + testFN.get(0)));
        Assert.assertTrue(compareFileContence(testFN.get(1), "outDir/" + testFN.get(1)));
        Assert.assertTrue(compareFileContence("empty-file.txt", "outDir/" + "empty-file.txt"));
        try {
            FileUtils.cleanDirectory(new File(outDirName));
            Files.deleteIfExists(Paths.get("empty-file.txt"));
        } catch (IOException ioEx) {
            fail(ioEx.getMessage());
        }
    }

    // By different encodings i mean just ascii ¯\_(ツ)_/¯
    @Test
    public void differentEncodings() {
        try {
            new File("ascii-file.txt").createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter("ascii-file.txt"));

            // Just some constants
            final int maxLines = 100;
            final int minLines = 10;
            final int maxLineLen = 100;

            final Random rand = new Random();
            final int lineLen = minLines + (Math.abs(rand.nextInt()) % maxLines);

            for (int i = 0; i < lineLen; i++) {
                writer.write(RandomStringUtils.random(maxLineLen));
                writer.newLine();
            }
            writer.close();
        } catch (IOException ioEx) {
            fail(ioEx.getMessage());
        }
        String[] testArgs = String.format("-out outDir/test-archive.txt %s %s %s",
                "ascii-file.txt", testFN.get(1), testFN.get(2)).split(" ");
        Tar.Main.main(testArgs);
        Assert.assertTrue(new File("outDir/test-archive.txt").exists());
        Tar.Main.main(unzipArgs);
        Assert.assertTrue(compareFileContence("ascii-file.txt", "outDir/" + "ascii-file.txt"));
        Assert.assertTrue(compareFileContence(testFN.get(1), "outDir/" + testFN.get(1)));
        Assert.assertTrue(compareFileContence(testFN.get(2), "outDir/" + testFN.get(2)));

        try {
            Files.deleteIfExists(Paths.get("ascii-file.txt"));
        } catch (IOException ioEx) {
            fail(ioEx.getMessage());
        }
    }

    @After
    public void deleteTestFiles() throws IOException {
        for (String filename : testFN) {
            Files.deleteIfExists(Paths.get(filename));
        }
        FileUtils.deleteDirectory(new File(outDirName));
    }
}
