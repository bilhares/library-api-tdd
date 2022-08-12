package com.cursotdd.model.repository;

import static org.assertj.core.api.Assertions.assertThat;

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
public class BookRepositoryTest {

	@Autowired
	TestEntityManager entityManager;

	@Autowired
	BookRepository repository;

	@Test
	@DisplayName("Deve retornar verdadeiro quando existir livro com isbn")
	public void shouldReturnTrueWhenExistsIsbn() {
		String isbn = "123";
		Book book = Book.builder().title("title").author("author").isbn(isbn).build();
		entityManager.persist(book);

		boolean exists = repository.existsByIsbn(isbn);

		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("Deve retornar falso quando não existir livro com isbn")
	public void shouldReturnFalseWhenNotExistsIsbn() {
		String isbn = "123";

		boolean exists = repository.existsByIsbn(isbn);

		assertThat(exists).isFalse();
	}
}
