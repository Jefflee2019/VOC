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
 * @site：网易手机/数码
 * @function：添加评论等
 * @author bfd_04
 *
 */
public class Emobile163ContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(Emobile163ContentRe.class);
	private static final Pattern PRICEPATTERN = Pattern.compile("(\\d+)");
	private static final Pattern PRODIDPATTERN = Pattern.compile("/Huawei/(.*?)/");
	private static final String COMM_URL_HEAD = "http://product.mobile.163.com/comments/getPage?productid=";
	private static final String COMM_URL_END= "&pagenum=1";
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String,Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		if(resultData != null ) {
			//deal with price
			 if(resultData.containsKey(Constants.PRICE)) {
				 String price = resultData.get(Constants.PRICE).toString();
				 Matcher priceMch = PRICEPATTERN.matcher(price);
				 if(priceMch.find()) {
					 price = priceMch.group(1);
				 } else {
					 price = "";
				 }
				 resultData.put(Constants.PRICE, price);
			 }

			 //deal with comment
			 Map<String, Object> commentTask= new HashMap<String, Object>();
			 Matcher commMatch = PRODIDPATTERN.matcher(url);
			 
			 if(commMatch.find()) {
				 try{
					 String prodId = commMatch.group(1);
					 StringBuilder sb = new StringBuilder();
					 String commUrl = sb.append(COMM_URL_HEAD).append(prodId)
							 .append(COMM_URL_END).toString();
					 commentTask.put("link", commUrl);
					 commentTask.put("rawlink", commUrl);
					 commentTask.put("linktype", "eccomment");
					if (resultData != null && !resultData.isEmpty()) {
						resultData.put(Constants.COMMENT_URL, commUrl);
						resultData.put(Constants.NEXTPAGE, commUrl);
						List<Map> tasks = (List<Map>) resultData.get("tasks");
						tasks.add(commentTask);	
					}
				 } catch (Exception e) {
					LOG.error("regex parse error");
				 }
			 }
			 
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
