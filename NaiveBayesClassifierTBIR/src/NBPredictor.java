import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.aarribas.io.TextFileReader;
import org.aarribas.io.TextFileSaver;


public class NBPredictor {

	private NBRawData rawData;
	
	double totalCounts;
	
	private LinkedHashMap<String,Double> totalFeatureCountsPerClass;

	private LinkedHashMap<String,LinkedHashMap<Integer, Double>> meanPerFeatureAndClass;

	private LinkedHashMap<String,LinkedHashMap<Integer, Double>> variancePerFeatureAndClass;

	LinkedHashMap<Character, Double> sectionProbs;

	LinkedHashMap<String, Double> classProbs;

	private List<Map<String, Double>> finalDocumentPredictions;

	private List<String> predictionResults;

	private TextFileReader textFileReader = new TextFileReader();

	private String testsFilename;

	private String resultsFilename;

	public NBPredictor(NBRawData rawData){
		this.rawData = rawData;

		//we replace all probabilities by logarithms in order to simplify what comes afterwards
		System.out.println("Computing means and variances per feature for each class.");
		computeMeansAndVariances();

		System.out.println("Computing log probabilities for each class and each section.");
		computeClassAndSectionLogProbabilities();


	}

	private void computeMeansAndVariances(){

		LinkedHashMap<String, Integer> classTotalCounts = rawData.getClassTotalCounts(); 

		meanPerFeatureAndClass = new LinkedHashMap<String,LinkedHashMap<Integer, Double>>();

		variancePerFeatureAndClass = new LinkedHashMap<String,LinkedHashMap<Integer, Double>>();
		
		totalFeatureCountsPerClass = new LinkedHashMap<String,Double>();

		//compute means
		for(String singleClass: classTotalCounts.keySet()){
			LinkedHashMap<Integer, Double> meanPerFeatureForOneClass = new LinkedHashMap<Integer, Double>();
			double totalFeatureCountsForOneClass = 0;
			for(Integer feature: rawData.getFeatureCountTotalsPerClass().get(singleClass).keySet()){

				//mean as the accumulated value for such feature and the number of times we observed the class
				double mean = (double)rawData.getFeatureCountTotalsPerClass().get(singleClass).get(feature) /
						(double)rawData.getClassTotalCounts().get(singleClass);

				meanPerFeatureForOneClass.put(feature, mean);
				totalFeatureCountsForOneClass = totalFeatureCountsForOneClass + (double)rawData.getFeatureCountTotalsPerClass().get(singleClass).get(feature);
			}
			
			meanPerFeatureAndClass.put(singleClass, meanPerFeatureForOneClass);
			totalFeatureCountsPerClass.put(singleClass,totalFeatureCountsForOneClass);

		}

		//compute variances
		for(String singleClass: classTotalCounts.keySet()){
			LinkedHashMap<Integer, Double> variancePerFeatureForOneClass = new LinkedHashMap<Integer, Double>();
			for(Integer feature: rawData.getFeatureCountTotalsPerClass().get(singleClass).keySet()){
				
				//the mean of the squared counts total
				double variance = (double)rawData.getFeatureCountSquaredTotalsPerClass().get(singleClass).get(feature) /
						(double)rawData.getClassTotalCounts().get(singleClass);
				
				//minus the square of the mean
				variance = variance - meanPerFeatureAndClass.get(singleClass).get(feature)*meanPerFeatureAndClass.get(singleClass).get(feature);
				
				variancePerFeatureForOneClass.put(feature, variance);
			}
			variancePerFeatureAndClass.put(singleClass, variancePerFeatureForOneClass);
		}
		
		totalCounts = 0;
		for(String singleClass: classTotalCounts.keySet()){
			totalCounts = totalCounts + rawData.getClassTotalCounts().get(singleClass);
			
		}
	}

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

	private void sortFinalDocumentPredictions(){
		//sort each map
		for(int documentIndex = 0; documentIndex < finalDocumentPredictions.size(); documentIndex++){
			finalDocumentPredictions.set(documentIndex, sortByComparator(finalDocumentPredictions.get(documentIndex)));
		}
	}

	private void populatePredictionResults(){
		//always created from scratch
		predictionResults = new LinkedList<String>();

		for(int documentIndex = 0; documentIndex < finalDocumentPredictions.size(); documentIndex++){
			List<String> classes = new ArrayList<String>(finalDocumentPredictions.get(documentIndex).keySet());
//			System.out.println(finalDocumentPredictions.get(documentIndex).get("a01b"));
//			System.out.println(finalDocumentPredictions.get(documentIndex).get(classes.get(classes.size()-1) ));
//			System.out.println(finalDocumentPredictions.get(documentIndex).get(classes.get(classes.size()-2) ));
//			System.out.println(finalDocumentPredictions.get(documentIndex).get(classes.get(classes.size()-3) ));
//			System.out.println(Arrays.toString(classes.toArray()));
//			Scanner scan = new Scanner(System.in);
//			scan.nextLine();
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

	private void calculateFinalPredictionsAndSave(){

		for(int lineIndex = 0; lineIndex < textFileReader.getTextFileLines().size(); lineIndex++){

			//every 1000 lines we save the results so far to the results file
			if(lineIndex !=0 && lineIndex % 1000 == 0){	

				System.out.println("Saving partial results.");

				//save predictions so far
				savePredictions();

				//clean up old finalDocumentPredictions (saves memory)
				finalDocumentPredictions = new ArrayList<Map<String, Double>>();
			}

			String line = textFileReader.getTextFileLines().get(lineIndex);

			System.out.println("Predicting line " + lineIndex + " of " + textFileReader.getTextFileLines().size() );

			//prepare to store the predictions per class for this documentVector
			Map <String,Double> predictions = new  LinkedHashMap<String, Double>();

			String[] vectorEntries = line.split(" ");

			//remove first element which normally contains a class
			vectorEntries = Arrays.copyOfRange(vectorEntries, 1, vectorEntries.length);

			for(String singleClass: classProbs.keySet()){

				//get class prediction (log probability) per vector entry assuming it is the right one (given a section)
				double classPredict = classProbs.get(singleClass) + calculateLogProbGivenClassAndVector(singleClass, vectorEntries);

				//class prediction must of course be corrected with the section prediction
				classPredict  =  sectionProbs.get(singleClass.charAt(0)) + classPredict;

				//save the log probability 
				predictions.put(singleClass, classPredict);

			}
//			System.out.println(predictions);
//			Scanner scan = new Scanner(System.in);
//			scan.nextLine();
			finalDocumentPredictions.add(predictions);	
		}

		//save final predictions
		savePredictions();

	}

	private double calculateLogProbGivenClassAndVector(String singleClass, String[] vectorEntries){

		double prob = 0d;

		for(String vectorEntry : vectorEntries){

			String[] featureEntry = vectorEntry.split(":");		

			if(meanPerFeatureAndClass.get(singleClass).get(Integer.valueOf(featureEntry[0])) != null){
				
				double v = Double.valueOf(featureEntry[1]);
				
				double temp = rawData.getFeatureCountTotalsPerClass().get(singleClass).get(Integer.valueOf(featureEntry[0]));
				double total = totalFeatureCountsPerClass.get(singleClass);
				
				prob = prob + Math.log((double)temp/(total))*v;
				
				//remove mean and variance
				//make sure I use the right final formula see multinomial in the slides. //so I have clean up to do
				//clean up code - add instructions
			}
			else{
				
				//laplace filtering approach - act as if we had observed once this feature
				double v = Double.valueOf(featureEntry[1]);
//				double mean = 1d/rawData.getClassTotalCounts().get(singleClass);
//				double variance = 1d/rawData.getClassTotalCounts().get(singleClass) - mean*mean;
//				double normalProb = -0.5*Math.log(2*Math.PI*variance) - ((v-mean)*(v-mean)/(2*variance));
				
//				double total = 0;
////				
//				for(Double mean : meanPerFeatureAndClass.get(singleClass).values()){
//					total = total  + mean * rawData.getClassTotalCounts().get(singleClass) ;
//				}
//				double temp = rawData.getFeatureCountTotalsPerClass().get(singleClass).get(Integer.valueOf(featureEntry[0]));
				double total = totalFeatureCountsPerClass.get(singleClass)*rawData.getClassTotalCounts().keySet().size();
				
//				total = 0;
//				for(String sc : rawData.getClassTotalCounts().keySet()){
//					total = total + totalFeatureCountsPerClass.get(singleClass);
//				}
				
				prob = prob + Math.log(1d/(totalCounts+1))*v;
				
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
