package boardGame;

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
	
	/**
	 * A {@link MoveTypeSet} is a set of {@link MoveType}s. 
	 * Each {@code MoveType} has a value reflecting the strength of that {@code MoveType}.
	 * @return the value of {@code MoveType} with the greatest (for Max) of least (for Min) value in the {@code MoveTypeSet}.
	 */
	public int getBestMoveValue(){
		return moveTypeSet.greatestMoveValue;
	}
} // end class Move
