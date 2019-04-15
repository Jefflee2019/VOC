package com.bfd.parse.reprocess;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

public class BnewsmthListRe implements ReProcessor{
	
	@Override
	@SuppressWarnings("unchecked")
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && resultData.containsKey(Constants.ITEMS)) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
			// 处理下一页
			int totalNum = Integer.parseInt((String)resultData.get("total_num")); // 帖子总数
			String url = unit.getUrl();
			int oldPageNum = getPage(url);
			if(oldPageNum < 0) {
				oldPageNum = 1;
			}
			if(totalNum > oldPageNum*30) { // 一页30条帖子
				Map nextpageTask = new HashMap();
				String nextpage;
				if (url.contains("&p=")) { // 前提要保证列表页url里有请求参数?ajax
					nextpage = url.replace("&p=" + oldPageNum, "&p=" + (oldPageNum + 1));
				} else {
					nextpage = url + "&p=2";
				}
				nextpageTask.put("link", nextpage);
				nextpageTask.put("rawlink", nextpage);
				nextpageTask.put("linktype", "bbspostlist");
				resultData.put("nextpage", nextpage);
				tasks.add(nextpageTask);	// 添加下一页任务
			}
			//处理发帖时间 ConstantFunc.convertTime(string)
			for (Map<String, Object> map : items) {
				String posttime = (String) map.get("posttime");
//				String replycontent = reply.get(Constants.REPLYCONTENT).toString();
				posttime = this.getCresult(posttime, "(\\d+[:|-]\\d+[:|-]\\d+)").trim();
				System.err.println(posttime);
				if (posttime.matches("\\d+:\\d+:\\d+")) {
					SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
					Date nowTime = new Date();
					posttime = df2.format(nowTime) + " " + posttime;
				}
				map.put("posttime", posttime);
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}
	
	/**
	 * 正则匹配字符串
	 * @param str
	 * @param pattern
	 * @return
	 */
	private String getCresult(String str,String reg){
		Pattern pattern = Pattern.compile(reg);
		Matcher mch = pattern.matcher(str);
		if(mch.find()){
			return mch.group(1);
		}
		return str;
	}

	
	//获取页数
	private int getPage(String url) {
		Pattern iidPatter = Pattern.compile("&p=(\\d+)");
		Matcher match = iidPatter.matcher(url);
		if (match.find()) {
			return Integer.valueOf(match.group(1));
		} else {
			return -1;
		}
	}
	
}
