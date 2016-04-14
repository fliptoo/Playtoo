package models.Playtoo.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.lang.StringUtils;

import com.relayrides.pushy.apns.ApnsEnvironment;
import com.relayrides.pushy.apns.ExpiredToken;
import com.relayrides.pushy.apns.ExpiredTokenListener;
import com.relayrides.pushy.apns.FailedConnectionListener;
import com.relayrides.pushy.apns.PushManager;
import com.relayrides.pushy.apns.PushManagerConfiguration;
import com.relayrides.pushy.apns.RejectedNotificationListener;
import com.relayrides.pushy.apns.RejectedNotificationReason;
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder;
import com.relayrides.pushy.apns.util.SSLContextUtil;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;
import com.relayrides.pushy.apns.util.TokenUtil;

import models.Playtoo.db.Platform;
import models.Playtoo.gcm.Message;
import models.Playtoo.gcm.Sender;
import play.Logger;
import play.Play;
import play.jobs.Job;

public class Notifier extends Job {

	static PushManager<SimpleApnsPushNotification> APNS;
	static String GCM_KEY;

	static {
		GCM_KEY = Play.configuration.getProperty("playtoo.gcm.key");
		String p12 = Play.configuration.getProperty("playtoo.apns.p12");
		String password = Play.configuration
				.getProperty("playtoo.apns.password");
		Boolean isApnsProd = Boolean.valueOf(
				Play.configuration.getProperty("playtoo.apns.production"));
		if (StringUtils.isNotEmpty(p12) && StringUtils.isNotEmpty(password)) {
			try {
				ApnsEnvironment apnsEnvironment = ApnsEnvironment
						.getSandboxEnvironment();
				if (isApnsProd)
					apnsEnvironment = ApnsEnvironment
							.getProductionEnvironment();
				APNS = new PushManager<SimpleApnsPushNotification>(
						apnsEnvironment,
						SSLContextUtil.createDefaultSSLContext(
								Play.getFile(p12).getAbsolutePath(), password),
						null, null, null, new PushManagerConfiguration(),
						"APNS");
				APNS.registerRejectedNotificationListener(
						new RejectedNotificationListener<SimpleApnsPushNotification>() {

							@Override
							public void handleRejectedNotification(
									PushManager<? extends SimpleApnsPushNotification> pushManager,
									SimpleApnsPushNotification notification,
									RejectedNotificationReason reason) {
								Logger.info(
										"## [APNS] %s was rejected with rejection reason %s\n",
										notification, reason);
							}

						});
				APNS.registerFailedConnectionListener(
						new FailedConnectionListener<SimpleApnsPushNotification>() {

							@Override
							public void handleFailedConnection(
									PushManager<? extends SimpleApnsPushNotification> pushManager,
									Throwable cause) {
								if (cause instanceof SSLHandshakeException) {
									Logger.error(
											"## [APNS] Handshake Exception: %@",
											cause.getMessage());
								}
							}
						});
				APNS.registerExpiredTokenListener(
						new ExpiredTokenListener<SimpleApnsPushNotification>() {

							@Override
							public void handleExpiredTokens(
									PushManager<? extends SimpleApnsPushNotification> pushManager,
									Collection<ExpiredToken> expiredTokens) {
								for (final ExpiredToken expiredToken : expiredTokens) {
									if (expiredToken.getExpiration()
											.after(new Date())) {
										// Stop sending push notifications to
										// each
										// expired token if the expiration
										// time is after the last time the app
										// registered that token.
									}
								}
							}
						});
				APNS.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public List<Platform> platforms;
	public String title;
	public Map<String, Object> body;

	public Notifier(Platform platform, String title, Map<String, Object> body) {
		this(Arrays.asList(platform), title, body);
	}

	public Notifier(List<Platform> platforms, String title,
			Map<String, Object> body) {
		this.platforms = platforms;
		this.title = title;
		this.body = body;
	}

	public void doJob() throws Exception {

		Message.Builder androidPayload = new Message.Builder()
				.collapseKey("datas").timeToLive(3).delayWhileIdle(true)
				.addData("message", title);

		ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
		payloadBuilder.setAlertBody(title);
		payloadBuilder.setSoundFileName("ring-ring.aiff");
		if (body != null) {
			for (Map.Entry<String, Object> entry : body.entrySet()) {
				payloadBuilder.addCustomProperty(entry.getKey(),
						entry.getValue());
				androidPayload.addData(entry.getKey(),
						entry.getValue().toString());
			}
		}
		String iosPayload = payloadBuilder.buildWithDefaultMaximumLength();

		List<String> androidTokens = new ArrayList<>();
		for (Platform platform : platforms) {
			if (platform.type.equalsIgnoreCase(Platform.IOS)) {
				final byte[] tokenByte = TokenUtil
						.tokenStringToByteArray(platform.token);
				APNS.getQueue().put(
						new SimpleApnsPushNotification(tokenByte, iosPayload));
			} else if (platform.type.equalsIgnoreCase(Platform.ANDROID))
				androidTokens.add(platform.token);
		}

		if (androidTokens.size() > 0) {
			Sender sender = new Sender(GCM_KEY);
			sender.send(androidPayload.build(), androidTokens, 5);
		}
	}
}
