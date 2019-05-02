/**
 * A simple example of JMail use
 */

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MailSender {

	private JMailer jmail;
	
	public MailSender(String host, String username, String password) {
		this(host, 25, null, username, password);
	}
	
	public MailSender(String host, int port,String encryption, String username, String password) {
		jmail = new JMailer();
		
		jmail.host = host;
		jmail.port = port;
		jmail.smtpAuth = true;
		jmail.username = username;
		jmail.password = password;
		if(encryption.equals("ssl")) {
			jmail.setSSL();
		} else if(encryption.equals("tls")) {
			jmail.setTLS();
		}
	}
	
	public boolean sendMail(String from, String fromAlias, String replyTo, String subject, String body, List<String> to, List<String> cc, List<String> bcc, List<File> attachments) {
		
		jmail.from = from;
		jmail.sender = fromAlias;
		jmail.addReplyTo(replyTo);
		
		jmail.subject = subject;
		jmail.body = body;
		
		if(cc == null) cc = new ArrayList<String>(0);
		if(bcc == null) bcc = new ArrayList<String>(0);
		if(attachments == null) attachments = new ArrayList<File>(0);
		
		for(String address : to) {
			jmail.addAddress(address);
		}
		
		for(String address : cc) {
			jmail.addCC(address);
		}
		
		for(String address : bcc) {
			jmail.addBCC(address);
		}
		
		for(File f : attachments) {
			jmail.addAttachment(f);
		}
		
		return jmail.send();
	}
	
}
