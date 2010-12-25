/******************************************************************************
* GenealoGeo
* Genealogy data (GEDCOM, etc) to KML converter for use in Google Earth, 
* Google Maps, and other mapping applications.
* 
* Author:       Mitchell Bowden <mitchellbowden AT gmail DOT com>
* License:      MIT License: http://creativecommons.org/licenses/MIT/
******************************************************************************/

package com.msbmsb.genealogeo;

import com.msbmsb.genealoj.*;
import com.msbmsb.gogogeocode.*;

import java.io.File;
import java.util.List;

/**
 * Main class for genealogy data to KML conversion
 */
public class GenealoGeo {
  /**
   * GenealoJ GEDCOM parser
   */
  GenealoJ gedcomParser;

  /**
   * Default constructor
   */
  public GenealoGeo() {
  }

  /**
   * Method for loading the input genealogy file
   * @param file File to load
   */
  public void load(File inputFile) {
    try {
      gedcomParser = new GenealoJ(inputFile);
    } catch(Exception e) {
      System.out.println("Failed to load input file: " + inputFile
                          + "; " + e.toString());
    }
  }

  /**
   * Method for loading the input genealogy file
   * @param String Filename to load
   */
  public void load(String inputFile) {
    load(new File(inputFile));
  }

  //
  // Testing methods 
  // 

  /**
   * Print all families in the loaded genealogy file
   */
  public void printFamilies() {
    if(gedcomParser == null) {
      System.out.println("Error: No Genealogy file loaded.");
      return;
    }

    // load gedcom family nodes directly
    List<GedcomNode> families = gedcomParser.getNodes(Utils.FAMILY_TAG);

    if(families == null) return;

    for(GedcomNode f : families) {
      System.out.println("Family: " + f);
    }
  }

  /**
   * Print all locations in the loaded genealogy file
   */
  public void printLocations() {
    if(gedcomParser == null) {
      System.out.println("Error: No Genealogy file loaded.");
      return;
    }

    // load gedcom place nodes directly
    List<GedcomNode> families = gedcomParser.getNodes(Utils.INDIVIDUAL_TAG);

    if(families == null) return;

    for(GedcomNode f : families) {
      List<GedcomNode> births = f.getChildrenWithTag("BIRT");
      if(births == null) continue;
      for(GedcomNode b : births) {
        List<GedcomNode> locations = b.getChildrenWithTag("PLAC");
  
        if(locations == null) continue;
  
        for(GedcomNode l : locations) {
          System.out.println("Location: " + l);
          System.out.println(GoGoGeocode.geocodeToString(l.data()));
        }
      }
    }
  }
}
