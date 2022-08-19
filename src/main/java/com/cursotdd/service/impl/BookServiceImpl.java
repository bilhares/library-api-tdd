package com.cursotdd.service.impl;

import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.cursotdd.exception.BusinessException;
import com.cursotdd.model.entity.Book;
import com.cursotdd.model.repository.BookRepository;
import com.cursotdd.service.BookService;

@Service
public class BookServiceImpl implements BookService {

	private BookRepository repository;

	public BookServiceImpl(BookRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public Book save(Book book) {
		if (repository.existsByIsbn(book.getIsbn())) {
			throw new BusinessException("isbn ja cadastrado");
		}

		return repository.save(book);
	}

	@Override
	public Optional<Book> getById(Long id) {
		return repository.findById(id);
	}

	@Override
	public void delete(Book book) {
		if (book == null || book.getId() == null)
			throw new IllegalArgumentException("Book id cant be null");

		this.repository.delete(book);
	}

	@Override
	public Book update(Book book) {
		if (book == null || book.getId() == null)
			throw new IllegalArgumentException("Book id cant be null");

		return this.repository.save(book);
	}

	@Override
	public Page<Book> find(Book filter, Pageable pageRequest) {
		Example<Book> example = Example.of(filter, ExampleMatcher.matching().withIgnoreCase().withIgnoreNullValues()
				.withStringMatcher(StringMatcher.CONTAINING));

		return repository.findAll(example, pageRequest);
	}

	@Override
	public Optional<Book> getBookByIsbn(String isbn) {
		return repository.findByIsbn(isbn);
	}

}
