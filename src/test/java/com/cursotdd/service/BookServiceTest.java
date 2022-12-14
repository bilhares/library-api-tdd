package com.cursotdd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
	public void shouldNotSaveBookWithDuplicatedIsbnTest() {
		Book book = createBook();
		Mockito.when(repository.existsByIsbn(book.getIsbn())).thenReturn(true);

		Throwable ex = Assertions.catchThrowable(() -> service.save(book));

		assertThat(ex).isInstanceOf(BusinessException.class).hasMessage("isbn ja cadastrado");

		Mockito.verify(repository, Mockito.never()).save(book);
	}

	@Test
	@DisplayName("Deve retornar um livro quando buscar por id")
	public void getByIdBookTest() {
		Long id = 1L;
		Book book = createBook();
		book.setId(id);

		Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

		Optional<Book> returnedBook = service.getById(id);

		assertThat(returnedBook.isPresent()).isTrue();
		assertThat(returnedBook.get().getId()).isEqualTo(id);
	}

	@Test
	@DisplayName("Deve retornar vazio ao obter livro inexistente")
	public void bookNotFoundByIdTest() {
		Long id = 1L;

		Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

		Optional<Book> book = service.getById(id);

		assertThat(book.isPresent()).isFalse();
	}

	@Test
	@DisplayName("Deve deletar um livro")
	public void deleteBookTest() {
		Book book = Book.builder().id(1L).build();

		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.delete(book));

		Mockito.verify(repository, Mockito.times(1)).delete(book);
	}

	@Test
	@DisplayName("Deve lancar erro ao tentar deletar um livro sem id")
	public void shouldNotDeleteBookWithoutId() {
		Book book = Book.builder().build();

		Throwable ex = Assertions.catchThrowable(() -> service.delete(book));

		assertThat(ex).isInstanceOf(IllegalArgumentException.class).hasMessage("Book id cant be null");

		Mockito.verify(repository, Mockito.never()).delete(book);
	}

	@Test
	@DisplayName("Deve atualizar um livro")
	public void updateBookTest() {
		Long id = 1l;

		Book bookToUpdate = Book.builder().id(id).build();

		Book updatedBook = createBook();
		updatedBook.setId(id);

		Mockito.when(repository.save(bookToUpdate)).thenReturn(updatedBook);

		Book returnedBook = service.update(bookToUpdate);

		Mockito.verify(repository, Mockito.times(1)).save(bookToUpdate);
		assertThat(returnedBook.getIsbn()).isEqualTo(updatedBook.getIsbn());
		assertThat(returnedBook.getAuthor()).isEqualTo(updatedBook.getAuthor());
	}

	@Test
	@DisplayName("Deve lancar erro atualizar um livro sem id")
	public void updateInvalidBookTest() {
		Book book = createBook();

		Throwable ex = Assertions.catchThrowable(() -> service.update(book));

		assertThat(ex).isInstanceOf(IllegalArgumentException.class).hasMessage("Book id cant be null");

		Mockito.verify(repository, Mockito.never()).save(book);
	}

	@Test
	@DisplayName("Deve filtrar livros pelas propriedades")
	public void findBookTest() {
		Book book = createBook();
		book.setId(1l);
		PageRequest pageRequest = PageRequest.of(0, 10);
		List<Book> lista = Collections.singletonList(book);
		Page<Book> page = new PageImpl<Book>(lista, pageRequest, 1);

		Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class))).thenReturn(page);

		Page<Book> result = service.find(book, pageRequest);

		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent()).isEqualTo(lista);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);
	}

	@Test
	@DisplayName("Deve obter livro por isbn")
	public void findBookByInexistentIsbnTest() {
		Book book = createBook();
		Mockito.when(repository.findByIsbn(book.getIsbn())).thenReturn(Optional.of(book));

		Optional<Book> returnedBook = service.getBookByIsbn(book.getIsbn());

		assertThat(returnedBook.isPresent()).isTrue();
		assertThat(returnedBook.get().getIsbn()).isEqualTo(book.getIsbn());

		Mockito.verify(repository, times(1)).findByIsbn(book.getIsbn());
	}

	@Test
	@DisplayName("Deve retornar vazio ao obter livro por isbn inexistente")
	public void findBookByIsbnTest() {
		Book book = createBook();
		Mockito.when(repository.findByIsbn(book.getIsbn())).thenReturn(Optional.empty());

		Optional<Book> returnedBook = service.getBookByIsbn(book.getIsbn());

		assertThat(returnedBook.isPresent()).isFalse();
	}

	private Book createBook() {
		return Book.builder().isbn("123").author("Fulano").title("Teste").build();
	}

}
