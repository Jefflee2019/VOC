package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点名：天猫华为官方旗舰店
 * <P>
 * 主要功能：取得大图，小图，价格
 * @author bfd_01
 *
 */
public class Etianmao_hwContentRe implements ReProcessor {
	private static final Log LOG = LogFactory
			.getLog(Etianmao_hwContentRe.class);
	private double marketlowerprice = 0.0;
	private double marketupperprice = 0.0;
	private double price = 0.0;

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (!resultData.isEmpty()) {
			String pageData = unit.getPageData();
			String url = unit.getUrl();
			String largeImg = "http:" + getLargeImg(pageData);
			String smallImg = "http:" + getSmallImg(pageData);
			resultData.put(Constants.LARGE_IMG, largeImg);
			resultData.put(Constants.SMALL_IMG, smallImg);
			// 价格
			getPrice(pageData);
			if (price == 0.0) {
				resultData.put(Constants.MARKETLOWERPRICE, marketlowerprice);
				resultData.put(Constants.MARKETUPPERPRICE, marketupperprice);
			} else {
				resultData.put(Constants.PRICE, price);
			}
			
			// 生成评论任务
			Map<String, Object> commentTask = new HashMap<String, Object>();
			String commentUrl = getCommentUrl(url, pageData);
			commentTask.put("link", commentUrl);
			commentTask.put("rawlink", commentUrl);
			commentTask.put("linktype", "eccomment");
			LOG.info("url:" + url + "taskdata is " + commentTask.get("link")
					+ commentTask.get("rawlink")
					+ commentTask.get("linktype"));
			if (!resultData.isEmpty()) {
				resultData.put(Constants.COMMENT_URL, commentUrl);
				List<Map<String,Object>> tasks = (List<Map<String,Object>>) resultData.get("tasks");
				tasks.add(commentTask);
			}
			// 后处理插件加上iid
			ParseUtils.getIid(unit, result);
			
		}
		return new ReProcessResult(processcode, processdata);

	}

	/**
	 * 大图链接
	 * 
	 * @param data
	 * @return
	 */
	private String getLargeImg(String data) {
		Pattern iidPatter = Pattern.compile("J_ImgBooth.*src=\"(.*?)\"");
		Matcher match = iidPatter.matcher(data);
		while (match.find()) {
			return match.group(1);
		}
		return null;
	}

	/**
	 * 小图链接
	 * 
	 * @param data
	 * @return
	 */
	private String getSmallImg(String data) {
		Pattern iidPatter = Pattern.compile("<a href=\"#\"><img src=\"(.*?)\"");
		Matcher match = iidPatter.matcher(data);
		while (match.find()) {
			return match.group(1);
		}
		return null;
	}

	/**
	 * 价格
	 * 
	 * @param data
	 * @return
	 */
	private void getPrice(String data) {
		Pattern priceArea = Pattern
				.compile("defaultItemPrice\":\"(\\d+.\\d+) - (\\d+.\\d+)\",\"goNewAuctionFlow\"");
		
		Pattern onlyPrice = Pattern
				.compile("defaultItemPrice\":\"(\\d+).\\d+\",\"goNewAuctionFlow\"");
		Matcher match = priceArea.matcher(data);
		Matcher matcher = onlyPrice.matcher(data);
		if (match.find()) {
			marketlowerprice = Double.valueOf(match.group(1));
			marketupperprice = Double.valueOf(match.group(2));
		} else if (matcher.find()){
			price = Double.valueOf(matcher.group(1));
		}
	}
	
	/**
	 * 评论链接
	 * @param data
	 * @return
	 */
	private String getCommentUrl(String url, String data) {
		String commentUrl = null;
		Pattern itemId = Pattern.compile("\\?id=(\\d+)");
		Pattern spuId = Pattern.compile("&spuId=(\\d+)&");
		Pattern sellerId = Pattern.compile("&sellerId=(\\d+)&");
		Matcher matchItem = itemId.matcher(url);
		Matcher matchSpu = spuId.matcher(data);
		Matcher matchSeller = sellerId.matcher(data);
		String item = null;
		String spu = null;
		String seller = null;
		while (matchItem.find()) {
			item = matchItem.group(1);
		}
		while (matchSpu.find()) {
			spu = matchSpu.group(1);
		}
		while (matchSeller.find()) {
			seller = matchSeller.group(1);
		}
		commentUrl = "https://rate.tmall.com/list_detail_rate.htm?itemId="
				+ item + "&spuId=" + spu + "&sellerId=" + seller
				+ "&order=3&currentPage=1";
		return commentUrl;
	}
}
