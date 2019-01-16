package com.ufrj.pesc.moeasp.adaptors

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.collection.JavaConverters.seqAsJavaListConverter

import org.apache.spark.SparkContext
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm
import org.moeaframework.algorithm.NSGAII
import org.moeaframework.analysis.plot.Plot
import org.moeaframework.core.EpsilonBoxDominanceArchive
import org.moeaframework.core.Initialization
import org.moeaframework.core.NondominatedPopulation
import org.moeaframework.core.NondominatedSortingPopulation
import org.moeaframework.core.Problem
import org.moeaframework.core.Selection
import org.moeaframework.core.Solution
import org.moeaframework.core.Variation
import org.moeaframework.core.comparator.ChainedComparator
import org.moeaframework.core.comparator.CrowdingComparator
import org.moeaframework.core.comparator.ParetoDominanceComparator
import org.moeaframework.core.operator.CompoundVariation
import org.moeaframework.core.operator.GAVariation
import org.moeaframework.core.operator.InjectedInitialization
import org.moeaframework.core.operator.RandomInitialization
import org.moeaframework.core.operator.TournamentSelection
import org.moeaframework.core.operator.real.PM
import org.moeaframework.core.operator.real.SBX

import com.ufrj.pesc.moeasp.core.MOEASpProblem
import com.ufrj.pesc.moeasp.core.MOEASpSolution
import com.ufrj.pesc.moeasp.core.OptimizationContext

class MOEAFrameworkAdaptor extends MOEASpAdaptor {

  def generateRandomPopulation(problem: MOEASpProblem, size: Int): Iterable[MOEASpSolution] = {

    val ini = new RandomInitialization(problem.asInstanceOf[Problem], size)

    ini.initialize().toList
  }

  def getNondominatedPopulation(population: Iterable[MOEASpSolution]): Iterable[MOEASpSolution] = {

    val solutions = new NondominatedPopulation(population.asInstanceOf[List[Solution]].asJava);

    solutions.asScala.toList
  }

  def run(pc: OptimizationContext, iniPopulation: Iterable[MOEASpSolution] = List[MOEASpSolution]()): (Iterator[MOEASpSolution], Iterator[MOEASpSolution]) = {

    if (iniPopulation.isEmpty) {
      val iniPopulation = generateRandomPopulation(pc.problem, 1000)
    }

    val initialization = new InjectedInitialization(
      pc.problem.asInstanceOf[Problem],
      iniPopulation.size,
      iniPopulation.asInstanceOf[List[Solution]].asJava);

    var maxNumberOfEvaluations = iniPopulation.size * pc.numberOfEvaluationsInIslandRatio

    var algorithm = new Object

    if (pc.algorithmId.equals("NSGAII")) {

      val selection = new TournamentSelection(
        2,
        new ChainedComparator(
          new ParetoDominanceComparator(),
          new CrowdingComparator()));

      val variation = new GAVariation(
        new SBX(1.0, 25.0),
        new PM(1.0 / pc.problem.asInstanceOf[Problem].getNumberOfVariables(), 30.0));

      algorithm = new NSGAII(
        pc.problem.asInstanceOf[Problem],
        new NondominatedSortingPopulation(),
        null, // no archive
        selection,
        variation,
        initialization);
    } else {
      throw new Exception
    }

    val algo = algorithm.asInstanceOf[AbstractEvolutionaryAlgorithm]

    while (algo.getNumberOfEvaluations < maxNumberOfEvaluations) {
      algo.step();
    }

    (algo.getResult.asScala.iterator, algo.getPopulation.asScala.iterator)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def showPlot(algorithm: String, population: Iterable[MOEASpSolution]) {

    val solutions = new NondominatedPopulation(population.asInstanceOf[List[Solution]].asJava);

    val p = new Plot()
      .add(algorithm, solutions)
      .show();
  }

  def printPopulation(result: Iterable[MOEASpSolution]) {
    for ((solution, i) <- result.toList.zipWithIndex) {
      val s = solution.asInstanceOf[Solution]

      var objectives = s.getObjectives();
      print("Solution " + i + ": (");

      objectives.foreach(o => print(" " + o))

      print(" ) ")

      println(s.getVariable(0));
    }

    println("Population size: " + result.size)

  }

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class NSGAII_SP(sc: SparkContext, problem: Problem, population: NondominatedSortingPopulation, archive: EpsilonBoxDominanceArchive,
                selection: Selection, variation: Variation, initialization: Initialization) extends NSGAII(problem, population, archive, selection, variation, initialization) with Serializable {

  override def evaluateAll(solutions: java.lang.Iterable[Solution]): Unit = {

    val p = problem

    val solutionScalaList = solutions.asScala
    val solutionsRDD = sc.parallelize(solutionScalaList.to[Seq], 4)
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