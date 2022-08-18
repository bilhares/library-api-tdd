package com.cursotdd.model.entity;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class Loan {

	private Long id;
	private String customer;
	private Book book;
	private LocalDate loanDate;
	private Boolean returned;
}
