/******************************************************************************
* GenealoGeoTest
* Simple junit test for the GenealoGeo class
* 
* Author:       Mitchell Bowden <mitchellbowden AT gmail DOT com>
* License:      MIT License: http://creativecommons.org/licenses/MIT/
******************************************************************************/

package com.msbmsb.genealogeo;

import com.msbmsb.genealogeo.GenealoGeo;
import com.msbmsb.genealogeo.kml.KmlBuilder;
import com.msbmsb.genealoj.*;
import com.msbmsb.gogogeocode.*;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Style;

import java.io.File;
import java.util.List;
import java.util.Scanner;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for GenealoGeo.
 */
public class GenealoGeoTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public GenealoGeoTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( GenealoGeoTest.class );
    }

    /**
     * Rigourous Test
     */
    public void testGenealoGeo()
    {
        String testFile = "data/example.ged";
        GenealoGeo genealoGeo = new GenealoGeo();

        genealoGeo.load(testFile);
        System.out.println("Output: ");
        genealoGeo.printFamilies();
//         genealoGeo.printLocations();

        // generate kml
        KmlBuilder kmlBuilder = new KmlBuilder("ggExample1");
        generateKmlOfFullTree(kmlBuilder, genealoGeo);
        
        assert(true);
    }

    public void generateKmlOfFullTree(KmlBuilder kmlBuilder, GenealoGeo genealoGeo) {
      List<IndividualNode> indivs = genealoGeo.gedcomParser.getIndividuals();
      for(IndividualNode i : indivs) {
        kmlBuilder.placeIndividual(i);
      }
      kmlBuilder.linkFamilies(genealoGeo.gedcomParser.getNodes(Utils.FAMILY_TAG));
      kmlBuilder.marshal("test.kml");
    }

    public void generateKml(KmlBuilder kmlBuilder, GenealoGeo genealoGeo)
    {

      List<GedcomNode> indivs = genealoGeo.gedcomParser.getNodes(Utils.INDIVIDUAL_TAG);
      boolean validInput = false;
      Scanner scanner = new Scanner(System.in);
      while(!validInput) {
        int j=1;
        System.out.println("Please enter the number of the person (0 to quit)");
        for(GedcomNode g : indivs) {
          IndividualNode i = (IndividualNode) g;
          String fullName = i.getFullName();
          System.out.println(j + "\t" + fullName);
          j++;
        }
  
        System.out.println("> ");
        int index = 3;
        System.out.println("You entered: " + index);
        if(index < 0 || index > j-1) {
          System.out.println(index + " is an invalid entry.");
          continue;
        } else {
          validInput = true;
          kmlBuilder.addAndLink((IndividualNode) indivs.get(index-1));
          kmlBuilder.marshal("test.kml");
          break;
        }
      }

    }

    public void generateKmlForParents(Kml kml, Document doc, Folder folder, IndividualNode indiv) {
      List<IndividualNode> parents = indiv.getParents();
      Coordinates indiv_coords = GoGoGeocode.geocode(indiv.getChildrenWithTag("BIRT").get(0).getChildrenWithTag("PLAC").get(0).data());
      for(IndividualNode i : parents) {
        String name = i.getSurname();
        String fullName = i.getFullName();
        Style style = doc.createAndAddStyle();
        style.withId("style_" + name)
          .createAndSetIconStyle().withScale(10.0);
        style.createAndSetLabelStyle().withColor("ff43b3ff").withScale(10.0);

        List<GedcomNode> births = i.getChildrenWithTag("BIRT");
        for(GedcomNode b : births) {
          List<GedcomNode> locations = b.getChildrenWithTag("PLAC");
    
          if(locations == null) continue;
    
          for(GedcomNode l : locations) {
            Coordinates coords = GoGoGeocode.geocode(l.data());
            Placemark placemark = folder.createAndAddPlacemark();
            placemark.withName(name)
              .withStyleUrl("#style_" + name)
              .withDescription(fullName + " - " + l.data())
              .createAndSetLookAt().withLongitude(coords.longitude).withLatitude(coords.latitude).withAltitude(0).withRange(12000000);
            placemark.createAndSetPoint().addToCoordinates(coords.longitude, coords.latitude);
            placemark.createAndSetLineString().withExtrude(true).withTessellate(true)
            .addToCoordinates(indiv_coords.toString())
            .addToCoordinates(coords.toString());
          }
        }
      }
      
      try {
        kml.marshal(new File("test.kml"));
      } catch(Exception e) {
      }
    }
}
