package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class Eyhd_hwCommentJson_20160303 implements JsonParser{

	private static final Log LOG = LogFactory.getLog(Eyhd_hwCommentJson.class);
	public Map<String,Object> test_data = new HashMap<String, Object>();
	
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient urlnormalizerClients, 
			ParseUnit unit) {
		int parsecode = 0;
		Map<String,Object> parsedata = new HashMap<String,Object>();
		
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
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata,json,data.getUrl(),unit);
			}catch(Exception e){
				e.printStackTrace();
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
			e.printStackTrace();
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
			
			//解析源码
			parseHtmlData(value.trim(), parsedata);
			
			//拼接下一页链接
			if(value.contains("latestnewnextpage")){
				Pattern pattern = Pattern.compile("currentPage=(\\d+)");
				Matcher matcher = pattern.matcher(url);
				if (matcher.find()) {
					int curPageNo = Integer.parseInt(matcher.group(1));
					int nextPageNo = curPageNo++;
					String nextpageUrl = url.replaceFirst("currentPage="+curPageNo, "currentPage="+nextPageNo);
					
					List<Map<String,String>> taskList =null;
					if(parsedata.get(Constants.TASKS) != null){
						taskList = (List<Map<String,String>>) parsedata.get(Constants.TASKS);					
					}else{
						taskList = new ArrayList<Map<String,String>>();
					}
					Map<String,String> nexpageTask = new HashMap<String,String>();
					nexpageTask.put(Constants.LINK, nextpageUrl);
					nexpageTask.put(Constants.RAWLINK, nextpageUrl);
					nexpageTask.put(Constants.LINKTYPE, "eccomment");		
					taskList.add(nexpageTask);
					
					parsedata.put(Constants.NEXTPAGE, nexpageTask);
				} 
			}
			
		} catch (Exception e) {
			e.printStackTrace();
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
	

	/**
	 * 评论整体其他信息(包括好评率，客户评价，全部，好评，中评，差评，晒单，有回复的回复数量)
	 * @param tn
	 * @param parsedata
	 * @throws Exception 
	 */
	private void getCommentOtherInfo(TagNode tn, Map<String, Object> parsedata) throws Exception {
		TagNode tnModCommentChart = (TagNode) (tn.evaluateXPath("//div[@class='mod_comment_chart fl']")[0]);
		
		//好评率
		//<div class="text">
		TagNode tnText = (TagNode) (tnModCommentChart.evaluateXPath("//div[@class='text']")[0]);
		String sGoodRate = parseByRegex(tnText.getText().toString(), "\\d+(?=%)");	
		
		//评论总数
		//<li data-tpc="TOTAL">
		TagNode tnTotal = (TagNode) (tn.evaluateXPath("//li[@data-tpc='TOTAL']")[0]);
		String sCommentCnt = parseByRegex(tnTotal.getText().toString(),"\\d+");
		
		//好评数
		//<li data-tpc="GOOD">
		TagNode tnGood = (TagNode) (tn.evaluateXPath("//li[@data-tpc='GOOD']")[0]);
		String sGoodCnt = parseByRegex(tnGood.getText().toString(),"\\d+");
		
		//中评数
		//<li data-tpc="MEDIUM">
		TagNode tnMedium = (TagNode) (tn.evaluateXPath("//li[@data-tpc='MEDIUM']")[0]);
		String sGeneralCnt = parseByRegex(tnMedium.getText().toString(),"\\d+");
		
		//差评数
		//<li data-tpc="POOR">
		TagNode tnPoor = (TagNode) (tn.evaluateXPath("//li[@data-tpc='POOR']")[0]);
		String sPoorCnt = parseByRegex(tnPoor.getText().toString(),"\\d+");
		
		//晒单数
		//<li data-tpc="SHINE">
		TagNode tnShine = (TagNode) (tn.evaluateXPath("//li[@data-tpc='SHINE']")[0]);
		String sShowCnt = parseByRegex(tnShine.getText().toString(),"\\d+");
		
		//有回复的评价数
		//<li data-tpc="REPLY">
		//TagNode tn_reply = (TagNode) (tn.evaluateXPath("//li[@data-tpc='REPLY']")[0]);
		//String has_reply_cnt = parseByRegex(tn_reply.getText().toString(),"\\d+");
		
		parsedata.put(Constants.GOOD_RATE, sGoodRate);
		parsedata.put(Constants.REPLY_CNT, sCommentCnt);
		parsedata.put(Constants.GOOD_CNT, sGoodCnt);
		parsedata.put(Constants.GENERAL_CNT, sGeneralCnt);
		parsedata.put(Constants.POOR_CNT, sPoorCnt);
		parsedata.put(Constants.WITHPIC_CNT, sShowCnt);
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
				TagNode tnText = (TagNode) (tnInformp.evaluateXPath("//span[@class='text']")[0]);
				String sCommentContent = tnText.getText().toString().trim();
				
				//点赞数 回复数
				//<div class="praise"> 
				TagNode tnPraise = (TagNode) tnInformp.evaluateXPath("//div[@class='praise']")[0];
				//<span data-tpc="UP"> <em>				
				TagNode tnFavor = (TagNode) tnPraise.evaluateXPath("//span[@data-tpc='UP']")[0];
				TagNode tnEmFavor = (TagNode)tnFavor.evaluateXPath("//em")[0];
				//<span data-tpc="REPLY"> <em>
				TagNode tnReply = (TagNode) tnPraise.evaluateXPath("//span[@data-tpc='REPLY']")[0];
				TagNode tnEmReply = (TagNode)tnReply.evaluateXPath("//em")[0];			
				
				//评论时间 
				//<span class="date">购买方式:非合约机        颜色:白色        内存ROM:16GB        版本:官方标配        &nbsp;&nbsp;&nbsp;2016-03-03 14:27:16</span>
				TagNode tnDate = (TagNode) tnInformp.evaluateXPath("//span[@class='date']")[0];
				String sTnDate = tnDate.getText().toString().replace("&nbsp;", "");
				sTnDate = sTnDate.replaceAll("\\s{2,}", "|");
				String[] strArray = sTnDate.split("\\|");
				if (strArray != null && strArray.length >= 5) {
					commentsDataMap.put(Constants.BUY_TYPE, strArray[0].replace("购买方式:", "").trim());
					commentsDataMap.put(Constants.COLOR, strArray[1].replace("颜色:", "").trim());
					commentsDataMap.put(Constants.BUY_RAM, strArray[2].replace("内存ROM:", "").trim());
//					commentsDataMap.put(Constants.VERSION, strArray[3].replace("版本:", "").trim());
					commentsDataMap.put(Constants.COMMENT_TIME, strArray[4]);
				}
				
				commentsDataMap.put(Constants.FAVOR_CNT, tnEmFavor==null?0:tnEmFavor.getText().toString().trim());
				commentsDataMap.put(Constants.REPLY_CNT, tnEmReply==null?0:tnEmReply.getText().toString().trim());
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
	
}
