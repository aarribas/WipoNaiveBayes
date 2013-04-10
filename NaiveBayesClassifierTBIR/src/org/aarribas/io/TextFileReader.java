package org.aarribas.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This class is used to read a text file.
 * Normally this class should be extended or used as a component to create custom loaders.
 * The ultimate management of the exceptions is also expected to be done by the caller.
 * @author andresaan
 *
 */
public class TextFileReader {
	
	protected List<String> textFileLines = new ArrayList<String>();
	Scanner inputStream = null;
	
	/**
	 * Reads the text file fileName line by line and saves it as a list of String.
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public void readTextFile(String fileName) throws FileNotFoundException
	{ 
		try {
			//read file
			inputStream = new Scanner(new File(fileName));
			
		} catch (FileNotFoundException e) {
			System.err.println("Error: The file " + fileName + " was not found.");
			e.printStackTrace();
			throw e;
		}
		while(inputStream.hasNextLine())
		{
			//save each line
			textFileLines.add(inputStream.nextLine());
		}
		
		inputStream.close();
	}
	
	public List<String> getTextFileLines()
	{
		return textFileLines;
	}

}
