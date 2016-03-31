package heuristic;

import java.util.Comparator;

public class IndividualComparator implements Comparator<Individual> {

	public int compare (Individual o1, Individual o2) {
		
		if (o1.eval == o2.eval) return 0;
		else if (o1.eval < o2.eval) return 1;
		else return -1;
	}
}