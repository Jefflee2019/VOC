package com.bfd.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.config.iid.ParseCfg;
import com.bfd.parse.config.iid.ParseConfigure;
import com.bfd.parse.config.iid.ParseReProcessor;
//import com.bfd.parse.data.ParseRule;
//import com.bfd.parse.data.ParseRuleMap;
//import com.bfd.parse.data.TitleRule;
//import com.bfd.parse.data.TitleRuleMap;
//import com.bfd.parse.data.Website;
//import com.bfd.parse.data.WebsiteMap;
//import com.bfd.parse.facade.parseunit.ParseUnit;
//import com.bfd.parse.service.PageRuleParser;
//import com.bfd.crawler.utils.ConfigUtils;
//import com.bfd.parse.util.JsonUtil;
//import com.bfd.parse.utils.HttpUtil;
//import com.bfd.parse.utils.UrlUtils;
//import com.bfd.parse.utils.XPathUtil;

public class AutoRuleParser {

//	private static final Log LOG = LogFactory.getLog(AutoRuleParser.class);
//	private URLNormalizerClient normalizer = new URLNormalizerClient();
//	private static final Pattern searchLinkPattern = Pattern.compile("search", Pattern.CASE_INSENSITIVE);
//
//	private static final Pattern[] urlExcludeFilter = {
//			Pattern.compile("redirect\\.php\\?tid=.*?(&goto=).*", Pattern.CASE_INSENSITIVE),
//			Pattern.compile(
//					ConfigUtils
//							.getInstance()
//							.getProp(
//									"url.binFileRegEx",
//									".*?\\.(jpg|bmp|gif|png|ico|jpeg|docx?|xlsx?|pptx?|iso|mpg|mpeg|rmvb|mp4|mov|swf|mp3|vob|avi|mkv|asf|wmv|wma|rm|wav|mid|flv|3gp|tiff|psd|mka|mpg|mpe|rss|zip|rar|z|txt|jar|tar|wps|pdf|exe|bin|vod|dotx?|chm|rpm)$"),
//					Pattern.CASE_INSENSITIVE) };
//
//	private String thdName;
////	private ParserFace parseFace;
//
//	public AutoRuleParser(String workname) {
//		this.thdName = workname;
////		this.parseFace = parseFace;
//	}
//
//	public String getThdName() {
//		return thdName;
//	}
//
//	public Map<String, Object> parse(ParseUnit unit) {
//		if (unit.isList()) {
//			return getUrls(unit.getPageData(), unit.getUrl(), unit.getPageType(), unit.getCid());
//		} else {
//			return parse(unit.getPageData(), unit.getUrl(), unit.getCid());
//		}
//	}
//
//	private Map<String, Object> parse(String pageData, String url, String cid) {
//		LOG.info(thdName + " begin to auto parse info page, url->" + url);
//		Website website = WebsiteMap.INSTANCE.get(cid);
//		boolean clean = (website == null) ? false : website.getCleantag() == 1;
//		List<ParseRule> titleRules = ParseRuleMap.getTitleParseRules(cid);
//		List<ParseRule> contentRules = ParseRuleMap.getContentParseRules(cid);
//		List<TitleRule> rule = TitleRuleMap.getRule(cid);
//		List<ParseRule> breadRules = ParseRuleMap.getBreadParseRules(cid);
//		List<ParseRule> dateRules = ParseRuleMap.getDateParseRules(cid);
//		List<Node> nodes = new ArrayList<Node>();
//		LOG.info(thdName + " begin to auto parse info page,PageRuleParser parse, url->" + url);
//		Map<String, Object> data = PageRuleParser.parse(pageData, cid, url, clean, titleRules, contentRules,
//				breadRules, dateRules, rule, nodes);
//		if (nodes != null && nodes.size() > 0) {
//			Map<String, Object> imageMap = getContentLinks(nodes.get(0), url, cid);
//			data.putAll(imageMap);
//		}
//		LOG.info(thdName + " finished auto parsing info page, url->" + url);
//		return data;
//	}
//
//	private Map<String, Object> getContentLinks(Node contentNode, String url, String cid) {
//		List<Map<String, String>> imgtasks = new ArrayList<Map<String, String>>();
//		Map<String, Object> result = new HashMap<String, Object>();
//		Map<String, Map<String, String>> contentImgs = new HashMap<String, Map<String, String>>();
//		int idx = 0;// getVideoImage(contentNode, imgtasks, contentImgs);
//		NodeList nodes = XPathUtil.searchNodeList(contentNode, ".//IMG");
//		if (nodes == null || nodes.getLength() == 0)
//			return result;
//
//		for (int i = 0; i < nodes.getLength(); i++) {
//			Node imgNode = nodes.item(i);
//			String rawImg = XPathUtil.getImageLink(imgNode);
//			if (StringUtils.isEmpty(rawImg)) {
//				continue;
//			}
//			String imgUrl = null;
//			imgUrl = this.normalizer.normalize(cid, "img", rawImg, url, true);
//			if (StringUtils.isEmpty(imgUrl)) {
//				LOG.warn(thdName + " normalized image failed, rawImg=" + rawImg + ", url=" + url);
//				continue;
//			}
//			String imgTag = "img_" + idx;
//			idx++;
//			Map<String, String> taskImg = new HashMap<String, String>();
//			taskImg.put("img", imgUrl);
//			taskImg.put("rawimg", rawImg);
//			taskImg.put("imgtag", imgTag);
//			imgtasks.add(taskImg);
//
//			Map<String, String> contentImg = new HashMap<String, String>();
//			contentImg.put("img", imgUrl);
//			contentImg.put("rawimg", rawImg);
//			contentImg.put("imgtag", imgTag);
//			contentImgs.put(imgTag, contentImg);
//			contentImgs.put(rawImg, contentImg);
//		}
//		if (imgtasks.size() > 0) {
//			result.put("imgtasks", imgtasks);
//			result.put("contentimgs", contentImgs);
//		}
//		return result;
//	}
//
//	private Map<String, Object> getUrls(String pageData, String url, String type, String cid) {
//		LOG.info(thdName + " autoparse parse listpage, begin to get urls, url -> " + url);
//		Map<String, Object> data = new HashMap<String, Object>();
//		Document doc = Jsoup.parse(pageData);
//		data.put("items", getInternalLinks(cid, url, type, doc));
//		return data;
//	}
//
//	private List<Map<String, Object>> getInternalLinks(String cid, String baseurl, String type, Document doc) {
//		Elements linkElements = doc.select("a");
//		List<Map<String, Object>> urlList = new ArrayList<Map<String, Object>>();
//		Set<String> urlDepSet = new HashSet<String>();
//		int cnt = 0;
//		Integer iidtype = WebsiteMap.INSTANCE.get(cid).getIidtype();
//		for (; cnt < linkElements.size(); cnt++) {
//			Element element = linkElements.get(cnt);
//			String rawUrl = element.attr("href");
//
//			if (StringUtils.isEmpty(rawUrl)) // 过滤为空的链接
//				continue;
//			rawUrl = rawUrl.trim();
//
//			final String text = element.text(); // 过滤无文字链接
//			if (StringUtils.isEmpty(text)) {
//				LOG.debug(getThdName() + " there is no text between <a> tag, will skip it, url=" + rawUrl);
//				continue;
//			}
//
//			if (rawUrl.startsWith("javascript") || rawUrl.startsWith("#")) // 过滤为javascript链接
//				continue;
//
//			Elements children = element.children(); // 直接图片的url不做抽取
//			if (children != null && children.size() > 0 && "img".equalsIgnoreCase(children.first().nodeName())) {
//				LOG.debug(getThdName() + " url is a img link, will skip it, url=" + rawUrl);
//				continue;
//			}
//
//			boolean exclude = false;
//			for (Pattern p : urlExcludeFilter) {
//				if (p.matcher(rawUrl).find()) {
//					LOG.debug(getThdName() + " urlExcludeFilter filte url unexcepted, will skip it, url=" + rawUrl);
//					exclude = true;
//					break;
//				}
//			}
//			if (exclude) {
//				continue;
//			}
//			LOG.debug(thdName + " begin to normalize, rawUrl=" + rawUrl + ", baseurl=" + baseurl);
//			Map<String, Object> resMap = this.normalizer.normalizeExt(cid, type, rawUrl, baseurl, true,
//					false);
//			Integer code = (Integer) resMap.get("code");
//			String url = (String) resMap.get("url");
//			String itemiid = (String) resMap.get("bfdiid");
//			if (code == null || (code != 0 && code != 3) || StringUtils.isEmpty(url) || StringUtils.isEmpty(itemiid)) {
//				LOG.debug(getThdName() + " normalized error code, or link is empty.");
//				continue;
//			}
//			if (urlDepSet.contains(url) || !UrlUtils.isInternalLink(url, baseurl) || baseurl.equalsIgnoreCase(url)
//					|| isSearchLink(url))
//				continue;
//
//			// iidtype为1时，使用iid规则
//			if (iidtype != null && iidtype == 1) { // 0-> md5, 1->iid规则
//				ParseCfg config = ParseConfigure.getInstance().getParseConfig(cid);
//				if (config == null) {
//					LOG.warn(getThdName() + " iid rule empty, cid -> " + cid);
//					continue;
//				}
//				Pattern pattern = config.getIidPattern();
//				itemiid = ParseReProcessor.parseIid(pattern, url);
//				if (StringUtils.isEmpty(itemiid)) {
//					LOG.warn(getThdName() + " url match iid rule failed, url -> " + url);
//					continue;
//				}
//				LOG.info(getThdName() + " got iid from url by iidrule, itemiid->" + itemiid + ", url->" + url);
//			}
//
//			urlDepSet.add(url);
//			Map<String, Object> itemMap = new HashMap<String, Object>();
//			Map<String, String> linkMap = new HashMap<String, String>();
//
//			linkMap.put("link", url);
//			linkMap.put("type", "info");
//			linkMap.put("rawlink", rawUrl);
//			itemMap.put("itemiid", itemiid);
//			itemMap.put("itemlink", linkMap);
//			itemMap.put("itemname", text.trim());
//			urlList.add(itemMap);
//		}
//		LOG.info(getThdName() + " got internal links size=" + urlList.size() + ", all=" + (cnt - 1));
//		return urlList;
//	}
//
//	/**
//	 * 链接的path部分是否为search形式
//	 */
//	protected boolean isSearchLink(String url) {
//		try {
//			String path = UrlUtils.getPath(url);
//			if (StringUtils.isEmpty(path)) {
//				return false;
//			}
//			return searchLinkPattern.matcher(path).find();
//		} catch (Exception e) {
//			LOG.warn("exception while judge search link, url=" + url, e);
//		}
//		return true;
//	}
//
//	public static void main(String[] args) {
//		ParserFace parseFace = new ParserFace("");
//		String url = "http://www.cnbeta.com/articles/80159.htm";
//		String normalize = parseFace.getNormalizerClient().normalize("Czhongwenyjzx", "img",
//				"http://static.cnbetacdn.com/upimg/100510/zhangxiaolu_191809588165534.jpg", url, true);
//		System.out.println(normalize);
//	}
//
//	public static void main2(String[] args) {
//		PropertyConfigurator.configure("log4j.properties");
//		String url = "http://www.yseeker.com/archives/3295.html";
//		String pageData = HttpUtil.getHtml(url);
//		String cid = "Cpinweiyahu";
//		// System.out.println(Parser.getPageType(pageData, cid, url, false));
////		AutoRuleParser autoParser = new AutoRuleParser("", new ParserFace(""));
////		// Map<String, Object> data = autoParser.getUrls(pageData, url,
////		// "list",
////		// cid);
////		Map<String, Object> data = autoParser.parse(pageData, url, cid);
////		System.out.println(JsonUtil.toJSONString(data));
//	}
}
