package com.bfd.parse.test.plugin;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.reprocess.Nhc360ContentRe;
import com.bfd.parse.util.JsonUtil;

public class Nhc360ContentReTest {
	public static void main(String[] args) throws Exception {
		testDemo();
	}

	public static void testDemo() throws Exception {
		// 要抓取链接
//		String url1 = "http://info.machine.hc360.com/2016/08/101540587348.shtml";
//		String url2 = "http://info.secu.hc360.com/2016/11/211101874903.shtml";
//		String url3 = "http://info.homea.hc360.com/2016/10/3118261164432.shtml";
//		String url4 = "http://info.ec.hc360.com/2016/11/180931885918.shtml";
//		String url5 = "http://info.it.hc360.com/2016/11/181905887071.shtml";
		String url6 = "http://info.jj.hc360.com/2016/08/311648759858.shtml";
//		String url7 = "http://info.shuma.hc360.com/2016/11/04105797765.shtml";
//		String url9 = "http://info.service.hc360.com/2016/07/181130463196.shtml";
		// 动态数据
//		List<String> ajaxlist = new ArrayList<String>();
//		ajaxlist.add(null);
		// 生成解析使用的bean
		Map<String, Object> map = CreateParseUnit.createMap("test", "test", 100, 100, url6, "gbk", null, null);
		// 生成解析需要结构
		ParseUnit unit = ParseUnit.fromMap(map, System.currentTimeMillis());
		// 模板
		String tmplStr = FileUtils.readFileToString(new File("tmpl/Nhc360_content_6.txt"));
		// 解析输出字段
		String outputFiled = "";
		ParserFace face = new ParserFace("test");
		// 执行解析流程
		// 参数分别是 预处理插件,json插件,后处理插件,模板,ParseFace,输出字段
		ParsePlugin testPlugin = new ParsePlugin(null, null, new Nhc360ContentRe(), tmplStr, face,
				outputFiled);
//		ParsePlugin testPlugin = new ParsePlugin(null, null, null, tmplStr, face, outputFiled);
		// 输出结果
		System.out.println(JsonUtil.toJSONString(testPlugin.parse(unit)));
	}
}
