/*
------------------------------------------------------------------------
JavaANPR - Automatic Number Plate Recognition System for Java
------------------------------------------------------------------------

This file is a part of the JavaANPR, licensed under the terms of the
Educational Community License

Copyright (c) 2006-2007 Ondrej Martinsky. All rights reserved

This Original Work, including software, source code, documents, or
other related items, is being provided by the copyright holder(s)
subject to the terms of the Educational Community License. By
obtaining, using and/or copying this Original Work, you agree that you
have read, understand, and will comply with the following terms and
conditions of the Educational Community License:

Permission to use, copy, modify, merge, publish, distribute, and
sublicense this Original Work and its documentation, with or without
modification, for any purpose, and without fee or royalty to the
copyright holder(s) is hereby granted, provided that you include the
following on ALL copies of the Original Work or portions thereof,
including modifications or derivatives, that you make:

# The full text of the Educational Community License in a location
viewable to users of the redistributed or derivative work.

# Any pre-existing intellectual property disclaimers, notices, or terms
and conditions.

# Notice of any changes or modifications to the Original Work,
including the date the changes were made.

# Any modifications of the Original Work must be distributed in such a
manner as to avoid any confusion with the Original Work of the
copyright holders.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

The name and trademarks of copyright holder(s) may NOT be used in
advertising or publicity pertaining to the Original or Derivative Works
without specific, written prior permission. Title to copyright in the
Original Work and any associated documentation will at all times remain
with the copyright holders. 

If you want to alter upon this work, you MUST attribute it in 
a) all source files
b) on every place, where is the copyright of derivated work
exactly by the following label :

---- label begin ----
This work is a derivate of the JavaANPR. JavaANPR is a intellectual 
property of Ondrej Martinsky. Please visit http://javaanpr.sourceforge.net 
for more info about JavaANPR. 
----  label end  ----

------------------------------------------------------------------------
                                         http://javaanpr.sourceforge.net
------------------------------------------------------------------------
*/

package intelligence.intelligence;

import intelligence.recognizer.CharacterRecognizer.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import org.w3c.dom.*;

import com.intelligence.intelligencyActivity;
import com.intelligency.R;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class Parser {
    public class PlateForm {
        public class Position {
            public char[] allowedChars;
            public Position(String data) {
                this.allowedChars = data.toCharArray();
            }
            public boolean isAllowed(char chr) {
                boolean ret = false;
                for (int i=0; i<this.allowedChars.length; i++)
                    if (this.allowedChars[i] == chr)
                        ret = true;
                return ret;
            }
        }
        Vector<Position> positions;
        String name;
        public boolean flagged = false;
        
        public PlateForm(String name) {
            this.name = name;
            this.positions = new Vector<Position>();
        }
        public void addPosition(Position p) {
            this.positions.add(p);
        }
        public Position getPosition(int index) {
            return this.positions.elementAt(index);
        }
        public int length() {
            return this.positions.size();
        }
        
    }
    public class FinalPlate {
        public String plate;
        public float requiredChanges = 0;
        FinalPlate() {
            this.plate = new String();
        }
        public void addChar(char chr) {
            this.plate = this.plate + chr;
        }
    }
    
    Vector<PlateForm> plateForms;
    
    /** Creates a new instance of Parser */
    public Parser() throws Exception {
        this.plateForms = new Vector<PlateForm>();
        this.plateForms = this.loadFromXml(R.raw.syntax);
    }
    
    public Vector<PlateForm> loadFromXml(int fileId) throws Exception {
        Vector<PlateForm> plateForms = new Vector<PlateForm>();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser = factory.newDocumentBuilder();
        InputStream is = intelligencyActivity.cntxt.getResources().openRawResource(fileId);
        Document doc = parser.parse(is);
        Node structureNode = doc.getDocumentElement();
        NodeList structureNodeContent = structureNode.getChildNodes();
        for (int i=0; i<structureNodeContent.getLength(); i++) {
            Node typeNode = structureNodeContent.item(i);
            if (!typeNode.getNodeName().equals("type")) continue;
            PlateForm form = new PlateForm(((Element)typeNode).getAttribute("name"));
            NodeList typeNodeContent = typeNode.getChildNodes();
            for (int ii=0; ii<typeNodeContent.getLength(); ii++) {
                Node charNode = typeNodeContent.item(ii);
                if (!charNode.getNodeName().equals("char")) continue;
                String content = ((Element)charNode).getAttribute("content");
                
                form.addPosition(form.new Position(  content.toUpperCase()  ));
            }
            plateForms.add(form);
        }
        return plateForms;
    }
    
    public void unFlagAll() {
        for (PlateForm form : this.plateForms)
            form.flagged = false;
    }

    public void flagEqualOrShorterLength(int length) {
        boolean found = false;
        for (int i=length; i>=1 && !found; i--) {
            for (PlateForm form : this.plateForms) {
                if (form.length() == i) {
                    form.flagged = true;
                    found = true;
                }
            }
        }
    }
    
    public void flagEqualLength(int length) {
        for (PlateForm form : this.plateForms) {
            if (form.length() == length) {
                form.flagged = true;
            }
        }
    }

    public void invertFlags() {
        for (PlateForm form : this.plateForms)
            form.flagged = !form.flagged;
    }    
    
    // syntax analysis mode : 0 (do not parse)
    //                      : 1 (only equal length)
    //                      : 2 (equal or shorter)
    public String parse(RecognizedPlate recognizedPlate, int syntaxAnalysisMode) throws IOException {
        if (syntaxAnalysisMode==0) {
            return recognizedPlate.getString();
        }
        
        int length = recognizedPlate.chars.size();
        this.unFlagAll();
        if (syntaxAnalysisMode==1) {
            this.flagEqualLength(length);
        } else {
            this.flagEqualOrShorterLength(length);
        }
        
        Vector<FinalPlate> finalPlates = new Vector<FinalPlate>();
        
        for (PlateForm form : this.plateForms) {
            if (!form.flagged) continue;
            for (int i=0; i<= length - form.length(); i++) {
                FinalPlate finalPlate = new FinalPlate();
                for (int ii=0; ii<form.length(); ii++) {
                    RecognizedChar rc = recognizedPlate.getChar(ii+i);
                    if (form.getPosition(ii).isAllowed(rc.getPattern(0).getChar())) {
                        finalPlate.addChar(rc.getPattern(0).getChar());
                    } else {
                        finalPlate.requiredChanges++;
                        for (int x=0; x<rc.getPatterns().size(); x++) {
                            if (form.getPosition(ii).isAllowed(rc.getPattern(x).getChar())) {
                                RecognizedChar.RecognizedPattern rp = rc.getPattern(x);
                                finalPlate.requiredChanges += (rp.getCost() / 100);
                                finalPlate.addChar(rp.getChar());
                                break;
                            }
                        }
                    }
                }
                finalPlates.add(finalPlate);
            }
        }
        
        if (finalPlates.size()==0) 
        	return recognizedPlate.getString();
        
        float minimalChanges = Float.POSITIVE_INFINITY;
        int minimalIndex = 0;
        for (int i=0; i<finalPlates.size(); i++) {
        	//Intelligence.console.console("::"+finalPlates.elementAt(i).plate+" "+finalPlates.elementAt(i).requiredChanges);
            if (finalPlates.elementAt(i).requiredChanges <= minimalChanges) {
                minimalChanges = finalPlates.elementAt(i).requiredChanges;
                minimalIndex = i;
            }
        }
        
        String toReturn = recognizedPlate.getString();
        if (finalPlates.elementAt(minimalIndex).requiredChanges <= 2.1)
            toReturn = finalPlates.elementAt(minimalIndex).plate;
        return toReturn;
    }
    
}
