package boardGame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author Jonathan Fetzer
 * 
 * A {@code MoveSet} contains the set containing each {@link Move} played.
 */
public class MoveSet extends ArrayList<Move> implements Comparator<Move>{

	private static final long serialVersionUID = 1L;
	
	/**
	 * Verifies that the {@code move} passed is unique to the {@link MoveSet}.
	 * @param move
	 * @return {@code True} if the {@link Move} is added to the set, {@code False} otherwise.
	 */
	public void addUnique(Move move){ 
		boolean duplicate = false;

		for (int i = 0; i < this.size(); i++) {
			if(this.get(i).moveString.equals(move.moveString)){ // MoveList contains this move already
				duplicate = this.get(i).update(move); // if updated, duplicate = true, else false.
			}
		}
		if(!duplicate){
			super.add(move);
		}
	}
	
	/**
	 * Adds all unique unique {@link Move moves} in {@code moveList} to the {@link MoveList} object that call this method.
	 * @param moveList
	 */
	public MoveSet addAll(MoveSet moveList){
		for(Move next : moveList){
			addUnique(next);
		}
		return this;
	}

	/**
	 * Reduces the {@link MoveSet} to only contain the {@code numberOfBestMoves}, i.e. the moves with the highest values.
	 * @param numberOfBestMoves
	 */
	public MoveSet reduceMax(int numberOfBestMoves) { 
		if( numberOfBestMoves <= 0 || numberOfBestMoves > this.size() || this.size() < 1){
			return this;
		}
		Collections.sort(this, new MaxMoveComparator()); // sort bestMoves with best first descending order
		this.removeRange(numberOfBestMoves, this.size());
		return this;
	}
	
	/**
	 * Reduces the {@link MoveSet} to only contain the {@code numberOfBestMoves}, i.e. the moves with the lowest values.
	 * @param numberOfBestMoves
	 * @return 
	 */
	public MoveSet reduceMin(int numberOfBestMoves) {
		if( numberOfBestMoves <= 0 || numberOfBestMoves > this.size() || this.size() < 1){
			return this;
		}
		Collections.sort(this, new MinMoveComparator()); // sort bestMoves with best first descending order
		this.removeRange(numberOfBestMoves, this.size());
		return this;
	}
	
	/**
	 * Used to compare the values of two {@link Move moves}
	 */
	@Override
	public int compare(Move m1, Move m2) {
		return Math.abs(m2.getValue()) - Math.abs(m1.getValue());
	}
	
	/**
	 * Used for debugging, prints the {@link MoveSet} to the console.
	 * @param msg
	 */
	public void print(){
		print("");
	}
	
	/**
	 * Used for debugging, prints the message {@code msg} then prints the {@link MoveSet} to the console.
	 * @param msg
	 */
	public void print(String msg){
		if(this.size() < 1){
			return;
		}
		if(!msg.equals("")){
			System.out.println(msg + ": ");
		}
		for (Move move: this) {
			System.out.println("\t" + move.moveString + ", max: " + move.getBestMoveValue() + ", total: " + move.getValue() + " - " + move.moveTypeSet.toStringValue() + "; ");
		} // end for
	}
	
	/**
	 * @return the {@link Move} with the greatest value.
	 */
	public Move max(){
		Collections.sort(this, new MaxMoveComparator()); // sort bestMoves into descending order by value
		return this.get(0);
	}
	
	/**
	 * @return the {@link Move} with the least value.
	 */
	public Move min(){
		Collections.sort(this, new MinMoveComparator()); // sort bestMoves into ascending order by value
		return this.get(0);
	}

	public MoveSet removeValues() {
		for(Move move : this){
			move.moveTypeSet.removeAll();
		}
		return this;
	}

	public int getNumOpen3() {
		int count = 0;
		for(Move move: this){
			if(!move.moveString.contains("PRE")){
				count++;
			}
		}
		return count;
	}

	public MoveSet getOpen3() {
		MoveSet open3 = new MoveSet();
		for(Move move: this){
			if(!move.moveString.contains("PRE")){
				open3.add(move);
			}
		}
		return open3;
	}

	/**
	 * @return True if {@link MoveSet} contains more than one CONNECT_4, meaning 
	 * the CONNECT_4 cannot be blocked, else false.
	 */
	public boolean unstopableLoss() {
		int count = 0;
		boolean max = false;
		if(this.size() > 0){
			if(this.get(0).getValue() > 0){ // Max
				max = true;
			}
		}
		if(max){
			for (int i = 0; i < this.size(); i++) {
				if(this.get(i).getBestMoveValue() == GameLogic.getMultiplierValue(GameLogic.MAX_WINS, GameLogic.BLOCK_MULT)){
					++count;
				} // end if
			} // end for
		} 
		else { // Min
			for (int i = 0; i < this.size(); i++) {
				if(this.get(i).getBestMoveValue() == GameLogic.getMultiplierValue(GameLogic.MIN_WINS, GameLogic.BLOCK_MULT)){
					++count;
				} // end if
			} // end for
		} // END ELSE
		
		if(count > 1) return true;
		return false;
	}
	/**
	 * @return True if {@link MoveSet} contains a {@type MoveType} CONNECT_4, else false.
	 */
	public boolean canWin() {
		for(Move move : this){
			if(move.moveTypeSet.containsElement(MoveType.Type.CONNECT_4)){
				return true;
			}
		}
		return false;
	}

	public boolean canBlockWin() {
		for(Move move : this){
			if(move.moveTypeSet.containsElement(MoveType.Type.BLOCK_CONNECT_4)){
				return true;
			}
		}
		return false;
	}

}
