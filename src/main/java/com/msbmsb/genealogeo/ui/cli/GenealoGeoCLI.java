/******************************************************************************
* GenealoGeoCLI
* Command-line interface for GenealoGeo
* 
* Author:       Mitchell Bowden <mitchellbowden AT gmail DOT com>
* License:      MIT License: http://creativecommons.org/licenses/MIT/
******************************************************************************/

package com.msbmsb.genealogeo.ui.cli;

import com.msbmsb.genealogeo.GenealoGeo;

import java.io.File;
import java.io.PrintWriter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Command-line interface class for GenealoGeo
 */
public class GenealoGeoCLI {
  public String usage;
  public Options opts;

  /**
   * GenealoGeo internal class
   */
  public GenealoGeo genealoGeo = null;

  /**
   * Constructor for command-line interface
   * Prints usage message and initializes options
   */
  public GenealoGeoCLI() {
    this.usage = "GenealoGeoCLI [options]";
    opts = new Options();
    opts.addOption("h", "help", false, "Show this help message.");
    opts.addOption("f", "file", true, "Path to input GEDCOM file");
    opts.addOption("o", "output", true, "Path to output KML file");
    genealoGeo = new GenealoGeo();
  }

  /**
   * Method for loading the input genealogy file
   * @param file File to load
   */
  public void load(File inputFile) {
    genealoGeo.load(inputFile);
  }

  /**
   * Display usage message
   */
  public void usage(String msg) {
    HelpFormatter hf = new HelpFormatter();
    PrintWriter pw = new PrintWriter(System.err);
    hf.setSyntaxPrefix("Usage: ");
    hf.printHelp(pw, hf.getWidth(), this.usage, null, this.opts,
      hf.getLeftPadding(), hf.getDescPadding(), msg, false);
    pw.flush();
    System.exit(1);
  }

  /**
   * Command-line interface
   */
  public static void main(String[] args) throws Exception {
    GenealoGeoCLI cli = new GenealoGeoCLI();

    // parse command line
    String inputFileName = "";
    try {
      CommandLineParser parser = new GnuParser();
      CommandLine cl = parser.parse(cli.opts, args);
      inputFileName = cl.getOptionValue("file");
      if(cl.hasOption("help"))
        throw new ParseException(null);
    } catch(ParseException e) {
      cli.usage(e.getMessage());
    }

    // require an input file
    if(inputFileName == null) {
      cli.usage("Error: No input file given.");
    }

    // verify and load the input file
    File inputFile = new File(inputFileName);
    if(!inputFile.exists()) {
      cli.usage("Error: Could not open input file: " + inputFileName);
    }

    cli.load(inputFile);

    //  testing functions
    cli.genealoGeo.printFamilies();
    cli.genealoGeo.printLocations();
  }
}
