package oculus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RssFeed {
	
	private final static String sep = System.getProperty("file.separator");
	private final String RSSFILE = System.getenv("RED5_HOME") + sep+"webapps"+sep+"oculus"+sep+"rss.xml";
	private static State state = State.getReference();
	private static Settings settings = Settings.getReference();
	private final static int LIMIT = 20; 
	
	public RssFeed() { }
	
	public void newItem(String str) {
		String title = str.substring(str.indexOf("[")+1, str.indexOf("]"));
		String description = str.substring(str.indexOf("]")+2);
		newItem(title, description);
	}
	
	public void newItem(String title, String description) {
		/*
		 * check if file exists, if so, read previous items
		 * create new item, add to top of existing items
		 * add header/footer, write to file, create file if not exists
		 */
		
		ArrayList<Item> items;
		if(new File(RSSFILE).exists()) { 
			items = readItems();  
			items.add(0, new Item(title, description));
		}
		else {
			items = new ArrayList<Item>();
			items.add(new Item(title, description));
		}
		writeFile(items);
	}
	
	private ArrayList<Item> readItems() {
		FileInputStream filein;
		ArrayList<Item> result = new ArrayList<Item>();
		try {
			filein = new FileInputStream(RSSFILE);
			BufferedReader reader = new BufferedReader(new InputStreamReader(filein));
			String line = "";
			String entirefile = "";
			while ((line = reader.readLine()) != null) { entirefile += line; }
	        Pattern p = Pattern.compile("<item>(.+?)</item>");
			Matcher m = p.matcher(entirefile);
			int i=0;
			while (m.find()) {
				i++;
				if (i<=LIMIT) {
					String str = m.group();
					String title = str.substring(str.indexOf("<title>")+7, str.indexOf("</title>"));
					String description = str.substring(str.indexOf("<description>")+13, str.indexOf("</description>"));
					String link = str.substring(str.indexOf("<link>")+6, str.indexOf("</link>"));
					String pubDate = str.substring(str.indexOf("<pubDate>")+9, str.indexOf("</pubDate>"));
					Item item = new Item(title, description);
					item.link = link;
					item.pubDate = pubDate;
					result.add(item);
				}
			}
			filein.close();
		} catch (Exception e) { e.printStackTrace(); } 			
		return result;	
	}
	
	private void writeFile(ArrayList<Item> items) {
		try {
			Util.log("writing "+RSSFILE, this);
			FileWriter fw = new FileWriter(new File(RSSFILE));
			fw.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<rss version=\"2.0\" ");
			fw.append("xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n");
			fw.append("<channel>\n<title>Oculus rss feed</title>\n");
			fw.append("<link>http://"+state.get(State.values.externaladdress)+":"+
					settings.readRed5Setting("http.port")+"/oculus/rss.xml</link>\n");
			fw.append("<description>Oculus events</description>\n");
			for (int i=0; i<items.size(); i++) {
				Item item = items.get(i);
				fw.append("<item>\n<title>"+item.title+"</title>\n<link>"+item.link+"</link>\n");
				fw.append("<description>"+item.description+"</description>\n");
				fw.append("<guid>"+item.guid+"</guid>\n");
				fw.append("<pubDate>"+item.pubDate+"</pubDate>\n</item>\n");
			}
			fw.append("</channel></rss>");
			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private class Item {
		
		public String title = null;
		public String description = null;
		public String link = null;
		public String pubDate = null; // eg Fri, 25 May 2012 12:05:32 +0000
		public String guid = null;
		
		Item(String t, String d) {
			this.title = t;
			this.description = d;
			
			DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
			Calendar cal = Calendar.getInstance();
			this.pubDate = dateFormat.format(cal.getTime());
			this.link = "http://"+state.get(State.values.externaladdress)+":"+
					settings.readRed5Setting("http.port")+"/oculus/rss.xml?title="+
					t.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
			this.guid = this.link;
		}
	}

	
	// urltitle = params[:title].strip.downcase.gsub(/[^a-z0-9]+/i, '-')
    public static void main(String[] args) {
        System.out.println("Testing");


    }

	
}
