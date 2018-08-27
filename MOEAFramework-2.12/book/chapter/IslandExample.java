package chapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Semaphore;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.SynchronizedRandomGenerator;
import org.moeaframework.algorithm.NSGAII;
import org.moeaframework.algorithm.PeriodicAction;
import org.moeaframework.algorithm.PeriodicAction.FrequencyType;
import org.moeaframework.analysis.plot.Plot;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.spi.AlgorithmFactory;

public class IslandExample {

	public static void main(String[] args) {
		final int numberOfIslands = 4;
		final int maxEvaluations = 1000000;
		final int migrationFrequency = 10000;
		final Problem problem = new KnapsackProblem();
		final Map<Thread, NSGAII> islands = new HashMap<Thread, NSGAII>();

		// this semaphore is used to synchronize locking
		// to prevent deadlocks
		final Semaphore semaphore = new Semaphore(1);

		// need to use a synchronized random number generator
		// instead of the default
		PRNG.setRandom(new RandomAdaptor(new SynchronizedRandomGenerator(new MersenneTwister())));

		// create the algorithm run on each island
		for (int i = 0; i < numberOfIslands; i++) {
			final NSGAII nsgaii = (NSGAII) AlgorithmFactory.getInstance().getAlgorithm("NSGAII", new Properties(), problem);
			//create a periodic action for handling migration events
			final PeriodicAction migration = new PeriodicAction(nsgaii, migrationFrequency, FrequencyType.EVALUATIONS) {

				@Override
				public void doAction() {
					try {
						Thread thisThread = Thread.currentThread();

						for (Thread otherThread : islands.keySet()) {
							if (otherThread != thisThread) {
								semaphore.acquire();

								Population oldIsland = islands.get(thisThread).getPopulation();
								Population newIsland = islands.get(otherThread).getPopulation();

								synchronized (oldIsland) {
									synchronized (newIsland) {
										int emigrant = PRNG.nextInt(oldIsland.size());
										newIsland.add(oldIsland.get(emigrant));

										System.out.println("Sending solution " + emigrant + " from " + Thread.currentThread().getName() + " to " + otherThread.getName());

									}

								}

								semaphore.release();

							}

						}
					}
					catch (InterruptedException e) {
						// ignore
					}
					//					catch (ConcurrentModificationException e) {
					//						// ignore
					////						e.printStackTrace();
					//					}
				}

			};

			//start each algorithm its own thread so they run
			//concurrently
			Thread thread = new Thread() {

				public void run() {
					while (migration.getNumberOfEvaluations() < maxEvaluations) {
						migration.step();
					}
				}

			};

			islands.put(thread, nsgaii);

		}
		//start the threads
		for (Thread thread : islands.keySet()) {
			thread.start();
		}

		//wait for all threads to finish and aggregate the result
		NondominatedPopulation result = new NondominatedPopulation();

		for (Thread thread : islands.keySet()) {
			try {
				thread.join();
				result.addAll(islands.get(thread).getResult());
			}
			catch (InterruptedException e) {
				System.out.println("Thread " + thread.getId() + " was interrupted!");
			}
		}

		System.out.println("Found " + result.size() + " solutions!");

		new Plot().add("NSGAII", result).show();

	}

}
