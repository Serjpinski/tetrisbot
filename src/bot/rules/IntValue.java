package bot.rules;

public class IntValue implements VarValue {

	public int value;
	
	public IntValue(int value) {
		
		this.value = value;
	}
	
	@Override
	public boolean isSatisfiedBy(VarValue value) {
		
		if (value == null) return false;
		
		if (value instanceof IntValue) {
			
			return ((IntValue) value).value == this.value;
		}
		
		return false;
	}
}
