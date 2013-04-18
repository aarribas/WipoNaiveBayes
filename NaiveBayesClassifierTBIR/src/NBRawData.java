import java.util.LinkedHashMap;
import java.util.Set;



/**
 * @author andresaan
 *	This class stores the counts per class
 *	and the total class and section counts
 */
public class NBRawData {

	private LinkedHashMap<String,LinkedHashMap<Integer, Integer>> featureCountTotalsPerClass;

	private LinkedHashMap<String, Integer> classTotalCounts;

	private LinkedHashMap<Character, Integer> sectionTotalCounts;

	public NBRawData() {
		featureCountTotalsPerClass = new LinkedHashMap<String,LinkedHashMap<Integer, Integer>>() ;

		classTotalCounts = new LinkedHashMap<String, Integer>();

		sectionTotalCounts = new LinkedHashMap<Character, Integer>();
	}


	/**
	 * Adds a count per class that a document belongs to.
	 * @param classes Array of classes that a document belongs to..
	 */
	public void addClassesCounts(String[] classes){

		for(String singleClass : classes){

			//if there is no entry at feature count level for this class add an empty entry
			if(featureCountTotalsPerClass.get(singleClass) == null){
				featureCountTotalsPerClass.put(singleClass, new LinkedHashMap<Integer, Integer>());
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

	/**
	 * Adds a count per feature for all classes the doc belongs to.
	 * @param featureIndex : id of the feature
	 * @param initialFeatureCount : number of times the feature has been observed
	 * @param classes : classes the document belongs to
	 */
	public void addFeatureCount(int featureIndex, int initialFeatureCount, String[] classes){
		
		int featureCount = initialFeatureCount; 
		
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
			
		}
	}


	public LinkedHashMap<String, LinkedHashMap<Integer, Integer>> getFeatureCountTotalsPerClass() {
		return featureCountTotalsPerClass;
	}


	public void setFeatureCountTotalsPerClass(
			LinkedHashMap<String, LinkedHashMap<Integer, Integer>> featureCountTotalsPerClass) {
		this.featureCountTotalsPerClass = featureCountTotalsPerClass;
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
