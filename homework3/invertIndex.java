import java.io.IOException;
import java.util.StringTokenizer;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/*
gs://dataproc-9ff91a0f-99d5-4d99-b3ae-4a0e23254573-us/JAR/invertedindex.jar
gs://dataproc-9ff91a0f-99d5-4d99-b3ae-4a0e23254573-us/dev_data
gs://dataproc-9ff91a0f-99d5-4d99-b3ae-4a0e23254573-us/output1
*/
/*
	JAVA_HOME is already set-up. Do not change this.
	export PATH=${JAVA_HOME}/bin:${PATH}
	export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar
*/
public class InvertIndex {
	/*
	This is the Mapper class. It extends the Hadoop's Mapper class.
	This maps input key/value pairs to a set of intermediate(output) key/value pairs.
	Here our input key is a LongWritable and input value is a Text.
	And the output key is a Text and value is an IntWritable.
	*/
	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {
		/*
		Hadoop supported data types. This is a Hadoop specific datatype that is used to handle
		numbers and Strings in a hadoop environment. IntWritable and Text are used instead of
		Java's Integer and String datatypes.
		Here 'one' is the number of occurances of the 'word' and is set to the value 1 during the
		Map process.
		*/
		/*
			1. find all the docIds;
			2. for each docId, find those words;
			3. reduce
		*/
		// equals to integer
		// private static IntWritable docId = new IntWritable(1);

		// equals to string
		private Text word = new Text();
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			// reading input one line at a time and tokenizing.
			String line = value.toString();
			StringTokenizer itr = new StringTokenizer(line);

			// get the first token, docID, it is a string
			String id = itr.nextToken();
			IntWritable docId = new IntWritable(Integer.parseInt(id));

			// iterating through all the words avaliable in that line and forming the key value pair.
			while (itr.hasMoreTokens()) {
				word.set(itr.nextToken());
				/*
				Sending to output collector(Context) which in-turn passes the output to Reducer,
				The output is as follows:
					'word1' docId
					'word1' docId
					'word2' docId
				*/
				// (key, values) for each document
				context.write(word, docId);
			}
		}
	}

	/*
	This is the Reducer class. It extends the Hadoop's Reducer class.
	This maps the intermediate key/value pairs we get from the mapper to a set
	of output key/value pairs, where the key is the word and the value is the word's count.
	Here our input key is a Text and input value is a IntWritable.
	And the output key is a Text and value is an IntWritable.
	*/
	public static class IntSumReducer extends Reducer<Text,IntWritable,Text,IntWritable> {
		// the values
		private IntWritable result = new IntWritable();
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			int sum = 0;
			// <docId, count>
			HashMap<Integer, Integer> hmap = new HashMap<Integer, Integer>();
			// for one key, check to see if its docId exists
			// if exists, add 1
			// if does not exists, make it to be 1
			for (IntWritable val : values) {
				if (hmap.containsKey(val.get())) {
					int count = hmap.get(val.get());
					count++;
				}
				else {
					hmap.put(val.get(), 1);
				}
			}

			// show hashmap result
			// hmap.forEach((k, v) -> System.out.print(k + ": " + v));
			result.set(hmap.forEach((k, v) -> System.out.print(k + ": " + v)));
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		if (args.length != 2) {
			System.err.println("Usage: Word Count <input path> <output path>");
			System.exit(-1);
		}

		// creating a Hadoop job and assigning a job name for identification.
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "inverted index");
		job.setJarByClass(InvertIndex.class);

		// the HDFS input and output directories to be fetched from the Dataproc job submission console.
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		// providing the mapper and reducer class names.
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumReducer.class);
		job.setReducerClass(IntSumReducer.class);

		// setting the job object with the data types of output key(Text) and value(IntWritable).
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}