package com.cursotdd.model.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.cursotdd.model.entity.Book;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositorytest {

	@Autowired
	TestEntityManager entityManager;

	@Autowired
	BookRepository repository;

	@Test
	@DisplayName("Deve retornar verdadeiro quando existir livro com isbn")
	public void shouldReturnTrueWhenExistsIsbn() {
		String isbn = "123";
		Book book = createValidBook(isbn);
		entityManager.persist(book);

		boolean exists = repository.existsByIsbn(isbn);

		assertThat(exists).isTrue();
	}

	private Book createValidBook(String isbn) {
		return Book.builder().title("title").author("author").isbn(isbn).build();
	}

	@Test
	@DisplayName("Deve retornar falso quando não existir livro com isbn")
	public void shouldReturnFalseWhenNotExistsIsbn() {
		String isbn = "123";

		boolean exists = repository.existsByIsbn(isbn);

		assertThat(exists).isFalse();
	}

	@Test
	@DisplayName("Deve obter um livro por id")
	public void findByIdTest() {
		Book book = createValidBook("123");
		entityManager.persist(book);

		Optional<Book> returnedBook = repository.findById(book.getId());

		assertThat(returnedBook.isPresent()).isTrue();
	}

	@Test
	@DisplayName("Deve salvar um livro")
	public void saveBookTest() {
		Book bookToSave = createValidBook("123");

		Book savedBook = repository.save(bookToSave);

		assertThat(savedBook.getId()).isNotNull();
	}

	@Test
	@DisplayName("Deve deletar um livro")
	public void deleteBookTest() {
		Book bookToDelete = createValidBook("123");
		entityManager.persist(bookToDelete);

		Book foundBook = entityManager.find(Book.class, bookToDelete.getId());

		repository.delete(foundBook);

		Book deletedBook = entityManager.find(Book.class, bookToDelete.getId());

		assertThat(deletedBook).isNull();
	}
}
