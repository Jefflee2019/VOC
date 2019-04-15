
package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 百度知道的列表页中回复数设置成-1024，即无法实现增量
 * 
 * @author BFD_499
 *
 */
public class BbaiduzhidaoListRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		
		String htmlContent = gethtml(unit.getUrl());
//		System.err.println(htmlContent);
		Map<String,Object> parsedata = new HashMap<String, Object>(16);
		Map<String, Object> resultData = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode tn = cleaner.clean(htmlContent);
		try {
			
			getItems(tn, resultData,unit.getUrl());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return new ReProcessResult(SUCCESS, parsedata);
	}
	
	
	/**
	 * 下载页面内容
	 */
	public static String gethtml (String url) {
		String htmlContent = "";
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");//UA[(int) (Math.random()*UA.length)]
        HttpClient client = builder.build();
		HttpGet request = new HttpGet(url);
		request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		request.setHeader("Accept-Encoding", "gzip, deflate, br");
		request.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
		request.setHeader("Connection", "Keep-Alive");
		request.setHeader("Cookie", "BAIDUID=45AD09651AA8A14728FE4A94EB86C6ED:FG=1");
		request.setHeader("Host","zhidao.baidu.com");
		request.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
		try {
			HttpResponse response = client.execute(request);
			htmlContent = EntityUtils.toString(response.getEntity(),"gbk");
			return htmlContent;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	
	/**
	 * 集成正则表达式匹配，
	 * 
	 * @param data (需要匹配的数据)
	 * @param regex (正则表达式)
	 * @return
	 */
	public static String parseByRegex(String data, String regex) {
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(data);
		if (matcher.find()) {
			data = matcher.group();
		}
		return data;
	}
	
	/**
	 * 获取item
	 * @param tn
	 * @param parsedata
	 * @param url
	 * @throws Exception
	 */
	private static  void getItems(TagNode tn, Map<String, Object> parsedata, String url)	throws Exception {
		
		List<Map<String, Object>> taskList = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> itemList = new ArrayList<Map<String,Object>>();
	
		String xpath = "//div[@class='question-title-section']";
		Object[] objli = null;
		objli = tn.evaluateXPath(xpath);

		if (objli != null && objli.length > 0) {
			for (Object obj : objli) {
				TagNode tnInformp = (TagNode) obj;

				// 问题标题 
				TagNode tnTitle = (TagNode) (tnInformp.evaluateXPath("//div[@class='question-title']//a[@class='title-link']")[0]);
				String sTnTitle = tnTitle.getText().toString().replaceAll("\\s+", "");
				//title链接
				String sTnLink = (String) (tnInformp.evaluateXPath("//div[@class='question-title']//a[@class='title-link']/@href")[0]);
				// 回答数 
//				TagNode tnNum = (TagNode) (tnInformp.evaluateXPath("//div[@class='question-info']//div[@class='answer-num']")[0]);
//				String sTnNum = parseByRegex(tnNum.getText().toString(),"\\d+(\\.\\d+)?");

				//提问时间26&#x5206;&#x949F;&#x524D;（26分钟前）
				TagNode tnTime = (TagNode) (tnInformp.evaluateXPath("//div[@class='question-info']//div[@class='question-time']")[0]);
				String sTnTime = tnTime.getText().toString().replaceAll("\\s+", "");
				sTnTime = sTnTime.replace("&#x", "%u").replace(";", "");
				sTnTime = unescape(sTnTime);
				sTnTime = ConstantFunc.convertTime(sTnTime);
				String sTnTimes = sTnTime.split(" ")[0];
				sTnLink = sTnLink + "&newstime=" + sTnTimes;
				
				Map<String,Object> task = new HashMap<String,Object>(4);
				Map<String,Object> item =new HashMap<String,Object>(4);
				task.put(Constants.LINK, sTnLink);
				task.put(Constants.RAWLINK, sTnLink);
				task.put(Constants.LINKTYPE, "bbspost");
				taskList.add(task);
				Map<String,String> itemLink = new HashMap<String,String>(4);
				itemLink.put(Constants.LINK, sTnLink);
				itemLink.put(Constants.RAWLINK, sTnLink);
				itemLink.put(Constants.LINKTYPE, "bbspost");
				item.put(Constants.ITEMLINK, itemLink);
				item.put(Constants.ITEMNAME, sTnTitle);
				item.put(Constants.REPLY_CNT, Integer.valueOf(-1024));
				item.put(Constants.POSTTIME, sTnTime);
				itemList.add(item);
			}
		}
		int len = objli.length;
		getNextpage(parsedata,url,taskList,len);
		parsedata.put(Constants.TASKS, taskList);
		parsedata.put(Constants.ITEMS, itemList);
	}
	/**
	 * 生成下一页任务
	 * @param parsedata
	 * @param url
	 */
	private static void getNextpage(Map<String, Object> parsedata,String url,List<Map<String, Object>> taskList,int len){
		//每页30条
		if(len == 30){
			String regex = "pn=(\\d+)";
			Matcher m = Pattern.compile(regex).matcher(url);
			int nextpageNum = 0;
			String nextpage;
			if(m.find()){
				nextpageNum = Integer.parseInt(m.group(1)) + 30;
			}
			//百度知道最多只有26页
			if(nextpageNum < 26*30 ){
				nextpage = url.replaceAll("pn=\\d+", "pn=" + nextpageNum);
				Map<String,Object> nextpageMap = new HashMap<String,Object>();
				nextpageMap.put(Constants.LINK, nextpage);
				nextpageMap.put(Constants.RAWLINK, nextpage);
				nextpageMap.put(Constants.LINKTYPE, "bbspostlist");
				taskList.add(nextpageMap);
				parsedata.put(Constants.NEXTPAGE, nextpage);
			}
		}
	}
	/**
	 * 处理时间字段中文编码问题
	 * @param src
	 * @return
	 */
	public static String unescape(String src) {
		StringBuffer tmp = new StringBuffer();
		tmp.ensureCapacity(src.length());
		int lastPos = 0, pos = 0;
		char ch;
		while (lastPos < src.length()) {
			pos = src.indexOf("%", lastPos);
			if (pos == lastPos) {
				if (src.charAt(pos + 1) == 'u') {
					ch = (char) Integer.parseInt(src.substring(pos + 2, pos + 6), 16);
					tmp.append(ch);
					lastPos = pos + 6;
				} else {
					ch = (char) Integer.parseInt(src.substring(pos + 1, pos + 3), 16);
					tmp.append(ch);
					lastPos = pos + 3;
				}
			} else {
				if (pos == -1) {
					tmp.append(src.substring(lastPos));
					lastPos = src.length();
				} else {
					tmp.append(src.substring(lastPos, pos));
					lastPos = pos;
				}
			}
		}
		return tmp.toString();
	}

}
