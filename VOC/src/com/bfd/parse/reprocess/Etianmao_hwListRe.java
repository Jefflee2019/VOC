package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：天猫华为官方旗舰店
 * <P>
 * 主要功能：取得列表页
 * @author bfd_01
 *
 */
public class Etianmao_hwListRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(Etianmao_hwListRe.class);
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			String pageno = "1";
			if (resultData.containsKey("pageno")) {
				pageno = resultData.get("pageno").toString();
			}
			if (!"1".equals(pageno)) {
				List<Map<String, Object>> items = (List<Map<String, Object>>) resultData
						.get(Constants.ITEMS);
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
						.get(Constants.TASKS);
				List<String> url = new ArrayList<String>();
				for (int i = 0; i < items.size(); i++) {
					if (items.get(i).containsKey("storename")
							&& !"华为官方旗舰店".equals(items.get(i).get("storename"))) {
						Map<String,Object> itemlink = (Map<String,Object>)items.get(i).get("itemlink");
						url.add(itemlink.get("link").toString());
					}
				}
				
				for (int i = 0; i < tasks.size(); i++) {
					Map<String, Object> temp = tasks.get(i);
					for (int j = 0; j < url.size(); j++) {
						if (temp.get("link").toString()
								.equals(url.get(j).toString())) {
							tasks.remove(i);
							i--;
							break;
						}
					}
				}
				items = null;
				tasks = null;
			}
			
			List<Map<String, Object>> data = (List<Map<String, Object>>) resultData
					.get(Constants.TASKS);
			for (int i=0;i< data.size();i++) {
				Map map = data.get(i);
				if ("eccontent".equals(data.get(i).get("linktype"))) {
					String url = data.get(i).get("link").toString().split("&skuId")[0];
					map.put("link", url);
				}
			}
		}
		return new ReProcessResult(processcode, processdata);
	}

}
