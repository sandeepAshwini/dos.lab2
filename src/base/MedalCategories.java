package base;

/**
 * The types of medals up for grabs.
 * @author sandeep
 *
 */
public enum MedalCategories {
	GOLD_LEAF("Gold Leaf"),
	SILVER_LEAF("Silver Leaf"),
	BRONZE_LEAF("Bronze Leaf");
	
	private String value;
	MedalCategories(String category) {
		this.value = category;
	}
	
	public String getCategory(){
		return this.value;
		
	}
}
