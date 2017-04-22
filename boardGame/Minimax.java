package boardGame;

import java.util.Scanner;

public class Minimax implements Runnable{

	BoardNode node;
	static Scanner in = new Scanner(System.in);
	int i;
	
	Minimax(BoardNode node, int i){
		this.node = node;
		this.i = i;
	}
	public void run() {
		min(node.getChild(i), 1);		
	}
	
	public static void min(BoardNode node, int depth){

		if(Connect4.SHOW_BOARDS){
			System.out.println("-------MIN------- Depth: " + depth + " " + node.lastMove.moveString);
			node.printBoard();
			if(!Connect4.SHOW_MOVES_MINIMAX){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		if(node.lastMove.getBestMoveValue() == Connect4.MAX_WINS){
			node.value = Connect4.MAX_WINS;
			node.maxWinDepth = depth;
			return;
		}

		if(depth == Connect4.MAX_DEPTH || node.getNumEmptySpaces() == 0){
			return;
		}
		
		MoveSet attacks = Connect4.getMostPromisingMoves(node, Connect4.ATTACK_MOVES);
		MoveSet blocks = Connect4.getMostPromisingMoves(node, Connect4.BLOCK_MOVES);
		MoveSet bestMoves = blocks.addAll(attacks).reduceMin(Connect4.MAX_WIDTH / 2);

		if(Connect4.SHOW_MOVES_MINIMAX){
			bestMoves.print("BestMoves");
			System.out.println("Press enter to continue:");
			in.nextLine();
		}
		
		node.addAll(bestMoves);
		
		for(int j = 0; j < node.size(); j++){ // most promising nodes
			max(node.getChild(j), depth + 1);
			if(node.getChild(j).value == Connect4.MAX_WINS){
				if(node.maxWinDepth < node.getChild(j).maxWinDepth){ // Find longest loss path
					node.maxWinDepth = node.getChild(j).maxWinDepth; 
				}
			} else if(node.getChild(j).value == Connect4.MIN_WINS){
				node.minWinDepth = node.getChild(j).minWinDepth;
				node.value = Connect4.MIN_WINS;
				node.removeAllChildNodesExcept(node.getChild(j));
				return;
			} 
			
		} // end for i
		node.value = node.minChildNode().value;
		node.removeAllChildNodes();
		return;
	} // end method min
	
	public static void max(BoardNode node, int depth){

		if(Connect4.SHOW_BOARDS){
			System.out.println("-------MAX------- Depth: " + depth + " " + node.lastMove.moveString);
			node.printBoard();
			if(!Connect4.SHOW_MOVES_MINIMAX){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		if(node.lastMove.getBestMoveValue() == Connect4.MIN_WINS){
			node.value = Connect4.MIN_WINS;
			node.minWinDepth = depth;
			return;
		}

		if(depth == Connect4.MAX_DEPTH || node.getNumEmptySpaces() == 0){
			return;
		}
		MoveSet attacks = Connect4.getMostPromisingMoves(node, Connect4.ATTACK_MOVES);
		MoveSet blocks = Connect4.getMostPromisingMoves(node, Connect4.BLOCK_MOVES);
		MoveSet bestMoves = attacks.addAll(blocks).reduceMax(Connect4.MAX_WIDTH / 2);

		if(Connect4.SHOW_MOVES_MINIMAX){
			
			bestMoves.print("BestMoves");
			System.out.println("Press enter to continue:");
			in.nextLine();
		}
 
		node.addAll(bestMoves);
		
		for(int i = 0; i < node.size(); i++){ // most promising nodes
			min(node.getChild(i), depth + 1);

			if(node.getChild(i).value == Connect4.MIN_WINS){
				if(node.minWinDepth < node.getChild(i).minWinDepth){ // Find longest loss path
					node.minWinDepth = node.getChild(i).minWinDepth;
				} 
			} else if(node.getChild(i).value == Connect4.MAX_WINS){
				node.maxWinDepth = node.getChild(i).maxWinDepth;
				node.value = Connect4.MAX_WINS;
				node.removeAllChildNodesExcept(node.getChild(i));
				return;
			} 
		} // end for i
		
		node.value = node.maxChildNode().value;
		node.removeAllChildNodes();
		return;
	} // end method max
	
}
