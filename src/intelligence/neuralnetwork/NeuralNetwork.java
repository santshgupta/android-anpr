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

package intelligence.neuralnetwork;

import intelligence.intelligence.Intelligence;

import java.text.ParseException;
import java.util.Vector;
import java.util.Random;
import java.util.Date;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import java.io.*;

import org.xml.sax.SAXException;

public class NeuralNetwork { 
    private Vector<NeuralLayer> listLayers = new Vector<NeuralLayer>();
    private Random randomGenerator;
    
    public NeuralNetwork(Vector<Integer> dimensions) {
        for (int i=0; i<dimensions.size(); i++) {
            this.listLayers.add(
                               new NeuralLayer(dimensions.elementAt(i).intValue(), this)
                               );
        }
        randomGenerator = new Random();
    }
    
    public NeuralNetwork(String fileName) throws ParserConfigurationException, SAXException, IOException, ParseException {
        loadFromXml(fileName);
        randomGenerator = new Random();
    }

    public Vector<Double> test (Vector<Double> inputs) {
        if (inputs.size() != this.getLayer(0).numberOfNeurons()) throw new ArrayIndexOutOfBoundsException("[Error] NN-Test: You are trying to pass vector with "+inputs.size()+" values into neural layer with "+this.getLayer(0).numberOfNeurons()+" neurons. Consider using another network, or another descriptors.");
        else return activities(inputs);
    }
    
    public void learn (SetOfIOPairs trainingSet, int maxK, double eps, double lambda, double micro) {
        if (trainingSet.pairs.size()==0) throw new NullPointerException("[Error] NN-Learn: You are using an empty training set, neural network couldn't be trained.");
        else if (trainingSet.pairs.elementAt(0).inputs.size() != this.getLayer(0).numberOfNeurons())
            throw new ArrayIndexOutOfBoundsException("[Error] NN-Test: You are trying to pass vector with "+trainingSet.pairs.elementAt(0).inputs.size()+" values into neural layer with "+this.getLayer(0).numberOfNeurons()+" neurons. Consider using another network, or another descriptors.");
        else if (trainingSet.pairs.elementAt(0).outputs.size() != this.getLayer(this.numberOfLayers()-1).numberOfNeurons())
            throw new ArrayIndexOutOfBoundsException("[Error] NN-Test:  You are trying to pass vector with "+trainingSet.pairs.elementAt(0).inputs.size()+" values into neural layer with "+this.getLayer(0).numberOfNeurons()+" neurons. Consider using another network, or another descriptors.");
        else adaptation(trainingSet,maxK,eps,lambda,micro);
    } 

    public int numberOfLayers() {
        return this.listLayers.size();
    }
  
    private void loadFromXml(String fileName) throws ParserConfigurationException, SAXException, IOException, ParseException {
       // Intelligence.console.console("NeuralNetwork : loading network topology from file "+fileName);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser = factory.newDocumentBuilder();
        File source = new File(android.os.Environment.getExternalStorageDirectory(), fileName);
        Document doc = parser.parse(source);

        Node nodeNeuralNetwork = doc.getDocumentElement();
        if (!nodeNeuralNetwork.getNodeName().equals("neuralNetwork")) throw new ParseException("[Error] NN-Load: Parse error in XML file, neural network couldn't be loaded.",0);
        NodeList nodeNeuralNetworkContent = nodeNeuralNetwork.getChildNodes();
        for (int innc=0; innc<nodeNeuralNetworkContent.getLength(); innc++) {
            Node nodeStructure = nodeNeuralNetworkContent.item(innc);
            if (nodeStructure.getNodeName().equals("structure")) { // for structure element
                NodeList nodeStructureContent = nodeStructure.getChildNodes();
                for (int isc=0; isc<nodeStructureContent.getLength();isc++) {
                    Node nodeLayer = nodeStructureContent.item(isc);
                    if (nodeLayer.getNodeName().equals("layer")) { // for layer element
                        NeuralLayer neuralLayer = new NeuralLayer(this);
                        this.listLayers.add(neuralLayer);
                        NodeList nodeLayerContent = nodeLayer.getChildNodes();
                        for (int ilc=0; ilc<nodeLayerContent.getLength();ilc++) {
                            Node nodeNeuron = nodeLayerContent.item(ilc);
                            if (nodeNeuron.getNodeName().equals("neuron")) { // for neuron in layer
                                Neuron neuron = new Neuron(Double.parseDouble(
                                                                             ((Element)nodeNeuron).getAttribute("threshold")
                                                                             ),neuralLayer);
                                neuralLayer.listNeurons.add(neuron);
                                NodeList nodeNeuronContent = nodeNeuron.getChildNodes();
                                for (int inc=0; inc < nodeNeuronContent.getLength();inc++) {
                                    Node nodeNeuralInput = nodeNeuronContent.item(inc);                                    
                                    if (nodeNeuralInput.getNodeName().equals("input")) {
                                        NeuralInput neuralInput = new NeuralInput(Double.parseDouble(
                                                                             ((Element)nodeNeuralInput).getAttribute("weight")   
                                                                             ),neuron);
                                        neuron.listInputs.add(neuralInput);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }    
    }

    public void saveToXml(String fileName) throws ParserConfigurationException, FileNotFoundException, TransformerException, TransformerConfigurationException {
       // Intelligence.console.console("Saving network topology to file "+fileName);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser = factory.newDocumentBuilder();
        Document doc = parser.newDocument();
        
        Element root = doc.createElement("neuralNetwork");
            root.setAttribute("dateOfExport",new Date().toString());
        Element layers = doc.createElement("structure");
            layers.setAttribute("numberOfLayers",Integer.toString(this.numberOfLayers()));
        
        for (int il=0; il<this.numberOfLayers(); il++) {
            Element layer = doc.createElement("layer");
            layer.setAttribute("index",Integer.toString(il));
            layer.setAttribute("numberOfNeurons",Integer.toString(this.getLayer(il).numberOfNeurons()));
            
            for (int in=0; in<this.getLayer(il).numberOfNeurons();in++) {
                Element neuron = doc.createElement("neuron");
                neuron.setAttribute("index",Integer.toString(in));
                neuron.setAttribute("NumberOfInputs",Integer.toString(this.getLayer(il).getNeuron(in).numberOfInputs()));
                neuron.setAttribute("threshold",Double.toString(this.getLayer(il).getNeuron(in).threshold));
                
                for (int ii=0; ii<this.getLayer(il).getNeuron(in).numberOfInputs();ii++) {
                    Element input = doc.createElement("input");
                    input.setAttribute("index",Integer.toString(ii));
                    input.setAttribute("weight",Double.toString(this.getLayer(il).getNeuron(in).getInput(ii).weight));
                    
                    neuron.appendChild(input);
                }                
                layer.appendChild(neuron);
            }            
            layers.appendChild(layer);
        }
        
        root.appendChild(layers);
        doc.appendChild(root);
        
        File xmlOutputFile = new File(android.os.Environment.getExternalStorageDirectory(), fileName);
        FileOutputStream fos;
        Transformer transformer;
        fos = new FileOutputStream(xmlOutputFile);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(fos);
        transformer.setOutputProperty("encoding","iso-8859-2");
        transformer.setOutputProperty("indent","yes");
        transformer.transform(source, result);
    }    
    
    public static class SetOfIOPairs {
        Vector<IOPair> pairs;
        public static class IOPair {
            Vector<Double> inputs;
            Vector<Double> outputs;
            public IOPair(Vector<Double> inputs, Vector<Double> outputs) {
                this.inputs = new Vector<Double>(inputs);
                this.outputs = new Vector<Double>(outputs);
            }
        }
        public SetOfIOPairs() {
            this.pairs = new Vector<IOPair>();
        }
        public void addIOPair(Vector<Double> inputs, Vector<Double> outputs) {
            this.addIOPair(new IOPair(inputs,outputs));
        }
        public void addIOPair(IOPair pair) {
            this.pairs.add(pair);
        }
        int size() {
            return pairs.size();
        }
    }
    private class NeuralInput {
        double weight;
        int index;
        Neuron neuron;
        
        NeuralInput(double weight, Neuron neuron) {
            this.neuron = neuron;
            this.weight = weight;
            this.index = this.neuron.numberOfInputs();
        }
    }
    private class Neuron {
        private Vector<NeuralInput> listInputs = new Vector<NeuralInput>();
        int index;
        public double threshold;
        public double output;
        NeuralLayer neuralLayer;

        Neuron(double threshold, NeuralLayer neuralLayer) {
            this.threshold = threshold;
            this.neuralLayer = neuralLayer;
            this.index = this.neuralLayer.numberOfNeurons();
        }
        
        Neuron(int numberOfInputs, double threshold, NeuralLayer neuralLayer) {
            this.threshold = threshold;
            this.neuralLayer = neuralLayer;
            this.index = this.neuralLayer.numberOfNeurons();
            for (int i = 0; i < numberOfInputs; i++) {
                this.listInputs.add(new NeuralInput(1.0,this));
            }
        }

        public int numberOfInputs() {
            return this.listInputs.size();
        }
        
        public NeuralInput getInput (int index) {
            return this.listInputs.elementAt(index);
        }        
    }

    private class NeuralLayer {
        private Vector<Neuron> listNeurons = new Vector<Neuron>();
        int index;
        NeuralNetwork neuralNetwork;        
        NeuralLayer(NeuralNetwork neuralNetwork) {
            this.neuralNetwork = neuralNetwork;
            this.index = this.neuralNetwork.numberOfLayers();
        }
        NeuralLayer(int numberOfNeurons, NeuralNetwork neuralNetwork) {
            this.neuralNetwork = neuralNetwork;
            this.index = this.neuralNetwork.numberOfLayers();
            for (int i = 0; i < numberOfNeurons;i++) {
                if (this.index == 0) {
                    this.listNeurons.add(new Neuron(1,0.0,this)); 
                } else {
                    this.listNeurons.add(
                                        new Neuron(this.neuralNetwork.getLayer(this.index-1).numberOfNeurons(), 0.0, this)
                                        );
                }
            }
        }
        
        public int numberOfNeurons() {
            return this.listNeurons.size();
        }
        
        public boolean isLayerTop() {
            return (this.index == this.neuralNetwork.numberOfLayers()-1);
        }
        
        public boolean isLayerBottom() {
            return (this.index == 0);
        }
        
        public NeuralLayer upperLayer() { 
            if (this.isLayerTop()) return null;
            return this.neuralNetwork.getLayer(index+1);
        }

        public NeuralLayer lowerLayer() { 
            if (this.isLayerBottom()) return null;
            return this.neuralNetwork.getLayer(index-1);
        }
        
        public Neuron getNeuron(int index) {
            return this.listNeurons.elementAt(index);
        }        
    }
    
    private class Gradients {
        Vector<Vector<Double>> thresholds;
        Vector<Vector<Vector<Double>>> weights;
        NeuralNetwork neuralNetwork;
        
        Gradients(NeuralNetwork network) {
            this.neuralNetwork = network;
            this.initGradients();
        }
        
        public void initGradients() {
            this.thresholds = new Vector<Vector<Double>>();
            this.weights = new Vector<Vector<Vector<Double>>>();
            for (int il = 0; il < this.neuralNetwork.numberOfLayers(); il++) {
                this.thresholds.add(new Vector<Double>());
                this.weights.add(new Vector<Vector<Double>>());
                for (int in = 0; in < this.neuralNetwork.getLayer(il).numberOfNeurons(); in++) {
                    this.thresholds.elementAt(il).add(0.0);
                    this.weights.elementAt(il).add(new Vector<Double>());
                    for (int ii = 0; ii < this.neuralNetwork.getLayer(il).getNeuron(in).numberOfInputs(); ii++) {
                        this.weights.elementAt(il).elementAt(in).add(0.0);
                    }
                }
            }
        }
        
        public void resetGradients() {
           for (int il = 0; il < this.neuralNetwork.numberOfLayers(); il++) {
               for (int in = 0; in < this.neuralNetwork.getLayer(il).numberOfNeurons(); in++) { 
                   this.setThreshold(il,in,0.0); 
                   for (int ii = 0; ii < this.neuralNetwork.getLayer(il).getNeuron(in).numberOfInputs(); ii++) {
                       this.setWeight(il,in,ii,0.0);
                   }
               }
           }
        }
        
        public double getThreshold(int il, int in) {
            return thresholds.elementAt(il).elementAt(in).doubleValue();
        }

        public void setThreshold(int il, int in, double value) {
            thresholds.elementAt(il).setElementAt(value, in);
        }
        
        public void incrementThreshold(int il, int in, double value) {
            this.setThreshold(il,in,this.getThreshold(il,in) + value);
        }
        
        public double getWeight (int il, int in, int ii) {
            return weights.elementAt(il).elementAt(in).elementAt(ii).doubleValue();
        }
        
        public void setWeight (int il, int in, int ii, double value) {
            weights.elementAt(il).elementAt(in).setElementAt(value,ii);
        }
         
        public void incrementWeight(int il, int in, int ii, double value) {
            this.setWeight(il,in,ii,this.getWeight(il,in,ii) + value);
        }

        public double getGradientAbs() {
            double currE = 0;
            
            for (int il=1;il<neuralNetwork.numberOfLayers();il++) {
                currE += this.vectorAbs(thresholds.elementAt(il)); 
                currE += this.doubleVectorAbs(weights.elementAt(il));
            }
            return currE;            
        }
        
        private double doubleVectorAbs(Vector<Vector<Double>> doubleVector) {
            double totalX = 0;
            for (Vector<Double> vector : doubleVector) {
                totalX += Math.pow(vectorAbs(vector),2);
            }
            return Math.sqrt(totalX);
        }
        
        private double vectorAbs(Vector<Double> vector) {
            double totalX = 0;
            for (Double x : vector) totalX += Math.pow(x,2);
            return Math.sqrt(totalX);
        }        
    }
    
    private double random() {
        return randomGenerator.nextDouble();
    }
    
    private void computeGradient(Gradients gradients, Vector<Double> inputs, Vector<Double> requiredOutputs) {
       activities(inputs);
       for (int il=this.numberOfLayers()-1; il>=1; il--) {
            NeuralLayer currentLayer = this.getLayer(il);
           
            if (currentLayer.isLayerTop()) {
                for (int in=0; in<currentLayer.numberOfNeurons(); in++) {
                    Neuron currentNeuron = currentLayer.getNeuron(in);
                    gradients.setThreshold(il, in, 
                            currentNeuron.output * (1 - currentNeuron.output) * (currentNeuron.output - requiredOutputs.elementAt(in))
                            );
                }
                for (int in=0; in<currentLayer.numberOfNeurons(); in++) {
                    Neuron currentNeuron = currentLayer.getNeuron(in);
                    for (int ii=0; ii<currentNeuron.numberOfInputs(); ii++) {
                        gradients.setWeight(il,in,ii,
                                gradients.getThreshold(il,in) * currentLayer.lowerLayer().getNeuron(ii).output    
                                );
                    }
                }                
            } else { 
                for (int in=0; in<currentLayer.numberOfNeurons();in++) {
                    double aux = 0;
                    for (int ia=0; ia<currentLayer.upperLayer().numberOfNeurons(); ia++) { 
                        aux += gradients.getThreshold(il+1,ia) * 
                               currentLayer.upperLayer().getNeuron(ia).getInput(in).weight;
                    }
                    gradients.setThreshold(il,in,
                            currentLayer.getNeuron(in).output * (1 - currentLayer.getNeuron(in).output) * aux
                            );
                }
                for (int in=0; in<currentLayer.numberOfNeurons(); in++) { // for each neuron
                    Neuron currentNeuron = currentLayer.getNeuron(in);
                    for (int ii=0; ii<currentNeuron.numberOfInputs(); ii++) { // for each neuron's input
                        gradients.setWeight(il, in, ii,
                                gradients.getThreshold(il,in) * currentLayer.lowerLayer().getNeuron(ii).output
                        );    
                    } 
                }
            }            
        }
    }
   
    private void computeTotalGradient(Gradients totalGradients, Gradients partialGradients, SetOfIOPairs trainingSet) {
        totalGradients.resetGradients();        
        for (SetOfIOPairs.IOPair pair : trainingSet.pairs) {
            computeGradient (partialGradients, pair.inputs, pair.outputs);
            for (int il = this.numberOfLayers()-1; il >= 1; il--) {
                NeuralLayer currentLayer = this.getLayer(il);
                for (int in=0; in<currentLayer.numberOfNeurons(); in++) {
                    totalGradients.incrementThreshold(il,in,partialGradients.getThreshold(il,in));
                    for (int ii=0; ii<currentLayer.lowerLayer().numberOfNeurons(); ii++) { // pre vsetky vstupy
                        totalGradients.incrementWeight(il,in,ii,partialGradients.getWeight(il,in,ii));
                    }
                }
            
            }
        }
    }

    private void adaptation(SetOfIOPairs trainingSet, int maxK, double eps , double lambda , double micro) {
        double delta;
        Gradients deltaGradients = new Gradients(this);
        Gradients totalGradients = new Gradients(this);
        Gradients partialGradients = new Gradients(this);    
        for (int il = this.numberOfLayers()-1; il >= 1; il--) {
            NeuralLayer currentLayer = this.getLayer(il);
            for (int in=0; in<currentLayer.numberOfNeurons(); in++) {
                Neuron currentNeuron = currentLayer.getNeuron(in);
                currentNeuron.threshold = 2*this.random()-1;
                for (int ii = 0; ii < currentNeuron.numberOfInputs(); ii++) {
                    currentNeuron.getInput(ii).weight = 2*this.random()-1;
                }
            }
        }         
        int currK = 0;
        double currE = Double.POSITIVE_INFINITY;        
        
       // Intelligence.console.console("entering adaptation loop ... (maxK = "+maxK+")");        
        
        while ( currK < maxK && currE > eps ) {
            computeTotalGradient(totalGradients,partialGradients,trainingSet);
            for (int il = this.numberOfLayers()-1; il >= 1; il--) {
                NeuralLayer currentLayer = this.getLayer(il);
                for (int in=0; in<currentLayer.numberOfNeurons(); in++) {
                    Neuron currentNeuron = currentLayer.getNeuron(in);
                    delta = -lambda * totalGradients.getThreshold(il,in) + micro * deltaGradients.getThreshold(il,in);
                    currentNeuron.threshold += delta;
                    deltaGradients.setThreshold(il,in,delta);
                }                
                for (int in=0; in<currentLayer.numberOfNeurons(); in++) { 
                    Neuron currentNeuron = currentLayer.getNeuron(in);
                    for (int ii = 0; ii < currentNeuron.numberOfInputs(); ii++) {
                        delta = -lambda * totalGradients.getWeight(il, in, ii) + micro * deltaGradients.getWeight(il,in,ii);
                        currentNeuron.getInput(ii).weight += delta;
                        deltaGradients.setWeight(il,in,ii,delta);
                    }
                }
            }            
            currE = totalGradients.getGradientAbs(); 
            currK++;
           // if (currK%25==0) Intelligence.console.console("currK="+currK+"   currE="+currE);
        }
    }
        
    private Vector<Double> activities (Vector<Double> inputs) {
        for (int il = 0; il < this.numberOfLayers();il++) {
            for (int in = 0; in < this.getLayer(il).numberOfNeurons();in++) {
                double sum = this.getLayer(il).getNeuron(in).threshold;
                for (int ii = 0; ii < this.getLayer(il).getNeuron(in).numberOfInputs(); ii++) {
                    if (il == 0) {
                        sum+=
                        this.getLayer(il).getNeuron(in).getInput(ii).weight *
                        inputs.elementAt(in).doubleValue();
                    } else {
                        sum+=
                        this.getLayer(il).getNeuron(in).getInput(ii).weight *
                        this.getLayer(il-1).getNeuron(ii).output;
                    }
                }
                this.getLayer(il).getNeuron(in).output = this.gainFunction(sum);
            }
        }
        Vector<Double> output = new Vector<Double>();        
        for (int i=0; i < this.getLayer(this.numberOfLayers()-1).numberOfNeurons(); i++) 
            output.add(this.getLayer(this.numberOfLayers() - 1).getNeuron(i).output);
        
        return output;
    }
        
    private double gainFunction (double x) {
        return 1/(1+Math.exp(-x)); 
    }
    
    private NeuralLayer getLayer(int index) {
        return this.listLayers.elementAt(index);
    }    
} 
