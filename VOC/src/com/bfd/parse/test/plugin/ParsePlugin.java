package com.bfd.parse.test.plugin;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.html.dom.HTMLDocumentImpl;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.jsoup.Jsoup;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.bfd.parse.DomParser;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParseResult.ParseData;
import com.bfd.parse.ParserFace;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.config.dom.DomCFGTree;
import com.bfd.parse.config.dom.DomSearch;
import com.bfd.parse.entity.ParsetemplateEntity;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.json.JsonData;
import com.bfd.parse.json.JsonParser;
import com.bfd.parse.json.JsonParserResult;
import com.bfd.parse.preprocess.PreProcessor;
import com.bfd.parse.reprocess.ReProcessor;
import com.bfd.parse.util.TextUtil;

public class ParsePlugin {
	public PreProcessor preProcessor = null;
	public JsonParser jsonParser = null;
	public ReProcessor reProcessor = null;
	public String tmpl = null;
	public ParserFace parseFace = null;
	public String output = null;

	/**
	 * 
	 * @param preProcessor
	 *            预处理插件
	 * @param jsonParser
	 *            json插件
	 * @param reProcessor
	 *            后处理插件
	 * @param tmpl
	 *            模板
	 * @param parseFace
	 * @param output
	 *            输出字段
	 */
	public ParsePlugin(PreProcessor preProcessor, JsonParser jsonParser, ReProcessor reProcessor, String tmpl, ParserFace parseFace,
			String output) {
		super();
		this.preProcessor = preProcessor;
		this.jsonParser = jsonParser;
		this.reProcessor = reProcessor;
		this.tmpl = tmpl;
		this.parseFace = parseFace;
		String o = "";
		if (output == null || output.length() <= 0) {
			if (tmpl != null && tmpl.length() > 0) {
				Pattern p = Pattern.compile("name=\"(.*?)\"");
				Matcher m = p.matcher(tmpl);
				while (m.find()) {
					o = o + m.group(1) + ",";
				}
			}
			if (o.length() > 0) {
				output = o.substring(0, o.length() - 1);
			}
		}
		this.output = output;
	}

	public Map<String, Object> parse(ParseUnit unit) throws Exception {
		URLNormalizerClient urlNormalizerClient = new URLNormalizerClient();
		ParseResult result = ParseResult.prepareObj(unit);
		unit.setPageData(unit.getData());
		// 预处理
		if (preProcessor != null) {
			preProcessor.process(unit, parseFace);
		}
		// tmpl
		if (tmpl != null && tmpl.trim().length() > 0) {
			tmpl = tmpl.trim();
			ParseData parsedata = result.getParsedata();
			Map<String, Object> webParseResult = webParse(unit, this.tmpl, this.output);
			parsedata.getData().putAll(webParseResult);
		}
		// json
		if (jsonParser != null) {
			List<JsonData> jsonDataList = TextUtil.wrapJsonData(unit, false);
			JsonParserResult jres = jsonParser.parse(unit.getTaskdata(), jsonDataList, urlNormalizerClient, unit);
			result.getParsedata().setParsecode(jres.getParsecode());
			result.getParsedata().addData(jres.getData());
		}
		// 后处理
		if (reProcessor != null) {
			reProcessor.process(unit, result, parseFace);
		}
		return result.getParsedata().getData();
	}

	/**
	 * 生成dom树
	 * @param input
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public static DocumentFragment createDom(String data, String charset) throws Exception {
		return createDom(new InputSource(new ByteArrayInputStream(data.replace((char) 26, (char) 32).getBytes(charset))), charset);
	}

	/**
	 * 生成dom树
	 * @param input
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public static DocumentFragment createDom(InputSource input, String encoding) throws Exception {
		DOMFragmentParser parser = new DOMFragmentParser();
		try {
			parser.setFeature("http://cyberneko.org/html/features/augmentations", true);
			parser.setProperty("http://cyberneko.org/html/properties/default-encoding", encoding);
			parser.setFeature("http://cyberneko.org/html/features/scanner/ignore-specified-charset", true);
			parser.setFeature("http://cyberneko.org/html/features/balance-tags/ignore-outside-content", false);
			parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment", true);
			parser.setFeature("http://cyberneko.org/html/features/report-errors", false);
		} catch (SAXException e) {
			e.printStackTrace();
		}
		HTMLDocumentImpl doc = new HTMLDocumentImpl();
		doc.setErrorChecking(false);
		DocumentFragment res = doc.createDocumentFragment();
		DocumentFragment frag = doc.createDocumentFragment();
		try {
			parser.parse(input, frag);
		} catch (Exception e) {
			throw e;
		}
		res.appendChild(frag);
		try {
			while (true) {
				frag = doc.createDocumentFragment();
				parser.parse(input, frag);
				if (!frag.hasChildNodes())
					break;
				res.appendChild(frag);
			}
		} catch (Exception e) {
			throw e;
		}
		return res;
	}

	public static DocumentFragment buildTmpl(String tmpl, String output) throws Exception {
		String XMLhead = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		StringBuilder sbBuilder = new StringBuilder();
		sbBuilder.append(XMLhead);
		sbBuilder.append(tmpl);
		DocumentFragment doc = createDom(sbBuilder.toString(), "utf8");
		return doc;
	}

	public Map<String, Object> webParse(ParseUnit unit, String tmplstr, String output) throws Exception {
		DocumentFragment tmplDoc = buildTmpl(tmplstr, output);
		ParsetemplateEntity tmplate = new ParsetemplateEntity();
		tmplate.setOutput(output);
		tmplate.setPagetypeid(unit.getPageTypeId());
		tmplate.setNum((byte) 1);
		tmplate.setStatus((byte) 1);
		tmplate.setTmplid(1);
		tmplate.setRequired("");
		DomCFGTree tmpl = new DomCFGTree(tmplDoc.getFirstChild(), tmplate);

		// 解析DOM数据
		DocumentFragment doc = null;
		// 标签补全
		try {
			String page = new String(unit.getPageData().getBytes(), "utf-8");
			page = Jsoup.parse(page).html();
			doc = DomParser.parse2Html(page.getBytes(unit.getCharset()), unit.getCharset());
		} catch (Exception e) {
			e.printStackTrace();
		}
		DomSearch domSearch = new DomSearch();
		domSearch.setNormalizerClient(new URLNormalizerClient());
		domSearch.executeTemplateParse(doc, tmpl, unit.getPageTypeId() + "", unit.getUrl(), unit.getCharset());
		Map<String, Map<String, String>> contentImg = domSearch.getContentimgs();
		if (contentImg != null && contentImg.size() > 0) {
			domSearch.getParseResult().put("contentimgs", contentImg);
		}
		return domSearch.getParseResult();
	}
}
