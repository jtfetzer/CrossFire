package Connect4;

import java.io.Serializable;

/**
 * Represents 
 */
public class Move implements Serializable{
	private static final long serialVersionUID = 1L;
	int row;
	int column;
	String moveString;
	int player;
	MoveTypeSet moveTypeSet = new MoveTypeSet();
	
	Move(int row, int column, int player, MoveType type){
		this.row = row;
		this.column = column;
		this.moveString = Connect4.getRow(row) + (column + 1);
		this.player = player;
		if(type != null){
			this.moveTypeSet.addUnique(type);
		}
	} // end constructor
	
	protected boolean update(Move move){ // needs to be expanded
			
		if(move.moveTypeSet.size() > 0){
			this.moveTypeSet.addUnique(move.moveTypeSet.get(0));
			return true;
		}
		return false;
	} // end method updateValue

	public int getValue(){
		return moveTypeSet.value;
	}
} // end class Move
