package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：一号店
 * 主要功能：获取评论信息
 * @author Yangzhaoyan
 *
 */
public class Eyhd_hwCommentJson implements JsonParser{

	private static final Log LOG = LogFactory.getLog(Eyhd_hwCommentJson.class);
	public Map<String,Object> test_data = new HashMap<String, Object>();
	
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient urlnormalizerClients, 
			ParseUnit unit) {
		int parsecode = 0;
		Map<String,Object> parsedata = new HashMap<String,Object>();
		String htmlcontent = gethtml(unit.getUrl());
		// 遍历dataList
		for(Object obj:dataList){
			JsonData data = (JsonData)obj;
			// 判断该ajax数据是否下载成功
			if(!data.downloadSuccess()){
				continue;
			}
			// 解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			try{
//				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
//						&& (json.indexOf("[") < json.indexOf("{"))) {
//					json = json.substring(json.indexOf("["),
//							json.lastIndexOf("]") + 1);
//				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
//					json = json.substring(json.indexOf("{"),
//							json.lastIndexOf("}") + 1);
//				}
				json = htmlcontent;
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata,json,data.getUrl(),unit);
				
			}catch(Exception e){
//				e.printStackTrace();
				LOG.warn("json:" + json + ".url:" + taskdata.get("url"));
				LOG.warn("AMJsonParse exception,taskdat url="+ taskdata.get("url") 
								+ ".jsonUrl:"+ data.getUrl(), e);
			}
		}	
		
		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try{
			result.setParsecode(parsecode);	
			result.setData(parsedata);
		}catch(Exception e){
//			e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings({"rawtypes", "unchecked" })
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		Object obj = null;
		String value = null;
		try {
			obj = JsonUtil.parseObject(json);
			if(obj instanceof Map){
				value = (String)((Map)obj).get("value");
			}
			//http://item.yhd.com/squ/comment/getCommentDetail.do?productId=5716985&pagenationVO.currentPage=1&pagenationVO.preCurrentPage=0&pagenationVO.rownumperpage=10&filter.commentFlag=0&filter.sortType=6
//			String htmlcontent = gethtml(url);
			
			//解析源码
			parseHtmlData(json, parsedata);
			
			List commentsList = (List) parsedata.get(Constants.COMMENTS);
			if(commentsList == null || commentsList.size() < 10){
				return;
			}
			//拼接下一页链接
			if(value.contains("latestnewnextpage")){
				Pattern pattern = Pattern.compile("currentPage=(\\d+)");
				Matcher matcher = pattern.matcher(url);
				if (matcher.find()) {
					int curPageNo = Integer.parseInt(matcher.group(1));
					int nextPageNo = 0;
					nextPageNo = curPageNo+1;
					String nextpageUrl = url.replaceFirst("currentPage="+curPageNo, "currentPage="+nextPageNo);
					
					List<Map<String,String>> taskList =null;
					if(parsedata.get(Constants.TASKS) != null){
						taskList = (List<Map<String,String>>) parsedata.get(Constants.TASKS);					
					}else{
						taskList = new ArrayList<Map<String,String>>();
					}
					if (curPageNo < 51) {
						Map<String,String> nexpageTask = new HashMap<String,String>();
						nexpageTask.put(Constants.LINK, nextpageUrl);
						nexpageTask.put(Constants.RAWLINK, nextpageUrl);
						nexpageTask.put(Constants.LINKTYPE, "eccomment");		
						taskList.add(nexpageTask);
						parsedata.put(Constants.NEXTPAGE, nexpageTask);
					}
				} 
			}
			
		} catch (Exception e) {
//			e.printStackTrace();
			LOG.error("json parse error or json is null");
		}
		
	}


	/**
	 * 解析JSON下载下来的HTML数据
	 * @param htmlData
	 * @param parsedata
	 * @param unit 
	 * @throws Exception
	 */
	private void parseHtmlData(String htmlData, Map<String, Object> parsedata) throws Exception {
		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode tn = cleaner.clean(htmlData);
		//评论items
		getCommentItems(tn, parsedata);
		
		//评论整体其他信息(包括好评率，客户评价，全部，好评，中评，差评，晒单，有回复的回复数量)
		getCommentOtherInfo(tn, parsedata);

	}
	
	public static String gethtml (String url) {
		HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient client = builder.build();
		HttpGet request = new HttpGet(url);
		request.setHeader("Referer", "http://item.yhd.com/5716985.html");
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
//		String sGoodRate = tnText.getText().toString() + "%";	
		String sGoodRate ="1234%";
		
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
		
		//有回复的评价数
		//<li data-tpc="REPLY">
		//TagNode tn_reply = (TagNode) (tn.evaluateXPath("//li[@data-tpc='REPLY']")[0]);
		//String has_reply_cnt = parseByRegex(tn_reply.getText().toString(),"\\d+");
		
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
		
		parsedata.put(Constants.GOOD_RATE, sGoodRate);
		parsedata.put(Constants.REPLY_CNT, total);
		parsedata.put(Constants.GOOD_CNT, good);
		parsedata.put(Constants.GENERAL_CNT, general);
		parsedata.put(Constants.POOR_CNT, poor);
		parsedata.put(Constants.WITHPIC_CNT, withpic);
		//parsedata.put(Constants.HAS_REPLY_CNT, has_reply_cnt);
		
	}

	/**
	 * 获取具体的评论Item信息
	 * @param tn
	 * @param parsedata
	 * @throws Exception 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void getCommentItems(TagNode tn, Map<String, Object> parsedata)	throws Exception {
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
				
				//点赞数 回复数
//				//<div class="praise"> 
//				TagNode tnPraise = (TagNode) tnInformp.evaluateXPath("//div[@class='praise']")[0];
//				//<span data-tpc="UP"> <em>				
//				TagNode tnFavor = (TagNode) tnPraise.evaluateXPath("//span[@data-tpc='UP']")[0];
//				TagNode tnEmFavor = (TagNode)tnFavor.evaluateXPath("//em")[0];
//				//<span data-tpc="REPLY"> <em>
//				TagNode tnReply = (TagNode) tnPraise.evaluateXPath("//span[@data-tpc='REPLY']")[0];
//				TagNode tnEmReply = (TagNode)tnReply.evaluateXPath("//em")[0];			
				
				//评论时间 
				//<span class="date">购买方式:非合约机        颜色:白色        内存ROM:16GB        版本:官方标配        &nbsp;&nbsp;&nbsp;2016-03-03 14:27:16</span>
				//颜色:钢铁侠|规格:均码|2016-02-25 07:10:46
				//购买方式:非合约机 颜色:香槟金 内存ROM:64GB    2016-03-13 19:22:07
				TagNode tnDate = (TagNode) tnInformp.evaluateXPath("//span[@class='date']")[0];
				String sTnDate = tnDate.getText().toString().replace("&nbsp;", "");
				sTnDate = sTnDate.replaceAll("\\s{2,}", "|");
				String[] strArray = sTnDate.split("\\|");
				
				if (strArray != null && strArray.length == 5) {
					commentsDataMap.put(Constants.COMMENT_TIME, strArray[4]);
					commentsDataMap.put(Constants.BUY_TYPE, strArray[2].trim());
					commentsDataMap.put(Constants.COLOR, strArray[1].trim());
					commentsDataMap.put(Constants.VERSION, strArray[3].trim());
				}
				
//				commentsDataMap.put(Constants.FAVOR_CNT, tnEmFavor==null?0:tnEmFavor.getText().toString().trim());
//				commentsDataMap.put(Constants.REPLY_CNT, tnEmReply==null?0:tnEmReply.getText().toString().trim());
				commentsDataMap.put(Constants.COMMENTER_NAME, tnName.getText().toString().replaceAll("\\s+", ""));
				commentsDataMap.put(Constants.COMMENT_CONTENT, sCommentContent.toString().replaceAll("\\s+", ""));
				
				commentsDataList.add(commentsDataMap);
			}
		}
		parsedata.put(Constants.COMMENTS,  commentsDataList);
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
	
}
