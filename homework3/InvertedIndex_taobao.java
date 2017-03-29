import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.util.HashMap; 
import java.util.Iterator;
import java.util.ArrayList;

/*
gs://dataproc-94d03919-b676-4893-a0f3-d5fd87e1b4d0-us/JAR/invertedindex.jar
gs://dataproc-94d03919-b676-4893-a0f3-d5fd87e1b4d0-us/dev_data
gs://dataproc-94d03919-b676-4893-a0f3-d5fd87e1b4d0-us/output

hadoop fs -mkdir -p /user/shurui91
JAVA_HOME is already set-up. Do not change this.
export PATH=${JAVA_HOME}/bin:${PATH}
export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar
env
hadoop fs -ls
*/

public class InvertedIndex {
	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			String[] sp = value.toString().split(" ");
			if (sp.length >= 2) {
				int docid = Integer.parseInt(sp[0]);
				for (int i = 1; i < sp.length; ++i) {
					word.set(sp[i]);
					context.write(word, new IntWritable(docid));
				}
			}
		}
	}

	public static class IntSumReducer extends Reducer<Text, IntWritable, Text, Text> {
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			// <docId, count>
			HashMap<Integer, Integer> doccnt = new HashMap<Integer, Integer>();
			for (IntWritable val : values) {
				int docid = val.get();
				if ( doccnt.containsKey(docid) == false) {
					doccnt.put( docid, 0 );
				}
				doccnt.put( docid, doccnt.get(docid) + 1 );
			}
		
			Text result = new Text(); 
			ArrayList<String> array = new ArrayList<String>();
			Iterator it = doccnt.keySet().iterator(); 
			while (it.hasNext()) {
				int docid = (Integer)it.next();
				int cnt = doccnt.get(docid);
				array.add( docid + ":" + cnt );
			}

			String s = new String();
			for (int i = 0; i < array.size(); ++i) {
				s += array.get(i);
				if (i != array.size() - 1) {
					s += "\t";
				}
			}
			result.set(s);
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: Word Count <input path> <output path>");
			System.exit(-1);
		}

		// creating a Hadoop job and assigning a job name for identification.
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "inverted index");
		job.setJarByClass(InvertedIndex.class);

		// the HDFS input and output directories to be fetched from the Dataproc job submission console.
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		// providing the mapper and reducer class names.
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumReducer.class);
		job.setReducerClass(IntSumReducer.class);
		
		// setting the job object with the data types of output key(Text) and value(IntWritable).
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}