import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WikiCrawler {
	
	private Queue<String> bfs;
	public HashMap<String, String> parent;
	public HashMap<String, Integer> depth;
	public String start, end;
	public int processed;
	public int totalLinks = 0;
	public int totalDuplicateLinks = 0;
	public double stepStartTime = 0;
	private boolean found;
	
	public String currentPage;
	
	WikiCrawler(String start, String end) {
		bfs = new LinkedList<>();
		parent = new HashMap<String, String>();
		depth = new HashMap<String, Integer>();
		this.start = start;
		this.end = end;
		bfs.add(start);
		parent.put(start, "-----");
		depth.put(start, 0);
		
		processed = 0;
		found = false;
	}
	
	public static WikipediaLinksAPIData linksOnPage(String pageName) throws IOException, ParseException {
		ArrayList<String> result = new ArrayList<String>();
		String articleName = null;
		URL url = new URL("https://en.wikipedia.org/w/api.php?action=query&titles=" + pageName.replace(" ", "%20") + "&prop=links&pllimit=max&format=json");

		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("GET");
		conn.connect();

		int responseCode = conn.getResponseCode();
		if(responseCode == 200) {
			String inline = "";
		    Scanner scanner = new Scanner(url.openStream());
		  
		    while (scanner.hasNext()) {
		       inline += scanner.nextLine();
		    }
		    
		    scanner.close();

		    JSONParser parse = new JSONParser();
		    JSONObject obj = (JSONObject) parse.parse(inline);
		    	
		    //System.out.println(obj);

		    JSONObject pages = (JSONObject)((JSONObject)obj.get("query")).get("pages");
		    String key = (String)pages.keySet().iterator().next();
		    
		    JSONArray links = (JSONArray)((JSONObject)pages.get(key)).get("links");
		    articleName = (String)((JSONObject)pages.get(key)).get("title");
		    if(links == null)
		    	return new WikipediaLinksAPIData(new ArrayList<String>(), articleName);
		    Iterator iter = links.iterator();
		    while(iter.hasNext()) {
		    	JSONObject link = (JSONObject)iter.next();
		    	if((long)link.get("ns") == 0)
		    		result.add((String)link.get("title"));
		    }
		}
		return new WikipediaLinksAPIData(result, articleName);
	}
	
	public void step() throws IOException, ParseException {
		
		stepStartTime = (double)System.currentTimeMillis() / 1000.0;
		
		String current = bfs.peek();
		int currDepth = depth.get(current);
		processed++;
		bfs.remove();
		ArrayList<String> links = linksOnPage(current).links;
		totalLinks += links.size();
		for(int i = 0; i < links.size(); i++) {
			String link = links.get(i);
			if(!parent.containsKey(link)) {
				parent.put(link, current);
				depth.put(link, currDepth + 1);
				bfs.add(link);
				if(link.equals(end))
					found = true;
			} else {
				totalDuplicateLinks++;
			}
		}
		
		currentPage = current;
		
	}
	
	public ArrayList<String> getPath(String target) {
		ArrayList<String> path = new ArrayList<String>();
		String curr = target;
		while(!curr.equals("-----")) {
			path.add(curr);
			curr = parent.get(curr);
		}
		Collections.reverse(path);
		return path;
	}
	
	public boolean finished() {
		return bfs.isEmpty() || found;
	}

	
}
