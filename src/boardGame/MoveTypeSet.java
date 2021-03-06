package boardGame;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 */
public class MoveTypeSet extends ArrayList<MoveType>{

	private static final long serialVersionUID = 1L;

	int value = 0;
	int greatestMoveValue = 0;
	
	MoveTypeSet(){
		super();
	}
	
	/**
	 * This method will add the {@code moveType} to the set if no value of that {@link MoveType} category, (Horizontal or Vertical), 
	 * is yet present in the {@link MoveTypeSet}. If a {@link MoveType} from the same category is present in the set, then it will replace
	 * the existing {@link MoveType} if the value of the new {@link MoveType} is greater than the list item from that category. E.g. an
	 * {@code MoveType.Type.OPEN_3_VERT} will replace a {@code MoveType.Type.THREE_4_VERT} as it has a greater value.
	 * 
	 * @param moveType
	 * @return {@code True} if {@code moveType} is unique to the set, else returns {@code False}
	 */
	public boolean addUnique(MoveType moveType){ 
		boolean newType = true;
		
		for(int i = 0; i < this.size(); i++){
			if(!this.get(i).distinctCategories(moveType)){ // if no move from moveType's category (vertical or horizontal) is  
				newType = false;						   // present in this set, add the move to the set below
				
				if(moveType.value >= 0){ // Max
					if(this.get(i).value < moveType.value){
						if(greatestMoveValue < moveType.value){
							greatestMoveValue = moveType.value;
						}
						value += moveType.value;
						value -= (this.set(i, moveType)).value; // set returns the item it is replacing
					}
				} else if(moveType.value < 0){ // Min
					if(this.get(i).value > moveType.value){
						if(greatestMoveValue > moveType.value){
							greatestMoveValue = moveType.value;
						}
						value += moveType.value;
						value -= (this.set(i, moveType)).value; // set returns the item it is replacing
					}
				}
			}
		} // end for
		if(newType){
			if(moveType.value >= 0){
				if(greatestMoveValue < moveType.value){
					greatestMoveValue = moveType.value;
				}
			} else if(moveType.value < 0){
				if(greatestMoveValue > moveType.value){
					greatestMoveValue = moveType.value;
				}
			}
		
			value += moveType.value;
			super.add(moveType);
//			if(this.size() > 1){
//				if(this.containsElement(MoveType.Type.THREE_4_HORIZ ) && 
//				   this.containsElement(MoveType.Type.PRE_OPEN_3_VERT)){ 
//					
//					System.out.println("Forced WIN");
//					addUnique( new MoveType(MoveType.Type.FORCE_WIN, (int) (CrossFire.MAX_WINS * .9)));
//				}
//				if(this.containsElement(MoveType.Type.THREE_4_VERT ) && 
//				   this.containsElement(MoveType.Type.PRE_OPEN_3_HORIZ)){ 
//					
//					System.out.println("Forced WIN");
//					addUnique( new MoveType(MoveType.Type.FORCE_WIN, (int) (CrossFire.MAX_WINS * .9)));
//				}
//			}
		}
		return newType;
	} // end method add
	
	public boolean containsElement(MoveType.Type moveType){
		Iterator<MoveType> it = this.iterator();
		while(it.hasNext()){
			if(it.next().type == moveType){
				return true;
			}
		}
		return false;
	}
	@Override
	public String toString(){
		
		String list = "";
		for(MoveType next : this){
			list += next.type + " ";
		}
		return list;
	} // end method toString

	public String toStringValue(){
		
		String list = "";
		if(this.size() > 1){
} else if(this.size() > 0){
			for(MoveType next : this){
				list += next.type + ": " + next.value;
			}
		}
		return list + "}";
	} // end method toString

	public void removeAll() {
		for (int i = 0; i < this.size(); i++) {
			this.remove(i--);
		} // end for i
		
	}
}
