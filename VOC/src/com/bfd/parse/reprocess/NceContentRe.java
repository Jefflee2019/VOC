package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.entity.Constants;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 	@site：中国经济网  Nce
 * 	@function：新闻内容页后处理
 * 	@author bfd_04
 *
 */
public class NceContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NceContentRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String,Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null) {
			/**
			 * "cate": [
                " 首页 > 质量频道 > 质量资讯 > 正文"
        ], 
			 */
			if(resultData.containsKey(Constants.CATE)) {
				 List cate = (List)resultData.get("cate");
				 String[] cateArr = cate.get(0).toString().split(" > ");
				 cate.clear();          //clear old cate
				 for(String temp : cateArr) {
					 cate.add(temp.trim());
				 }
				 resultData.put(Constants.CATE, cate);
			 }
			/**
			 *  "editor": "（责任编辑：彭金美）",
			 */
			 if(resultData.containsKey("editor")) {
				String editor = resultData.get("editor").toString();
				editor = editor.replace("（责任编辑：", "").replace("）", "").trim();
				resultData.put("editor", editor);
			 }
			/**
			 * "source": "来源：中华网财经",s
			 */
			 if(resultData.containsKey(Constants.SOURCE)) {
				 String source = resultData.get(Constants.SOURCE).toString();
				 source = source.replace("来源：", "").trim();
				 resultData.put(Constants.SOURCE, source);
			 }
		}
		return new ReProcessResult(SUCCESS, processdata);
	}
}
