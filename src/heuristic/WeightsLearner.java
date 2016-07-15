package heuristic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import core.Grid;
import core.Misc;
import core.Move;

/**
 * Class containing the implementation of the heuristic optimization using a genetic algorithm.
 */
public class WeightsLearner {

	private static final int WEIGHT_NUM = 11;
	private static final int POPSIZE = 24;
	private static final int FIT_ITER = 1;
	private static final int ITER_DIFF = 50;

	private static final int MMX_PARENTS = 3;
	private static final double FEE_A = -0.001;
	private static final double FEE_B = -0.5;
	private static final double FEE_C = 0.7;
	private static final double FEE_D = 0.25;
	
	private static final double MAX_LINES_WITHOUT_OUTPUT = 100000;

	private static final boolean NEXT_PIECE = false;
	private static final int PRED_DEPTH = 0;
	private static final boolean REDUCED = true;

	/**
	 * Executes the optimization process.
	 */
	public static void main (String args[]) {
		
		learn();
	}

	/**
	 * Implements the optimization process.
	 */
	private static void learn() {

		Random rand = new Random();
		long time = 0;
		
		ArrayList<Double> bestFit = new ArrayList<Double>();
		ArrayList<Double> avgFit = new ArrayList<Double>();

		System.out.println("Initializating population... [POPSIZE = " + POPSIZE + "]");
		Individual[] pop = initialization(rand);
		
		for (int i = 0; i < ITER_DIFF || avgFit.get(i - 1) > avgFit.get(i - ITER_DIFF); i++) {

			System.out.println("\nIteration " + (i + 1));
			time = System.currentTimeMillis();

			System.out.println("Selection...");
			Individual[] sPop = selection(pop, rand);
			System.out.println("Crossover...");
			Individual[] xPop = crossover(sPop, rand);
			System.out.println("Replacement...");
			pop = replacement(pop, xPop);
			System.out.println("Fitness update...");
			fitnessUpdate(pop,  rand);

			time = System.currentTimeMillis() - time;
			System.out.println("Iteration completed in " + (time / 1000.0) + " seconds");

			double sum = 0;
			Individual best = pop[0];
			
			for (int j = 0; j < POPSIZE; j++) {
				
				sum += pop[j].eval;
				if (pop[j].eval > best.eval) best = pop[j];
			}
			
			System.out.println("Best fitness: " + best.eval + " (age " + best.age + ")");
			System.out.println("Avg. fitness: " + (sum / POPSIZE));
			
			bestFit.add(best.eval);
			avgFit.add(sum / POPSIZE);
			
			System.out.println("bestFit = " + Misc.arrayListToString(bestFit));
			System.out.println("avgFit = " + Misc.arrayListToString(avgFit));
		}
	}

	/**
	 * Computes the fitness of an individual.
	 */
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
				int linesCleared = best.getLinesCleared();
				lines += linesCleared;
				
				if (linesCleared > 0 && lines % MAX_LINES_WITHOUT_OUTPUT < linesCleared)
					System.out.println(Misc.arrayToString(weights) + " > "
						+ (int) (lines - lines % MAX_LINES_WITHOUT_OUTPUT) + "!");

				activePiece = nextPiece;
				nextPiece = rand.nextInt(7);
				best = search(grid, activePiece, nextPiece, weights);
			}
			
			fitness += lines;
		}
		
		return fitness / FIT_ITER;
	}

	/**
	 * Calls the appropriate heuristic search method depending on the arguments.
	 */
	private static Move search(boolean[][] grid, int activePiece, int nextPiece, double[] weights) {
	
		if (NEXT_PIECE) return HeuristicAI.search(grid, activePiece, nextPiece, weights, PRED_DEPTH);
		if (REDUCED) return HeuristicAI.search(Grid.getSteps(grid), activePiece, weights, PRED_DEPTH).fixRow(grid);
		return HeuristicAI.search(grid, activePiece, weights, PRED_DEPTH);
	}

	/**
	 * Initializes the population.
	 */
	private static Individual[] initialization(Random rand) {

		Individual[] pop = new Individual[POPSIZE];

		for (int i = 0; i < POPSIZE; i++) {

			double[] weights = randomWeights(rand);
			pop[i] = new Individual(weights, fitness(weights, rand), 1);
			System.out.println(pop[i]);
		}

		return pop;
	}

	/**
	 * Selects individuals for crossover.
	 */
	private static Individual[] selection(Individual[] pop, Random rand) {

		Individual[] sPop = new Individual[POPSIZE / 2];

		ArrayList<Individual> pool = new ArrayList<Individual>(POPSIZE);
		pool.addAll(Arrays.asList(pop));
		Collections.shuffle(pool, rand);

		for (int i = 0; i < sPop.length; i++) {

			Individual ind1 = pool.get(2 * i);
			Individual ind2 = pool.get(2 * i + 1);

			if (ind1.eval < ind2.eval) sPop[i] = ind2;
			else sPop[i] = ind1;
		}

		return sPop;
	}

	/**
	 * Generates new individuals using the crossover operator (MMX).
	 */
	private static Individual[] crossover(Individual[] sPop, Random rand) {

		Individual[] xPop = new Individual[(sPop.length / MMX_PARENTS) * 2];

		for (int i = 0; i < sPop.length / MMX_PARENTS; i++) {

			double[] ch1 = new double[WEIGHT_NUM];
			double[] ch2 = new double[WEIGHT_NUM];
			
			Individual[] parents = new Individual[MMX_PARENTS];
			for (int j = 0; j < MMX_PARENTS; j++) parents[j] = sPop[MMX_PARENTS * i + j];
			
			double[][] intervals = xIntervals(parents);

			for (int j = 0; j < WEIGHT_NUM; j++) {

				ch1[j] = Math.min(1, Math.max(0,
						intervals[j][0] + (intervals[j][1] - intervals[j][0]) * rand.nextDouble()));
				
				ch2[j] = Math.min(1, Math.max(0,
						intervals[j][0] + intervals[j][1] - ch1[j]));
			}

			Misc.normalizeArray(ch1);
			xPop[2 * i] = new Individual(ch1, fitness(ch1, rand), 1);
			System.out.println(xPop[2 * i]);

			Misc.normalizeArray(ch2);
			xPop[2 * i + 1] = new Individual(ch2, fitness(ch2, rand), 1);
			System.out.println(xPop[2 * i + 1]);
		}

		return xPop;
	}

	/**
	 * Computes the intervals for MMX.
	 */
	private static double[][] xIntervals(Individual[] parents) {

		double[][] intervals = new double[WEIGHT_NUM][2];
		
		double[] fee = new double[WEIGHT_NUM];
		double[] min = new double[WEIGHT_NUM];
		double[] max = new double[WEIGHT_NUM];

		for (int i = 0; i < WEIGHT_NUM; i++) min[i] = 1;

		for (int i = 0; i < parents.length; i++) {

			double[] parent = parents[i].weights;

			for (int j = 0; j < WEIGHT_NUM; j++) {

				if (parent[j] < min[j]) min[j] = parent[j];
				if (parent[j] > max[j]) max[j] = parent[j];				
			}
		}

		// Genetic diversity
		for (int i = 0; i < WEIGHT_NUM; i++) fee[i] = max[i] - min[i];

		for (int i = 0; i < WEIGHT_NUM; i++) {
			
			// Exploration-Exploitation function
			if (fee[i] < FEE_C) fee[i] = FEE_A + (fee[i] * ((FEE_B - FEE_A) / FEE_C));
			else fee[i] = ((fee[i] - FEE_C) * (FEE_D / (1 - FEE_C)));
			
			// Crossover intervals
			intervals[i][0] = min[i] + fee[i];
			intervals[i][1] = max[i] - fee[i];
		}
		
		return intervals;
	}

	/**
	 * Selects the population for the next iteration.
	 */
	private static Individual[] replacement(Individual[] pop, Individual[] xPop) {

		ArrayList<Individual> newPop = new ArrayList<Individual>(pop.length + xPop.length);
		newPop.addAll(Arrays.asList(pop));
		newPop.addAll(Arrays.asList(xPop));
		newPop.sort(new IndividualComparator());
		return newPop.subList(0, pop.length).toArray(new Individual[0]);
	}

	/**
	 * Updates the fitness of the entire population.
	 */
	private static void fitnessUpdate(Individual[] pop, Random rand) {
	
		for (int i = 0; i < pop.length; i++) {
	
			pop[i].eval = (pop[i].eval * pop[i].age + fitness(pop[i].weights, rand)) / ++pop[i].age;
			System.out.println(pop[i]);
		}
	}

	/**
	 * Generates a random individual.
	 */
	private static double[] randomWeights(Random rand) {

		double[] weights = new double[WEIGHT_NUM];
		for (int i = 0; i <weights.length; i++) weights[i] = rand.nextDouble();
		Misc.normalizeArray(weights);
		return weights;
	}
}
