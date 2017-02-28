package Connect4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Expand {

		static int column;
		public static void main(String args[]) throws FileNotFoundException {
			PrintWriter out = new PrintWriter("expand.txt");
			String fileName = "data.txt";
			String fileContent = "";
			String line = "";
			String previousOperator = "";
			
			try (Scanner scanner = new Scanner(new File(fileName))) {
				String A, B, C, operator, newString;
				int a,b,c;
				
				while (scanner.hasNext()){
					line = scanner.nextLine();
					if(line.equals("\n")){
						continue;
					}
					
					A = "";
					B = "";
					C = "";
					operator = "";
					newString = "";
					
					a = line.indexOf("b");
					b = line.indexOf("==") + 3;
					if(line.contains("&&")){
						c = line.indexOf("&&");
						operator = "&&";
					} else if(line.contains("||")){
						c = line.indexOf("||");
						operator = "||";
					} else {
						c = line.indexOf("){");
					}
					if( a > 0){
						A = line.substring(0, a);
					}
					if(b > 0){
						B = line.substring(a, b);
					}

					C = line.substring(c+2, line.length()) + "\n";
					
					if(!A.trim().equals("")){
						if(operator.equals("&&")){
							newString += A + "(" + B + "0 ||" + C;
						} else {
							newString +=" " + A + B + "0 ||" + C;
						}
						if(operator.equals("&&")) {
							newString += "    " + B + "i)" + operator + C;
						} else {
							newString += "    " + B + "i " + operator + C;
						}
					} else {
						if(operator.equals("")){
							if(previousOperator.equals("||")){
								newString += "    " + B + "0 ||" + C + "    " +B +"i)){" + C;
							} else {
								newString += "   (" + B + "0 ||" + C + "    " +B +"i)){" + C;
							}
						} else {
							if(operator.equals("&&")) {
								if(previousOperator.equals("&&")){
									newString += "   (" + B + "0 ||" + C;
									newString += "    " + B + "i)" + operator + C;
								} else {
									newString += "    " + B + "0 ||" + C;
									newString += "    " + B + "i)" + operator + C;
								}
							} else {
								if(previousOperator.equals("&&")){
									newString += "   (" + B + "0 ||" + C;
									newString += "    " + B + "i " + operator + C;
								} else {
									newString += "    " + B + "0 ||" + C;
									newString += "    " + B + "i " + operator + C;
								}
							}
						}
						
					}
					
					previousOperator = operator;
					fileContent += newString;
				} // end while
				out.write(fileContent);
				out.close();
			}
	} // end method main
}

