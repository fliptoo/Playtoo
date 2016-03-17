package models.Playtoo.job;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Play;
import play.jobs.Job;

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

public class Notifier extends Job {

	static PushManager<SimpleApnsPushNotification> APNS;

	static {
		String p12 = Play.configuration.getProperty("playtoo.apns.p12");
		String password = Play.configuration
				.getProperty("playtoo.apns.password");
		if (StringUtils.isNotEmpty(p12) && StringUtils.isNotEmpty(password)) {
			try {
				APNS = new PushManager<SimpleApnsPushNotification>(
						ApnsEnvironment.getProductionEnvironment(),
						SSLContextUtil.createDefaultSSLContext(Play
								.getFile(p12).getAbsolutePath(), password),
						null, null, null, new PushManagerConfiguration(),
						"APNS");
				APNS.registerRejectedNotificationListener(new RejectedNotificationListener<SimpleApnsPushNotification>() {

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
				APNS.registerFailedConnectionListener(new FailedConnectionListener<SimpleApnsPushNotification>() {

					@Override
					public void handleFailedConnection(
							PushManager<? extends SimpleApnsPushNotification> pushManager,
							Throwable cause) {
						if (cause instanceof SSLHandshakeException) {
							Logger.error("## [APNS] Handshake Exception: %@",
									cause.getMessage());
						}
					}
				});
				APNS.registerExpiredTokenListener(new ExpiredTokenListener<SimpleApnsPushNotification>() {

					@Override
					public void handleExpiredTokens(
							PushManager<? extends SimpleApnsPushNotification> pushManager,
							Collection<ExpiredToken> expiredTokens) {
						for (final ExpiredToken expiredToken : expiredTokens) {
							if (expiredToken.getExpiration().after(new Date())) {
								// Stop sending push notifications to each
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

	public List<String> tokens;
	public String message;

	public Notifier() {

	}

	public void doJob() throws Exception {
		final byte[] token = TokenUtil
				.tokenStringToByteArray("<45c2d81685dbd3f8d6b514295eecb5c913bdab1f7375d45a07d7a1c076843f55>");
		final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
		payloadBuilder.setAlertBody("Hello Sam");
		payloadBuilder.setSoundFileName("ring-ring.aiff");
		// payloadBuilder.addCustomProperty("request_id", 21);
		final String payload = payloadBuilder.buildWithDefaultMaximumLength();
		APNS.getQueue().put(new SimpleApnsPushNotification(token, payload));
	}
}
