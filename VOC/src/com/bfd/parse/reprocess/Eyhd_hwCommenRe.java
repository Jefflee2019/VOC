package com.bfd.parse.reprocess;

import java.text.NumberFormat;
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

import com.alibaba.fastjson.JSONObject;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点：Eyhd_hw
 * 功能：后处理获取处理评论
 * @author dph 2018年3月14日
 *
 */
public class Eyhd_hwCommenRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		
		String htmlcontent = gethtml(unit.getUrl());
		System.out.println(htmlcontent);
		Map<String,Object> parsedata = new HashMap<String, Object>(16);
		Map<String, Object> resultData = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		JSONObject json = JSONObject.parseObject(htmlcontent);  
		String value = null;
		if(json instanceof Map){
			value = (String) json.get("value");
			HtmlCleaner cleaner = new HtmlCleaner();
			TagNode tn = cleaner.clean(value);
			try {
				//评论items
				getCommentItems(tn, resultData,unit.getUrl());
				getCommentOtherInfo(tn, resultData);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			getNextpage(resultData,unit.getUrl());
		}
		return new ReProcessResult(SUCCESS, parsedata);
	}
	
	public static String gethtml (String url) {
		HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient client = builder.build();
		HttpGet request = new HttpGet(url);
		String regex = "productId=(\\d+)";
		Matcher m = Pattern.compile(regex).matcher(url);
		if(m.find()){
			String productId = m.group(1);
			request.setHeader("Referer", "http://item.yhd.com/" + productId + ".html");
		}
		try {
			HttpResponse response = client.execute(request);
			return EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	/**
	 * 评论整体其他信息(包括好评率，客户评价，全部，好评，中评，差评，晒单，有回复的回复数量)
	 * @param tn
	 * @param parsedata
	 * @throws Exception 
	 */
	private void getCommentOtherInfo(TagNode tn, Map<String, Object> parsedata) throws Exception {
		TagNode tnModCommentChart = (TagNode) (tn.evaluateXPath("//div[@class='mod_comment_content clearfix']")[0]);
		
		//好评率
		//<div class="text">
		TagNode tnText = (TagNode) (tnModCommentChart.evaluateXPath("//span[@class='pct']")[0]);
		String sGoodRate = tnText.getText().toString();	
		double a = Double.parseDouble(sGoodRate);
		double b = 100;
		NumberFormat nbf=NumberFormat.getInstance();   
		nbf.setMinimumFractionDigits(2);
		String sGoodRated = nbf.format(a/b);
		
		
		
		//评论总数
		//<li data-tpc="TOTAL">
		TagNode tnTotal = (TagNode) (tn.evaluateXPath("//li[@tag='all-comment']")[0]);
		String sCommentCnt = parseByRegex(tnTotal.getText().toString(),"(\\d+(\\.\\d+)?)");
		
		//好评数
		//<li data-tpc="GOOD">
		TagNode tnGood = (TagNode) (tn.evaluateXPath("//li[@tag='good-comment']")[0]);
		String sGoodCnt = parseByRegex(tnGood.getText().toString(),"\\d+(\\.\\d+)?");
		
		//中评数
		//<li data-tpc="MEDIUM">
		TagNode tnMedium = (TagNode) (tn.evaluateXPath("//li[@tag='general-comment']")[0]);
		String sGeneralCnt = parseByRegex(tnMedium.getText().toString(),"\\d+(\\.\\d+)?");
		
		//差评数
		//<li data-tpc="POOR">
		TagNode tnPoor = (TagNode) (tn.evaluateXPath("//li[@tag='bad-comment']")[0]);
		String sPoorCnt = parseByRegex(tnPoor.getText().toString(),"\\d+(\\.\\d+)?");
		
		//晒单数
		//<li data-tpc="SHINE">
		TagNode tnShine = (TagNode) (tn.evaluateXPath("//li[@tag='show-comment']")[0]);
		String sShowCnt = parseByRegex(tnShine.getText().toString(),"\\d+(\\.\\d+)?");
		
		
		int total = 0;
		int good = 0;
		int general = 0;
		int poor = 0;
		int withpic = 0;
		total = getTotal(sCommentCnt,tnTotal.getText().toString());
		good = getTotal(sGoodCnt,tnGood.getText().toString());
		general = getTotal(sGeneralCnt,tnMedium.getText().toString());
		poor = getTotal(sPoorCnt,tnPoor.getText().toString());
		withpic = getTotal(sShowCnt,tnShine.getText().toString());
		
		parsedata.put(Constants.GOOD_RATE, sGoodRated);
		parsedata.put(Constants.REPLY_CNT, total);
		parsedata.put(Constants.GOOD_CNT, good);
		parsedata.put(Constants.GENERAL_CNT, general);
		parsedata.put(Constants.POOR_CNT, poor);
		parsedata.put(Constants.WITHPIC_CNT, withpic);
		
	}
	
	/**
	 * 集成正则表达式匹配，
	 * 
	 * @param data (需要匹配的数据)
	 * @param regex (正则表达式)
	 * @return
	 */
	public String parseByRegex(String data, String regex) {
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(data);
		if (matcher.find()) {
			data = matcher.group();
		}
		return data;
	}
	
	private int getTotal(String num,String flag) {
		int result = 0;
			if (num.contains(".") && flag.contains("万")) {
				result = Integer.valueOf(num.split("\\.")[0]) * 10000
						+ Integer.valueOf(num.split("\\.")[1]) * 1000;
			} else if (flag.contains("万")) {
				result = Integer.valueOf(num)*10000;
			}else {
				result = Integer.valueOf(num);
			}
		return result;
	}
	
	/**
	 * 获取具体的评论Item信息
	 * @param tn
	 * @param parsedata
	 * @throws Exception 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void getCommentItems(TagNode tn, Map<String, Object> parsedata, String url)	throws Exception {
		List commentsDataList = new ArrayList();	
		Map commentsDataMap = null;
	
		String xpath = "//div[@class='item good-comment']";
		Object[] objli = null;
		objli = tn.evaluateXPath(xpath);

		if (objli != null && objli.length > 0) {
			for (Object obj : objli) {
				commentsDataMap = new HashMap();
				TagNode tnInformp = (TagNode) obj;

				// 评论人姓名 
				//<span class="name">
				TagNode tnName = (TagNode) (tnInformp.evaluateXPath("//span[@class='name']")[0]);
				
				// 评论内容  
				//<dd class="clearfix"> 
				TagNode tnText = (TagNode) (tnInformp.evaluateXPath("//span[@class='text comment_content_text']")[0]);
				String sCommentContent = tnText.getText().toString().trim();
				
				
				//评论时间 
				//<span class="date">购买方式:非合约机        颜色:白色        内存ROM:16GB        版本:官方标配        &nbsp;&nbsp;&nbsp;2016-03-03 14:27:16</span>
				//颜色:钢铁侠|规格:均码|2016-02-25 07:10:46
				//购买方式:非合约机 颜色:香槟金 内存ROM:64GB    2016-03-13 19:22:07
				TagNode tnDate = (TagNode) tnInformp.evaluateXPath("//span[@class='date']")[0];
				String sTnDate = tnDate.getText().toString().replace("&nbsp;", "");
				sTnDate = sTnDate.replaceAll("\\s{2,}", "|");
				String[] strArray = sTnDate.split("\\|");
				commentsDataMap.put(Constants.COMMENT_TIME, sTnDate);
				if (strArray != null ) {
					commentsDataMap.put(Constants.COMMENT_TIME, strArray[strArray.length-1]);
				}
				
				commentsDataMap.put(Constants.COMMENTER_NAME, tnName.getText().toString().replaceAll("\\s+", ""));
				commentsDataMap.put(Constants.COMMENT_CONTENT, sCommentContent.toString().replaceAll("\\s+", ""));
				
				commentsDataList.add(commentsDataMap);
			}
		}
		getNextpage(parsedata,url);
		parsedata.put(Constants.COMMENTS,  commentsDataList);
	}
	/**
	 * 生成下一页任务
	 * @param parsedata
	 * @param url
	 */
	private void getNextpage(Map<String, Object> parsedata,String url){
		String regex = "currentPage=(\\d+)";
		Matcher m = Pattern.compile(regex).matcher(url);
		int nextpageNum = 1;
		String nextpage;
		if(m.find()){
			nextpageNum = Integer.parseInt(m.group(1)) + 1;
		}
		nextpage = url.replaceAll("currentPage=\\d+", "currentPage=" + nextpageNum);
		parsedata.put(Constants.NEXTPAGE, nextpage);
		List<Map<String, Object>> tasks  = new ArrayList<Map<String, Object>>();
		Map<String,Object> nextpageMap = new HashMap<String,Object>();
		nextpageMap.put(Constants.LINK, nextpage);
		nextpageMap.put(Constants.RAWLINK, nextpage);
		nextpageMap.put(Constants.LINKTYPE, "eccomment");
		tasks.add(nextpageMap);
		parsedata.put(Constants.TASKS, tasks);
	}

}
