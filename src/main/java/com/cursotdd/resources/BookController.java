package com.cursotdd.resources;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cursotdd.dto.BookDto;
import com.cursotdd.dto.LoanDto;
import com.cursotdd.model.entity.Book;
import com.cursotdd.model.entity.Loan;
import com.cursotdd.service.BookService;
import com.cursotdd.service.LoanService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/books")
@Api("Book Api")
@Slf4j
public class BookController {

	private BookService service;
	private ModelMapper modelMapper;
	private LoanService loanService;

	public BookController(BookService service, ModelMapper modelMapper, LoanService loanService) {
		this.service = service;
		this.modelMapper = modelMapper;
		this.loanService = loanService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("CREATE A BOOK")
	public BookDto create(@RequestBody @Valid BookDto dto) {
		log.info("creating a book for isbn: {}", dto.getIsbn());
		Book entity = modelMapper.map(dto, Book.class);
		entity = service.save(entity);
		return modelMapper.map(entity, BookDto.class);
	}

	@GetMapping("{id}")
	@ApiOperation("OBTAINS A BOOK DETAILS BY ID")
	public BookDto get(@PathVariable Long id) {
		log.info("getting a book for id: {}", id);
		return service.getById(id).map(book -> modelMapper.map(book, BookDto.class))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	@DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation("DELETE BOOK")
	public void delete(@PathVariable Long id) {
		log.info("deleting a book for id: {}", id);
		Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		service.delete(book);
	}

	@PutMapping("{id}")
	@ApiOperation("UPDATE A BOOK INFOS")
	public BookDto update(@PathVariable Long id, @RequestBody @Valid BookDto dto) {
		log.info("updating a book for id: {}", id);
		return service.getById(id).map(book -> {
			book.setAuthor(dto.getAuthor());
			book.setTitle(dto.getTitle());
			book = service.update(book);
			return modelMapper.map(book, BookDto.class);
		}).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	@GetMapping
	@ApiOperation("OBTAINS A BOOK BY FILTERS")
	public Page<BookDto> find(BookDto dto, Pageable pageRequest) {
		log.info("get book by filters");
		Book filter = modelMapper.map(dto, Book.class);
		Page<Book> result = service.find(filter, pageRequest);
		List<BookDto> list = result.getContent().stream().map(entity -> modelMapper.map(entity, BookDto.class))
				.collect(Collectors.toList());
		return new PageImpl<BookDto>(list, pageRequest, result.getTotalElements());
	}

	@GetMapping("{id}/loans")
	@ApiOperation("OBTAINS LOANS FROM BOOK")
	public Page<LoanDto> loansByBook(@PathVariable Long id, Pageable pageable) {
		log.info("getting loans from book id: {}", id);
		Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Page<Loan> result = loanService.getLoansByBook(book, pageable);

		List<LoanDto> list = result.getContent().stream().map(entity -> {
			BookDto bookDto = modelMapper.map(entity.getBook(), BookDto.class);
			LoanDto loanDto = modelMapper.map(entity, LoanDto.class);
			loanDto.setBook(bookDto);
			return loanDto;
		}).collect(Collectors.toList());

		return new PageImpl<LoanDto>(list, pageable, result.getTotalElements());
	}
}
