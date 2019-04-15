package com.bfd.parse.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;

public class NbilibiliContentJson implements JsonParser{
	
	private static final Log LOG = LogFactory.getLog(NbilibiliContentJson.class);
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
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
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
				executeParse(parseData, json, jsonData.getUrl(), unit);
			} catch (Exception e) {
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
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}
	
	
	/**
	 * 处理xml
	 * @param parsedata
	 * @param json
	 * @param url
	 * @param unit
	 */
	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parseData,
			String json, String url, ParseUnit unit){
		List<Map> barrages = new ArrayList<Map>();
		parseData.put("barrages", barrages);
		try {
            InputStream in = new ByteArrayInputStream(json.getBytes());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(in);
            XPathFactory factory = XPathFactory.newInstance();  
            XPath xpath = factory.newXPath();  
            NodeList nodes = (NodeList)xpath.evaluate("/i/d", doc, XPathConstants.NODESET);  
            if (nodes.getLength() == 0) {
				return;
			}
            for (int i = 0; i < nodes.getLength(); i++) {  
            	Map barrage = new HashMap();
                Node node = nodes.item(i);    
                String barrage_content = node.getTextContent(); //弹幕内容
                String properties = (String) xpath.evaluate("@p", node, XPathConstants.STRING);  
                String[] value = properties.split(",");
                String barrage_posttime = this.TimeStamp2Date(value[4]); //弹幕发表时间
//                    System.out.println("barrage_posttime="+barrage_posttime+";barrage_content="+barrage_content);
                barrage.put("barrage_content", barrage_content);
                barrage.put("barrage_posttime", barrage_posttime);
                barrages.add(barrage);
           }  
		} catch (XPathExpressionException e) {  
            e.printStackTrace();  
        } catch (ParserConfigurationException e) {  
            e.printStackTrace();  
        } catch (SAXException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
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
	/**
	 * 正则匹配字符串
	 * @param str
	 * @param pattern
	 * @return
	 */
	private String getCresult1(String str,String reg){
		Pattern pattern = Pattern.compile(reg);
		Matcher mch = pattern.matcher(str);
		if(mch.find()){
			return mch.group(0);
		}
		return str;
	}
	/**
	 * 转换时间戳
	 * @param timestampString
	 * @return
	 */
	private String TimeStamp2Date(String timestampString){  
	    Long timestamp = Long.parseLong(timestampString)*1000;  
	    String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp));  
	    return date;  
	} 
	
}
