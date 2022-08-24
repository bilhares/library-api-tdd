package com.cursotdd.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cursotdd.model.entity.Loan;

@Service
public class ScheduleService {

	private static final String CRON_LATE_LOANS = "0 0 0 1/1 * ?";

	@Value("${application.mail.late.loans.message}")
	private String message;

	private LoanService loanService;
	private EmailService emailService;

	public ScheduleService(LoanService loanService, EmailService emailService) {
		this.loanService = loanService;
		this.emailService = emailService;
	}

	@Scheduled(cron = CRON_LATE_LOANS)
	public void sendMailToLateLoans() {
		List<Loan> allLateLoans = loanService.getAllLateLoans();

		List<String> mailsList = allLateLoans.stream().map(loan -> loan.getCustomerEmail())
				.collect(Collectors.toList());

		emailService.send(message, mailsList);
	}
}
