import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ExtractLinks {
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		HashMap<String,String> fileUrlMap = new HashMap<String,String>();
		HashMap<String,String> urlFileMap = new HashMap<String,String>();
		Set<String> edges = new HashSet<String>();

		File csv = new File("mapABCNewsDataFile.csv");	// 18782
		File dir = new File("ABCNewsDownloadData");		// 18782

		BufferedWriter writer = new BufferedWriter(new FileWriter("edgelist.txt"));
		String delimeter = ",";
		BufferedReader fileReader = null;

		try {
			String line = "";
			// Create the file reader
			fileReader = new BufferedReader(new FileReader(csv));

			// Read the file line by line
			while ((line = fileReader.readLine()) != null) {
				// Get all tokens available in line
				String[] tokens = line.split(delimeter);
				// 0 is the HTML filename, 1 is the URL
				fileUrlMap.put(tokens[0],tokens[1]);
				urlFileMap.put(tokens[1],tokens[0]);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		finally {
			try {
				fileReader.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// file counter
		int i = 1;
		for (File file : dir.listFiles()) {
			// the 3rd parameter is URL
			Document doc = Jsoup.parse(file, "UTF-8", fileUrlMap.get(file.getName()));
			System.out.println(i++);
			Elements links = doc.select("a[href]");
			
			for (Element link : links) {
				String url = link.attr("abs:href").trim();
				if (urlFileMap.containsKey(url)) {
					String edge = file.getName() + " " + urlFileMap.get(url);
					edges.add(edge);
				}
			}
		}

		Iterator<String> iter = edges.iterator();
		while (iter.hasNext()) {
			System.out.println(iter.next());
			writer.write(iter.next() + "\n");
		}
		writer.flush();
		writer.close();
	}
}