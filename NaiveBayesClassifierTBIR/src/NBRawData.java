import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Set;


public class NBRawData {

	private LinkedHashMap<String,LinkedHashMap<Integer, Integer>> featureCountTotalsPerClass;

	private LinkedHashMap<String,LinkedHashMap<Integer, Integer>> featureCountSquaredTotalsPerClass;

	private LinkedHashMap<String, Integer> classTotalCounts;

	private LinkedHashMap<Character, Integer> sectionTotalCounts;

	public NBRawData() {
		featureCountTotalsPerClass = new LinkedHashMap<String,LinkedHashMap<Integer, Integer>>() ;

		featureCountSquaredTotalsPerClass = new LinkedHashMap<String,LinkedHashMap<Integer, Integer>>();

		classTotalCounts = new LinkedHashMap<String, Integer>();

		sectionTotalCounts = new LinkedHashMap<Character, Integer>();
	}


	public void addClassesCounts(String[] classes){

		for(String singleClass : classes){

			//if there is no entry at feature count level for this class add an empty entry
			if(featureCountTotalsPerClass.get(singleClass) == null){
				featureCountTotalsPerClass.put(singleClass, new LinkedHashMap<Integer, Integer>());
			}

			if(featureCountSquaredTotalsPerClass.get(singleClass) == null){
				featureCountSquaredTotalsPerClass.put(singleClass, new LinkedHashMap<Integer, Integer>());
			}

			//same for the class counts (initialise to 1)
			if(classTotalCounts.get(singleClass) == null){
				classTotalCounts.put(singleClass, 1);
			}
			else{
				//just add the count
				classTotalCounts.put(singleClass, classTotalCounts.get(singleClass) + 1);
			}

		}

	}

	public void addSectionsCounts(Set<Character> sections){

		for(Character singleSection : sections){
			//same for the class counts (initialise to 1)
			if(sectionTotalCounts.get(singleSection) == null){
				sectionTotalCounts.put(singleSection, 1);
			}
			else{
				//just add the count
				sectionTotalCounts.put(singleSection, sectionTotalCounts.get(singleSection) + 1);
			}
		}

	}

	public void addFeatureCount(int featureIndex, int initialFeatureCount, String[] classes){
		
		//add one for the laplace filtering
		int featureCount = initialFeatureCount+1; 
		
		//add feature count at class level
		for(String singleClass : classes){
			//if there is no entry for the feature add the entry initialised to featureCount
			if(featureCountTotalsPerClass.get(singleClass).get(featureIndex) == null){
				featureCountTotalsPerClass.get(singleClass).put(featureIndex, featureCount);
			}
			else{
				//otherwise add feature count to the entry (we store the accumulator)
				featureCountTotalsPerClass.get(singleClass).put(featureIndex, featureCountTotalsPerClass.get(singleClass).get(featureIndex) + featureCount);
			}
			
			//if there is no entry for the feature add the entry initialised to featureCount squared
			if(featureCountSquaredTotalsPerClass.get(singleClass).get(featureIndex) == null){
				featureCountSquaredTotalsPerClass.get(singleClass).put(featureIndex, featureCount*featureCount);
			}
			else{
				//otherwise add feature squared to the total of squared counts
				featureCountSquaredTotalsPerClass.get(singleClass).put(featureIndex, featureCountSquaredTotalsPerClass.get(singleClass).get(featureIndex) + featureCount*featureCount);
			}
		}
	}


	public LinkedHashMap<String, LinkedHashMap<Integer, Integer>> getFeatureCountTotalsPerClass() {
		return featureCountTotalsPerClass;
	}


	public void setFeatureCountTotalsPerClass(
			LinkedHashMap<String, LinkedHashMap<Integer, Integer>> featureCountTotalsPerClass) {
		this.featureCountTotalsPerClass = featureCountTotalsPerClass;
	}


	public LinkedHashMap<String, LinkedHashMap<Integer, Integer>> getFeatureCountSquaredTotalsPerClass() {
		return featureCountSquaredTotalsPerClass;
	}


	public void setFeatureCountSquaredTotalsPerClass(
			LinkedHashMap<String, LinkedHashMap<Integer, Integer>> featureCountSquaredTotalsPerClass) {
		this.featureCountSquaredTotalsPerClass = featureCountSquaredTotalsPerClass;
	}


	public LinkedHashMap<String, Integer> getClassTotalCounts() {
		return classTotalCounts;
	}


	public void setClassTotalCounts(LinkedHashMap<String, Integer> classTotalCounts) {
		this.classTotalCounts = classTotalCounts;
	}


	public LinkedHashMap<Character, Integer> getSectionTotalCounts() {
		return sectionTotalCounts;
	}


	public void setSectionTotalCounts(
			LinkedHashMap<Character, Integer> sectionTotalCounts) {
		this.sectionTotalCounts = sectionTotalCounts;
	}
	
}
