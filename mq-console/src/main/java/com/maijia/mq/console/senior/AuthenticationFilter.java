package com.maijia.mq.console.senior;

import com.maijia.mq.console.AbstractFilter;
import com.maijia.mq.console.Util;

import java.util.Map;

public class AuthenticationFilter extends AbstractFilter {

	@Override
	public CheckResult filter(Object... inParam) {
		
		Map<String,Object> map=(Map<String,Object>)inParam[1];
		
		String systemid = Util.getStrOfObj(map.get("systemid"));
		String uuid = Util.getStrOfObj(map.get("uuid"));
		String uuidkey = Util.getStrOfObj(map.get("uuidkey"));
		/*if(Constants.keyStore.get(systemid)==null){
			return CheckResult.NULL_SYSTEMID;
		}*/
		if("".equals(systemid)||"".equals(uuid)||"".equals(uuidkey)){
			return CheckResult.NULL_UUID_UUIDKEY_SYSTEMID ;
		}
		if(!checkAccess(systemid,uuid,uuidkey)){
			return CheckResult.CHECK_FAILURE;
		}
//		if(!checkUuid(uuid)){
//			return CheckResult.DUPLICATED_UUID;
//		}
		return getSuccessor().filter(inParam);//deliver to the next filter

	}
	
	
	/**
	 * 解析参数中的key，返回是否通过验证
	 * 参数中的key标示需要约定，暂定为platAccess
	 * map 该map中至少要包含三个参数，systemid-平台标识，
	 * uuid-请求方生成的不重复的32位标示符，uuidkey-请求方对uuid加密后形成的密文
	 * @return int
	 * -3------systemid不正确
	 * -2------参数中系统标识、uuid或者uuidkey缺失，验证失败
	 * -1------uuid和uuidkey不匹配，验证失败
	 *  0------密钥中的uuid重复，验证失败
	 *  1------密钥通过验证，验证成功
	 */
	public int securityFilter() {
		
//		String systemid = Util.getStrOfObj(map.get("systemid"));
//		String uuid = Util.getStrOfObj(map.get("uuid"));
//		String uuidkey = Util.getStrOfObj(map.get("uuidkey"));
//		if(Constants.keyStore.get(systemid)==null){
//			return -3;
//		}
//		if("".equals(systemid)||"".equals(uuid)||"".equals(uuidkey)){
//			return -2;
//		}
//		if(!checkAccess(systemid,uuid,uuidkey)){
//			return -1;
//		}
////		if(!checkUuid(uuid)){
////			return 0;
////		}
		return 1;
	}

	/**
	 * 根据平台标识验证密钥是否正确
	 * 判断根据平台对应的密钥生成的密文与请求方传来的密文是否相同，相同表示验证通过，否则验证失败
	 * @param systemid
	 * @param uuid
	 * @param uuidkey
	 * @return boolean 
	 */
	private boolean checkAccess(String systemid,String uuid,String uuidkey){
		return true;
		/*String key = Constants.keyStore.get(systemid);
		try {
			if(uuidkey.equals(AESUtil.encryptStr(uuid,key))){
				return true;
			}
		} catch (Exception e) {
//			logger.info(e.getMessage());
		}
		return false;*/
	}
	
	/**
	 * 验证密钥中的uuid是否重复
	 * @return
	 *//*
	private boolean checkUuid(String uuid){
		
		if(SecurityParamCache.uuidMap.containsKey(uuid)){
			return false;
		}else{
			SecurityParamCache.uuidMap.put(uuid, "");
			return true;
		}
	}*/
}
