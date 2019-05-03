package it.loxdegio;

/**
* A simple example of JMail use
*/

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import it.loxdegio.enums.Encryption;

public class MailSender {

	private JMailer jmail;

	public MailSender(String host, String username, String password) {
		this(host, 25, null, username, password);
	}

	public MailSender(String host, int port, Encryption encryption, String username, String password) {
		jmail = new JMailer(host, port, true, username, password, encryption);
	}

	public boolean sendMail(String from, String fromAlias, String replyTo, String subject, String body, List<String> to,
			List<String> cc, List<String> bcc, List<File> attachments) {

		jmail.setFrom(from);
		jmail.setSender(fromAlias);
		jmail.addReplyTo(replyTo);

		jmail.setSubject(subject);
		jmail.setBody(body);

		if (CollectionUtils.isEmpty(cc))
			cc = new ArrayList<>(0);
		if (CollectionUtils.isEmpty(bcc))
			bcc = new ArrayList<>(0);
		if (CollectionUtils.isEmpty(attachments))
			attachments = new ArrayList<>(0);

		for (String address : to)
			jmail.addAddress(address);

		for (String address : cc)
			jmail.addCC(address);

		for (String address : bcc)
			jmail.addBCC(address);

		for (File f : attachments)
			jmail.addAttachment(f);

		return jmail.send();
	}

}
