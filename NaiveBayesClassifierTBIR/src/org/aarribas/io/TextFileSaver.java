package org.aarribas.io;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * The TextFileSaver simply saves a list of strings to a file.
 * @author andresaan
 *
 */
public class TextFileSaver {

	protected PrintWriter outputStream = null;
	public enum SaveMode{
		NEW,
		APPEND
	}

	/**
	 * The following method saves to a given file the given text.
	 * Note that the method replaces completely the file if it already exits.
	 * 
	 * @param outputFileName The file to save the text too.
	 * @param textLines ArrayList or text lines to save.
	 * @throws IOException 
	 */
	public void saveTextFile(String outputFileName, List<String> textLines, SaveMode saveMode) throws IOException
	{
		if(saveMode == SaveMode.APPEND){
			try {
				//we open the file to overwrite
				outputStream = new PrintWriter(new FileWriter(outputFileName, true));
			} catch(IOException e){
				System.err.println("Error opening the file " + outputFileName + " for append.");
				e.printStackTrace();
				throw e;
			}
		}
		else{
			try {
				//we open the file to overwrite
				outputStream = new PrintWriter(outputFileName);
			} catch (FileNotFoundException e) {
				System.err.println("The file " + outputFileName + " could not be found upon saving.");
				e.printStackTrace();
				throw e;
			} 
		}

		//visit all lines to be saved
		for(int visitedLine = 0 ; visitedLine < textLines.size(); visitedLine++)
		{
			//write one line at a time.
			outputStream.println(textLines.get(visitedLine));
		}
		outputStream.close();

	}
}
