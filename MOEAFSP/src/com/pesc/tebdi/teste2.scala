package com.pesc.tebdi

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
import chapter.KnapsackProblem
import org.moeaframework.util.Vector;

import scala.collection.JavaConversions._
import org.moeaframework.core.operator.RandomInitialization

object teste2 extends App {

  val problem = new KnapsackProblem();
  
//  val initialization = new RandomInitialization(
//                problem,
//                100);

  val nsgaii = AlgorithmFactory.getInstance().getAlgorithm("NSGAII", new Properties(), problem).asInstanceOf[NSGAII];

  for (_ <- 1 to 1000) nsgaii.step()

  val result = nsgaii.getResult
  
  println(nsgaii getPopulation() size())

  var i = 1
  for (solution <- result) {

    var objectives = solution.getObjectives();

    // negate objectives to return them to their maximized form
    objectives = Vector.negate(objectives);

    println("Solution " + i + ":");
    println("	" + objectives(0));
    println("	" + objectives(1));
    println("	" + solution.getVariable(0));

    i += 1

  }

  val p = new Plot()
    .add("NSGAII", result)
    .show();

}