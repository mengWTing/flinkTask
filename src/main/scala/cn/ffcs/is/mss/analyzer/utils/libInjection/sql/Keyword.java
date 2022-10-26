package cn.ffcs.is.mss.analyzer.utils.libInjection.sql;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;

class Keyword {

	HashMap<String, Character> keywordMap = new HashMap<String, Character>();

	Keyword(String filename, String fileSystemType) {
		String word;
		char type;
		Pattern wordpattern, typepattern;
		Matcher matchedword, matchedtype;

		try {
			org.apache.hadoop.fs.FileSystem fileSystem = org.apache.hadoop.fs.FileSystem
				.get(URI.create(fileSystemType), new Configuration());
			FSDataInputStream fsDataInputStream = fileSystem.open(new Path(filename));
			BufferedReader br = new BufferedReader(new InputStreamReader(fsDataInputStream));
			String line = "";
			while ((line = br.readLine()) != null) {
				wordpattern = Pattern.compile("\\{\"(.*)\"");
				typepattern = Pattern.compile("\'(.*)\'");
				matchedword = wordpattern.matcher(line);
				matchedtype = typepattern.matcher(line);

				while (matchedword.find() && matchedtype.find()) {
					word = matchedword.group(1);
					type = matchedtype.group(1).charAt(0);
					keywordMap.put(word, type);
				}
			}
			br.close();
		} catch (FileNotFoundException ex) {
			System.out.println("file not found");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void printKeywordMap() {
		for (String keyword : keywordMap.keySet()) {
			String keytype = keywordMap.get(keyword).toString();
			System.out.println("word: " + keyword + " type: " + keytype);
		}
		System.out.println("table size: " + keywordMap.size());
	}
}
