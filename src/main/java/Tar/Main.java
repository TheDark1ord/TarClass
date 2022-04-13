package Tar;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        final ArgParser parser = new ArgParser();
        parser.parseArguments(args);

        try {
            if (parser.filesToZip != null) {
                Archiver archiver = new Archiver(parser.filesToZip, parser.outFilename);
                archiver.archive();
            } else {
                Archive archive = new Archive(parser.fileToUnzip);
                archive.unzip();
            }
        } catch (RuntimeException | IOException rtEx) {
            printError(rtEx.getMessage());
            System.exit(-1);
        }
    }

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    public static void printWarning(String text) {
        System.out.print(ANSI_YELLOW + "WARNING: " + ANSI_RESET);
        System.out.println(text);
    }

    public static void printError(String text) {
        System.out.print(ANSI_RED + "ERROR: " + ANSI_RESET);
        System.out.println(text);
    }
}
