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
 * 站点名：人民网-论坛
 * <p>
 * 主要功能：处理下一页
 * @author bfd_01
 *
 */
public class BpeopleListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(BpeopleListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (!resultData.isEmpty()) {
			String url = unit.getUrl();
			List<Map<String,Object>> items = (List<Map<String,Object>>)resultData.get(Constants.ITEMS);
			for (int i = 0; i < items.size(); i++) {
				Map<String,Object> map = (Map<String,Object>) items.get(i);
				if (map.containsKey(Constants.REPLY_CNT)) {
					String replyCnt = map.get(Constants.REPLY_CNT)
							.toString();
					replyCnt = replyCnt.split("/")[1].replace(")", "");
					map.put(Constants.REPLY_CNT, Integer.valueOf(replyCnt));
				}
				if (map.containsKey(Constants.POSTTIME)) {
					String posttime = map.get(Constants.POSTTIME)
							.toString();
					map.put(Constants.POSTTIME, getPostTime(posttime));
				}
			}
			
			String nextpage = null;
			int searchCnt = 0;
			int pageSize = 50;
			int pageNum = 0;
			
			if (resultData.containsKey(Constants.VIEW_CNT)) {
				searchCnt = getSearchCnt(resultData.get(Constants.VIEW_CNT).toString());
			}
			//总条数大于50
			if (searchCnt > 50) {
				int n = searchCnt / pageSize;
				int m = searchCnt % pageSize;
				int tempNo = 2;
				if (m != 0) {
					n += 1;
				} 
				//链接包含翻页字段
				if (url.contains("pageNo")) {
					tempNo = Integer.parseInt(url.split("&pageNo=")[1]);
					if (tempNo < n) {
						tempNo += 1;
					}
				}
				nextpage = url.split("&pageNo=")[0] + "&pageNo=" + tempNo;
				
				if (nextpage != null) {
					// 处理下一页链接
					Map<String, Object> nextpageTask = new HashMap<String, Object>();
					nextpageTask.put("link", nextpage);
					nextpageTask.put("rawlink", nextpage);
					nextpageTask.put("linktype", "bbspostlist");
					LOG.info("url:" + url + "taskdata is "
							+ nextpageTask.get("link")
							+ nextpageTask.get("rawlink")
							+ nextpageTask.get("linktype"));
					if (!resultData.isEmpty()) {
						resultData.put("nextpage", nextpage);
						List<Map<String,Object>> tasks = (List<Map<String,Object>>) resultData.get("tasks");
						tasks.add(nextpageTask);
					}
				}
			}
			// 后处理插件加上iid
			ParseUtils.getIid(unit, result);
		}
		resultData.remove(Constants.VIEW_CNT);
		return new ReProcessResult(processcode, processdata);
	}
	
	private String getPostTime(String time) {
		String posttime = null;
		Pattern p = Pattern.compile("(\\d+年\\d+月\\d+日\\s\\d+:\\d+)");
		Matcher m = p.matcher(time);
		while (m.find()) {
			posttime = m.group(1);
		}
		if (posttime!=null && posttime.contains("年") && posttime.contains("月")
				&& posttime.contains("日")) {
			posttime = posttime.replace("年", "-").replace("月", "-")
					.replace("日", "");
		}
		return posttime;
	}
	
	private int getPage(String url) {
		Pattern iidPatter = Pattern.compile("&pageNo=(\\d+)");
		Matcher match = iidPatter.matcher(url);
		while (match.find()) {
			return Integer.valueOf(match.group(1));
		}
		return 0;
	}
	private int getSearchCnt(String url) {
		Pattern iidPatter = Pattern.compile("(\\d+)条");
		Matcher match = iidPatter.matcher(url);
		while (match.find()) {
			return Integer.valueOf(match.group(1));
		}
		return 0;
	}

}
