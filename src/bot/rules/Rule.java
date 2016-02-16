package bot.rules;

import java.util.HashMap;

public class Rule {

	public HashMap<String, VarValue> ants;
	public HashMap<String, VarValue> cons;
	
	public Rule() {
		
		ants = new HashMap<String, VarValue>();
		cons = new HashMap<String, VarValue>();		
	}
	
	public boolean isTriggeredBy (HashMap<String, VarValue> preconditions) {
		
		for (String name : ants.keySet()) {
			
			VarValue value = preconditions.get(name);
			
			if (value != null || !ants.get(name).isSatisfiedBy(value))
				return false;
		}
		
		return true;
	}
}
