package com.bfd.parse.preprocess;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;


public class BbaidutiebajingpinListPre
    implements PreProcessor
{

	private static final Log LOG = LogFactory.getLog(BbaidutiebajingpinListPre.class);
	private static final Pattern patter = Pattern.compile("<code class=\"pagelet_html\" id=\"pagelet_html_frs-list/pagelet/thread_list\" style=\"display:none;\"><!--[\\s\\S]*?</code>");
	
    public BbaidutiebajingpinListPre()
    {
    }

    public boolean process(ParseUnit unit, ParserFace parseFace)
    {
        String pageData = unit.getPageData();
        Matcher match = patter.matcher(pageData);
        if(match.find())
            try
            {
                String attrs = match.group(0);
                String after_attrs = attrs.replaceAll("<!--", "").replaceAll("-->", "").replace("<code class=\"pagelet_html\" id=\"pagelet_html_frs-list/pagelet/thread_list\" style=\"display:none;\">", "").replace("</code>", "");
                pageData = pageData.replace(attrs, after_attrs);
                LOG.info("wan cheng yu chu li  >>>> ");
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        unit.setPageData(pageData);
        unit.setPageBytes(pageData.getBytes());
        unit.setPageEncode("utf8");
        return true;
    }

}

