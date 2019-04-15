package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * Etaobao_hw商品后处理
 * @function 获取大小图、获取评论页url、当前价格
 * @author BFD_499
 *
 */
public class Etaobao_hwContentRe implements ReProcessor{
	private static final Log LOG = LogFactory.getLog(Etaobao_hwContentRe.class);
	private static final Pattern BIGIMGP = Pattern.compile("<img id=\"J_ImgBooth\" src=\"(.*?)\"");
	//找出当前价格
	private static final Pattern CUR_PRICEP = Pattern.compile("<input type=\"hidden\" name=\"current_price\" value= \"(.*?)\"");
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		String pageData = unit.getPageData();
		Matcher bigimgM = BIGIMGP.matcher(pageData);
		Matcher curPriceM = CUR_PRICEP.matcher(pageData);
		Map<String, Object> resultData = result.getParsedata().getData();
		Map<String, Object> processdata = new HashMap<String, Object>();
		//bigImg smallImg
		if(bigimgM.find())
		{
			try {
				String bigImgUrl = bigimgM.group(1);
				String smallImgUrl = bigImgUrl.replace("400x400", "50x50");
				if (!resultData.isEmpty()) {
					resultData.put(Constants.LARGE_IMG, "http:" + bigImgUrl);
					resultData.put(Constants.SMALL_IMG, "http:" + smallImgUrl);
				}
			} catch (Exception e) {
				LOG.error(e);
			}
		}
		/**
		 * 站点评论页需要 Cookie 暂时不给出评论页地址
		 */
		//获取评论页url:https://rate.taobao.com/feedRateList.htm?auctionNumId=520019430951&userNumId=1700182551&currentPageNum=1&pageSize=20&callback=jsonp_tbcrate_reviews_list
//		if(auctionNumIdM.find() && useridM.find())
//		{
//			String auctionNumId = auctionNumIdM.group(1);
//			String userid = useridM.group(1);
//			String comment_url = urlHead + auctionNumId + urlMid + userid + urlEnd;
//			comment_task.put("link", comment_url);
//			comment_task.put("rawlink", comment_url);
//			comment_task.put("linktype", "eccomment");
//			resultData.put("comment_url", comment_url);
//			@SuppressWarnings("unchecked")
//			List<Map> tasks = (List<Map>) resultData.get("tasks");
//			tasks.add(comment_task);	
//		}
		//找到当前价格
		if(curPriceM.find())
		{
			String curPrice = curPriceM.group(1);
			resultData.put(Constants.PRICE, curPrice);
		}
//		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

}
