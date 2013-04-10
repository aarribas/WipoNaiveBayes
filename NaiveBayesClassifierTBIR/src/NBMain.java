import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

import org.aarribas.io.TextFileSaver;


public class NBMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length < 2){
			System.out.println("Not enough arguments provided\n" +
					"First argument shall be the train file\n" +
					"Second argument shall be the test file\n" + 
					"Third argument shall be the file to save the results to.\n");
			System.exit(0);
		}
		
		
		NBFileProcessor fileProcessor = new NBFileProcessor();
		fileProcessor.processFile(args[0]);
		
		//produce the probabilities according from the counts
		//NBProbabilities probs = new NBProbabilities(fileProcessor.getNBRawData());
		
		//create a new predictor instance based on the given probabilities
		NBPredictor predictor = new NBPredictor(fileProcessor.getNBRawData());
		
		//run the predictions on the test file
		predictor.predictAndSave(args[1], args[2]);
	
	}

}
