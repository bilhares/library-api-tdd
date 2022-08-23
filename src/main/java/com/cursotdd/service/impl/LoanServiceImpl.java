package com.cursotdd.service.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.cursotdd.dto.LoanFilterDto;
import com.cursotdd.exception.BusinessException;
import com.cursotdd.model.entity.Loan;
import com.cursotdd.model.repository.LoanRepository;
import com.cursotdd.service.LoanService;

@Service
public class LoanServiceImpl implements LoanService {

	private LoanRepository repository;

	public LoanServiceImpl(LoanRepository repository) {
		this.repository = repository;
	}

	@Override
	public Loan save(Loan loan) {
		if (repository.existsByBookAndNotReturned(loan.getBook())) {
			throw new BusinessException("livro ja emprestado");
		}

		return repository.save(loan);
	}

	@Override
	public Optional<Loan> getById(Long id) {
		return repository.findById(id);
	}

	@Override
	public Loan update(Loan loan) {
		return repository.save(loan);
	}

	@Override
	public Page<Loan> find(LoanFilterDto filter, Pageable pageRequest) {
		return repository.findByBookIsbnOrCustomer(filter.getIsbn(), filter.getCustomer(), pageRequest);
	}

}
