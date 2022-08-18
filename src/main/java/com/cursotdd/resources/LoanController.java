package com.cursotdd.resources;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cursotdd.dto.LoanDto;
import com.cursotdd.model.entity.Book;
import com.cursotdd.model.entity.Loan;
import com.cursotdd.service.BookService;
import com.cursotdd.service.LoanService;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

	private final LoanService service;
	private final BookService bookService;

	public LoanController(LoanService service, BookService bookService) {
		this.service = service;
		this.bookService = bookService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Long create(@RequestBody LoanDto dto) {
		Book book = bookService.getBookByIsbn(dto.getIsbn())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"livro nao encontrado para o isbn informado"));
		Loan entity = Loan.builder().book(book).customer(dto.getCustomer()).loanDate(LocalDate.now()).build();

		entity = service.save(entity);

		return entity.getId();
	}
}
