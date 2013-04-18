import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aarribas.io.TextFileReader;
import org.aarribas.io.TextFileSaver;


/**
 * @author andresaan
 * This class is in charge of processing the test file and producing the predictions.
 */
public class NBPredictor {

	private NBRawData rawData;
	
	double totalCounts;
	
	double numberOfDifferentFeatures;
	
	private LinkedHashMap<String,Double> totalFeatureCountsPerClass;

	LinkedHashMap<Character, Double> sectionProbs;

	LinkedHashMap<String, Double> classProbs;

	private List<Map<String, Double>> finalDocumentPredictions;

	private List<String> predictionResults;

	private TextFileReader textFileReader = new TextFileReader();

	private String testsFilename;

	private String resultsFilename;

	/**
	 * Constructor. Expects NBRawData which shall contain the raw counts to base the predictions on
	 * @param rawData
	 */
	public NBPredictor(NBRawData rawData){
		this.rawData = rawData;

		System.out.println("Computing total counts per class, and overall.");
		computeTotalCounts();

		System.out.println("Computing log probabilities per class and section.");
		computeClassAndSectionLogProbabilities();


	}

	/**
	 * Computes absolute total number of counts and total number of different counts.
	 */
	private void computeTotalCounts(){

		LinkedHashMap<String, Integer> classTotalCounts = rawData.getClassTotalCounts(); 

		totalFeatureCountsPerClass = new LinkedHashMap<String,Double>();

		//compute total counts per class
		for(String singleClass: classTotalCounts.keySet()){
			double totalFeatureCountsForOneClass = 0;
			for(Integer feature: rawData.getFeatureCountTotalsPerClass().get(singleClass).keySet()){
				
				totalFeatureCountsForOneClass = totalFeatureCountsForOneClass + (double)rawData.getFeatureCountTotalsPerClass().get(singleClass).get(feature);
			}
			
			totalFeatureCountsPerClass.put(singleClass,totalFeatureCountsForOneClass);

		}
		
		
		//compute the complete total counts
		totalCounts = 0;
		for(String singleClass: classTotalCounts.keySet()){
			totalCounts = totalCounts + rawData.getClassTotalCounts().get(singleClass);
			
		}
		
		Set<Integer> featureIds  = new HashSet<Integer>();
		for(String sClass: classTotalCounts.keySet()){
		
			featureIds.addAll(rawData.getFeatureCountTotalsPerClass().get(sClass).keySet());
		}
		numberOfDifferentFeatures = featureIds.size();
	}

	/**
	 * Pre-computes the log probabilities per section and per class.  
	 */
	private void computeClassAndSectionLogProbabilities(){

		//save the log probability per class
		LinkedHashMap<String, Integer> classTotalCounts = rawData.getClassTotalCounts();
		classProbs = new LinkedHashMap<String, Double>();

		for(String singleClass: classTotalCounts.keySet()){

			//the class probability is only computed given a section (the section of the class)
			double probability =  (double)classTotalCounts.get(singleClass)
					/(double)rawData.getSectionTotalCounts().get(singleClass.charAt(0));
			classProbs.put(singleClass, Math.log(probability)); 
		}


		//save the probability per section
		LinkedHashMap<Character, Integer> sectionTotalCounts = rawData.getSectionTotalCounts(); 
		sectionProbs = new LinkedHashMap<Character, Double>();
		
		//first compute the total number of observations
		Double Total = 0d;
		for(Character singleSection: sectionTotalCounts.keySet()){

			Total = Total + sectionTotalCounts.get(singleSection);
		}

		//save the probability per section as the number of times we observed the section/number of observations
		for(Character singleSection: sectionTotalCounts.keySet()){
			double probability = (double)sectionTotalCounts.get(singleSection)/Total;
			sectionProbs.put(singleSection, Math.log(probability)); 

		}

	}

	/**
	 * Runs the prediction for each document vector in the test file and saves the prediction to the results file.
	 * @param testsFilename
	 * @param resultsFilename
	 */
	public void predictAndSave(String testsFilename, String resultsFilename){

		//prepare structures to save the predictions per document
		finalDocumentPredictions = new ArrayList<Map<String, Double>>();

		//save filenames
		this.testsFilename = testsFilename;
		this.resultsFilename = resultsFilename;

		deleteResultsFile();

		readTestFile();

		//compute the final probabilities
		System.out.println("Computing probabilities per class per text file entry vectors.");
		calculateFinalPredictionsAndSave();
	}

	private void deleteResultsFile(){
		File file = new File(resultsFilename);
		file.delete();
		System.out.println("Old results file has been deleted");
	}

	private void readTestFile(){
		//read the test file content
		System.out.println("Reading test file.");
		try {
			textFileReader.readTextFile(testsFilename);
		} catch (FileNotFoundException e) {
			System.err.println("Could not load the file with the counts.");
			System.exit(1);
		}
	}

	/**
	 * Saves a 3 class prediction per document to the results file.
	 *
	 */
	private void savePredictions(){
		//sort the predictions in descending order so that the final 3 predictions are the most probable ones
		System.out.println("Sorting predictions.");
		sortFinalDocumentPredictions();

		//populate prediction results
		System.out.println("Generating results (3 predicted classes per entry vector).");
		populatePredictionResults();

		//save it with TextFileSaver
		System.out.println("Saving results to file.");
		TextFileSaver saver = new TextFileSaver();

		try {
			saver.saveTextFile(resultsFilename, predictionResults, TextFileSaver.SaveMode.APPEND);
		} catch (IOException e) {
			System.err.println("Could not write to file:" + resultsFilename);
			e.printStackTrace();
		}

	}

	/**
	 * Sorts a set of predictions (class and computed probability) in ascending order
	 */
	private void sortFinalDocumentPredictions(){
		//sort each map in ascending order
		for(int documentIndex = 0; documentIndex < finalDocumentPredictions.size(); documentIndex++){
			finalDocumentPredictions.set(documentIndex, sortByComparator(finalDocumentPredictions.get(documentIndex)));
		}
	}

	private void populatePredictionResults(){
		//always created from scratch
		predictionResults = new LinkedList<String>();
		
		//for all predictions per doc save the three most probable classes
		for(int documentIndex = 0; documentIndex < finalDocumentPredictions.size(); documentIndex++){
			List<String> classes = new ArrayList<String>(finalDocumentPredictions.get(documentIndex).keySet());
			predictionResults.add(new String(classes.get(classes.size()-1) + " " + classes.get(classes.size()-2) + " " + classes.get(classes.size()-3)));
		}
	}

	public List<String> getPredictionResults() {
		return predictionResults;
	}

	public void setPredictionResults(List<String> predictionResults) {
		this.predictionResults = predictionResults;
	}


	public List<Map<String,Double>> getFinalPredictions(){
		return finalDocumentPredictions;
	}

	/**
	 * This method will process each line in the test file
	 * It decomposes the line into expected class and the vector counts
	 * Then based on the counts, will compute the probability for a given class given the observation.
	 * The section prediction is used, as the class prediction implies a section assumption.
	 * P(A|W) = Prod(P(Wi|C)) * P(C|S) * P(S) / P(W)
	 * P(W) is the same for all classes hence, we can ignore it when applying Naive Bayes.
	 */
	private void calculateFinalPredictionsAndSave(){
		
		//process each line of the text file
		for(int lineIndex = 0; lineIndex < textFileReader.getTextFileLines().size(); lineIndex++){

			//every 1000 lines we save the results so far to the results file
			if(lineIndex !=0 && lineIndex % 1000 == 0){	

				System.out.println("--> Saving results so far:");

				//save predictions so far
				savePredictions();

				//clean up old finalDocumentPredictions (saves memory)
				finalDocumentPredictions = new ArrayList<Map<String, Double>>();
			}

			//get the line
			String line = textFileReader.getTextFileLines().get(lineIndex);
			
			//inform of the status
			System.out.println("Predicting line " + lineIndex + " of " + (textFileReader.getTextFileLines().size()-1) );

			//prepare to store the predictions per class for this documentVector
			Map <String,Double> predictions = new  LinkedHashMap<String, Double>();

			String[] vectorEntries = line.split(" ");

			//remove first element which is precisely the class to predict
			vectorEntries = Arrays.copyOfRange(vectorEntries, 1, vectorEntries.length);

			for(String singleClass: classProbs.keySet()){

				//get class prediction (log probability) per vector entry assuming it is the right one (given a section)
				double classPredict = classProbs.get(singleClass) + calculateLogProbGivenClassAndVector(singleClass, vectorEntries);

				//class prediction must of course be corrected with the section prediction
				classPredict  =  sectionProbs.get(singleClass.charAt(0)) + classPredict;

				//save the log probability 
				predictions.put(singleClass, classPredict);

			}
			finalDocumentPredictions.add(predictions);	
		}

		//save final predictions
		savePredictions();

	}

	/**
	 *  Compute given the observed vectorEntries/count and a given class, the probabiblity for that class.
	 * @param singleClass
	 * @param vectorEntries
	 * @return
	 */
	private double calculateLogProbGivenClassAndVector(String singleClass, String[] vectorEntries){

		double prob = 0d;

		for(String vectorEntry : vectorEntries){

			String[] featureEntry = vectorEntry.split(":");		
			
			
			if(rawData.getFeatureCountTotalsPerClass().get(singleClass).get(Integer.valueOf(featureEntry[0])) != null){
				//if the given feature was observed at training time for this class
				//we take the multinomial approach
		
				double v = Double.valueOf(featureEntry[1]);
				double temp = rawData.getFeatureCountTotalsPerClass().get(singleClass).get(Integer.valueOf(featureEntry[0]))+1d;
				double total = totalFeatureCountsPerClass.get(singleClass) + rawData.getFeatureCountTotalsPerClass().get(singleClass).keySet().size();
				
				prob = prob + Math.log((double)temp/(total))*v;
			}
			else{
				
				//if feature was not observed for the current class: consider a constant probability 
				//of observing a previously non-observed feature
				
				double v = Double.valueOf(featureEntry[1]);
				prob = prob + Math.log(1d/(totalCounts + numberOfDifferentFeatures))*v;
				
			}
		}

		return prob;

	}
	

	private static Map sortByComparator(Map unsortMap) {

		List list = new LinkedList(unsortMap.entrySet());

		// sort list based on comparator
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue())
						.compareTo(((Map.Entry) (o2)).getValue());
			}
		});


		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

}
