package com.infa.presales.spark;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;

// See https://communities.informatica.com/infakb/faq/5/Pages/80008.aspx
import scala.Tuple2;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

/**
 * Main Class - Creates a spark streaming job with UMQ receiver and does a word count on the input
 * @author pyoung
 *
 */
public class UmqWordCount  {
	private static final Pattern SPACE = Pattern.compile(" ");

	private static Iterable<String> iterable(final Iterator<String> iterator) {
		if (iterator == null) {
			throw new NullPointerException();
		}
		return new Iterable<String>() {
			public Iterator<String> iterator() {
				return iterator;
			}
		};
	}

	private static PairFunction<String, String, Integer> pairFunc = new PairFunction<String, String, Integer>() {
		private static final long serialVersionUID = 4895567529955824249L;

		@Override
		public Tuple2<String, Integer> call(String s) {
			return new Tuple2<>(s, 1);
		}
	};
	
	private static FlatMapFunction<String, String> flatMapFunc = new FlatMapFunction<String, String>() {
		private static final long serialVersionUID = 526350238754780048L;
		// Changed return type from Iterator<String>
		@Override
		public Iterable<String> call(String x) {
			Iterable<String> iter = iterable( Arrays.asList(SPACE.split(x)).iterator());
			return  iter;
		}
	};
	
	private static Function2<Integer, Integer, Integer> func2 = new Function2<Integer, Integer, Integer>() {
		private static final long serialVersionUID = -7724047571776423617L;

		@Override
		public Integer call(Integer i1, Integer i2) {
			return i1 + i2;
		}
	};

	private static void setStreamingLogLevels() {
		Logger logger = Logger.getRootLogger();
		boolean log4jInitialized = logger.getAllAppenders().hasMoreElements();
		if (!log4jInitialized) {
			// We first log something to initialize Spark's default logging,
			// then we override the
			// logging level.
			logger.warn("Setting log level to [WARN] for streaming example."
					+ " To override add a custom log4j.properties to the classpath.");
			logger.setLevel(Level.WARN);
		}
		logger.setLevel(Level.WARN);
	}

	public static void main(String[] args) throws Exception {
		String m_appName = Thread.currentThread().getStackTrace()[1].getClassName();

		UmqSparkReceiver rcvapp = new UmqSparkReceiver(args);
		setStreamingLogLevels();
		
		System.out.println("Starting main");
		SparkConf sparkConf = new SparkConf().setAppName(m_appName);
		JavaStreamingContext ssc = new JavaStreamingContext(sparkConf, new Duration(5000));
		JavaReceiverInputDStream<String> lines = ssc.receiverStream(rcvapp);
		JavaDStream<String> words = lines.flatMap(flatMapFunc);
		JavaPairDStream<String, Integer> wordCounts = words.mapToPair(pairFunc).reduceByKey(func2);

		wordCounts.print(100);
		ssc.start();
		ssc.awaitTermination();
		ssc.close();
	}
	
}
