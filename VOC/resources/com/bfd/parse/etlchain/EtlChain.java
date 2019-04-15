package com.bfd.parse.etlchain;

import java.util.Map;

public interface EtlChain {

	public void etl(String str,Map<String, Object> rs);
}
