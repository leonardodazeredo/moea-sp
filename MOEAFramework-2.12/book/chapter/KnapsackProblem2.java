package chapter;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;
import org.moeaframework.util.Vector;

public class KnapsackProblem2 extends AbstractProblem {

	/**
	 * The number of sacks.
	 */
	public static int		nsacks		= 1;

	/**
	 * The number of items.
	 */
	public static int		nitems		= 5;

	/**
	 * Entry {@code profit[i][j]} is the profit from including item {@code j} in sack {@code i}.
	 */
	public static int[][]	profit		= { { 2, 5 }, { 1, 4 }, { 6, 2 }, { 5, 1 }, { 3, 3 } };

	/**
	 * Entry {@code weight[i][j]} is the weight incurred from including item {@code j} in sack {@code i}.
	 */
	public static int[][]	weight		= { { 3, 3 }, { 4, 2 }, { 1, 5 }, { 5, 3 }, { 5, 2 } };

	/**
	 * Entry {@code capacity[i]} is the weight capacity of sack {@code i}.
	 */
	public static int[]		capacity	= { 10, 8 };

	public KnapsackProblem2() {
		super(1, nsacks, nsacks);
	}

	public void evaluate(Solution solution) {
		boolean[] d = EncodingUtils.getBinary(solution.getVariable(0));
		double[] f = new double[nsacks];
		double[] g = new double[nsacks];

		// calculate the profits and weights for the knapsacks
		for (int i = 0; i < nitems; i++) {

			if (d[i]) {
				for (int j = 0; j < nsacks; j++) {
					f[j] += profit[i][j];
					g[j] += weight[i][j];
				}
			}

		}

		// check if any weights exceed the capacities
		for (int j = 0; j < nsacks; j++) {
			if (g[j] <= capacity[j]) {
				g[j] = 0.0;
			}
			else {
				g[j] = g[j] - capacity[j];
			}
		}

		// negate the objectives since Knapsack is maximization
		solution.setObjectives(Vector.negate(f));
		solution.setConstraints(g);

	}

	public Solution newSolution() {
		Solution solution = new Solution(1, nsacks, nsacks);
		solution.setVariable(0, EncodingUtils.newBinary(nitems));
		return solution;
	}
}
