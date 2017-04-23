package boardGame;
/**
 * This class updates the state of the root {@link BoardNode}. 
 * @author Jonathan Fetzer
 */
public class UpdateThread extends Thread {

	int row, col, player;
	
	UpdateThread(int row, int col, int player){
		this.row = row;
		this.col = col;
		this.player = player;
	}
	
    public void run() {
    	if(player == 1 || CrossFire.load){
    		CrossFire.root.update(new Move(row, col, player, null)); // human
    	
	    	if(!CrossFire.load){
	   				CrossFire.root.update(GameLogic.chooseMove(CrossFire.root)); // computer
   			}	
    	} 
    } // end method run
} // end class HelloThread