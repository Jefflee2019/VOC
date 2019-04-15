package com.bfd.parse.test.plugin;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.reprocess.NyidianzixunContentRe;
import com.bfd.parse.util.JsonUtil;

public class CommonTest {
	public static void main(String[] args) throws Exception {
		testDemo();
	}

	public static void testDemo() throws Exception {
		// 要抓取链接
		// String url = "http://www.cww.net.cn/article?id=376792";
		// String url = "http://caijing.edushi.com/info/5-29-n231104.html";
		// String url = "http://finance.ynet.com/2018/06/12/1250016t632.html";
		// String url =
		// "https://www.zhihu.com/question/280480075/answers/created";
		// String url =
		// "http://search.cctv.com/search.php?qtext=%E5%8D%8E%E4%B8%BA&type=web";
		// String url = "https://search.jd.com/Search?keyword=澳柯玛冰箱&enc=utf-8";
		// String url = "https://www.toutiao.com/a6609161334300869133";
		String url = "http://www.yidianzixun.com/article/0LhMnQq8?searchword=%E5%8D%8E%E4%B8%BA";
		// String url = "http://mobile.zol.com.cn/532/5325383.html";
		// String url = "http://mobile.pconline.com.cn/1108/11085990.html";
		// String url =
		// "http://it.enorth.com.cn/system/2018/05/04/035463777.shtml";
		// String curl =
		// "http://it.enorth.com.cn/system/2018/05/29/035598286.shtml";
		// String url =
		// "http://news.enorth.com.cn/system/2016/08/29/031126211.shtml";
		// String url =
		// "http://it.enorth.com.cn/system/2018/05/09/035486234.shtml";
		// 动态数据
//		 List<String> ajaxlist = new ArrayList<String>();
//		 ajaxlist.add("http://www.elecfans.com/webapi/arcinfo/apiGetCommentJson?page=1&order=new&aid=705348");
		// 生成解析使用的bean
//		 Map<String, Object> map = CreateParseUnit.createMap("test", "test",100, 100, url, "gbk", null, ajaxlist);
		Map<String, Object> map = CreateParseUnit.createMap("test", "test", 100, 100, url, "utf8", null, null);
		// 生成解析需要结构
		ParseUnit unit = ParseUnit.fromMap(map, System.currentTimeMillis());
		// 模板
		String tmplStr = FileUtils.readFileToString(new File("tmpl/Nyidianzixun_content_1.txt"));
		// 解析输出字段
		String outputFiled = "";
		ParserFace face = new ParserFace("test");
		// 执行解析流程
		// 参数分别是 预处理插件,json插件,后处理插件,模板,ParseFace,输出字段
//		 ParsePlugin testPlugin = new ParsePlugin(null,new NelecfansCommentJson(),null, null, face,outputFiled);
//		ParsePlugin testPlugin = new ParsePlugin(new BfengPostPre(), null,new BfengPostRe() , tmplStr, face, outputFiled);
		 ParsePlugin testPlugin = new ParsePlugin(null, null, new NyidianzixunContentRe(), tmplStr, face, outputFiled);
		// 输出结果
		System.out.println(JsonUtil.toJSONString(testPlugin.parse(unit)));
	}
}
