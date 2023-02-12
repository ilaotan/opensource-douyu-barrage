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

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.CronTask;
import cn.hutool.cron.task.Task;
import com.yijianguanzhu.douyu.barrage.bootstrap.Douyu;
import com.yijianguanzhu.douyu.barrage.config.DouyuConfiguration;
import com.yijianguanzhu.douyu.barrage.enums.MessageType;
import com.yijianguanzhu.douyu.barrage.model.DouyuCookie;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.sound.midi.Soundbank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yijianguanzhu 2020年9月12日
 */
public class Main {



	static {
		// System.setProperty( "org.slf4j.simpleLogger.defaultLogLevel", "debug" );
		System.setProperty( "org.slf4j.simpleLogger.showDateTime", "true" );
		System.setProperty( "org.slf4j.simpleLogger.dateTimeFormat", "yyyyMMdd HH:mm:ss" );
	}

	private static Logger logger = LoggerFactory.getLogger(Main.class);

	private static AtomicReference<ChannelHandlerContext> context = new AtomicReference<>();


	public static void main(String[] args) throws InterruptedException {


//		/**
//		 * 拉取弹幕示例代码(不需要登陆)
//		 */
//		Douyu.pull().registerMessageListener( MessageType.CHAT_MSG, ( jsonString, baseMessage, context ) -> {
//			StringBuilder builder = new StringBuilder();
//			builder.append( "[辛巴][lv" ).append( baseMessage.getLevel() )
//					.append( "][" ).append( baseMessage.getNn() )
//					.append( "]: " ).append( baseMessage.getTxt() );
//			System.out.println( builder.toString() );
//		} ).room().roomId( 3487376L )
//				.login();
//		Douyu.pull().registerMessageListener( MessageType.CHAT_MSG, ( jsonString, baseMessage, context ) -> {
//			StringBuilder builder = new StringBuilder();
//			builder.append( "[智勋][lv" ).append( baseMessage.getLevel() )
//					.append( "][" ).append( baseMessage.getNn() )
//					.append( "]: " ).append( baseMessage.getTxt() );
//			System.out.println( builder.toString() );
//		} ).room().roomId( 312212L )
//				.login();

		/**
		 * 发送弹幕/挂机直播间示例代码(需要登陆)，登录信息可从网页里的cookie获取，
		 * 也可以使用{@link https://github.com/yijianguanzhu/douyu-hongbao}工具扫码登录后，从cookie.txt文件找到相关信息
		 */

		String acf_username = System.getProperty("acf_username");
		long acf_uid = Long.parseLong(System.getProperty("acf_uid"));
		String acf_stk = System.getProperty("acf_stk");
		String acf_did = System.getProperty("acf_did");
		int acf_biz = Integer.parseInt(System.getProperty("acf_biz", "1"));
		long acf_ltkid = Long.parseLong(System.getProperty("acf_ltkid"));
		long roomId = Long.parseLong(System.getProperty("roomId"));


		DouyuCookie cookie = new DouyuCookie();
		// 从cookie.txt文件中找到 acf_biz 对应的值
		cookie.setAcf_biz(acf_biz);
		// 从cookie.txt文件中找到 acf_did 对应的值
		cookie.setAcf_did(acf_did); // web
		// 从cookie.txt文件中找到 acf_ltkid 对应的值
		cookie.setAcf_ltkid(acf_ltkid);
		// 从cookie.txt文件中找到 acf_username 对应的值
		cookie.setAcf_username(acf_username);
		// 从cookie.txt文件中找到 acf_uid 对应的值
		cookie.setAcf_uid(acf_uid);

		// 从cookie.txt文件中找到 acf_stk 对应的值
		cookie.setAcf_stk(acf_stk);



//        // 获取直播间弹幕
//        Douyu.pull().registerMessageListener(MessageType.CHAT_MSG, (jsonString, baseMessage, c) -> {
//            StringBuilder builder = new StringBuilder();
//            builder.append("[lv").append(baseMessage.getLevel())
//                    .append("][").append(baseMessage.getNn())
//                    .append("]: ").append(baseMessage.getTxt());
//            System.out.println(builder.toString());
////            // 弹幕跟读示例，应当设置合理间隔，否则会触发短信验证
//
//            String txt = "#打卡";
//            if (context.get() != null) {
//                context.get().writeAndFlush(String
//                        .format(DouyuConfiguration.defaultPushMessageType().getPushMessage(),
//                                encode(txt),
//                                cookie.getAcf_uid(), System.currentTimeMillis()));
//            }
//
//        }).room().roomId(4332L).login();

		Douyu.push(cookie).registerMessageListener(MessageType.ALL, (a, b, c) -> {
//            System.out.println(a);
			context.set(c);
//            logger.info("本地拿到了 context");
		}).room().roomId(roomId).login();


		CronUtil.schedule("18,48 */1 * * *", new Task() {
			@Override
			public void execute() {

				String txt = "#打卡";
				if (context.get() != null) {

//					try {
//						TimeUnit.SECONDS.sleep(RandomUtil.randomLong(1,40));
//					}
//					catch (InterruptedException e) {
//						e.printStackTrace();
//					}


					context.get().writeAndFlush(String
							.format(DouyuConfiguration.defaultPushMessageType().getPushMessage(),
									encode(txt),
									cookie.getAcf_uid(), System.currentTimeMillis()));


					logger.info("发送 #打卡 信息");
				}else {
					logger.info("发送失败 没有找到 context");
				}

			}
		});


		CronUtil.schedule("28,58 */1 * * *", new Task() {
			@Override
			public void execute() {

				String txt = "#查询";
				if (context.get() != null) {
//					try {
//						TimeUnit.SECONDS.sleep(RandomUtil.randomLong(1,40));
//					}
//					catch (InterruptedException e) {
//						e.printStackTrace();
//					}
					context.get().writeAndFlush(String
							.format(DouyuConfiguration.defaultPushMessageType().getPushMessage(),
									encode(txt),
									cookie.getAcf_uid(), System.currentTimeMillis()));


					logger.info("发送 #查询 信息");
				}else {
					logger.info("发送失败 没有找到 context");
				}

			}
		});


		// 支持秒级别定时任务
		CronUtil.setMatchSecond(true);
		CronUtil.start();
		logger.info("定时任务启动");
	}

	private static String encode(String txt) {
		if (!txt.isEmpty() && (txt.contains("@") || txt.contains("/"))) {
			return txt.replaceAll("@", "@A").replaceAll("/", "@S");
		}
		return txt;
	}
}
