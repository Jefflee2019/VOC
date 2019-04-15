package com.bfd.parse.reprocess;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：慧聪网
 * <p>
 * 主要功能：处理新闻内容页字段
 * <p>
 * @author bfd_01
 *
 */
public class Nhc360ContentRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(Nhc360ContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			
			// source
			if (resultData.containsKey(Constants.SOURCE)) {
				String source = resultData.get(Constants.SOURCE).toString();
				source = source.replace("来源：", "");
				resultData.put(Constants.SOURCE, source);
			}
			
			// author
			if (resultData.containsKey(Constants.AUTHOR)) {
				String author = resultData.get(Constants.AUTHOR).toString();
				author = author.replace("作者：", "");
				resultData.put(Constants.AUTHOR, author);
			}
			
			if (resultData.containsKey(Constants.POST_TIME)) {
				String posttime = resultData.get(Constants.POST_TIME).toString();
				posttime = posttime.replace("年", "-").replace("月", "-").replace("日", " ");
				resultData.put(Constants.POST_TIME, posttime);
			}
			
			//cate
			if (resultData.containsKey(Constants.CATE)
					&& resultData.get(Constants.CATE).toString().contains(">")) {
				// 取到cate
				List<String> cate = (List<String>) resultData
						.get(Constants.CATE);
				String[] catetemp = cate.get(0).toString().replace("[", "")
						.replace("]", "").split(" > ");
				resultData.put(Constants.CATE, Arrays.asList(catetemp));
			}
			
		}
		return new ReProcessResult(processcode, processdata);
	}
}
