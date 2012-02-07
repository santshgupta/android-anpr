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

package intelligence.recognizer;

import intelligence.intelligence.Intelligence;

import java.io.File;
//import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.transform.TransformerException;
//import org.xml.sax.SAXException;
//import javaanpr.configurator.Configurator;
//import javaanpr.gui.ReportGenerator;
import intelligence.imageanalysis.Char;
import intelligence.neuralnetwork.NeuralNetwork;

public class NeuralPatternClassificator extends CharacterRecognizer {
    
    private static int normalize_x =
            Intelligence.configurator.getIntProperty("char_normalizeddimensions_x");
    private static int normalize_y =
            Intelligence.configurator.getIntProperty("char_normalizeddimensions_y");
    
    public NeuralNetwork network;
    
    public NeuralPatternClassificator() throws Exception {
        this(false);
    }
    
    public NeuralPatternClassificator(boolean learn) throws Exception {
        Vector<Integer> dimensions = new Vector<Integer>();
        int inputLayerSize;
        if (Intelligence.configurator.getIntProperty("char_featuresExtractionMethod")==0)
            inputLayerSize = normalize_x * normalize_y;
        else 
        	inputLayerSize = CharacterRecognizer.features.length*4;
        dimensions.add(inputLayerSize);
        dimensions.add(Intelligence.configurator.getIntProperty("neural_topology"));
        dimensions.add(CharacterRecognizer.alphabet.length);
        this.network = new NeuralNetwork(dimensions);
        if (learn) {
        	learnAlphabet(Intelligence.configurator.getStrProperty("char_learnAlphabetPath"));
        } else {
            this.network = new NeuralNetwork(Intelligence.configurator.getPathProperty("char_neuralNetworkPath"));
        }
    }
    
    public RecognizedChar recognize(Char imgChar) {
        imgChar.normalize();
        Vector<Double> output = this.network.test(imgChar.extractFeatures());
        RecognizedChar recognized = new RecognizedChar();
        for (int i=0; i<output.size(); i++) {
            recognized.addPattern(recognized.new RecognizedPattern(CharacterRecognizer.alphabet[i], output.elementAt(i).floatValue()));
        }
        //Intelligence.console.consoleBitmap(recognized.render());
        recognized.sort(1);
        return recognized;
    }

    public NeuralNetwork.SetOfIOPairs.IOPair createNewPair(char chr, Char imgChar) {
        Vector<Double> vectorInput = imgChar.extractFeatures();
        Vector<Double> vectorOutput = new Vector<Double>();
        for (int i=0; i<CharacterRecognizer.alphabet.length; i++)
            if (chr == CharacterRecognizer.alphabet[i]) 
            	vectorOutput.add(1.0); 
            else 
            	vectorOutput.add(0.0);
        return (new NeuralNetwork.SetOfIOPairs.IOPair(vectorInput, vectorOutput));
    }
    
    public void learnAlphabet(String path) throws IOException {
        String alphaString = "0123456789abcehkmoptxy";     
        File folder = new File(android.os.Environment.getExternalStorageDirectory(), path);
        NeuralNetwork.SetOfIOPairs train = new NeuralNetwork.SetOfIOPairs();
        for (String fileName : folder.list()) {
            if (alphaString.indexOf(fileName.toLowerCase().charAt(0))==-1)
                continue; 
            Char imgChar = new Char(path+File.separator+fileName);
            imgChar.normalize();
            train.addIOPair(this.createNewPair(fileName.toUpperCase().charAt(0), imgChar));
        }
        this.network.learn(train,
                Intelligence.configurator.getIntProperty("neural_maxk"),
                Intelligence.configurator.getDoubleProperty("neural_eps"),
                Intelligence.configurator.getDoubleProperty("neural_lambda"),
                Intelligence.configurator.getDoubleProperty("neural_micro")
                );
    }
}
