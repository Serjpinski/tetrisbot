package bot.rules;

import java.util.HashSet;

public class MultIntValue implements VarValue {

	public HashSet<Integer> values;
	
	public MultIntValue() {
		
		values = new HashSet<Integer>();
	}
	
	public void addValue(int value) {
		
		values.add(value);
	}
	
	@Override
	public boolean isSatisfiedBy(VarValue value) {
		
		if (value == null) return false;
		
		if (value instanceof IntValue) {
			
			return values.contains(((IntValue) value).value);
		}
		
		return false;
	}
}
