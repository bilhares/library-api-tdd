package com.cursotdd.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cursotdd.model.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

	boolean existsByIsbn(String isbn);

}
