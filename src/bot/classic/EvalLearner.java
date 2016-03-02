package bot.classic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import core.Grid;
import core.Misc;
import core.Move;

public class EvalLearner {

	private static final int WEIGHT_NUM = 7;
	private static final int POPSIZE = 24;
	private static final int FIT_ITER = 1;
	private static final int MAXITER = 1000;

	private static final double FEE_A = -0.001;
	private static final double FEE_B = -0.5;
	private static final double FEE_C = 0.7;
	private static final double FEE_D = 0.5;

	private static boolean NEXT_PIECE = true;
	private static int PRED_DEPTH = 0;
	private static boolean REDUCED = false;

	public static void main (String args[]) {

		NEXT_PIECE = false;
		PRED_DEPTH = 1;
		REDUCED = true;
		learn();
	}

	private static double[] learn() {

		Random rand = new Random();
		long time = 0;

		System.out.println("Initializating population... [POPSIZE = " + POPSIZE + "]");
		EvalInd[] pop = initialization(rand);

		for (int i = 0; i < MAXITER; i++) {

			System.out.println("\nIteration " + (i + 1) + " of " + MAXITER);
			time = System.currentTimeMillis();

			System.out.println("Selection...");
			EvalInd[] sPop = selection(pop, rand);
			System.out.println("Crossover...");
			EvalInd[] xPop = crossover(pop, sPop, rand);
			System.out.println("Replacement...");
			pop = replacement(pop, xPop);
			System.out.println("Fitness update...");
			fitnessUpdate(pop,  rand);

			time = System.currentTimeMillis() - time;
			System.out.println("Iteration completed in " + (time / 1000.0) + " seconds");

			double sum = 0;
			EvalInd best = pop[0];
			
			for (int j = 0; j < POPSIZE; j++) {
				
				sum += pop[j].eval;
				if (pop[j].eval > best.eval) best = pop[j];
			}
			
			System.out.println("Best fitness: " + best.eval + " (age " + best.age + ")");
			System.out.println("Avg. fitness: " + (sum / POPSIZE));
		}

		return pop[0].weights;
	}

	private static double fitness(double[] weights, Random rand) {

		double fitness = 0;

		for (int i = 0; i < FIT_ITER; i++) {
			
			boolean[][] grid = Grid.emptyGrid();
			int lines = 0;
			int activePiece = rand.nextInt(7);
			int nextPiece = rand.nextInt(7);
			Move best = search(grid, activePiece, nextPiece, weights);
			while (best != null) {

				best.place(grid);
				lines += best.getLinesCleared();

				activePiece = nextPiece;
				nextPiece = rand.nextInt(7);
				best = search(grid, activePiece, nextPiece, weights);
			}
			
			fitness += lines;
		}
		
		return fitness / FIT_ITER;
	}

	private static Move search(boolean[][] grid, int activePiece, int nextPiece, double[] weights) {
	
		if (NEXT_PIECE) return ClassicBot.search(grid, activePiece, nextPiece, weights, PRED_DEPTH);
		if (REDUCED) return ClassicBot.search(Grid.getSteps(grid), activePiece, weights, PRED_DEPTH).fixRow(grid);
		return ClassicBot.search(grid, activePiece, weights, PRED_DEPTH);
	}

	private static EvalInd[] initialization(Random rand) {

		EvalInd[] pop = new EvalInd[POPSIZE];

		for (int i = 0; i < POPSIZE; i++) {

			double[] weights = randomWeights(rand);
			pop[i] = new EvalInd(weights, fitness(weights, rand), 1);
			System.out.println(pop[i]);
		}

		return pop;
	}

	private static EvalInd[] selection(EvalInd[] pop, Random rand) {

		EvalInd[] sPop = new EvalInd[POPSIZE / 2];

		ArrayList<EvalInd> pool = new ArrayList<EvalInd>(POPSIZE);
		pool.addAll(Arrays.asList(pop));
		Collections.shuffle(pool, rand);

		for (int i = 0; i < sPop.length; i++) {

			EvalInd ind1 = pool.get(2 * i);
			EvalInd ind2 = pool.get(2 * i + 1);

			if (ind1.eval < ind2.eval) sPop[i] = ind2;
			else sPop[i] = ind1;
		}

		return sPop;
	}

	private static EvalInd[] crossover(EvalInd[] pop, EvalInd[] sPop, Random rand) {

		EvalInd[] xPop = new EvalInd[sPop.length];
		double[] fee = fee(pop);

		for (int i = 0; i < sPop.length / 2; i++) {

			double[] ch1 = new double[WEIGHT_NUM];
			double[] ch2 = new double[WEIGHT_NUM];

			for (int j = 0; j < WEIGHT_NUM; j++) {

				double x1 = sPop[2 * i].weights[j];
				double x2 = sPop[2 * i + 1].weights[j];
				double min = Math.min(x1, x2);
				double max = Math.max(x1, x2);
				double dev = fee[j]; // * (max - min);
				double gimin = min + dev;
				double gimax = max - dev;

				ch1[j] = Math.min(1, Math.max(0, gimin + (gimax - gimin) * rand.nextDouble()));
				ch2[j] = Math.min(1, Math.max(0, min + max - ch1[j]));
			}

			Misc.normalizeArray(ch1);
			xPop[2 * i] = new EvalInd(ch1, fitness(ch1, rand), 1);
			System.out.println(xPop[2 * i]);

			Misc.normalizeArray(ch2);
			xPop[2 * i + 1] = new EvalInd(ch2, fitness(ch2, rand), 1);
			System.out.println(xPop[2 * i + 1]);
		}

		return xPop;
	}

	private static double[] fee(EvalInd[] pop) {

		double[] fee = new double[WEIGHT_NUM];
		double[] min = new double[WEIGHT_NUM];
		double[] max = new double[WEIGHT_NUM];

		for (int i = 0; i < WEIGHT_NUM; i++) min[i] = 1;

		for (int i = 0; i < pop.length; i++) {

			double[] ind = pop[i].weights;

			for (int j = 0; j < WEIGHT_NUM; j++) {

				if (ind[j] < min[j]) min[j] = ind[j];
				if (ind[j] > max[j]) max[j] = ind[j];				
			}
		}

		// Genetic diversity
		for (int i = 0; i < WEIGHT_NUM; i++) fee[i] = max[i] - min[i];

		System.out.println(Misc.arrayToString(fee) + " <== Genetic Diversity");

		// Exploration-Exploitation function
		for (int i = 0; i < WEIGHT_NUM; i++)
			if (fee[i] < FEE_C) fee[i] = FEE_A + (fee[i] * ((FEE_B - FEE_A) / FEE_C));
			else fee[i] = ((fee[i] - FEE_C) * (FEE_D / (1 - FEE_C)));

		return fee;
	}

	private static EvalInd[] replacement(EvalInd[] pop, EvalInd[] xPop) {

		ArrayList<EvalInd> newPop = new ArrayList<EvalInd>(pop.length + xPop.length);
		newPop.addAll(Arrays.asList(pop));
		newPop.addAll(Arrays.asList(xPop));
		newPop.sort(new EvalIndComparator());
		return newPop.subList(0, pop.length).toArray(new EvalInd[0]);
	}

	private static void fitnessUpdate(EvalInd[] pop, Random rand) {
	
		for (int i = 0; i < pop.length; i++) {
	
			pop[i].eval = (pop[i].eval * pop[i].age + fitness(pop[i].weights, rand)) / ++pop[i].age;
			System.out.println(pop[i]);
		}
	}

	private static double[] randomWeights(Random rand) {

		double[] weights = new double[WEIGHT_NUM];
		for (int i = 0; i <weights.length; i++) weights[i] = rand.nextDouble();
		Misc.normalizeArray(weights);
		return weights;
	}
}
