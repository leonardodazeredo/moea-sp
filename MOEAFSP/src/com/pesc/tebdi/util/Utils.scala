package com.pesc.tebdi.util

import org.moeaframework.analysis.plot.Plot
import org.moeaframework.core.NondominatedPopulation

object Utils {

  def showPlot(result: NondominatedPopulation) {
    val p = new Plot()
      .add("NSGAII", result)
      .show();
  }

}