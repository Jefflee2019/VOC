package com.bfd.parse.config.iid;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.util.JsonUtil;

public class ParseReProcessor {
	private static final Log LOG = LogFactory.getLog(ParseReProcessor.class);

	public static boolean parseListIid(Pattern iidPattern, Map<String, Object> resultData, String baseurl) {
//		LOG.info("url:"+baseurl+".resultData is "+JsonUtil.toJSONString(resultData));
		List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get("items");
		LOG.info("url:"+baseurl+".items is "+JsonUtils.toJSONString(items));
		boolean result = false;
		if (items != null && items.size() > 0) {
			for (Map<String, Object> item : items) {
				Map<String, String> itemlink = (Map<String, String>) item.get("itemlink");
				if(itemlink==null){
					LOG.info("itemlink is null.listurl is "+baseurl+".");
					continue;
				}
				String url = itemlink.get("link");
				String type = itemlink.get("type");
				String iid = parseIid(iidPattern, url);
//				LOG.info("baseurl is :"+baseurl+"url:"+url+" iid is "+iid);
				if(url.indexOf("aipintuan")>0&&"list".equals(type)){
					LOG.info("url is "+url+".iid is "+iid+".iidPattern is "+iidPattern.pattern());
				}
				//增加list type判断，是为了聚美优品的某些list的解析结果也是list的情况。
				if (StringUtils.isNotEmpty(iid)) {
					item.put("itemiid", iid);
//					LOG.info("Cdida reprocess list got iid=" + iid);
					result = true;
				} else if ("list".equals(type)){
					LOG.info("parseListIid :: type is list return ture!url is "+url);
					result = true;
				}
				else {
					LOG.warn(" got iid failed, url=" + url + ", baseurl=" + baseurl+",iidpattern is "+iidPattern.pattern());
				}
			}
		}else if(items==null||items.size()==0){
			result = true;
		}
		return result;
	}

	public static String parseIid(Pattern iidPattern, String url) {
		Matcher matcher = iidPattern.matcher(url);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
}
