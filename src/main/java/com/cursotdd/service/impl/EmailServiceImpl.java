package com.cursotdd.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.cursotdd.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

	@Value("${application.mail.default-remetent}")
	private String remetent;

	private JavaMailSender javaMailSender;

	public EmailServiceImpl(JavaMailSender javaMailSender) {
		super();
		this.javaMailSender = javaMailSender;
	}

	@Override
	public void send(String message, List<String> mailsList) {

		String[] mails = mailsList.toArray(new String[mailsList.size()]);

		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom(remetent);
		mailMessage.setSubject("Livro com empréstimo atrasado");
		mailMessage.setText(message);
		mailMessage.setTo(mails);

		javaMailSender.send(mailMessage);
	}

}
