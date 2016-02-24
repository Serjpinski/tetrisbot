package bot.classic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import logic.Grid;
import logic.Move;

public class EvalLearner {
	
	private static final int VERBOSE = 2;
	
	private static final int POPSIZE = 12;
	private static final int MAXITER = 100;
	private static final int FIT_ITER = 10;
	private static final int INIT_FIT_LIMIT = 100;
	private static final double FIT_LIMIT_INC = 2;
	
	private static final double FEE_A = -0.01;
	private static final double FEE_B = -0.1;
	private static final double FEE_C = 0.5;
	private static final double FEE_D = 0.3;

	public static EvalWeights learn() {
		
		Random r = new Random();
		long time = 0;
		int fitLimit = INIT_FIT_LIMIT;

		if (VERBOSE > 0) System.out.println("Initializating population [POPSIZE = " + POPSIZE + "]");
		EvalInd[] pop = initialization(fitLimit, r);
		
		for (int i = 0; i < MAXITER; i++) {
			
			if (VERBOSE > 0) {
				
				System.out.println("\nIteration " + (i + 1) + " of " + MAXITER);
				time = System.currentTimeMillis();
			}
			
			boolean increaseLimit = true;
			while (increaseLimit) {

				increaseLimit = false;
				
				for (int j = 0; j < POPSIZE; j++)
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

			if (VERBOSE > 0) System.out.println("Selection...");
			EvalInd[] sPop = selection(pop, r);
			if (VERBOSE > 0) System.out.println("Crossover...");
			EvalInd[] xPop = crossover(pop, sPop, fitLimit, r);
			if (VERBOSE > 0) System.out.println("Replacement...");
			pop = replacement(pop, xPop);
			
			if (VERBOSE > 0) {
				
				time = System.currentTimeMillis() - time;
				System.out.println("Iteration completed in " + (time / 1000.0) + " seconds");
				System.out.println("Best individual: " + pop[0].weights.toString());
				System.out.println("Best fitness: " + pop[0].eval);
				System.out.println("Fitness limit: " + fitLimit);
				
				double sum = 0;
				for (int j = 0; j < POPSIZE; j++) sum += pop[j].eval;
				
				System.out.println("Average fitness: " + (sum / POPSIZE));
			}
		}
		
		return pop[0].weights;
	}
	
	public static double fitness(EvalWeights weights, int fitLimit, Random r) {

		if (VERBOSE > 1) System.out.print("... evaluating " + weights.toString());

		double fitness = 0;

		for (int i = 0; i < FIT_ITER; i++) {

			boolean[][] grid = Grid.emptyGrid();

			int lines = 0;

			int activePiece = r.nextInt(7);
			int nextPiece = r.nextInt(7);
			Move best = ClassicBot.search(grid, activePiece, nextPiece, weights, 0);

			while (best != null && lines < fitLimit) {

				best.place(grid);
				lines += best.getLinesCleared();

				activePiece = nextPiece;
				nextPiece = r.nextInt(7);
				best = ClassicBot.search(grid, activePiece, nextPiece, weights, 0);
			}

			if (lines >= fitLimit) fitness++;
		}

		fitness = fitness / FIT_ITER;

		if (VERBOSE > 1) System.out.println(" = " + fitness);

		return fitness;
	}
	
	public static int fitnessOld1(EvalWeights weights, Random r) {
		
		if (VERBOSE > 1) System.out.print("... evaluating " + weights.toString());
		
		boolean[][] grid = Grid.emptyGrid();
		
		int lines = 0;

		int activePiece = r.nextInt(7);
		int nextPiece = r.nextInt(7);
		Move best = ClassicBot.search(grid, activePiece, nextPiece, weights, 0);

		while (best != null) {

			best.place(grid);
			lines += best.getLinesCleared();

			activePiece = nextPiece;
			nextPiece = r.nextInt(7);
			best = ClassicBot.search(grid, activePiece, nextPiece, weights, 0);
		}
		
		if (VERBOSE > 1) System.out.println(" = " + lines);
		
		return lines;
	}
	
	private static void recomputeFitness(EvalInd[] pop, int fitLimit, Random r) {
		
		for (int i = 0; i < pop.length; i++)
			pop[i].eval = fitness(pop[i].weights, fitLimit, r);
	}

	private static EvalInd[] initialization(int fitLimit, Random r) {
				
		EvalInd[] pop = new EvalInd[POPSIZE];
		
		for (int i = 0; i < POPSIZE; i++) {
			
			EvalWeights weights = new EvalWeights(r);
			
			pop[i] = new EvalInd(weights, fitness(weights, fitLimit, r));
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
			
			double[] ch1 = new double[EvalWeights.NUM];
			double[] ch2 = new double[EvalWeights.NUM];

			for (int j = 0; j < EvalWeights.NUM; j++) {

				double x1 = sPop[2 * i].weights.weights[j];
				double x2 = sPop[2 * i + 1].weights.weights[j];
				double min = Math.min(x1, x2);
				double max = Math.max(x1, x2);
				double dev = fee[j];// * (max - min);
				double gimin = min + dev;
				double gimax = max - dev;
				
				ch1[j] = Math.min(1, Math.max(0, gimin + (gimax - gimin) * r.nextDouble()));
				ch2[j] = Math.min(1, Math.max(0, min + max - ch1[j]));
			}
			
			EvalWeights ch1ew = new EvalWeights(ch1);
			xPop[2 * i] = new EvalInd(ch1ew, fitness(ch1ew, fitLimit, r));
			
			EvalWeights ch2ew = new EvalWeights(ch2);
			xPop[2 * i + 1] = new EvalInd(ch2ew, fitness(ch2ew, fitLimit, r));
		}
		
		return xPop;
	}

	private static double[] fee(EvalInd[] pop) {

		double[] fee = new double[EvalWeights.NUM];
		double[] min = new double[]{ 1, 1, 1, 1 };
		double[] max = new double[]{ 0, 0, 0, 0 };
		
		// Genetic diversity computation
		for (int i = 0; i < pop.length; i++) {
			
			EvalWeights ind = pop[i].weights;
			
			for (int j = 0; j < EvalWeights.NUM; j++) {
				
				if (ind.weights[j] < min[j]) min[j] = ind.weights[j];
				if (ind.weights[j] > max[j]) max[j] = ind.weights[j];				
			}
		}
		
		for (int i = 0; i < EvalWeights.NUM; i++) {
			
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
	
	public static void main (String args[]) {
	
		learn();
	}
}
