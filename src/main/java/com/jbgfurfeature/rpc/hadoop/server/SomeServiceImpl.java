package com.bgfurfeature.rpc.hadoop.server;

import com.bgfurfeature.rpc.hadoop.protocol.SomeService;

/**
 *  服务端协议的具体处理实现
 */
public class SomeServiceImpl implements SomeService {

	public String heartBeat(String name) {
		System.out.println("接收到客户端 " + name + " 的心跳，正常连接………………");
		return "心跳成功！";
	}
}
