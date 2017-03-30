package Connect4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Transpose {

	static int column;
	public static void main(String args[]) throws FileNotFoundException {
		PrintWriter out = new PrintWriter("transpose.txt");
		String fileName = "data.txt";

		try (Scanner scanner = new Scanner(new File(fileName))) {

			while (scanner.hasNext()){
				out.write(processLine(scanner.nextLine()) + "\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		out.close();
		System.exit(0);
	}
	public static String processLine(String line){
		
		if(!(line.contains("row")|| line.contains("col"))){ // empty line
			return line;
		}
		
		int a = line.indexOf("row") + 3;
		int b = line.indexOf("col") + 3;
		int c, d;
		int comment = line.indexOf("//") + 3;
		
		if(line.contains("Move")){
			c = line.indexOf(",",a);
			d = line.indexOf(",",b);
		} else{
			c = line.indexOf("]",a);
			d = line.indexOf("]",b);
		}
		
		String rowMove = line.substring(a,c);
		String colMove= line.substring(b,d);
		if(line.contains("Move")){
			line = line.substring(0, line.indexOf("row") + 3) + colMove + ", col" + rowMove + ", player, type)); // " + line.substring(comment, line.length());
			line = line.replace("up", "templ");
			line = line.replace("down", "tempr");
			
			line = line.replace("left", "up");
			line = line.replace("right", "down");
			
			line = line.replace("templ", "left");
			line = line.replace("tempr", "right");
			return line;
		}
		
		line = line.replace("up", "tempu");
		line = line.replace("down", "tempd");
		
		line = line.replace("left", "up");
		line = line.replace("right", "down");
		
		line = line.replace("tempd", "right");
		line = line.replace("tempu", "left");
		
		String A = line.substring(0, line.indexOf("board"));
		String B = line.substring(d + 1, line.length());
		
		line = A + "board[row" + colMove + "][col" + rowMove + "]" + B;// + rightLeft + " " + upDown;
		if((line.contains("up") || line.contains("down")) && (line.contains("right") || line.contains("left"))){
			if(line.contains("up") && line.indexOf("up") < line.indexOf("// " + 5)) {
				return line;
			} else {
				if(line.indexOf("down") < line.indexOf("// " + 5)) {
					return line;
				}
			}
			
			return reverse(line);
		} else {
			return line;
		}
			
	}
	private static String reverse(String input) {
		String line = input;
		String upDown = "";
		String leftRight = "";
		
		if(line.contains("up")){
			upDown = line.substring(line.indexOf("up"), line.length());
			leftRight = line.substring(line.indexOf("// ") + 3, line.indexOf("up"));
		} else if(line.contains("down")){
			upDown = line.substring(line.indexOf("down"), line.length());
			leftRight = line.substring(line.indexOf("// ") + 3, line.indexOf("down"));
		}
		return line.substring(0, line.indexOf("// ") + 3) + upDown  + " " + leftRight;
	}
	
}
