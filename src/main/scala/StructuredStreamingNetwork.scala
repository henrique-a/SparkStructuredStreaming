import org.apache.log4j._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._

object StructuredStreamingNetwork {

  def main(args: Array[String]) {

    Logger.getLogger("org").setLevel(Level.ERROR)

    val spark = SparkSession
      .builder
      .appName("StructuredStreamingNetwork")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Create DataFrame representing the stream of input lines from connection to localhost:9999
    val lines = spark.readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 9999)
      .load()

    // Split the lines into words
    val words = lines.as[String]flatMap(_.split(" "))

    // Generate running word count
    val wordCounts = words.groupBy("value").count()

    // Start running the query that prints the running counts to the console
    val query = wordCounts.writeStream
      .outputMode("complete")
      .format("console")
      .start()

    query.awaitTermination()
  }
}