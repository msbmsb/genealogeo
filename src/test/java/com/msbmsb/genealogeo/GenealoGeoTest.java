/******************************************************************************
* GenealoGeoTest
* Simple junit test for the GenealoGeo class
* 
* Author:       Mitchell Bowden <mitchellbowden AT gmail DOT com>
* License:      MIT License: http://creativecommons.org/licenses/MIT/
******************************************************************************/

package com.msbmsb.genealogeo;

import com.msbmsb.genealogeo.GenealoGeo;

import java.io.File;

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
        genealoGeo.printLocations();
        
        assert(true);
    }
}
