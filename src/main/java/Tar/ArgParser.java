package Tar;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.util.*;

class ArgParser {
    @Option(metaVar = "FILENAME", name = "-u", forbids = "-out", usage = "Unzip archive")
    public String fileToUnzip;

    @Option(metaVar = "FILENAME", name = "-out", forbids = "-u", usage = "Set output filename")
    public String outFilename;

    @Argument(metaVar = "FILENAMES", multiValued = true, usage = "Files to zip")
    public List<String> filesToZip;

    public void parseArguments(final String[] args) {
        final CmdLineParser parser = new CmdLineParser(this);
        if (args.length == 0) {
            parser.printUsage(System.out);
            System.exit(-1);
        }
        try {
            parser.parseArgument(args);

            if (fileToUnzip == null && filesToZip == null) {
                Main.printError("Please specify files to zip");
                System.exit(-1);
            } else if (fileToUnzip == null && outFilename == null) {
                Main.printError("Please specify action(-u or -out key)");
                System.exit(-1);
            } else if (fileToUnzip != null && filesToZip != null) {
                Main.printWarning("Do not specify input files when unzipping");
                // Empty the array just in case
                filesToZip = null;
            }
            // Delete all duplicates if present
            if (filesToZip != null)
                filesToZip = filesToZip.stream().distinct().toList();

        } catch (CmdLineException clEx) {
            Main.printError(clEx.getMessage());
            System.exit(-1);
        }
    }
}