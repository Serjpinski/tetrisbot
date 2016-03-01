package bot.classic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import core.Grid;
import core.Misc;
import core.Move;

public class EvalLearner {
	
	private static final int VERBOSE = 2;
	private static final int WEIGHT_NUM = 7;
	private static final int POPSIZE = 24;
	private static final int MAXITER = 1000;
	private static final int FIT_ITER = 100;
	//private static final int INIT_FIT_LIMIT = 16;
	//private static final double FIT_LIMIT_INC = 2;
	private static final int FIT_STAG_LIMIT = 5;
	
	private static final double FEE_A = -0.01;
	private static final double FEE_B = -0.1;
	private static final double FEE_C = 0.4;
	private static final double FEE_D = 0.3;
	
	private static boolean NEXT_PIECE = true;
	private static int PRED_DEPTH = 0;
	private static boolean REDUCED = false;
	
	public static void main (String args[]) {
	
		NEXT_PIECE = false;
		PRED_DEPTH = 0;
		REDUCED = true;
		learn();
	}

	public static double[] learn() {
		
		Random rand = new Random();
		long time = 0;
		
		int fitnessStagnation = 0;
		double lastFitness = 0;

		if (VERBOSE > 0) System.out.println("Initializating population... [POPSIZE = " + POPSIZE + "]");
		EvalInd[] pop = initialization(rand);
		computeFitness(pop, rand);
		
		for (int i = 0; i < MAXITER; i++) {
			
			if (VERBOSE > 0) {
				
				System.out.println("\nIteration " + (i + 1) + " of " + MAXITER);
				time = System.currentTimeMillis();
			}

			if (VERBOSE > 0) System.out.println("Selection...");
			EvalInd[] sPop = selection(pop, rand);
			if (VERBOSE > 0) System.out.println("Crossover...");
			EvalInd[] xPop = crossover(pop, sPop, rand);
			if (VERBOSE > 0) System.out.println("Replacement...");
			pop = replacement(pop, xPop);
			
			if (VERBOSE > 0) {
				
				time = System.currentTimeMillis() - time;
				System.out.println("Iteration completed in " + (time / 1000.0) + " seconds");
				System.out.println("Best individual: " + Misc.arrayToString(pop[0].weights));
				System.out.println("Best fitness: " + pop[0].eval);
				
				double sum = 0;
				for (int j = 0; j < POPSIZE; j++) sum += pop[j].eval;
				
				System.out.println("Average fitness: " + (sum / POPSIZE));
			}
			
			if (pop[0].eval == lastFitness) fitnessStagnation++;
			else lastFitness = pop[0].eval;
			
			if (fitnessStagnation == FIT_STAG_LIMIT) {
				
				System.out.println("Recomputing fitness...");
				computeFitness(pop, rand);
				fitnessStagnation = 0;
			}
		}
		
		return pop[0].weights;
	}
	
	public static double fitness(double[] weights, int fitLimit, Random rand) {

		if (VERBOSE > 1) System.out.print("... evaluating " + Misc.arrayToString(weights));

		double fitness = 0;

		for (int i = 0; i < FIT_ITER; i++) {

			boolean[][] grid = Grid.emptyGrid();

			int lines = 0;

			int activePiece = rand.nextInt(7);
			int nextPiece = rand.nextInt(7);
			Move best = search(grid, activePiece, nextPiece, weights);

			while (best != null && lines < fitLimit) {

				best.place(grid);
				lines += best.getLinesCleared();

				activePiece = nextPiece;
				nextPiece = rand.nextInt(7);
				best = search(grid, activePiece, nextPiece, weights);
			}

			if (lines >= fitLimit) fitness++;
		}

		fitness = fitness / FIT_ITER;

		if (VERBOSE > 1) System.out.println(" = " + fitness);

		return fitness;
	}
	
	public static double fitness2(double[] weights, Random rand) {
		
		if (VERBOSE > 1) System.out.print("... evaluating " + Misc.arrayToString(weights));

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

		fitness = fitness / FIT_ITER;

		if (VERBOSE > 1) System.out.println(" = " + fitness);

		return fitness;
	}
	
	private static Move search(boolean[][] grid, int activePiece, int nextPiece, double[] weights) {
		
		if (NEXT_PIECE) return ClassicBot.search(grid, activePiece, nextPiece, weights, PRED_DEPTH);
		if (REDUCED) return ClassicBot.search(Grid.getSteps(grid), activePiece, weights, PRED_DEPTH).fixRow(grid);
		return ClassicBot.search(grid, activePiece, weights, PRED_DEPTH);
	}
	
//	private static int increaseLimit(EvalInd[] pop, int fitLimit, Random rand) {
//		
//		boolean increaseLimit = true;
//		while (increaseLimit) {
//
//			increaseLimit = false;
//			
//			for (int j = 0; j < POPSIZE && !increaseLimit; j++)
//				if (pop[j].eval == 1) increaseLimit = true;
//			
//			if (increaseLimit) {
//
//				fitLimit *= FIT_LIMIT_INC;
//
//				if (VERBOSE > 0) {
//
//					System.out.println("NEW FITNESS LIMIT = " + fitLimit);
//					System.out.println("Recomputing fitness...");
//				}
//
//				recomputeFitness(pop, fitLimit, rand);
//			}
//		}
//		
//		return fitLimit;
//	}
	
	private static void computeFitness(EvalInd[] pop, Random rand) {
		
		for (int i = 0; i < pop.length; i++) {
			
//			pop[i].eval = fitness(pop[i].weights, fitLimit, rand);
//			if (pop[i].eval == 1) return;
			pop[i].eval = fitness2(pop[i].weights, rand);
		}
	}

	private static EvalInd[] initialization(Random rand) {
				
		EvalInd[] pop = new EvalInd[POPSIZE];
		
		for (int i = 0; i < POPSIZE; i++) {
			
			double[] weights = randomWeights(rand);
			pop[i] = new EvalInd(weights, 1);
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
			Misc.normalizeArray(ch2);
//			xPop[2 * i] = new EvalInd(ch1, fitness(ch1, fitLimit, rand));
//			xPop[2 * i + 1] = new EvalInd(ch2, fitness(ch2, fitLimit, rand));
			xPop[2 * i] = new EvalInd(ch1, fitness2(ch1, rand));
			xPop[2 * i + 1] = new EvalInd(ch2, fitness2(ch2, rand));
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
		
		System.out.println("Genetic Diversity = " + Misc.arrayToString(fee));
		
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
	
	private static double[] randomWeights(Random rand) {
		
		double[] weights = new double[WEIGHT_NUM];
		for (int i = 0; i <weights.length; i++) weights[i] = rand.nextDouble();
		Misc.normalizeArray(weights);
		return weights;
	}
}
