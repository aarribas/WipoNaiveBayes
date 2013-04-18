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
		
		//process the file - produces raw counts
		NBFileProcessor fileProcessor = new NBFileProcessor();
		fileProcessor.processFile(args[0]);
		
		//predict the classes given the counts and save to file
		NBPredictor predictor = new NBPredictor(fileProcessor.getNBRawData());
		predictor.predictAndSave(args[1], args[2]);
	
	}

}
