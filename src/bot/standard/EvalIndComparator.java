package bot.standard;

import java.util.Comparator;

public class EvalIndComparator implements Comparator<EvalInd> {

	public int compare (EvalInd o1, EvalInd o2) {
		
		if (o1.eval == o2.eval) return 0;
		else if (o1.eval < o2.eval) return 1;
		else return -1;
	}
}