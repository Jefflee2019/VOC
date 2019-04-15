package com.bfd.parse.reprocess;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReprocessHelper {
	private static final Log LOG = LogFactory.getLog(ReprocessHelper.class);

	public static boolean parseItemiidOfList(String cid, Pattern pattern, Map<String, Object> resultData, String baseurl) {
		List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get("items");
		boolean result = false;
		if (items != null && items.size() > 0) {
			for (Map<String, Object> item : items) {
				Map<String, String> itemlink = (Map<String, String>) item.get("itemlink");
				String url = itemlink.get("link");
				String iid = getiid(url, pattern);
				if (StringUtils.isNotEmpty(iid)) {
					item.put("itemiid", iid);
					LOG.info(cid + " reprocess list got iid=" + iid);
					result = true;
				} else {
					LOG.warn(cid + " reprocess got iid failed, itemurl=" + url + ", baseurl=" + baseurl);
				}
			}
		}
		return result;
	}

	public static String getiid(String url, Pattern pattern) {
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
}
