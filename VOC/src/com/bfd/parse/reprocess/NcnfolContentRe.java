
package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：Ncnfol
 * 
 * 主要功能：处理作者字段
 * 
 * @author bfd_01
 */
public class NcnfolContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if(resultData != null && !resultData.isEmpty()) {
//			if (resultData.containsKey(Constants.AUTHOR)) {
//				String author = resultData.get(Constants.AUTHOR).toString();
//				resultData.put(Constants.AUTHOR, author.replace("作者：", ""));
//			}
			//来源:中国网
			if (resultData.containsKey(Constants.SOURCE)) {
				String source = resultData.get(Constants.SOURCE).toString();
				resultData.put(Constants.SOURCE, source.replace("来源:", ""));
			}
			
//			if (resultData.get(Constants.AUTHOR).equals(
//					resultData.get(Constants.SOURCE))
//					&& resultData.get(Constants.AUTHOR).equals(
//							resultData.get(Constants.POST_TIME))) {
//				String temp = resultData.get(Constants.AUTHOR).toString();
//				String posttime = formatDate(temp);
//				resultData.put(Constants.POST_TIME, posttime);
//				String temp1 = temp.split(posttime)[1];
//				if (temp1.split(" ").length == 3) {
//					resultData.put(Constants.SOURCE, temp1.split(" ")[1]);
//					resultData.remove(Constants.AUTHOR);
//				} else {
//					resultData.put(Constants.SOURCE, temp1.split(" ")[1]);
//					resultData.put(Constants.AUTHOR, temp1.split(" ")[2]);
//				}
//				
//			}
			
//			if (resultData.containsKey(Constants.CATE)) {
//				List list = (List)resultData.get(Constants.CATE);
//				for (int i=0;i<list.size();i++) {
//					if ("".equals(list.get(i))) {
//						list.remove(i);
//						i--;
//					}
//				}
//				resultData.put(Constants.CATE, list);
//			}
			
		}
		return new ReProcessResult(processcode, processdata);
	}
	
//	private String formatDate(String date) {
//		String posttime = null;
//		Pattern p = Pattern.compile("(\\d+\\S\\d+\\S\\d+\\S*\\s*\\d+:\\d+)");
//		Matcher m = p.matcher(date);
//		while(m.find()) {
//			posttime = m.group();
//		}
//		return posttime;
//	}
	
}
