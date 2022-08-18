package com.cursotdd.resource;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.cursotdd.dto.BookDto;
import com.cursotdd.exception.BusinessException;
import com.cursotdd.model.entity.Book;
import com.cursotdd.resources.BookController;
import com.cursotdd.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
class BookControllerTest {

	static String BOOK_API = "/api/books";

	@Autowired
	MockMvc mvc;

	@MockBean
	BookService service;

	@Test
	@DisplayName("Deve criar um livro com sucesso")
	void createBookTest() throws Exception {
		BookDto dto = createNewBookDto();

		Book savedBook = Book.builder().id(101L).author("Autor").title("Meu Livro").isbn("123456").build();

		BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);

		String json = new ObjectMapper().writeValueAsString(dto);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(json);

		mvc.perform(request).andExpect(status().isCreated()).andExpect(jsonPath("id").value(101L))
				.andExpect(jsonPath("title").value(dto.getTitle()));
	}

	@Test
	@DisplayName("Deve lancar erro quando tentar cadastrar livro sem informacoes")
	void createInvalidBookTest() throws Exception {
		String json = new ObjectMapper().writeValueAsString(new BookDto());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(json);

		mvc.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("errors", Matchers.hasSize(3)));
	}

	@Test
	@DisplayName("Deve lancar erro ao tentar cadastrar livro com isbn ja utilizado")
	void createBookWithInvalidIsbnTest() throws Exception {
		String msgErro = "isbn ja cadastrado";
		BookDto dto = createNewBookDto();

		String json = new ObjectMapper().writeValueAsString(dto);
		BDDMockito.given(service.save(Mockito.any(Book.class))).willThrow(new BusinessException(msgErro));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(json);

		mvc.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("errors", Matchers.hasSize(1)))
				.andExpect(jsonPath("errors[0]").value(msgErro));
	}

	@Test
	@DisplayName("Obter os detalhes de um livro")
	public void getBookDetailsTest() throws Exception {
		Long id = 1L;
		Book book = Book.builder().id(id).author("Autor").title("Meu Livro").isbn("123456").build();
		BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + id))
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request).andExpect(status().isOk()).andExpect(jsonPath("id").value(id))
				.andExpect(jsonPath("title").value(book.getTitle()));
	}

	@Test
	@DisplayName("Deve retornar notfound quando nao encontrar detalhes de um livro")
	public void getInvalidBookDetailsTest() throws Exception {
		Long id = 1L;

		BDDMockito.given(service.getById(id)).willReturn(Optional.empty());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + id))
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Deve deletar um livro")
	public void deleteBookTest() throws Exception {
		Long id = 1L;

		Book book = Book.builder().id(id).author("Autor").title("Meu Livro").isbn("123456").build();
		BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/" + id));

		mvc.perform(request).andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("Deve retornar notfound quando tentar deletar um livro inexistente")
	public void deleteInvalidBookTest() throws Exception {
		Long id = 1L;
		BDDMockito.given(service.getById(id)).willReturn(Optional.empty());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/" + id));

		mvc.perform(request).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Deve atualizar um livro")
	public void updateBookTest() throws Exception {
		Long id = 1L;

		String json = new ObjectMapper().writeValueAsString(createNewBookDto());
		Book updatingBook = Book.builder().id(id).title("title 1").author("aut 1").isbn("456").build();
		Book updatedBook = Book.builder().id(id).author("Autor").title("Meu Livro").isbn("456").build();

		BDDMockito.given(service.getById(id)).willReturn(Optional.of(updatingBook));

		BDDMockito.given(service.update(updatingBook)).willReturn(updatedBook);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API.concat("/" + id))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(json);

		mvc.perform(request).andExpect(status().isOk()).andExpect(jsonPath("id").value(id))
				.andExpect(jsonPath("title").value(createNewBookDto().getTitle()))
				.andExpect(jsonPath("isbn").value("456"));
	}

	@Test
	@DisplayName("Deve retornar notfount ao tentar atualizar um livro inexistente")
	public void updateInexistentBookTest() throws Exception {
		Long id = 1L;
		String json = new ObjectMapper().writeValueAsString(createNewBookDto());

		BDDMockito.given(service.getById(id)).willReturn(Optional.empty());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API.concat("/" + id))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(json);

		mvc.perform(request).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("Deve filtrar livros")
	public void findBookTest() throws Exception {
		Long id = 1l;

		Book book = Book.builder().id(id).title(createNewBookDto().getTitle()).author(createNewBookDto().getAuthor())
				.isbn(createNewBookDto().getIsbn()).build();

		BDDMockito.given(service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
				.willReturn(new PageImpl<Book>(Collections.singletonList(book), PageRequest.of(0, 100), 1));

		String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(), book.getAuthor());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat(queryString))
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request).andExpect(status().isOk()).andExpect(jsonPath("content", Matchers.hasSize(1)))
				.andExpect(jsonPath("totalElements").value(1)).andExpect(jsonPath("pageable.pageSize").value(100))
				.andExpect(jsonPath("pageable.pageNumber").value(0));
	}

	private BookDto createNewBookDto() {
		return BookDto.builder().author("Autor").title("Meu Livro").isbn("123456").build();
	}

}
