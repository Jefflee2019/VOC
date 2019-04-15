package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;
/**
 * 站点：天河购
 * 功能：获取评论页评论json
 * @author dph 2017年11月10日
 *
 */
public class EthmallCommentJson implements JsonParser{
	private static final Log LOG = LogFactory.getLog(EthmallCommentJson.class);
	private static final Pattern PATTERN_REPLY_CNT = Pattern.compile("全部\\(\\d+\\)");
	private static final Pattern PATTERN_GOOD_CNT = Pattern.compile("好评\\(\\d+\\)");
	private static final Pattern PATTERN_GENERAL_CNT = Pattern.compile("中评\\(\\d+\\)");
	private static final Pattern PATTERN_POOR_CNT = Pattern.compile("差评\\(\\d+\\)");
	private static final Pattern PATTERN_AGAIN_CNT = Pattern.compile("追加评论\\(\\d+\\)");
	private static final Pattern PATTERN_COMMENT = Pattern.compile("<span class=\"light\">"
			+ "\\S* \\(\\d+-\\d+-\\d+\\)</span> \\(<span style=\"color:\\S+;\">\\S+</span>\\)"
			+ "</dt>\\s*<dd>\\S+\\s*\\S+</dd>");
	private static final Pattern PATTERN_REPLYUSERNAME = Pattern.compile(">\\S+ \\(");
	private static final Pattern PATTERN_REPLYDATE = Pattern.compile("\\d+-\\d+-\\d+");
	private static final Pattern PATTERN_REPLYCONTENT = Pattern.compile("<dd>\\S+\\s*\\S+</dd>");
	private static final Pattern PATTERN_CURPAGE = Pattern.compile("curpage=\\d+");
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		Map<String,Object> parsedata = new HashMap<String,Object>(5);
		int parsecode = 0;
		for(Object obj : dataList){
			JsonData data = (JsonData) obj;
			if(!data.downloadSuccess()){
				continue;
			}
			//解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			executeParse(parsedata,json, unit);
		}
		JsonParserResult result = new JsonParserResult();
		try{
			result.setParsecode(parsecode);	
			result.setData(parsedata);
		}catch(Exception e){
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}
	/**
	 * 从htmlData中取得需要的数据
	 * @param json
	 * @param htmlData
	 */
	@SuppressWarnings("unchecked")
	private void executeParse(Map<String, Object> parsedata, String json, ParseUnit unit) {
		Matcher commentM = PATTERN_COMMENT.matcher(json);
		String comment = null;
		String replyusername = null;
		String replydate = null;
		String replycontent = null;
		int count = 0;
		int countM = 10;
		String url = unit.getUrl();
		List<Map<String, Object>> commentList =null;
		if(parsedata.get(Constants.ITEMS) != null){
			commentList = (List<Map<String,Object>>) parsedata.get(Constants.ITEMS);					
		}else{
			commentList = new ArrayList<Map<String,Object>>();
		}
		//添加评论内容
		while(commentM.find()){
			comment = commentM.group(0);
			Map<String,Object> commentMap =new HashMap<String,Object>(4);
			Matcher replyusernameM = PATTERN_REPLYUSERNAME.matcher(comment);
			while(replyusernameM.find()){
				replyusername = replyusernameM.group(0);
				replyusername = replyusername.replace(">", "").replace("(", "").trim();
			}
			Matcher replydateM = PATTERN_REPLYDATE.matcher(comment);
			while(replydateM.find()){
				replydate = replydateM.group(0);
			}
			Matcher replycontentM = PATTERN_REPLYCONTENT.matcher(comment);
			while(replycontentM.find()){
				replycontent = replycontentM.group(0);
				replycontent = replycontent.replace("<dd>", "").replace("</dd>", "").trim();
			}
			commentMap.put(Constants.REPLYUSERNAME, replyusername);
			commentMap.put(Constants.REPLYDATE, replydate);
			commentMap.put(Constants.REPLYCONTENT, replycontent);
			commentList.add(commentMap);
			parsedata.put(Constants.COMMENTS, commentList);
			count++;
		}
		String replycnt = null;
		String goodcnt = null;
		String generalcnt = null;
		String poorcnt = null;
		String againcnt = null;
		Matcher replycntM = PATTERN_REPLY_CNT.matcher(json);
		while(replycntM.find()){
			replycnt = replycntM.group(0);
			replycnt = replycnt.replace("全部(", "").replace(")", "").trim();
			parsedata.put(Constants.REPLY_CNT, replycnt);
		}
		Matcher goodcntM = PATTERN_GOOD_CNT.matcher(json);
		while(goodcntM.find()){
			goodcnt = goodcntM.group(0);
			goodcnt = goodcnt.replace("好评(", "").replace(")", "").trim();
			parsedata.put(Constants.GOOD_CNT, goodcnt);
		}
		Matcher generalcntM = PATTERN_GENERAL_CNT.matcher(json);
		while(generalcntM.find()){
			generalcnt = generalcntM.group(0);
			generalcnt = generalcnt.replace("中评(", "").replace(")", "").trim();
			parsedata.put(Constants.GENERAL_CNT, generalcnt);
		}
		Matcher poorcntM = PATTERN_POOR_CNT.matcher(json);
		while(poorcntM.find()){
			poorcnt = poorcntM.group(0);
			poorcnt = poorcnt.replace("差评(", "").replace(")", "").trim();
			parsedata.put(Constants.POOR_CNT, poorcnt);
		}
		Matcher againcntM = PATTERN_AGAIN_CNT.matcher(json);
		while(againcntM.find()){
			againcnt = againcntM.group(0);
			againcnt = againcnt.replace("追加评论(", "").replace(")", "").trim();
			parsedata.put(Constants.AGAIN_CNT, againcnt);
		}
		//下一页
		String link = null;
		String rawlink = null;
		List<Map<String, Object>> taskList =null;
		if(parsedata.get(Constants.TASKS) != null){
			taskList = (List<Map<String,Object>>) parsedata.get(Constants.TASKS);					
		}else{
			taskList = new ArrayList<Map<String,Object>>();
		}
		Map<String, Object> nextpage= new HashMap<String,Object>(4);
		int page = 1;
		link = url.replaceAll("curpage=\\d+", "curpage=");
		if(0 != count){
			Matcher curpageM = PATTERN_CURPAGE.matcher(url);
			while(curpageM.find()){
				//curpageStr = "curpage=2"
				String curpageStr = curpageM.group(0);
				String curpage = curpageStr.replace("curpage=", "").trim();
				page = Integer.parseInt(curpage);
				page = page + 1;
			}
			//如果评论数小于10证明为最后一页,然后下一页为第一页
			if(countM > count){
				page = 1;
			}
		}
		link = link + page ;
		rawlink = link.replace("http:", "").trim();
		nextpage.put(Constants.LINK, link);
		nextpage.put(Constants.RAWLINK, rawlink);
		nextpage.put(Constants.LINKTYPE, "eccomment");
		taskList.add(nextpage);
		parsedata.put(Constants.NEXTPAGE, nextpage);
		parsedata.put(Constants.TASKS, taskList);
	}

}
