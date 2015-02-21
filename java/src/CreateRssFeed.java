import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class CreateRssFeed {

	public static void main(String[] args) {
		LinkedList<String> items = new LinkedList<String>();
		File dir = new File("/var/www/indiedisco/");
		List<File> list = Arrays.asList(dir.listFiles());
		Collections.sort(list);
		for (int i = list.size() - 1; i >= 0; i--) {
			File f = list.get(i);
			String s = f.getName();
			if (s.endsWith(".mp3") && s.contains("_(")) {
				s = s.substring(s.indexOf("_(") + 2);
				s = s.substring(0, s.indexOf(")"));
				int indexOf = Integer.parseInt(s);
				String otherName = f.getPath().replace("_(" + indexOf,
						"_(" + (indexOf - 1));
				if (indexOf == 1)
					otherName = f.getPath().replace("_(" + indexOf + ")", "");
				try {
					cat(f.getPath(), otherName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		for (File f : dir.listFiles()) {

			if (f.getName().endsWith(".mp3")) {
				Path path = FileSystems.getDefault().getPath("/var/www/indiedisco/", f.getName());
				BasicFileAttributes attr;
				try {
					attr = Files.readAttributes(path, BasicFileAttributes.class);
					Date time = new Date(attr.creationTime().toMillis());
					items.add("\t<item>");
					items.add("\t\t<title>"+ f.getName().replaceAll("_", " ").replace(".mp3", "") + "</title>");
					items.add("\t\t<description>Indie Disco with Clowd on Spin1038</description>");
					items.add("\t\t<itunes:subtitle>Indie Disco with Clowd on Spin1038</itunes:subtitle>");
					items.add("\t\t<itunes:summary>Indie Disco with Clowd on Spin1038</itunes:summary>");
					items.add("\t\t<itunes:image href='http://spin1038.com/content/003/images/podcasts/000001/18978_player_podcast_series_230_1400x1400.jpg' />");
					items.add("\t\t<pubDate>" + getDateAsRFC822String(time)+ "</pubDate>");
					items.add("\t\t<enclosure url='http://128.204.195.198/indiedisco/"+ f.getName()+ "' length='"+ f.length()+ "' type='audio/mpeg'/>");
					items.add("\t\t<guid>http://128.204.195.198/indiedisco/"+ f.getName() + "</guid>");
					items.add("\t</item>");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("feed.xml"));
			writeFile("header.xml", bw);
			for (String s : items) {
				// System.out.println(s);
				bw.write(s + "\n");
			}
			writeFile("footer.xml", bw);
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static SimpleDateFormat RFC822DATEFORMAT = new SimpleDateFormat(
			"EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

	public static String getDateAsRFC822String(Date date) {
		return RFC822DATEFORMAT.format(date);
	}

	private static void cat(String path, String otherName) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter("merge.log", true));
		bw.write((new Date()) + " : " + path + " : " + otherName + "\n");
		bw.flush();
		bw.close();
		// exec("/root/indiedisco/cat.sh "+path+" "+otherName);
		String[] cmds = new String[] { "/bin/bash", "-c",
				"/root/indiedisco/cat.sh", path, otherName };
		try {
			Runtime.getRuntime().exec(cmds).waitFor();
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeFile(String string, BufferedWriter bw)
			throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(string));
		String line;
		while ((line = br.readLine()) != null) {
			bw.write(line + "\n");
		}
		br.close();
		bw.flush();
	}

}
