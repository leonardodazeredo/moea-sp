package com.pesc.tebdi

import org.moeaframework.Executor
import chapter.KnapsackProblem
import org.moeaframework.core.NondominatedPopulation
import org.moeaframework.analysis.plot.Plot
import org.moeaframework.util.Vector;

import scala.collection.JavaConversions._


object teste extends App {

      val exe = new Executor()
				.withProblemClass(classOf[KnapsackProblem])
				.withAlgorithm("NSGAII")
				.withMaxEvaluations(10000);
		
		val result = exe
				.distributeOnAllCores()
				.run();
		
//		exe.
		
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