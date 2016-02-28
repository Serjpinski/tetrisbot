package bot.classic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import logic.Grid;
import logic.Move;

public class EvalLearner {
	
	private static final int VERBOSE = 2;
	private static final int WEIGHT_NUM = 4;
	private static final int POPSIZE = 12;
	private static final int MAXITER = 1000;
	private static final int FIT_ITER = 10;
	private static final int INIT_FIT_LIMIT = 25;
	private static final double FIT_LIMIT_INC = 2;
	
	private static final double FEE_A = -0.01;
	private static final double FEE_B = -0.1;
	private static final double FEE_C = 0.5;
	private static final double FEE_D = 0.3;
	
	private static boolean NEXT_PIECE = true;
	private static int PRED_DEPTH = 0;
	private static boolean REDUCED = false;
	
	public static void main (String args[]) {
	
		NEXT_PIECE = false;
		PRED_DEPTH = 1;
		REDUCED = true;
		learn();
	}

	public static double[] learn() {
		
		Random r = new Random();
		long time = 0;
		int fitLimit = INIT_FIT_LIMIT;

		if (VERBOSE > 0) System.out.println("Initializating population [POPSIZE = " + POPSIZE + "]");
		EvalInd[] pop = initialization(fitLimit, r);
		increaseLimit(pop, fitLimit, r);
		
		for (int i = 0; i < MAXITER; i++) {
			
			if (VERBOSE > 0) {
				
				System.out.println("\nIteration " + (i + 1) + " of " + MAXITER);
				time = System.currentTimeMillis();
			}
			
			increaseLimit(pop, fitLimit, r);

			if (VERBOSE > 0) System.out.println("Selection...");
			EvalInd[] sPop = selection(pop, r);
			if (VERBOSE > 0) System.out.println("Crossover...");
			EvalInd[] xPop = crossover(pop, sPop, fitLimit, r);
			if (VERBOSE > 0) System.out.println("Replacement...");
			pop = replacement(pop, xPop);
			
			if (VERBOSE > 0) {
				
				time = System.currentTimeMillis() - time;
				System.out.println("Iteration completed in " + (time / 1000.0) + " seconds");
				System.out.println("Best individual: " + weightsToString(pop[0].weights));
				System.out.println("Best fitness: " + pop[0].eval);
				System.out.println("Fitness limit: " + fitLimit);
				
				double sum = 0;
				for (int j = 0; j < POPSIZE; j++) sum += pop[j].eval;
				
				System.out.println("Average fitness: " + (sum / POPSIZE));
			}
		}
		
		return pop[0].weights;
	}
	
	public static double fitness(double[] weights, int fitLimit, Random r) {

		if (VERBOSE > 1) System.out.print("... evaluating " + weightsToString(weights));

		double fitness = 0;

		for (int i = 0; i < FIT_ITER; i++) {

			boolean[][] grid = Grid.emptyGrid();

			int lines = 0;

			int activePiece = r.nextInt(7);
			int nextPiece = r.nextInt(7);
			Move best = search(grid, activePiece, nextPiece, weights);

			while (best != null && lines < fitLimit) {

				best.place(grid);
				lines += best.getLinesCleared();

				activePiece = nextPiece;
				nextPiece = r.nextInt(7);
				best = search(grid, activePiece, nextPiece, weights);
			}

			if (lines >= fitLimit) fitness++;
		}

		fitness = fitness / FIT_ITER;

		if (VERBOSE > 1) System.out.println(" = " + fitness);

		return fitness;
	}
	
	public static int fitnessOld1(double[] weights, Random r) {
		
		if (VERBOSE > 1) System.out.print("... evaluating " + weightsToString(weights));
		
		boolean[][] grid = Grid.emptyGrid();
		
		int lines = 0;

		int activePiece = r.nextInt(7);
		int nextPiece = r.nextInt(7);
		Move best = search(grid, activePiece, nextPiece, weights);

		while (best != null) {

			best.place(grid);
			lines += best.getLinesCleared();

			activePiece = nextPiece;
			nextPiece = r.nextInt(7);
			best = search(grid, activePiece, nextPiece, weights);
		}
		
		if (VERBOSE > 1) System.out.println(" = " + lines);
		
		return lines;
	}
	
	private static Move search(boolean[][] grid, int activePiece, int nextPiece, double[] weights) {
		
		if (NEXT_PIECE) return ClassicBot.search(grid, activePiece, nextPiece, weights, PRED_DEPTH);
		if (REDUCED) return ClassicBot.search(Grid.getSteps(grid), activePiece, weights, PRED_DEPTH).fixRow(grid);
		return ClassicBot.search(grid, activePiece, weights, PRED_DEPTH);
	}
	
	private static int increaseLimit(EvalInd[] pop, int fitLimit, Random r) {
		
		boolean increaseLimit = true;
		while (increaseLimit) {

			increaseLimit = false;
			
			for (int j = 0; j < POPSIZE && !increaseLimit; j++)
				if (pop[j].eval == 1) increaseLimit = true;
			
			if (increaseLimit) {

				fitLimit *= FIT_LIMIT_INC;

				if (VERBOSE > 0) {

					System.out.println("NEW FITNESS LIMIT = " + fitLimit);
					System.out.println("Recomputing fitness...");
				}

				recomputeFitness(pop, fitLimit, r);
			}
		}
		
		return fitLimit;
	}
	
	private static void recomputeFitness(EvalInd[] pop, int fitLimit, Random r) {
		
		for (int i = 0; i < pop.length; i++) {
			
			pop[i].eval = fitness(pop[i].weights, fitLimit, r);
			if (pop[i].eval == 1) return;
		}
	}

	private static EvalInd[] initialization(int fitLimit, Random r) {
				
		EvalInd[] pop = new EvalInd[POPSIZE];
		
		for (int i = 0; i < POPSIZE; i++) {
			
			double[] weights = randomWeights(r);
			pop[i] = new EvalInd(weights, 1);
		}
		
		return pop;
	}
	
	private static EvalInd[] selection(EvalInd[] pop, Random r) {
		
		EvalInd[] sPop = new EvalInd[POPSIZE / 2];
		
		ArrayList<EvalInd> pool = new ArrayList<EvalInd>(POPSIZE);
		pool.addAll(Arrays.asList(pop));
		Collections.shuffle(pool, r);
		
		for (int i = 0; i < sPop.length; i++) {
			
			EvalInd ind1 = pool.get(2 * i);
			EvalInd ind2 = pool.get(2 * i + 1);
			
			if (ind1.eval < ind2.eval) sPop[i] = ind2;
			else sPop[i] = ind1;
		}
		
		return sPop;
	}

	private static EvalInd[] crossover(EvalInd[] pop, EvalInd[] sPop, int fitLimit, Random r) {

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
				double dev = fee[j];// * (max - min);
				double gimin = min + dev;
				double gimax = max - dev;
				
				ch1[j] = Math.min(1, Math.max(0, gimin + (gimax - gimin) * r.nextDouble()));
				ch2[j] = Math.min(1, Math.max(0, min + max - ch1[j]));
			}
			
			normalizeWeights(ch1);
			normalizeWeights(ch2);
			xPop[2 * i] = new EvalInd(ch1, fitness(ch1, fitLimit, r));
			xPop[2 * i + 1] = new EvalInd(ch2, fitness(ch2, fitLimit, r));
		}
		
		return xPop;
	}

	private static double[] fee(EvalInd[] pop) {

		double[] fee = new double[WEIGHT_NUM];
		double[] min = new double[WEIGHT_NUM];
		double[] max = new double[WEIGHT_NUM];
		for (int i = 0; i < WEIGHT_NUM; i++) max[i] = 1;
		
		// Genetic diversity computation
		for (int i = 0; i < pop.length; i++) {
			
			double[] ind = pop[i].weights;
			
			for (int j = 0; j < WEIGHT_NUM; j++) {
				
				if (ind[j] < min[j]) min[j] = ind[j];
				if (ind[j] > max[j]) max[j] = ind[j];				
			}
		}
		
		for (int i = 0; i < WEIGHT_NUM; i++) {
			
			fee[i] = max[i] - min[i];
			
			// Exploration-Exploitation function
			if (fee[i] < FEE_C) fee[i] = FEE_A + (fee[i] * ((FEE_B - FEE_A) / FEE_C));
			else fee[i] = ((fee[i] - FEE_C) * (FEE_D / (1 - FEE_C)));
		}
		
		return fee;
	}

	private static EvalInd[] replacement(EvalInd[] pop, EvalInd[] xPop) {
		
		ArrayList<EvalInd> newPop = new ArrayList<EvalInd>(pop.length + xPop.length);
		newPop.addAll(Arrays.asList(pop));
		newPop.addAll(Arrays.asList(xPop));
		newPop.sort(new EvalIndComparator());
		return newPop.subList(0, pop.length).toArray(new EvalInd[0]);
	}
	
	private static double[] randomWeights(Random r) {
		
		double[] weights = new double[WEIGHT_NUM];
		for (int i = 0; i <weights.length; i++) weights[i] = r.nextDouble();
		normalizeWeights(weights);
		return weights;
	}

	private static void normalizeWeights(double[] weights) {
		
		double sum = 0;
		for (int i = 0; i < weights.length; i++) sum += weights[i];
		if (sum > 0) for (int i = 0; i < weights.length; i++) weights[i] /= sum;
	}
	
	public static String weightsToString(double[] weights) {
		
		if (weights.length == 0) return "[]";
		
		String string = String.format("[%.6f", weights[0]);
		for (int i = 1; i < weights.length; i++) string += String.format(", %.6f", weights[i]);
		return string + "]";
	}
}
