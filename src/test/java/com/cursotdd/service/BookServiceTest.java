package com.cursotdd.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.cursotdd.exception.BusinessException;
import com.cursotdd.model.entity.Book;
import com.cursotdd.model.repository.BookRepository;
import com.cursotdd.service.impl.BookServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

	BookService service;

	@MockBean
	BookRepository repository;

	@BeforeEach
	public void setup() {
		this.service = new BookServiceImpl(repository);
	}

	@Test
	@DisplayName("Deve salvar um livro")
	public void saveBookTest() {
		Book book = createBook();

		Mockito.when(repository.save(book))
				.thenReturn(Book.builder().id(1L).isbn("123").author("Fulano").title("Teste").build());
		Mockito.when(repository.existsByIsbn(book.getIsbn())).thenReturn(false);

		Book savedBook = service.save(book);

		assertThat(savedBook.getId()).isNotNull();
		assertThat(savedBook.getIsbn()).isEqualTo(book.getIsbn());
		assertThat(savedBook.getTitle()).isEqualTo(book.getTitle());
		assertThat(savedBook.getAuthor()).isEqualTo(book.getAuthor());
	}

	@Test
	@DisplayName("Deve lancar erro de negocio ao tentar salvar um livro com isbn duplicado")
	public void shouldNotSaveBookWithDuplicatedIsbn() {
		Book book = createBook();
		Mockito.when(repository.existsByIsbn(book.getIsbn())).thenReturn(true);

		Throwable ex = Assertions.catchThrowable(() -> service.save(book));

		assertThat(ex).isInstanceOf(BusinessException.class).hasMessage("isbn ja cadastrado");

		Mockito.verify(repository, Mockito.never()).save(book);
	}

	private Book createBook() {
		return Book.builder().isbn("123").author("Fulano").title("Teste").build();
	}

}
