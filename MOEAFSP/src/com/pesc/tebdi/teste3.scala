package com.pesc.tebdi

import scala.collection.JavaConverters._

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.moeaframework.algorithm.NSGAII
import org.moeaframework.analysis.plot.Plot
import org.moeaframework.core.EpsilonBoxDominanceArchive
import org.moeaframework.core.Initialization
import org.moeaframework.core.NondominatedSortingPopulation
import org.moeaframework.core.Problem
import org.moeaframework.core.Selection
import org.moeaframework.core.Solution
import org.moeaframework.core.Variation
import org.moeaframework.core.comparator.ChainedComparator
import org.moeaframework.core.comparator.CrowdingComparator
import org.moeaframework.core.comparator.ParetoDominanceComparator
import org.moeaframework.core.operator.GAVariation
import org.moeaframework.core.operator.RandomInitialization
import org.moeaframework.core.operator.TournamentSelection
import org.moeaframework.core.operator.real.PM
import org.moeaframework.core.operator.real.SBX

import org.moeaframework.util.Vector;

import chapter.KnapsackProblem

class NSGAII_Sp(sc: SparkContext, problem: Problem, population: NondominatedSortingPopulation, archive: EpsilonBoxDominanceArchive,
  selection: Selection, variation: Variation, initialization: Initialization) extends NSGAII(problem, population, archive, selection, variation, initialization) with Serializable {

  override def evaluateAll(solutions: java.lang.Iterable[Solution]): Unit = {

    implicit def arrayToList[A](a: Array[A]) = a.toList

    val p = problem;

    val solutionScalaList = solutions.asScala
    val solutionsRDD = sc.parallelize(solutionScalaList.to[collection.immutable.Seq])
    val rdd = solutionsRDD.map(s => { p.evaluate(s); s })
    val ss = rdd.collect

    this.numberOfEvaluations += ss.size

    var i = 0
    val iter = solutions.iterator()
    while (iter.hasNext()) {
      val s = iter.next().asInstanceOf[Solution]
      s.setConstraints(ss(i).getConstraints)
      s.setObjectives(ss(i).getObjectives)
      i += 1
    }
  }
}

object teste3 extends App {

  //Start the Spark context
  val conf = new SparkConf()
    .setAppName("teste")
    .setMaster("local")
  val sc = new SparkContext(conf)

  val problem = new KnapsackProblem();

  val initialization = new RandomInitialization(
    problem,
    1000);

  val selection = new TournamentSelection(
    2,
    new ChainedComparator(
      new ParetoDominanceComparator(),
      new CrowdingComparator()));

  val variation = new GAVariation(
    new SBX(1.0, 25.0),
    new PM(1.0 / problem.getNumberOfVariables(), 30.0));

  val algorithm = new NSGAII_Sp(
    sc,
    problem,
    new NondominatedSortingPopulation(),
    null, // no archive
    selection,
    variation,
    initialization);

  while (algorithm.getNumberOfEvaluations < 5000) {
    algorithm.step();
  }

  val intermediateResult = algorithm.getPopulation();

  val result = algorithm.getResult

  println(algorithm getPopulation () size ())

//  var i = 1
//  for (solution <- result) {
//
//    var objectives = solution.getObjectives();
//
//    // negate objectives to return them to their maximized form
//    objectives = Vector.negate(objectives);
//
//    println("Solution " + i + ":");
//    println("	" + objectives(0));
//    println("	" + objectives(1));
//    println("	" + solution.getVariable(0));
//
//    i += 1
//
//  }

  val p = new Plot()
    .add("NSGAII", result)
    .show();

}