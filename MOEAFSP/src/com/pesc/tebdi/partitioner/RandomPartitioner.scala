package com.pesc.tebdi.partitioner

import scala.util.Random

import org.apache.spark.Partitioner

/**
 * Random shuffle with unbalanced partition sizes
 *
 * @author xoan
 *
 * @param numParts The number of partitions
 */
class RandomPartitioner(numParts: Int = 2) extends Partitioner {
  override def numPartitions = numParts

  /**
   * Assigns a new random partition to the individual based on its key
   * There is not guarantee about partition sizes
   *
   * @param key The individual key
   * @return The id of the partition the individual was assigned to
   */
  override def getPartition(key: Any): Int = {
    (new Random()).nextInt(numPartitions - 1)
  }

  /**
   * Test for equality of partitioners
   *
   * @param other The other partitioner
   * @return true if other has the same type and number of partitions, false otherwise
   */
  override def equals(other: Any): Boolean = other match {
    case rp: RandomPartitioner =>
      rp.numPartitions == numPartitions
    case _ =>
      false
  }
}