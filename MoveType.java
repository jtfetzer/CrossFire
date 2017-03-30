package Connect4;

public class MoveType {
	
	public enum Type {
		CONNECT_4, BLOCK_CONNECT_4,
		
		SMALL_OPEN_L, BLOCK_SMALL_OPEN_L, BIG_OPEN_L, BLOCK_BIG_OPEN_L,
		
		PRE_SMALL_OPEN_L, BLOCK_PRE_SMALL_OPEN_L, PRE_BIG_OPEN_L, BLOCK_PRE_BIG_OPEN_L,
		
		OPEN_3_HORIZ, BLOCK_OPEN_3_HORIZ, OPEN_3_VERT, BLOCK_OPEN_3_VERT, 
		
		BLOCK_MOST_SPACE_HORIZ, BLOCK_MOST_SPACE_VERT,
		
		PRE_OPEN_3_HORIZ, BLOCK_PRE_OPEN_3_HORIZ, PRE_OPEN_3_VERT, BLOCK_PRE_OPEN_3_VERT,
		
		THREE_4_HORIZ, BLOCK_THREE_4_HORIZ, THREE_4_VERT, BLOCK_THREE_4_VERT,
		
		TWO_4_HORIZ, BLOCK_TWO_4_HORIZ, TWO_4_VERT, BLOCK_TWO_4_VERT,
		  
		ONE_4,
		
		ZEROS }
	
	public Type type;
	public int value;
	
	MoveType(Type type, int value){
		this.type = type;
		this.value = value;
	}
	
	public String toString(){
		return type.toString();
	}
	
	public void changeValue(int value){
		this.value = value;
	}
	
	public boolean isVertical(){
		if(this.type.toString().contains("VERT")){
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isHorizontal(){
		if(this.type.toString().contains("HORIZ")){
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isBlock(){
		if(this.type.toString().contains("BLOCK")){
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isNotBlock(){
		if(!this.type.toString().contains("BLOCK")){
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isNotVerticalOrHorizontal(){
		if(!this.type.toString().contains("Vert")){
			if(!this.type.toString().contains("HORIZ")){
				return true;
			}
		}
		return false;
	}

	private boolean isOpen() {
		if(this.type.toString().contains("OPEN")){
			return true;
		} else {
			return false;
		}
	}
	
	private boolean withModifiers(){
		if(this.type.toString().contains("PRE") || this.type.toString().contains("BLOCK")){
			return false;
		}
		return true;
	}
	/**
	 * 
	 * @param moveType
	 * @return {@code True} if categories {@code moveType} belongs to a distinct category
	 * from all other move types in the {@link MoveTypeSet}. E.g. A vertical and a horizontal
	 * move type would be distinct, and this method would then return {@code True}. Two verticals
	 * would not be distinct and would return {@code False}. A block and a non-block are distinct 
	 * categories and would return {@code True}.
	 */
	public boolean distinctCategories(MoveType moveType) {
		if(this.type.compareTo(moveType.type) == 0){
			
			return false;
		}
		if(this.type.toString().contains("CONNECT_4") || this.type.toString().contains("CONNECT_4")){
			return false;	// Connect4 is its own category.
		}
		if(( this.isVertical() && this.isBlock() ) 	&& ( moveType.isVertical() && moveType.isBlock() ) ||  
		   ( this.isVertical() && isNotBlock() ) && ( moveType.isVertical() && moveType.isNotBlock() )){
			
			return false;
		} else if(( this.isHorizontal() && this.isBlock() ) 	&& ( moveType.isHorizontal() && moveType.isBlock() ) ||  
				   ( this.isHorizontal() && this.isNotBlock() ) && ( moveType.isHorizontal() && moveType.isNotBlock() )){
			
			return false;
		} 
//		else if((isOpen() && withModifiers()) && (moveType.isOpen() && moveType.withModifiers()) ){
//			
//			return false;
//		}
		
		return true;
	}

}