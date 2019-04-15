package com.bfd.parse.preprocess;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;


public class NcnmoContentPre
    implements PreProcessor
{

	private static final Log LOG = LogFactory.getLog(NcnmoContentPre.class);
	
    public NcnmoContentPre()
    {
    }

    public boolean process(ParseUnit unit, ParserFace parseFace)
    {
        String pageData = unit.getPageData();
        pageData = pageData.replace("<html style=\"visibility: hidden;\" >", "<html style=\"visibility: visible;\" class=\"false\">");
        
        unit.setPageData(pageData);
        unit.setPageBytes(pageData.getBytes());
        unit.setPageEncode("utf8");
        return true;
    }

}

