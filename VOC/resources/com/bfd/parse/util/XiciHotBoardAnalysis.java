package com.bfd.parse.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.bfd.crawler.utils.FileUtil;

public class XiciHotBoardAnalysis {
	private static final Log LOG = LogFactory.getLog(XiciHotBoardAnalysis.class);
	public Map<String, String> citys = new HashMap<String, String>();

	public static void main(String[] args) {
		PropertyConfigurator.configure("/home/ian/dev/parser/log4j.properties");
		XiciHotBoardAnalysis t = new XiciHotBoardAnalysis();
		t.loadCityMap("/home/ian/dev/parser/data/city");
		System.out.println(t.citys);
		// t.test("/home/ian/dev/parser/data/hotboard.log.info");
		t.test("/home/ian/xici");
	}

	private void test(String path) {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(path, "r");
			String line;
			boolean f = true;
			int skip = 0;
			int skip2 = 0;
			int skip3 = 0;
			int total = 0;
			Date date = new Date();
			long curtime = date.getTime();
			System.out.println(curtime);
			// String ss = FileUtil.readFromFile2Str("/home/ian/test.t");
			// System.out.println(ss);
			// System.exit(0);
			while (f) {
				line = raf.readLine();
				if (line == null)
					break;
				total++;
				line = new String(line.getBytes("8859_1"), "utf8");
				String[] s = line.split("=");
				StringBuilder sb = new StringBuilder();
				try {
					// if (curtime - Long.valueOf(s[7].split(",")[0]) >
					// 2592000000L * 2) {
					// // System.out.println("skip " + s[1].split(",")[0]);
					// skip++;
					// continue;
					// }
					if (s[1].split(",")[0].trim().equals("b2488")) {
						System.out.println(line);
						skip++;
						continue;
					}
					// if (Integer.valueOf(s[8].split(",")[0].trim()) < 77) {
					// skip3++;
					// continue;
					// }
				} catch (Exception e) {
					System.out.println("ERROR, " + line);
					skip2++;
					continue;
				}
				sb.append(s[10].split(",")[0]);
				sb.append("\t");
				sb.append(s[2].split(",")[0]);
				sb.append("\t");
				sb.append(s[6].split(",")[0]);
				sb.append("\t");
				sb.append(s[7].split(",")[0]);
				sb.append("\t");
				sb.append(s[1].split(",")[0]);
				sb.append("\t");
				sb.append(s[4].split(",")[1].replaceAll("\"", ""));
				sb.append("\t");
				sb.append(s[3].split(",")[0]);
				sb.append("\t");

				String province = citys.get(s[5].split(",")[0].trim());
				if (StringUtils.isEmpty(province)) {
					province = "null";// s[5].split(",")[0].trim();
				}
				sb.append(province);
				sb.append("\t");
				sb.append(s[5].split(",")[0]);
				// System.out.println("");
				LOG.info(sb.toString());
				FileUtil.writeToFile("/home/ian/dev/parser/data/xicihotboard_res/" + province + ".csv", sb.append("\n")
						.toString().getBytes(), true);
				// f = false;
			}
			System.out.println("timeskip=" + skip + ", excptionskip=" + skip2 + ", sizeskip=" + skip3 + ", total="
					+ total);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (raf != null)
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private void loadCityMap(String path) {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(path, "r");
			String line;
			while (true) {
				line = raf.readLine();
				if (line == null)
					break;
				line = new String(line.getBytes("8859_1"), "utf8");
				String[] s = line.split(",");
				String c = s[1];
				if (c.endsWith("市") && c.length() > 2) {
					c = c.substring(0, c.lastIndexOf("市"));
				}
				citys.put(c, s[0]);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (raf != null)
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
}
