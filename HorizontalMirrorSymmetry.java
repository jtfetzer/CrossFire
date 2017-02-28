package Connect4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class HorizontalMirrorSymmetry{

	static int column;
	public static void main(String args[]) throws FileNotFoundException {
		PrintWriter out = new PrintWriter("horizontal.txt");
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
			column = Integer.parseInt(line.substring(index, index + 1));
		}
		if(line.contains("row == ")){
			if(count(line, "row") > 2) { // used if(move.row == 3 || move.row == 4).
				int low =line.indexOf("row == ") + 7;
				int lowRow = Integer.parseInt(line.substring(low, line.indexOf(" ||", low)));
				int high =line.lastIndexOf("row == ") + 7;
				int highRow = Integer.parseInt(line.substring(high, line.indexOf(")", high)));
				
				line = line.substring(0,line.indexOf("{") + 1) + " // row " + lowRow +"-" + highRow + " column " + column;
				
			} else {
				int index = line.indexOf("row == ") + "row == ".length();
				int row = 7 - Integer.valueOf(line.substring(index, index + 1));
				line = line.substring(0, index) + row + line.substring(index + 1, line.length());
				if(count(line, "row") < 2) { // no comments in line
					line = line + " // row " + row + " column " + column;
				} else {
					index = line.lastIndexOf("row ") + "row ".length();
					line = line.substring(0, index) + row + line.substring(index + 1, line.length());
				} // end else
			} // end else
		} // end if
		if(line.contains("up") || line.contains("down")){
			line = line.replace("up", "temp");
			line = line.replace("down", "up");
			line = line.replace("temp", "down");
		}
		return processLine(line);
	}
	
	private static int count(String line, String str) {
		int count = 0;
		while(line.indexOf(str) > 0){
			line = line.substring(line.indexOf(str) + str.length());
			++count;
		}
		return count;
	}
	
	private static String processLine(String line) {
		if(line.contains("down")){
			line = line.replace("row -", "row +");
		} else if(line.contains("up")){
			line = line.replace("row +", "row -");
		} 
		return line;
	}
}

