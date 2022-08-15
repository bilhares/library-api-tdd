package com.cursotdd.service;

import java.util.Optional;

import com.cursotdd.model.entity.Book;

public interface BookService {

	Book save(Book book);

	Optional<Book> getById(Long id);

	void delete(Book book);

	Book update(Book book);

}
