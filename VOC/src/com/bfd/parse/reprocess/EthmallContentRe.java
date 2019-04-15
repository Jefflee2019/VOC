package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点：天河购
 * 功能：商品详情页后处理
 * @author dph 2017年11月9日
 *
 */
public class EthmallContentRe implements ReProcessor{

	private static Pattern PATTERN_COLLECTION_CNT = Pattern.compile("\\d+");
	private static Pattern PATTERN_ID = Pattern.compile("goods-\\d+");
	@SuppressWarnings({ "unchecked" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String,Object> resultData = result.getParsedata().getData();
		Map<String,Object> processdata = new HashMap<String, Object>(16);
		//"collection_cnt": "受关注度:2011 次", 
		if(resultData.containsKey(Constants.COLLECTION_CNT)){
			String collectionCnt = (String) resultData.get(Constants.COLLECTION_CNT);
			Matcher collectionCntM = PATTERN_COLLECTION_CNT.matcher(collectionCnt);
			while(collectionCntM.find()){
				String str = collectionCntM.group(0);
				resultData.put(Constants.COLLECTION_CNT, str);
			}
		}
		//添加评论页链接
		List<Map<String, Object>> tasks =null;
		if(resultData.get(Constants.TASKS) != null){
			tasks = (List<Map<String,Object>>) resultData.get(Constants.TASKS);					
		}else{
			tasks = new ArrayList<Map<String,Object>>();
		}
		Map<String,Object> comment =  new HashMap<String,Object>();
		String url = unit.getUrl();
		String id = null;
		String link = null;
		String rawlink = null;
		Matcher collectionCntM = PATTERN_ID.matcher(url);
		while(collectionCntM.find()){
			id = collectionCntM.group(0);
			id = id.replace("goods-", "").trim();
		}
		link = "http://www.thmall.com/index.php?act=goods&op=newComments&goods_id=" + id;
		rawlink = "//www.thmall.com/index.php?act=goods&op=newComments&goods_id=" + id;
		comment.put(Constants.LINK, link);
		comment.put(Constants.RAWLINK, rawlink);
	    comment.put(Constants.LINKTYPE, Constants.COMMENT_URL);
		tasks.add(comment);
		resultData.put(Constants.COMMENT_URL, link);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
