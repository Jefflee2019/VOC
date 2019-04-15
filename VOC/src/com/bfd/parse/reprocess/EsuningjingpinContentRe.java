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
 * 站点：苏宁易购荣耀旗舰店 作用：添加评论，咨询url
 * 
 * @author bfd_04
 *
 */
public class EsuningjingpinContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(EsuningjingpinContentRe.class);
	private static final String COMM_URL_TEMP = "http://review.suning.com/ajax/review_lists/general-prdid_temp-shopid_temp-total-1-default-10-----reviewList.htm?callback=reviewList";

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String, Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null) {
			// 获取店铺名称
			getStorename(resultData);
			// deal with comment
			Map<String, Object> commentTask = new HashMap<String, Object>();
			String url = unit.getUrl();
			Pattern p = Pattern.compile("(\\d+)/(\\d+)");
			Matcher m = p.matcher(url);
			if (m.find()) {
				try {
					String itemId = m.group(2);
					String data = unit.getPageData();
//					"vendorCode":"0000000000",
					Pattern p1 = Pattern.compile("\"vendorCode\":\"(\\d+)\"");
					Matcher m1 = p1.matcher(data);
					System.err.println(data.contains("vendorCode"));
					System.err.println(m1.groupCount());
					if (m1.find()) {
						String shopid = m1.group(1);
						String str = "000000000000000000";
						itemId = str.substring(0, 18 - itemId.length()) + itemId;
						String commUrl = COMM_URL_TEMP.replaceAll("prdid_temp", itemId).replaceAll("shopid_temp", shopid);
						commentTask.put("link", commUrl);
						commentTask.put("rawlink", commUrl);
						commentTask.put("linktype", "eccomment");
						resultData.put(Constants.COMMENT_URL, commUrl);
						List<Map> tasks = (List<Map>) resultData.get("tasks");
						tasks.add(commentTask);
					}
				} catch (Exception e) {
					// e.printStackTrace();
					LOG.error("regex parse error");
				}
			}

		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
	
	private void getStorename(Map<String, Object> resultData) {
		String storename = (String) resultData.get(Constants.STORENAME);
		if (storename.contains("小米")) {
			storename = "小米官方旗舰店";
		}
		if (storename.contains("Apple")) {
			storename = "苹果官方旗舰店";
		}
		if (storename.contains("魅族")) {
			storename = "魅族官方旗舰店";
		}
		if (storename.contains("三星")) {
			storename = "三星官方旗舰店";
		}
		if (storename.contains("酷派")) {
			storename = "酷派官方旗舰店";
		}
		if (storename.contains("中兴")) {
			storename = "中兴官方旗舰店";
		}
		if (storename.contains("OPPO")) {
			storename = "OPPO官方旗舰店";
		}
		if (storename.contains("vivo")) {
			storename = "VIVO官方旗舰店";
		}
		if (storename.contains("一加")) {
			storename = "一加官方旗舰店";
		}
		if (storename.contains("LG")) {
			storename = "LG官方旗舰店";
		}
		resultData.put(Constants.STORENAME, storename);
		
	}

}
