package com.cursotdd.service.impl;

import java.util.Optional;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(Book book) {
		// TODO Auto-generated method stub

	}

	@Override
	public Book update(Book book) {
		// TODO Auto-generated method stub
		return null;
	}

}
