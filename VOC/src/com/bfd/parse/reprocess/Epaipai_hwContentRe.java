package com.bfd.parse.reprocess;

import java.util.ArrayList;
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
 * 拍拍荣耀旗舰店
 * 电商详情下
 * @author bfd_05
 *
 */
public class Epaipai_hwContentRe implements ReProcessor {
	private static final Pattern PNUM = Pattern.compile("\\d+");
	private static final Pattern SMALLPIC = Pattern.compile("[http:/\\w\\.-]*80x80.jpg");
	private static final Pattern IID = Pattern.compile("([\\w+\\d+]*)\\?ptag=40042.14.12");
	
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processData = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getTaskdata().get("url").toString();
		String commUrl = "http://shop1.paipai.com/cgi-bin/creditinfo/NewCmdyEval?sCmdyId=%s&nCurPage=1&nTotal=0"
				+ "&resettime=1&nFilterType=1&nSortType=11&g_ty=ls";
		Matcher iidMch = IID.matcher(url);
		if(iidMch.find()){
			String iidStr = iidMch.group(1);
			commUrl = String.format(commUrl, iidStr);
			resultData.put(Constants.COMMENT_URL, commUrl);
			Map<String, Object> commUrlMap = new HashMap<String, Object>();
			commUrlMap.put("link", commUrl);
			commUrlMap.put("rawlink", commUrl);
			commUrlMap.put("linktype", "eccomment");
			List<Map<String, Object>>  tasks = (List<Map<String, Object>>) resultData.get("tasks");
			tasks.add(commUrlMap);
		}
		
		String pageData = unit.getPageData();
		//小图
		Pattern picListPat = Pattern.compile("picList:\\[.*?\\]]");
		Matcher picListMch = picListPat.matcher(pageData);
		if(picListMch.find()){
			String picListStr = picListMch.group();
			Matcher picMch = SMALLPIC.matcher(picListStr);
			List<String> smallImgList = new ArrayList<>();
			while (picMch.find()) {
				smallImgList.add(picMch.group());
			}
			resultData.put(Constants.SMALL_IMG, smallImgList);
		}
		// 市场价
		if (resultData.containsKey(Constants.MARKETLOWERPRICE)) {
			String maketprice = (String) resultData.get(Constants.MARKETLOWERPRICE);
			String[] maketprices = maketprice.split("-");
			if(maketprices.length > 1){
				resultData.put(Constants.MARKETLOWERPRICE, maketprices[0].trim());
				resultData.put(Constants.MARKETUPPERPRICE, maketprices[1].trim());
			}
			else {
				resultData.put(Constants.MARKETLOWERPRICE, maketprice);
				resultData.put(Constants.MARKETUPPERPRICE, maketprice);
			}
		}
		if(resultData.containsKey(Constants.VIEW_CNT)){
			String viewCnt = (String) resultData.get(Constants.VIEW_CNT);
			Matcher mch = PNUM.matcher(viewCnt);
			if(mch.find()){
				resultData.put(Constants.VIEW_CNT, mch.group());
			}
		}
		if (resultData.containsKey(Constants.BRIEF)) {
			String brief = (String) resultData.get(Constants.BRIEF);
			brief = brief.replace("\t", "");
			resultData.put(Constants.BRIEF, brief);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processData);
	}
}
