package io.bw.spark

import java.io.File

import org.apache.commons.io.FileUtils
import org.apache.spark.sql.SparkSession

/**
  * @author Byungwook lee on 2018. 8. 15.
  *         quddnr153@gmail.com
  *         https://github.com/quddnr153
  */
object WordCountExample {
  def main(args: Array[String]): Unit = {
    FileUtils.deleteDirectory(new File("word-counts"))

    val spark = SparkSession.builder
      .master("local[*]")
      .appName("WordCountExample")
      .getOrCreate()

    val shakespeare = spark.read.textFile("shakespeare.txt")

    import spark.implicits._

    val words = shakespeare.flatMap(_.toLowerCase.split("[ |.|,|)|(|<|>|\\[|\\]|'|!|;|_|?|:|\"|\\-]"))
      .map((_, 1))
      .filter(_._1.length > 0)
      .withColumnRenamed("_1", "word")
      .withColumnRenamed("_2", "value")
      .groupBy("word")
      .sum()
      .withColumnRenamed("sum(value)", "count")
      .orderBy($"count".desc)

    words.repartition(1)
      .write
      .format("csv")
      .save("word-counts")
  }
}
