package com.bfd.parse.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JD、suning列表页转码
 * @author Administrator
 *
 */

public class UrlEncode {

	public static void main(String[] args) {
		File readFile = new File("F:\\wlm_resource\\keyword\\ec_keyword.txt");
		File writeFile = new File("F:\\wlm_resource\\keyword\\ec_keyword_encode.txt");
		try {
			FileReader reader = new FileReader(readFile);
			BufferedReader br = new BufferedReader(reader);
			FileWriter writer = new FileWriter(writeFile);
			BufferedWriter bw = new BufferedWriter(writer);
			String line = null;
			// http://search.yhd.com/c0-0/多芬密集滋养洗发%20700ml#page=1
			// 一号店
		/*	while ((line = br.readLine()) != null) {
				String yhdRegex = "c0-0/(.*)#page";
				String head = "http://search.yhd.com/c0-0/k";
				String tail = "#page=1";
				Matcher match = Pattern.compile(yhdRegex).matcher(line);
				if (match.find()) {
					String urlKeyword = match.group(1);
					urlKeyword = URLEncoder.encode(urlKeyword, "UTF-8");
					urlKeyword = URLEncoder.encode(urlKeyword, "UTF-8");
					StringBuffer sb = new StringBuffer();
					// url-keyword在encode之后，前面需要添加上"k"
					line = sb.append(head).append(urlKeyword).append(tail).toString();
					bw.write(line);
					bw.write("\r");
				} else {
					break;
				}
			}*/
			
			// 京东
			while ((line = br.readLine()) != null) {
				String jdRegex = "keyword=(.*)&enc";
				String head = "https://search.jd.com/Search?keyword=";
				String tail = "&enc=utf-8&qrst=1";
				Matcher match = Pattern.compile(jdRegex).matcher(line);
				if (match.find()) {
					String urlKeyword = match.group(1);
					urlKeyword = URLEncoder.encode(urlKeyword, "UTF-8");
					// urlKeyword = URLEncoder.encode(urlKeyword, "UTF-8");
					StringBuffer sb = new StringBuffer();
					// url-keyword在encode之后，前面需要添加上"k"
					line = sb.append(head).append(urlKeyword).append(tail).toString();
					bw.write(line);
					bw.write("\r");
				}
			}

		//苏宁易购  --苏宁不可以直接转码，但是特殊符号如‘空格’'-'必须转换，不然搜索结果错误
				/*while ((line = br.readLine()) != null) {
				String suningRegex = "suning.com/(.*)/";
				String head = "https://search.suning.com/";
				String tail = "/";
				Matcher match = Pattern.compile(suningRegex).matcher(line); 
				if (match.find()) {
					String urlKeyword = match.group(1);
					if(urlKeyword.contains(" ")) {
						urlKeyword = urlKeyword.replace(" ", "%20");
					}
					if(urlKeyword.contains("-")) {
						urlKeyword = urlKeyword.replace("-", "%252d");
					}
					StringBuffer sb = new StringBuffer();
					// url-keyword在encode之后，前面需要添加上"k"
					line = sb.append(head).append(urlKeyword).append(tail).toString();
					bw.write(line);
					bw.write("\r");
				}
			}*/
			
			//飞牛网
			//http://search.feiniu.com/?q=%E7%BE%8E%E8%B5%9E%E8%87%A3&page=1
/*			while ((line = br.readLine()) != null) {
				String feiniuRegex = "q=(.*)&page";
				String head = "http://search.feiniu.com/?q=";
				String tail = "&page=1";
				Matcher match = Pattern.compile(feiniuRegex).matcher(line);
				if (match.find()) {
					String urlKeyword = match.group(1);
					urlKeyword = URLEncoder.encode(urlKeyword, "UTF-8");
					StringBuffer sb = new StringBuffer();
					// url-keyword在encode之后，前面需要添加上"k"
					line = sb.append(head).append(urlKeyword).append(tail).toString();
					bw.write(line);
					bw.write("\r");
				}
			}*/
			
			//亚马逊
			/*while ((line = br.readLine()) != null) {
				String feiniuRegex = "field-keywords=(.*)";
				String head = "https://www.amazon.cn/s/ref=nb_sb_noss_1?__mk_zh_CN=亚马逊网站&url=search-alias%3Daps&field-keywords=";
				Matcher match = Pattern.compile(feiniuRegex).matcher(line);
				if (match.find()) {
					String urlKeyword = match.group(1);
					urlKeyword = URLEncoder.encode(urlKeyword, "UTF-8");
					StringBuffer sb = new StringBuffer();
					// url-keyword在encode之后，前面需要添加上"k"
					line = sb.append(head).append(urlKeyword).toString();
					bw.write(line);
					bw.write("\r");
				}
			}*/
			System.out.println("success!");
			bw.close();
			br.close();
			writer.close();
			reader.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
