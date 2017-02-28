package Connect4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class VerticalMirrorSymmetry{

	static int column = 0;
	public static void main(String args[]) throws FileNotFoundException {
		PrintWriter out = new PrintWriter("vertical.txt");
		String fileName = "data.txt";

		try (Scanner scanner = new Scanner(new File(fileName))) {

			while (scanner.hasNext()){
				out.write(preProcessLine(scanner.nextLine()) + "\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		out.close();
	}
	public static String preProcessLine(String line){
		if(line.contains("col == ")){
			int index = line.indexOf("col == ") + "col == ".length();
			column = 7 - Integer.valueOf(line.substring(index, index + 1));
			line = line.substring(0, index) + column + line.substring(index + 1, line.length());
			if(count(line, "column") < 2) { // no comments in line
				line = line + " // column " + column;
			} else {
				index = line.lastIndexOf("column ") + "column ".length();
				line = line.substring(0, index) + column + line.substring(index + 1, line.length());
			} // end else
		} // end if
		
		if(line.contains("row == ") && line.contains("column")){
			int index = line.lastIndexOf("column ") + "column ".length();
			line = line.substring(0, index) + column;
		}  // end if
			
		if(line.contains("left") || line.contains("right")){
			line = line.replace("right", "temp");
			line = line.replace("left", "right");
			line = line.replace("temp", "left");
		} // end if
		return processLine(line);
	} // end method preProcessLine
	
	private static int count(String line, String str) {
		int count = 0;
		while(line.indexOf(str) > 0){
			line = line.substring(line.indexOf(str) + str.length());
			++count;
		} // end while
		return count;
	} // end method cound
	
	private static String processLine(String line) {
		if(line.contains("right")){
			line = line.replace("col -", "col +");
		} else if(line.contains("left")){
			line = line.replace("col +", "col -");
		} 
		return line;
	}
}

