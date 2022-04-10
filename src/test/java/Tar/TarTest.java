package Tar;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
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
    private static final List<String> testFN = Arrays.asList(
            "test-random-file-1.txt",
            "test-random-file-2.txt",
            "test-random-file-3.txt",
            "test-random-file-4.txt");
    private static final String outDirName = "outDir";
    private static final String[] unzipArgs = "-u outDir/test-archive.txt".split(" ");

    // Max dimentions for a test file
    private static final int maxLines = 100;
    private static final int minLines = 10;
    private static final int maxLineLen = 100;
    private static final int minLineLen = 10;

    @BeforeClass
    public static void generateRandomTestFiles() {
        try {
            // Create directory for output files
            new File(outDirName).mkdirs();

            for (String filename : testFN) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

                final Random rand = new Random();
                final int lineLen = minLines + (Math.abs(rand.nextInt()) % maxLines);

                for (int i = 0; i < lineLen; i++) {
                    writer.write(RandomStringUtils.randomAscii(minLineLen, maxLineLen));
                    writer.newLine();
                }
                writer.close();
            }

        } catch (IOException ioEx) {
            Tar.Main.printError("Failed to create a test file");
            fail(ioEx.getMessage());
        }
    }

    @After
    public void cleanTestDirectory() {
        try {
            FileUtils.cleanDirectory(new File(outDirName));
        } catch (IOException ioEx) {
            fail(ioEx.getMessage());
        }
    }

    private boolean compareFileContent(String filename1, String filename2) {
        if (!(new File(filename1).exists() && new File(filename2).exists()))
            return false;

        if (new File(filename1).length() != new File(filename2).length())
            return false;

        try {
            try (BufferedReader reader1 = new BufferedReader(new FileReader(filename1));
                    BufferedReader reader2 = new BufferedReader(new FileReader(filename2))) {

                char[] buffer1 = new char[Constants.max_size / 2];
                char[] buffer2 = new char[Constants.max_size / 2];

                // A this point we know, that filesizes are identical
                long toRead = new File(filename1).length();
                do {
                    reader1.read(buffer1, 0, (int) Math.min(toRead, Constants.max_size));
                    reader2.read(buffer2, 0, (int) Math.min(toRead, Constants.max_size));

                    if (!Arrays.equals(buffer1, buffer2)) {
                        return false;
                    }
                    toRead -= Constants.max_size;
                } while (toRead > 0);
                return true;
            }
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
        Assert.assertTrue(compareFileContent(testFN.get(0), "outDir/" + testFN.get(0)));

        cleanTestDirectory();

        Tar.Main.main(testArgs2);
        Assert.assertTrue(new File("outDir/test-archive.txt").exists());
        Tar.Main.main(unzipArgs);
        Assert.assertTrue(compareFileContent(testFN.get(0), "outDir/" + testFN.get(0)));
        Assert.assertTrue(compareFileContent(testFN.get(1), "outDir/" + testFN.get(1)));
        Assert.assertTrue(compareFileContent(testFN.get(2), "outDir/" + testFN.get(2)));

        cleanTestDirectory();

        Tar.Main.main(testArgs3);
        Assert.assertTrue(new File("outDir/test-archive.txt").exists());
        Tar.Main.main(unzipArgs);
        Assert.assertTrue(compareFileContent(testFN.get(0), "outDir/" + testFN.get(0)));
        Assert.assertTrue(compareFileContent(testFN.get(1), "outDir/" + testFN.get(1)));
        Assert.assertTrue(compareFileContent(testFN.get(2), "outDir/" + testFN.get(2)));
        Assert.assertTrue(compareFileContent(testFN.get(3), "outDir/" + testFN.get(3)));
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
        Assert.assertTrue(compareFileContent("empty-file.txt", "outDir/" + "empty-file.txt"));
        Assert.assertTrue(compareFileContent(testFN.get(1), "outDir/" + testFN.get(1)));
        Assert.assertTrue(compareFileContent(testFN.get(2), "outDir/" + testFN.get(2)));

        cleanTestDirectory();

        Tar.Main.main(testArgs2);
        Assert.assertTrue(new File("outDir/test-archive.txt").exists());
        Tar.Main.main(unzipArgs);
        Assert.assertTrue(compareFileContent(testFN.get(0), "outDir/" + testFN.get(0)));
        Assert.assertTrue(compareFileContent("empty-file.txt", "outDir/" + "empty-file.txt"));
        Assert.assertTrue(compareFileContent(testFN.get(2), "outDir/" + testFN.get(2)));

        cleanTestDirectory();

        Tar.Main.main(testArgs3);
        Assert.assertTrue(new File("outDir/test-archive.txt").exists());
        Tar.Main.main(unzipArgs);
        Assert.assertTrue(compareFileContent(testFN.get(0), "outDir/" + testFN.get(0)));
        Assert.assertTrue(compareFileContent(testFN.get(1), "outDir/" + testFN.get(1)));
        Assert.assertTrue(compareFileContent("empty-file.txt", "outDir/" + "empty-file.txt"));

        new File("empty-file.txt").delete();
    }

    // By different encodings i mean just ascii ¯\_(ツ)_/¯
    @Test
    public void differentEncodings() {
        try {
            new File("ascii-file.txt").createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter("ascii-file.txt"));

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
        Assert.assertTrue(compareFileContent("ascii-file.txt", "outDir/" + "ascii-file.txt"));
        Assert.assertTrue(compareFileContent(testFN.get(1), "outDir/" + testFN.get(1)));
        Assert.assertTrue(compareFileContent(testFN.get(2), "outDir/" + testFN.get(2)));

        new File("ascii-file.txt").delete();
    }

    @Test
    public void filenames() {
        List<String> filenames = Arrays.asList(
                "normalFilename.txt",
                "with spaces.txt",
                "without extention",
                "multiple dots.jpg.txt",
                "strange name [1002]");

        try {
            for (String filename : filenames) {
                new File(filename).createNewFile();
            }

        } catch (IOException ioEx) {
            Tar.Main.printError("Failed to crate a test file");
            fail(ioEx.getMessage());
        }

        String[] testArgs = new String[] {
                "-out", "outDir/test-archive.txt",
                filenames.get(0), filenames.get(1),
                filenames.get(2), filenames.get(3),
                filenames.get(4),
        };
        Tar.Main.main(testArgs);
        Tar.Main.main(unzipArgs);
        for (String filename : filenames) {
            Assert.assertTrue(new File("outDir/" + filename).exists());
        }


        for (String filename : filenames) {
            new File(filename).delete();
        }
    }

    @AfterClass
    public static void deleteTestFiles() throws IOException {
        for (String filename : testFN) {
            Files.deleteIfExists(Paths.get(filename));
        }
        FileUtils.deleteDirectory(new File(outDirName));
    }
}
