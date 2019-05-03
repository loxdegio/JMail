package it.loxdegio;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.net.MediaType;

import it.loxdegio.enums.EmailPriority;
import it.loxdegio.enums.Encoding;
import it.loxdegio.enums.Encryption;

/**
 * A Java mail client
 */
public class JMailer {
	/**
	 * Email priority (1 = High, 3 = Normal, 5 = low).
	 */
	private EmailPriority priority = EmailPriority.Normal;

	/**
	 * Sets the Content-type of the message.
	 */
	private MediaType contentType = MediaType.ANY_TEXT_TYPE;

	/**
	 * Sets the Encoding of the message. Options for this are "8bit", "7bit",
	 * "binary", "base64", and "quoted-printable". Content-Transfer-Encoding
	 */
	private Encoding encoding = Encoding.BIT8;

	/**
	 * Sets the From email address for the message.
	 */
	private String from = "root@localhost";

	/**
	 * Sets the Sender email (Return-Path) of the message. If not empty, will be
	 * sent via -f to sendmail or as 'MAIL FROM' in smtp mode.
	 */
	private String sender;

	/**
	 * Sets the Subject of the message.
	 */
	private String subject;

	/**
	 * Sets the Body of the message. This can be either an HTML or text body. If
	 * HTML then run IsHTML(true).
	 */
	private String body;

	// ///////////////////////////////////////////////
	// SMTP VARIABLES
	// ///////////////////////////////////////////////

	/**
	 * Sets the SMTP hosts. All hosts must be separated by a semicolon. You can
	 * also specify a different port for each host by using this format:
	 * [hostname:port] (e.g. "smtp1.example.com:25;smtp2.example.com"). Hosts
	 * will be tried in order.
	 */
	private String host = "localhost";

	/**
	 * Sets the default SMTP server port.(25)
	 */
	private int port = 25;

	/**
	 * Sets SMTP authentication. Uses the Username and Password variables.
	 * default (false)
	 */
	private boolean smtpAuth = false;

	/**
	 * Sets SMTP username.
	 */
	private String username;

	/**
	 * Sets SMTP password.
	 */
	private String password;

	// ///////////////////////////////////////////
	// ENCRYPTION
	// //////////////////////////////////////////

	/**
	 * Sets TLS/STARTTLS Protocol
	 */
	private Encryption encryption;

	private List<String> to = new ArrayList<String>(0);
	private List<String> cc = new ArrayList<String>(0);
	private List<String> bcc = new ArrayList<String>(0);
	private List<String> replyTo = new ArrayList<String>(0);
	//
	private List<File> attachment = new ArrayList<>(0);
	private Date sendDate = new Date();

	private Hashtable<String, String> customHeader = new Hashtable<>();

	public JMailer() {
	}
	
	public JMailer(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public JMailer(String host, int port, boolean smtpAuth, String username, String password, Encryption encryption) {
		this(host, port);
		this.smtpAuth = smtpAuth;
		this.username = username;
		this.password = password;
		this.encryption = encryption;
	}

	/**
	 * Send message.
	 *
	 * @return
	 */
	public boolean send() {
		boolean ok = false;

		Session session = createSession();

		final MimeMessage msg = new MimeMessage(session);
		try {
			msg.setRecipients(RecipientType.TO, parseAddress(to));

			Address[] ccAddress = parseAddress(cc);
			if (ArrayUtils.isNotEmpty(ccAddress))
				msg.setRecipients(RecipientType.CC, ccAddress);
			Address[] bccAddress = parseAddress(bcc);
			if (ArrayUtils.isNotEmpty(bccAddress))
				msg.setRecipients(RecipientType.BCC, bccAddress);
			Address[] replyToAddress = parseAddress(replyTo);
			if (ArrayUtils.isNotEmpty(replyToAddress))
				msg.setReplyTo(replyToAddress);
			if (StringUtils.isNotBlank(from)) {
				if (StringUtils.isNotBlank(sender))
					msg.setFrom(new InternetAddress(from, sender));
				else
					msg.setFrom(new InternetAddress(from));
			}

			msg.setSentDate(sendDate);

			msg.setSubject(subject);
			msg.addHeader("Content-Transfer-Encoding", encoding.getVal());
			msg.addHeader("X-Priority", priority.getVal());

			msg.addHeader("Content-Type", contentType.toString());
			// add custom header
			for (Map.Entry<String, String> entry : customHeader.entrySet())
				msg.addHeader(entry.getKey(), entry.getValue());

			// attach the file to the message
			if (CollectionUtils.isNotEmpty(attachment)) {

				// create and fill the message body
				MimeBodyPart body1 = new MimeBodyPart();
				body1.setContent(body, contentType.toString());

				// create the Multipart and its parts to it
				Multipart mp = new MimeMultipart();
				mp.addBodyPart(body1);

				for (File a : attachment) {
					FileDataSource fds = new FileDataSource(a);
					MimeBodyPart body2 = new MimeBodyPart();
					body2.setDataHandler(new DataHandler(fds));
					body2.setFileName(fds.getName());

					mp.addBodyPart(body2);
				}

				msg.setContent(mp);
			} else {
				msg.setContent(body, contentType.toString());
			}

			Transport.send(msg);
			ok = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ok;
	}

	// create a session
	private Session createSession() {
		Authenticator authenticator = null;
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		switch (encryption) {
		case SSL:
			props.put("mail.smtp.socketFactory.port", port);
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			break;
		case TLS:
			props.put("mail.smtp.starttls.enable", "true");
			break;
		}

		if (smtpAuth) {
			props.put("mail.smtp.auth", "true");
			authenticator = new Authenticator() {
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			};
		}
		return Session.getInstance(props, authenticator);
	}

	private Address[] parseAddress(List<String> addresses) throws AddressException {
		if (CollectionUtils.isEmpty(addresses))
			return null;

		List<Address> addressesList = new ArrayList<>(addresses.size());
		for (String a : addresses)
			addressesList.add(new InternetAddress(a));
		return addressesList.toArray(new Address[0]);
	}

	/**
	 * GETTERS AND SETTERS
	 */

	public void setPriority(EmailPriority priority) {
		this.priority = priority;
	}

	public void setContentType(MediaType contentType) {
		this.contentType = contentType;
	}

	public void setEncoding(Encoding encoding) {
		this.encoding = encoding;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setSmtpAuth(boolean smtpAuth) {
		this.smtpAuth = smtpAuth;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * Sets message type to HTML.
	 *
	 * @param isHTML
	 */
	public void setHTML(boolean isHTML) {
		contentType = isHTML ? MediaType.HTML_UTF_8 : MediaType.ANY_TEXT_TYPE;
	}

	/**
	 * Adds a "To" address.
	 *
	 * @param address
	 */
	public void addAddress(String address) {
		if (StringUtils.isNotBlank(address))
			to.add(address);
	}

	/**
	 * Adds a "Cc" address.
	 *
	 * @param address
	 */
	public void addCC(String address) {
		if (StringUtils.isNotBlank(address))
			cc.add(address);
	}

	/**
	 * Adds a "Bcc" address.
	 *
	 * @param address
	 */
	public void addBCC(String address) {
		if (StringUtils.isNotBlank(address))
			bcc.add(address);
	}

	/**
	 * Adds a "Reply-to" address
	 *
	 * @param address
	 */
	public void addReplyTo(String address) {
		if (StringUtils.isNotBlank(address))
			replyTo.add(address);
	}

	/**
	 * Adds a custom header.
	 *
	 * @param name
	 * @param value
	 */
	public void addHeader(String name, String value) {
		customHeader.put(name, value);
	}

	public void removeHeader(String name) {
		customHeader.remove(name);
	}

	/**
	 * Sets send date.
	 *
	 * @param d
	 */
	public void setSendDate(Date d) {
		sendDate = d;
	}

	public void addAttachment(String filePath) {
		addAttachment(new File(filePath));
	}

	public void addAttachment(File f) {
		if (f != null && f.isFile())
			attachment.add(f);
	}

	public void setEncryption(Encryption encryption) {
		this.encryption = encryption;
	}
	
	

}
