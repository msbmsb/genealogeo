/******************************************************************************
* KmlBuilder
* KML writer and construction class for creating rich KML 
* of genealogeo mappings
* 
* Author:       Mitchell Bowden <mitchellbowden AT gmail DOT com>
* License:      MIT License: http://creativecommons.org/licenses/MIT/
******************************************************************************/

package com.msbmsb.genealogeo.kml;

import com.msbmsb.genealogeo.kml.*;
import com.msbmsb.genealoj.*;
import com.msbmsb.gogogeocode.*;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.LineStyle;

import java.io.File;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class KmlBuilder {
  private Kml m_kml;
  private Document m_doc;
  private Folder m_folder;
  private Style m_defaultStyle;
  private Map<String, Coordinates> m_refToCoordMap;

  public KmlBuilder(String kmlName) {
    m_kml = new Kml();
    m_doc = m_kml.createAndSetDocument().withName(kmlName).withOpen(true);
    m_folder = m_doc.createAndAddFolder();
    m_defaultStyle = m_doc.createAndAddStyle();
    m_defaultStyle.withId("style_default")
      .createAndSetIconStyle().withScale(10.0);
    m_defaultStyle.createAndSetLabelStyle().withColor("000000").withScale(10.0);
    m_defaultStyle.createAndSetLineStyle()
      .withColor("760000ff")
      .withWidth(3.0d);
    m_refToCoordMap = new HashMap<String, Coordinates>();
  }

  public void placeIndividual(IndividualNode indi) {
    // add placemark for birth
    // add placemark for death
    // link if both exist
    // add ref and coord pair to map
    GedcomNode birt = indi.getLocation("BIRT");
    GedcomNode deat = indi.getLocation("DEAT");
    Coordinates birt_coords = null, deat_coords = null;
    Placemark birt_placemark, deat_placemark;
    String fullName = indi.getFullName();

    if(birt != null) {
      birt_coords = GoGoGeocode.geocode(birt.data());
      birt_placemark = addPlacemarkWithLabel(birt_coords, fullName + " - birth");
      m_refToCoordMap.put(indi.reference(), birt_coords);
    }
    if(deat != null) {
      deat_coords = GoGoGeocode.geocode(deat.data());
      deat_placemark = addPlacemarkWithLabel(deat_coords, fullName + " - death");
      if(birt == null) {
        m_refToCoordMap.put(indi.reference(), deat_coords);
      }
    }

    if(birt != null && deat != null) {
      linkPlacemarks(birt_coords, deat_coords);
    }

    // TODO?
    // link all available locations on this individual
  }

  public Placemark addPlacemarkWithLabel(Coordinates coords, String label) {
    Placemark placemark = m_folder.createAndAddPlacemark();
    placemark.withName(label)
      .withStyleUrl("#style_default")
      .withDescription(label)
      .createAndSetLookAt().withLongitude(coords.longitude).withLatitude(coords.latitude).withAltitude(0).withRange(12000000);
    placemark.createAndSetPoint().addToCoordinates(coords.longitude, coords.latitude);
    return placemark;
  }

  public void linkPlacemarks(Coordinates coords0, Coordinates coords1) {
    if(coords0 == null || coords1 == null) return;

    Placemark placemark = m_folder.createAndAddPlacemark();
    placemark.withName("link")
      .withStyleUrl("#style_default")
      .withDescription("placemark link");
    placemark.createAndSetLineString().withExtrude(true).withTessellate(true)
      .addToCoordinates(coords0.toString())
      .addToCoordinates(coords1.toString());
  }

  public void linkFamilies(List<GedcomNode> families) {
    for(GedcomNode f : families) {
      List<GedcomNode> husbs = f.getChildrenWithTag("HUSB");
      List<GedcomNode> wifes = f.getChildrenWithTag("WIFE");
      List<GedcomNode> chils = f.getChildrenWithTag("CHIL");
      // if there are no children, there is nothing to link here
      if(chils == null) continue;

      Coordinates husb_coords = null, wife_coords = null;

      if(husbs != null) {
        husb_coords = m_refToCoordMap.get(husbs.get(0).data());
      }
      if(wifes != null) {
        wife_coords = m_refToCoordMap.get(wifes.get(0).data());
      }

      for(GedcomNode c : chils) {
        Coordinates child_coords = m_refToCoordMap.get(c.data());
        linkPlacemarks(child_coords, husb_coords);
        linkPlacemarks(child_coords, wife_coords);
      }
    }
  }

  public void addAndLink(IndividualNode curr) {
    addAndLink(curr, null, null);
  }

  public void addAndLink(IndividualNode curr, IndividualNode prev, Coordinates prevCoords) {
    // add curr, link to prev
    // then recurse

    String fullName = curr.getFullName();
    Coordinates currCoords = GoGoGeocode.geocode(curr.getChildrenWithTag("BIRT").get(0).getChildrenWithTag("PLAC").get(0).data());

    Placemark placemark = m_folder.createAndAddPlacemark();
    placemark.withName(curr.getSurname())
      .withStyleUrl("#style_default")
      .withDescription(fullName + " - birth")
      .createAndSetLookAt().withLongitude(currCoords.longitude).withLatitude(currCoords.latitude).withAltitude(0).withRange(12000000);
    placemark.createAndSetPoint().addToCoordinates(currCoords.longitude, currCoords.latitude);

    // if prev coords available, make connecting line
    if(prevCoords != null)
      placemark.createAndSetLineString().withExtrude(true).withTessellate(true)
        .addToCoordinates(currCoords.toString())
        .addToCoordinates(prevCoords.toString());

    List<IndividualNode> parents = curr.getParents();
    for(IndividualNode i : parents) {
      addAndLink(i, curr, currCoords);
    }
  }

  public void marshal(String fileName) {
    try {
      m_kml.marshal(new File(fileName));
    } catch(Exception e) {
    }
  }
}
