package com.bgfurfeature.rpc.hadoop.client;

import java.net.InetSocketAddress;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;

import com.bgfurfeature.rpc.hadoop.protocol.SomeService;

public class MyClient {

	public static void main(String[] args) throws Exception {
		// 通过代理获取遵守协议实例
		SomeService someService = RPC.getProxy(
				SomeService.class, Long.MAX_VALUE, new InetSocketAddress(
						"localhost", 5555), new Configuration());
		String ret = someService.heartBeat("wilson");
		System.out.println(ret);
	}
}
