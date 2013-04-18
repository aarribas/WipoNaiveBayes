import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import org.aarribas.io.TextFileReader;


/**
 * @author andresaan
 * NBFileProcessor reads the train file with name filename and calls the methods in rawData to update the counts.
 */
public class NBFileProcessor {

	private NBRawData rawData = new NBRawData();

	TextFileReader textFileReader = new TextFileReader();

	/**
	 * Reads the file and processes each line assuming one line represents the data vector for a document
	 * (Classes separated by semi colon, then word counts given as id:count and separated by blank space)
	 * @param filename : name of train file
	 */
	public void processFile(String filename){

		//try to read the file
		System.out.println("Reading train file.");
		try {
			textFileReader.readTextFile(filename);
		} catch (FileNotFoundException e) {
			System.err.println("Could not load the file with the counts.");
			System.exit(1);
		}

		//process the content
		System.out.println("Producing counts per vector from the train file.");
		for(int visitedLine = 0; visitedLine < textFileReader.getTextFileLines().size(); visitedLine++){
		
			String[] documentVectorData = textFileReader.getTextFileLines().get(visitedLine).split(" ");

			processDocumentVectorData(documentVectorData);
		}

	}

	/**
	 * This method processes one vector of counts for a document.
	 * @param documentVectorData
	 */
	private void processDocumentVectorData(String[] documentVectorData){

		//first extract the classes and update the counts
		String[] classes = documentVectorData[0].split(",");
		rawData.addClassesCounts(classes);

		//extract the sections and update the counts
		Set<Character> sectionIds = new HashSet<Character>();
		for(String singleClass: classes){

			sectionIds.add(singleClass.charAt(0));
		}
		
		rawData.addSectionsCounts(sectionIds);

		//then extract the features and their counts and save those in rawData
		for(int vectorEntryIndex = 0; vectorEntryIndex<documentVectorData.length; vectorEntryIndex++ ){

			if(vectorEntryIndex != 0){

				String[] featureInfo = documentVectorData[vectorEntryIndex].split(":");

				rawData.addFeatureCount(Integer.valueOf(featureInfo[0]), //feature index
						Integer.valueOf(featureInfo[1]), //feature count
						classes); //classes to add the count to 
			}

		}
	}

	public NBRawData getNBRawData(){
		return rawData;
	}
}
