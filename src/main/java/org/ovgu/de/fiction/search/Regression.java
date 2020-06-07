package org.ovgu.de.fiction.search;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import org.ovgu.de.fiction.utils.FRGeneralUtils;

import weka.classifiers.functions.LinearRegression;

public class Regression{
	public void runLR() throws Exception{
		//Load Data set
		String FEATURE_ARFF_FILE = FRGeneralUtils.getPropertyVal("file.results.arff");
		DataSource source = new DataSource(FEATURE_ARFF_FILE);
		Instances dataset = source.getDataSet();
		//set class index to the last attribute
		dataset.setClassIndex(dataset.numAttributes()-1);
		
		//Build model
		LinearRegression model = new LinearRegression();
		model.buildClassifier(dataset);
		//output model
		System.out.println("LR FORMULA : "+model);	
		
		/* Now Predicting the cost 
		Instance myHouse = dataset.lastInstance();
		double price = model.classifyInstance(myHouse);
		System.out.println("-------------------------");	
		System.out.println("PRECTING THE PRICE : "+price);	
		*/
	}
}
