/*
 * Copyright (c) 2021-2031, yijianguanzhu (yijianguanzhu@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *
 */
package com.yijianguanzhu.douyu.barrage;

import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import com.yijianguanzhu.douyu.barrage.bootstrap.Douyu;
import com.yijianguanzhu.douyu.barrage.config.DouyuConfiguration;
import com.yijianguanzhu.douyu.barrage.enums.MessageType;
import com.yijianguanzhu.douyu.barrage.model.DouyuCookie;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	private static Logger                                 logger;
	private static AtomicReference<ChannelHandlerContext> context;

	public Main() {
	}

	public static void main(String[] args) throws InterruptedException {

		String username = System.getProperty("username");
		long uid = Long.parseLong(System.getProperty("uid"));
		String stk = System.getProperty("stk");
		String did = System.getProperty("did");
		int biz = Integer.parseInt(System.getProperty("biz", "1"));
		long ltkid = Long.parseLong(System.getProperty("ltkid"));
		long roomId = Long.parseLong(System.getProperty("roomId"));


		DouyuCookie cookie = new DouyuCookie();
		cookie.setAcf_biz(biz);
		cookie.setAcf_did(did);
		cookie.setAcf_ltkid(ltkid);
		cookie.setAcf_username(username);
		cookie.setAcf_uid(uid);
		cookie.setAcf_stk(stk);

		Douyu.push(cookie).registerMessageListener(MessageType.ALL, (a, b, c) -> {
			context.set(c);
		}).room().roomId(roomId).login();
		CronUtil.schedule("16,46 */1 * * *", (Task) () -> {
			String txt = "#打卡";
			if (Main.context.get() != null) {
				((ChannelHandlerContext)Main.context.get()).writeAndFlush(String.format(DouyuConfiguration.defaultPushMessageType().getPushMessage(), Main.encode(txt), cookie.getAcf_uid(), System.currentTimeMillis()));
				Main.logger.info("发送 #打卡 信息");
			} else {
				Main.logger.info("发送失败 没有找到 context");
			}

		});
		CronUtil.setMatchSecond(true);
		CronUtil.start();
		logger.info("定时任务启动");
	}

	private static String encode(String txt) {
		return txt.isEmpty() || !txt.contains("@") && !txt.contains("/") ? txt : txt.replaceAll("@", "@A").replaceAll("/", "@S");
	}

	static {
		System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
		System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yyyyMMdd HH:mm:ss");
		logger = LoggerFactory.getLogger(Main.class);
		context = new AtomicReference();
	}
}
