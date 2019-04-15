package com.bfd.parse.test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

import com.bfd.crawler.utils.DataUtil;
import com.bfd.crawler.utils.EncodeUtil;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.DomParser;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.config.PageDefine.PageDefineConfig;
import com.bfd.parse.config.dom.DomTemplate;
import com.bfd.parse.config.fldmap.ItemFldMapConfig;
import com.bfd.parse.config.fldmap.ItemInfoParser;
import com.bfd.parse.config.shelf.JudgeRule;
import com.bfd.parse.config.shelf.JudgeStatue;
import com.bfd.parse.config.website.WebsiteCache;
import com.bfd.parse.entity.PagedefineEntity;
import com.bfd.parse.entity.ParsetemplateEntity;
import com.bfd.parse.entity.WebsiteEntity;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

public class ParseTester {

	private static final Log LOG = LogFactory.getLog(ParseTester.class);

	private DomParser domParser;

	private static URLNormalizerClient normalizerClient; // 所有的tester共用同一个归一化链接

	static {
		normalizerClient = new URLNormalizerClient();
	}

	public ParseTester() {
		this.domParser = new DomParser("worker-test", normalizerClient);
	}

	public ParseTester(DomParser domParser) {
		this.domParser = domParser;
	}

	public String test(byte[] page, Map<String, Object> pathMap) {
		String type = (String) pathMap.get("type");
		LOG.info("parseTester::test::pathMap is "+JsonUtil.toJSONString(pathMap));
		String charset = EncodeUtil.getHtmlEncode(page);
		
		if ("tmpl".equalsIgnoreCase(type)) {
			page = TextUtil.balanceTag(page,charset);
			if (pathMap.containsKey("dompath")) {
				return testTmplParse(page, pathMap);
			} else if (pathMap.containsKey("parseInfo")) {
				return testInfoParse(page, pathMap);
			}
		} else if ("path".equalsIgnoreCase(type)) {
			String dom = (String) pathMap.get("dompath");
			//前端标模板的时候，在已经选定的区域，表东西，会增加这个class属性，原始页面里没有，需要去掉。
			dom.replace("bfd_click_node", "");
			String fldName = (String) pathMap.get("fldName");
			return testPathParse(page, dom, fldName);
		} else if ("rule".equalsIgnoreCase(type)) {
			String url = (String) pathMap.get("url");
			Map rule = (Map) pathMap.get("rule");
			return testJudgeRule(page, url, rule);
		} else if("tagbalance".equalsIgnoreCase(type)){
			page = TextUtil.balanceTag(page,charset);
			try {
				String balanceHtml = new String(page,charset);
				String zBalanceHtml = DataUtil.zipAndEncode(balanceHtml, charset);
				String res = "{\"code\":0,\"page\":\""+zBalanceHtml+"\"}";
//				LOG.info("type is tagbalance.the res is "+res);
				return res;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "{\"code\":1}";
	}

	private String testJudgeRule(byte[] page, String url, Map ruleMap) {
		JudgeRule rule = JudgeRule.create(ruleMap);
		List<JudgeRule> rules = new ArrayList<JudgeRule>();
		rules.add(rule);
		String onshelf = "";
		try {
			onshelf = JudgeStatue.judgeStatus(url, new String(page, EncodeUtil.getHtmlEncode(page)), rules);
		} catch (UnsupportedEncodingException e) {
			LOG.warn(e);
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("code", 0);
		result.put("onshelf", onshelf);
		return JsonUtil.toJSONString(result);
	}

	private String testInfoParse(byte[] page, Map<String, Object> pathMap) {
		@SuppressWarnings("unchecked")
		Map<String, Object> parseInfo = (Map<String, Object>) pathMap.get("parseInfo");
		String cid = String.valueOf(parseInfo.get("cid"));
		// 获取解析模板
		DomTemplate tpl = domParser.getDomTemplate(cid);
		// 解析DOM数据
		DocumentFragment doc = null;
		String charset = "utf8";
		try {
			charset = EncodeUtil.getHtmlEncode(page);
			doc = DomParser.parse2Html(page, charset);
		} catch (Exception e) {
			LOG.warn("parse to html exception, while parseTest by info,", e);
		}
		Map<String, Object> errMsg = new HashMap<String, Object>();
		Map<String, Object> rmap = domParser.parseData((String) parseInfo.get("type"), cid,
				(String) parseInfo.get("url"), doc, tpl, charset,errMsg);
		Map<String, Object> result = new HashMap<String, Object>();
		if (rmap != null) {
			String pageidx = null;
			int iPageidx = 1;
			if (parseInfo.containsKey("pageidx")) {
				pageidx = (String) parseInfo.get("pageidx");
				if (StringUtils.isNotEmpty(pageidx) && StringUtils.isNumeric(pageidx)) {
					iPageidx = Integer.valueOf(pageidx);
				}
			}
			handleExtraParser(page, (String) parseInfo.get("type"), cid, rmap, iPageidx, (String) parseInfo.get("url"));
			result.put("code", 0);
			result.put("data", rmap);
		} else {
			result.put("code", 1);
			result.put("errtree", domParser.getDomSearch().getTreeString());
		}
		return JsonUtil.toJSONString(result);
	}

	private void handleExtraParser(byte[] page, String type, String cid, Map<String, Object> rmap, int pageidx,
			String url) {
		// item类型判定上下架状态
//		String itemStatus = null;
//		if ("item".equalsIgnoreCase(type)) {
//			itemStatus = domParser.judgeSellStatus(cid, "", type, page, EncodeUtil.getHtmlEncode(page));
//		}
//		if (itemStatus != null)
//			rmap.put("onshelf", itemStatus);
		String nextpage = "";
		if (rmap.containsKey("nextpage")) {
			Object obj = rmap.get("nextpage");
			rmap.put("nextpage_", obj);
			nextpage = domParser.handleNextPage(pageidx, obj, cid, type, "");
			if (url.equalsIgnoreCase(nextpage))
				nextpage = "";
			rmap.put("nextpage", nextpage);
		}

		// 如果存在imgs，抽取 text(key)和img(value)信息组成Map，放入imgList
		if (domParser.getDomSearch().hasImgs() && rmap.containsKey("imgs")) {
			List<Map> imgs = (List) rmap.get("imgs");
			List<Map> imgList = new ArrayList<Map>();
			for (int i = 0; i < imgs.size(); i++) {
				Map<String, Object> valueMap = new HashMap<String, Object>();
				Map<String, Object> imgMap = imgs.get(i);
				String text = (String) imgMap.get("imgtag");
				String img = (String) imgMap.get("img");
				if (StringUtils.isEmpty(text)) {
					text = "" + (i + 1);
				}
				valueMap.put(text, StringUtils.isEmpty(img) ? "" : img);
				imgList.add(valueMap);
			}
			if (imgList.size() > 0) {
				rmap.put("imglist", imgList);
			}
		}
	}

	private String testTmplParse(byte[] data, Map<String, Object> pathMap) {
		Map<String, Object> param = (Map<String, Object>) pathMap.get("dompath");
//		ParseTemplate template = ParseTemplate.fromMap(param);
		ParsetemplateEntity template = ParsetemplateEntity.fromMap(JsonUtils.toJSONString(param));
		DomTemplate tmpl = new DomTemplate(template);
		String charset = EncodeUtil.getHtmlEncode(data);//
		LOG.debug("guess page charset=" + charset);
		DocumentFragment doc = null;
		try {
			doc = DomParser.parse2Html(data, charset);
		} catch (Exception e) {
			LOG.warn("parse to html exception, while parseTest by tmpl,", e);
		}
		domParser.getDomSearch().setMaxPrintLevel(16);
		domParser.getDomSearch().setPrintMode(2);
		Map<String, Object> errMsg = new HashMap<String, Object>();
		Map<String, Object> rmap = domParser.parseData(template.getPagetypeid()+"", template.getSiteid()+"", 
				template.getTesturl(), doc, tmpl, charset,errMsg);

		Map<String, Object> result = new HashMap<String, Object>();
		if (rmap != null) {
//			if (ItemFldMapConfig.getInstance().getFldMapRule(template.getCid()) != null) {
//				Map<String, Object> tmp = new HashMap<String, Object>();
//				try {
//					if (ItemInfoParser.getBfdItemInfo(new String(data, charset), template.getCid(),
//							(String) pathMap.get("url"), tmp))
//						rmap.putAll(tmp);
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				}
//			}
			String pageidx = null;
			int iPageidx = 1;
			if (pathMap.containsKey("pageidx")) {
				pageidx = (String) pathMap.get("pageidx");
				if (StringUtils.isNotEmpty(pageidx) && StringUtils.isNumeric(pageidx)) {
					iPageidx = Integer.valueOf(pageidx);
				}
			}
			PagedefineEntity pd = PageDefineConfig.getInstance().getPageDefine(template.getPagetypeid()+"");
			WebsiteEntity ws = WebsiteCache.getInstance().getWebsite(template.getSiteid()+"");
			handleExtraParser(data, pd.getPagenameen(), ws.getCid(), rmap, iPageidx, (String) pathMap.get("url"));
			result.put("code", 0);
			result.put("data", rmap);
		} else {
			result.put("code", 1);
			result.put("errtree", domParser.getDomSearch().getTreeString());
		}
		return JsonUtil.toJSONString(result);
	}

	private String testPathParse(byte[] data, String domPath, String fldName) {
		LOG.debug("execute parseTester::testPathParse param is domPath is "+domPath+".fldName is "+fldName);
		String charset = EncodeUtil.getHtmlEncode(data);
		DocumentFragment doc = null;
		try {
			doc = DomParser.parse2Html(data, charset);
		} catch (Exception e) {
			LOG.warn("parse to html exception, while parseTest by path,", e);
		}
		domParser.getDomSearch().setMaxPrintLevel(15);
		domParser.getDomSearch().setPrintMode(2);
		boolean isLink = false;
		if (domPath.startsWith("true|||")) {
			isLink = true;
			domPath = domPath.substring(7, domPath.length());
		}
		Node node = domParser.getDomSearch().searchNode(doc, domPath);
		String res = null;
		if (node != null) {
			res = domParser.getDomSearch().getNodeData(
					node,
					"",
					"",
					"imgurl".equalsIgnoreCase(fldName) || "small_img".equalsIgnoreCase(fldName)
							|| "large_img".equalsIgnoreCase(fldName));
			if (isLink) {
				res += ",link=" + domParser.getDomSearch().matchUrl(node, "item", "href");
			}
		}
		Map<String, Object> result = new HashMap<String, Object>();
		if (res == null) {
			result.put("code", 1);
			result.put("data", domParser.getDomSearch().getTreeString());
		} else {
			result.put("code", 0);
			result.put("data", res);
		}
		return JsonUtil.toJSONString(result);
	}
}
