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

public class KmlBuilder {
  private Kml m_kml;
  private Document m_doc;
  private Folder m_folder;
  private Style m_defaultStyle;

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
