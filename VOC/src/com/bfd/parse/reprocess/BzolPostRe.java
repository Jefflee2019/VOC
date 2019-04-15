package com.bfd.parse.reprocess;

import java.util.ArrayList;
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

/**
 * 中关村论坛帖子页 
 * 后处理插件
 * @author bfd_05
 *
 */
public class BzolPostRe implements ReProcessor{

//	private static final Log LOG = LogFactory.getLog(BzolPostRe.class);
	private static final Pattern PNUM = Pattern.compile("\\d+");
	private static final Pattern PAGE = Pattern.compile("_\\d+_(\\d+).html");
	private static final Pattern PATTIME = Pattern.compile("[0-9]{4}[-年][0-9]{1,2}[-月][0-9]{1,2}[日\\s]*([0-9]{2}:[0-9]{2})*");
	private static final Pattern USERID = Pattern.compile("http://my.zol.com.cn/bbs/(\\w+)/");
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getTaskdata().get("url").toString();
		if(url.contains("bbs")){
			Matcher mch = PAGE.matcher(url);
			int pageIndex = 1;
			if(mch.find()){
				pageIndex = Integer.valueOf(mch.group(1));
			}
			if (pageIndex > 1) {
				resultData.remove(Constants.CONTENTS);
				resultData.remove(Constants.NEWSTIME);
				resultData.remove(Constants.AUTHOR);
			}
			if(resultData.containsKey(Constants.CONTENTS)){
				String contents = (String) resultData.get(Constants.CONTENTS);
				resultData.put(Constants.CONTENTS, ConstantFunc.replaceBlank(contents));
			}
			if(resultData.containsKey(Constants.NEWSTIME)){
				String newstime = (String) resultData.get(Constants.NEWSTIME);
				newstime = newstime.replace("发表于", "");
				newstime = ConstantFunc.convertTime(newstime);
				resultData.put(Constants.NEWSTIME, newstime.trim());
			}
			if(resultData.containsKey(Constants.AUTHOR)){
				List<Map<String, Object>> authorList = (List<Map<String, Object>>) resultData.get(Constants.AUTHOR);
				Map<String, Object> author = authorList.get(0);
				if(author.containsKey(Constants.ESSENCE_CNT)){
					parseByReg(author, Constants.ESSENCE_CNT, PNUM);
				}
				if(author.containsKey(Constants.POST_CNT)){
					parseByReg(author, Constants.POST_CNT, PNUM);
				}
				if(author.containsKey(Constants.USER_CITY)){
					String replyCity = (String) author.get(Constants.USER_CITY);
					author.put(Constants.USER_CITY, replyCity.replace("城 市：", "").trim());
				}
				if(author.containsKey(Constants.REG_TIME)){
					parseByReg(author, Constants.REG_TIME, PATTIME);
				}
			}
			//json插件保存的map
			Map<String, Object> dataMap = null;
			if(resultData.containsKey("dataMap")){
				Object dataObj = resultData.get("dataMap");
				if(dataObj instanceof Map){
					dataMap = (Map<String, Object>) dataObj;
				}
				resultData.remove("dataMap");
			}
			if(resultData.containsKey(Constants.REPLYS)){
				Object obj = resultData.get(Constants.REPLYS);
				if(obj instanceof List){
					List<Map<String, Object>> replys = (List<Map<String, Object>>) obj;
					//改论坛第一个是最赞回复会重复
//					replys.remove(0);.
					for(int i = 0; i < replys.size(); ){
						Map<String, Object> reply = replys.get(i);
						if(reply.containsKey("praise")){
							replys.remove(i);
							continue;
						}
						if(reply.containsKey(Constants.REPLYFLOOR)){
							parseByReg(reply, Constants.REPLYFLOOR, PNUM);
						}
						if(reply.containsKey(Constants.REPLYDATE)){
							String replyDate = (String) reply.get(Constants.REPLYDATE);
							replyDate = replyDate.replace("发表于", "");
							replyDate = ConstantFunc.convertTime(replyDate.trim());
							reply.put(Constants.REPLYDATE, replyDate.trim());
						}
						//回复名字为空给默认值
						if(reply.get(Constants.REPLYUSERNAME) == null || "".equals(reply.get(Constants.REPLYUSERNAME))){
							reply.put(Constants.REPLYUSERNAME, "zoluser");
						}
						/*if(dataMap != null && reply.containsKey(Constants.REPLYLINK)){
							Object linkObj = reply.get(Constants.REPLYLINK);
							reply.remove(Constants.REPLYLINK);
							if(linkObj instanceof Map){
								Map<String, Object>  linkMap = (Map<String, Object>) linkObj;
								if(linkMap.get("link") != null){
									String link = linkMap.get("link").toString();
									Matcher m = USERID.matcher(link);
									if(m.find()){
										String userIDStr = m.group(1);
										Object objMap = null;
										//能找到对应的名字的是回复人的，找不到的楼主的api返回楼主的键是admincode47
										if(dataMap.containsKey(userIDStr)){
											objMap = dataMap.get(userIDStr);
										}
										if(objMap instanceof Map){
											Map<String, Object> dataInerMap = (Map<String, Object>) objMap;
											if (dataInerMap.containsKey("bookNum")) {
												reply.put(Constants.REPLY_POST_CNT, dataInerMap.get("bookNum"));
											}
											if (dataInerMap.containsKey("goodNum")) {
												reply.put(Constants.REPLY_ESSENCE_CNT, dataInerMap.get("goodNum"));
											}
										}
									}
								}
							}
						}*/
						/*if(reply.containsKey(Constants.REPLY_USER_CITY)){
							String replyCity = (String) reply.get(Constants.REPLY_USER_CITY);
							reply.put(Constants.REPLY_USER_CITY, replyCity.replace("城 市：", "").trim());
						}*/
						/*if(reply.containsKey(Constants.REPLY_REG_TIME)){
							parseByReg(reply, Constants.REPLY_REG_TIME, PATTIME);
						}*/
						i++;
					}
				}
			}else{
				//没有replys字段的时候日志中会报错，没有回复时候只能强行给一个
				resultData.put(Constants.REPLYS, new ArrayList<Map<String, Object>>());
			}
		}
		else{
			//问答的
			if(resultData.containsKey(Constants.AUTHORNAME)){
				String authorname = resultData.get(Constants.AUTHORNAME).toString();
				String[] strs = authorname.split("\\|");
				if(strs.length > 1){
					authorname = strs[0].replace("提问者：", "").trim();
					resultData.put(Constants.AUTHORNAME, authorname);
				}
				
			}
		}
		ParseUtils.getIid(unit, result);
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
		return new ReProcessResult(SUCCESS, processdata);
	}
	
	private void parseByReg(Map<String, Object> dataMap, String conststr, Pattern p){
		String  resultStr = (String) dataMap.get(conststr);
		Matcher mch = p.matcher(resultStr);
		if(mch.find()){
			resultStr = mch.group();
			resultStr = resultStr.replace("年", "-")
								 .replace("月", "-")
								 .replace("日", "");
		}
		dataMap.put(conststr, resultStr.trim());
	}
}
