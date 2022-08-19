package com.cursotdd.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.cursotdd.dto.LoanFilterDto;
import com.cursotdd.model.entity.Loan;

public interface LoanService {

	Loan save(Loan loan);

	Optional<Loan> getById(Long id);

	Loan update(Loan loan);
	
	Page<Loan> find(LoanFilterDto filter, Pageable pageRequest);

}
