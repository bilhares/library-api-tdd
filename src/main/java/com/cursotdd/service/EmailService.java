package com.cursotdd.service;

import java.util.List;


public interface EmailService {

	void send(String message, List<String> mailsList);

}
