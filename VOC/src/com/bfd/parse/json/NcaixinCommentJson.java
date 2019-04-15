package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;
/**
 *	站点：拍拍华为官方旗舰店
 *	作用：处理商品评论
 * @author bfd_05
 *
 */
public class NcaixinCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NcaixinCommentJson.class);
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient arg2, ParseUnit unit) {
			int parsecode = 0;
			Map<String, Object> parseData = new HashMap<String, Object>();
			// 遍历dataList
			for (JsonData jsonData : dataList) {
				if (!jsonData.downloadSuccess()) {
					continue;
				}
				String json = TextUtil.getUnzipJson(jsonData, unit);
				try {
					
					if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
							&& (json.indexOf("[") < json.indexOf("{"))) {
						json = json.substring(json.indexOf("["),
								json.lastIndexOf("]") + 1);
					} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0 && json.indexOf("try{") > 0) {
						
						json = json.substring(json.indexOf("{"),
								json.lastIndexOf("try{") - 1);
					}
					
					executeParse(parseData, json, jsonData.getUrl(), unit);
				} catch (Exception e) {
//					e.printStackTrace();
					parsecode = 500012;
					LOG.warn(
							"JsonParser exception, taskdata url="
									+ taskdata.get("url") + ".jsonUrl :"
									+ jsonData.getUrl(), e);
				}
			}
			// 组装返回结果
			JsonParserResult result = new JsonParserResult();
			try {
				result.setParsecode(parsecode);
				result.setData(parseData);
			} catch (Exception e) {
//				e.printStackTrace();
				LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
			}
			return result;
		}
	
	@SuppressWarnings("unchecked")
	private void executeParse(Map<String, Object> parseData, String json, String url, ParseUnit unit) {
		Object obj = null;
		
		try {
			JSONObject jbs = new JSONObject(json);
			obj = JsonUtil.parseObject(jbs.toString());
		} catch (Exception e) {
			LOG.error("Ncaixin_CommentJson convert json error" );
		}
		
		//"http://c2.caixin.com/comment-api-caixin/comment/treelist.do?appid=100&topic_id=101076184&req_type=99&page=1&size=15";
		if(obj instanceof Map){
			Map<String, Object> data = (Map<String, Object>) obj;
			boolean success = (boolean) data.get("success");
			int cc = Integer.parseInt((String) data.get("cc")); //总条数
			if (success && cc != 0) {
				/**+
				 * 解析数据
				 */
				List result = (List) data.get("list");
				List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();//评论
				for (Object object : result) {
					Map map = (Map) object;
					Map comment = new HashMap();//评论
					String comment_content = (String) map.get("content");//内容
					String comment_time = (String) map.get("createTime");//回复日期
					int up_cnt = Integer.parseInt((String) map.get("count"));//顶！d=====(￣▽￣*)b
					String username = (String) map.get("author");//作者
					comment.put(Constants.COMMENT_CONTENT, comment_content);
					comment.put(Constants.COMMENT_TIME, ConstantFunc.convertTime(comment_time));
					comment.put(Constants.UP_CNT, up_cnt);
					comment.put(Constants.USERNAME, username);
					list.add(comment);
				}
				parseData.put(Constants.COMMENTS, list);
				/**
				 * 下一页链接
				 */
				int size = 15;//单页条数
				int page = cc / 15;
				String pageNo = this.getCresult(url, "page=(\\d+)");
				int pp = Integer.parseInt(pageNo);
				if (pp <= page) {
					pp += 1;
					String tmp = "page=" + pp;
					String nextUrl = url.replace(this.getCresult(url, "(page=\\d+)"), tmp);
					Map<String, Object> nexpageTask = new HashMap<String, Object>();
					List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
					nexpageTask.put(Constants.LINK, nextUrl);
					nexpageTask.put(Constants.RAWLINK, nextUrl);
					nexpageTask.put(Constants.LINKTYPE, "newscomment");		
					taskList.add(nexpageTask);
					parseData.put(Constants.TASKS, taskList);
					parseData.put(Constants.NEXTPAGE, nexpageTask);
				}
			}
		}
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

}
