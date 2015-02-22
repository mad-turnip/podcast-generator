import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;


public class CreateRssFeed {

	public static void main(String[] args) {
		boolean mergeFiles = false;
		String propsFile = args[0];
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(propsFile));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.err.println("Expected argument 1 to be a valid properties file");
			return;
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		
		String show = prop.getProperty("show");
		
		String headerXml = prop.getProperty("header");
		String footerXml = prop.getProperty("footer");

		if(args.length >= 2){
			try {
				mergeFiles = Boolean.parseBoolean(args[1]);
			} catch(Exception e){
				System.err.println("Expected argument 2 to be a boolean value");
				return;
			}
		}
		LinkedList<String> items = new LinkedList<String>();
		File dir = new File("/var/www/"+show+"/");
		
		if(mergeFiles){
			mergeMp3Parts(dir);
		}
		
		//if file dates get reset this allows you to reset them based on the filename
		//resestFileDates(dir);
		
		
		items.add("\t<lastBuildDate>"+getDateAsRFC822String(new Date())+"</lastBuildDate>");
		addMp3sToPodcast(items,prop,dir);
		
		writePodcastRss(items,headerXml,footerXml);
		
	}

	private static void writePodcastRss(LinkedList<String> items,String headerXml, String footerXml) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("feed.xml"));
			writeFile(headerXml, bw);
			for (String s : items) {
				bw.write(s);
				bw.newLine();
			}
			writeFile(footerXml, bw);
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void addMp3sToPodcast(LinkedList<String> items,Properties prop,File dir) {
		
		String show = prop.getProperty("show");
		String description = prop.getProperty("description");
		String cover = prop.getProperty("cover");
		String host = prop.getProperty("host");
		List<File> list = Arrays.asList(dir.listFiles());
		Collections.sort(list);
		for (int i = list.size() - 1; i >= 0; i--) {
			File f = list.get(i);
			if (f.getName().endsWith(".mp3")) {
				Path path = FileSystems.getDefault().getPath("/var/www/"+show+"/", f.getName());
				BasicFileAttributes attr;
				try {
					attr = Files.readAttributes(path, BasicFileAttributes.class);
					Date time = new Date(attr.creationTime().toMillis());
					items.add("\t<item>");
					items.add("\t\t<title>"+ f.getName().replaceAll("_", " ").replace(".mp3", "") + "</title>");
					items.add("\t\t<description>"+description+"</description>");
					items.add("\t\t<itunes:subtitle>"+description+"</itunes:subtitle>");
					items.add("\t\t<itunes:summary>"+description+"</itunes:summary>");
					items.add("\t\t<itunes:image href='"+cover+"' />");
					items.add("\t\t<pubDate>" + getDateAsRFC822String(time)+ "</pubDate>");
					items.add("\t\t<enclosure url='http://"+host+"/"+show+"/"+ f.getName()+ "' length='"+ f.length()+ "' type='audio/mpeg'/>");
					items.add("\t\t<guid>http://"+host+"/"+show+"/"+ f.getName() + "</guid>");
					items.add("\t</item>");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private static void resestFileDates(File dir) {
		List<File> list = Arrays.asList(dir.listFiles());
		Collections.sort(list);
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
		for (int i = list.size() - 1; i >= 0; i--) {
			File f = list.get(i);
			Date d;
			try {
				String time = f.getName().substring(12,18);
				d = sdf.parse(time);
				Path path = Paths.get(f.getAbsolutePath());
		        FileTime fileTime = FileTime.fromMillis(d.getTime());
		        BasicFileAttributeView attributes = Files.getFileAttributeView(path, BasicFileAttributeView.class);
		        attributes.setTimes(fileTime, fileTime, fileTime);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}

	private static SimpleDateFormat RFC822DATEFORMAT = new SimpleDateFormat(
			"EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

	private static String getDateAsRFC822String(Date date) {
		return RFC822DATEFORMAT.format(date);
	}

	private static void cat(String path, String otherName) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter("merge.log", true));
		bw.write((new Date()) + " : " + path + " : " + otherName + "\n");
		bw.flush();
		bw.close();
		
		OutputStream out = new FileOutputStream(otherName,true);
	    byte[] buf = new byte[1024];
	    InputStream in = new FileInputStream(path);
		int b = 0;
		while ( (b = in.read(buf)) >= 0) {
	        out.write(buf, 0, b);
	    }
		out.flush();
		in.close();
	    out.close();
	    
	    File f = new File(path);
	    f.delete();
	}

	private static void writeFile(String fileName, BufferedWriter bw)
			throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line;
		while ((line = br.readLine()) != null) {
			bw.write(line);
			bw.newLine();
		}
		br.close();
		bw.flush();
	}
	
	private static void mergeMp3Parts(File dir){
		List<File> list = Arrays.asList(dir.listFiles());
		Collections.sort(list);
		for (int i = list.size() - 1; i >= 0; i--) {
			File f = list.get(i);
			String s = f.getName();
			if (s.endsWith(".mp3") && s.contains("_(")) {
				s = s.substring(s.indexOf("_(") + 2);
				s = s.substring(0, s.indexOf(")"));
				int indexOf = Integer.parseInt(s);
				String otherName = f.getPath().replace("_(" + indexOf,"_(" + (indexOf - 1));
				if (indexOf == 1)
					otherName = f.getPath().replace("_(" + indexOf + ")", "");
				try {
					cat(f.getPath(), otherName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
