package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 拍拍华为旗舰店
 * 后处理插件
 * @author bfd_05
 *
 */
public class Epaipai_hwListRe implements ReProcessor{

//	private static final Log LOG = LogFactory.getLog(Epaipai_hwListRe.class);
	private static final Pattern NEXTPAT = Pattern.compile("(\\d+)/0-00000000000-0-1-(\\d+)-\\d+-\\d+-0-0-0/");// -\\d+-\\d+-0-0-0/
	
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
		
		String url = unit.getTaskdata().get("url").toString();
		Matcher mch = NEXTPAT.matcher(url);
		int pageIndex = 1;
		int totalPage = 1;
		if(mch.find()){
			pageIndex = Integer.valueOf(mch.group(2));
			String iid = mch.group(1);
			if(resultData.containsKey(Constants.TOTAL_CNT)){
				int totalCnt = Integer.valueOf((String)resultData.get(Constants.TOTAL_CNT));
				totalPage = totalCnt%40 == 0 ? totalCnt/40 : totalCnt/40 + 1;
			}
			if(pageIndex < totalPage){
				Map<String, Object> nextpMap = new HashMap<String, Object>();
				String nextpage = "http://shop.paipai.com/%s/0-00000000000-0-1-%s-0-0-0-0-0/index.shtml";
				nextpage = String.format(nextpage, iid, totalPage);
				resultData.put(Constants.NEXTPAGE, nextpage);
				nextpMap.put("link", nextpage);
				nextpMap.put("rawlink", nextpage);
				nextpMap.put("linktype", "eclist");
				tasks.add(nextpMap);
			}
		}
		//改商店每页40个商品
		resultData.put("tasks", tasks);
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
