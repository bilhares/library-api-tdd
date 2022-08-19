package com.cursotdd.resources;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cursotdd.dto.BookDto;
import com.cursotdd.dto.LoanDto;
import com.cursotdd.dto.LoanFilterDto;
import com.cursotdd.dto.ReturnedLoanDto;
import com.cursotdd.model.entity.Book;
import com.cursotdd.model.entity.Loan;
import com.cursotdd.service.BookService;
import com.cursotdd.service.LoanService;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

	private final LoanService service;
	private final BookService bookService;
	private ModelMapper modelMapper;

	public LoanController(LoanService service, BookService bookService, ModelMapper modelMapper) {
		this.service = service;
		this.bookService = bookService;
		this.modelMapper = modelMapper;
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

	@PatchMapping("{id}")
	public void returnBook(@PathVariable("id") Long id, @RequestBody ReturnedLoanDto dto) {
		Loan loan = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		loan.setReturned(dto.getReturned());

		service.update(loan);
	}

	@GetMapping
	public Page<LoanDto> filterLoan(LoanFilterDto dto, Pageable pageRequest) {
		Page<Loan> result = service.find(dto, pageRequest);

		List<LoanDto> loans = result.getContent().stream().map(entity -> {
			Book book = entity.getBook();
			BookDto bookDto = modelMapper.map(book, BookDto.class);
			LoanDto loanDto = modelMapper.map(entity, LoanDto.class);
			loanDto.setBook(bookDto);
			return loanDto;
		}).collect(Collectors.toList());

		return new PageImpl<LoanDto>(loans, pageRequest, result.getTotalElements());
	}
}
