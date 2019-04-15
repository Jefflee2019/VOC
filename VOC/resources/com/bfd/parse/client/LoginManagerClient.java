package com.bfd.parse.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.DownloaderPrx;
import com.bfd.crawler.LoginManagerService;
import com.bfd.crawler.LoginManagerServicePrx;
import com.bfd.crawler.LoginManagerServicePrxHelper;
import com.bfd.parse.ParserFace;

import Ice.ObjectPrx;

public class LoginManagerClient extends AbstractClient {
	private static final Log logger = LogFactory.getLog(LoginManagerClient.class);
	@Override
	public String name() {
		
		return "LoginManagerService";
	}

	@Override
	protected ObjectPrx checkedCast() {
		
		return LoginManagerServicePrxHelper.checkedCast(base);
	}

	@Override
	protected String getProxyConfig() {
		
		return "LoginManagerService";
	}
	
	@Override
	protected LoginManagerServicePrx getService() {
		return ((LoginManagerServicePrx) super.getService());
	}
	
	public void callLogin(String cid,String userId){
		getService().releaseLoginInfo(cid, userId, "invalid");
	}

	public static void main(String[] args) {
		LoginManagerClient client = new LoginManagerClient();
		String rs = client.getService().getLoginInfo("sina");
	}
}
