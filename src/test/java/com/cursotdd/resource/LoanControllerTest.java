package com.cursotdd.resource;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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

import com.cursotdd.dto.LoanDto;
import com.cursotdd.dto.LoanFilterDto;
import com.cursotdd.dto.ReturnedLoanDto;
import com.cursotdd.exception.BusinessException;
import com.cursotdd.model.entity.Book;
import com.cursotdd.model.entity.Loan;
import com.cursotdd.resources.LoanController;
import com.cursotdd.service.BookService;
import com.cursotdd.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

	static String LOAN_API = "/api/loans";

	@Autowired
	MockMvc mvc;

	@MockBean
	BookService bookService;

	@MockBean
	LoanService loanService;

	@Test
	@DisplayName("Deve realizar um emprestimo")
	public void createLoanTest() throws Exception {
		LoanDto dto = LoanDto.builder().customer("Fulano").isbn("123").customerEmail("customer@email.com").build();
		String json = new ObjectMapper().writeValueAsString(dto);

		Book book = Book.builder().id(1L).isbn("123").build();
		BDDMockito.given(bookService.getBookByIsbn(dto.getIsbn())).willReturn(Optional.of(book));

		Loan loan = Loan.builder().id(1L).customer("Fulano").book(book).loanDate(LocalDate.now()).build();
		BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json);

		mvc.perform(request).andExpect(status().isCreated()).andExpect(content().string("1"));
	}

	@Test
	@DisplayName("Deve retornar erro ao fazer emprestimo de um livro inexistente")
	public void invalidIsbnCreateLoanTest() throws Exception {
		LoanDto dto = LoanDto.builder().customer("Fulano").isbn("123").build();
		String json = new ObjectMapper().writeValueAsString(dto);

		BDDMockito.given(bookService.getBookByIsbn(dto.getIsbn())).willReturn(Optional.empty());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json);

		mvc.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("errors", Matchers.hasSize(1)))
				.andExpect(jsonPath("errors[0]").value("livro nao encontrado para o isbn informado"));
	}

	@Test
	@DisplayName("Deve retornar erro ao fazer emprestimo de um livro emprestado")
	public void loanedBookOnCreateLoanTest() throws Exception {
		LoanDto dto = LoanDto.builder().customer("Fulano").isbn("123").build();
		String json = new ObjectMapper().writeValueAsString(dto);

		Book book = Book.builder().id(1L).isbn("123").build();
		BDDMockito.given(bookService.getBookByIsbn(dto.getIsbn())).willReturn(Optional.of(book));

		BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
				.willThrow(new BusinessException("livro ja emprestado"));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json);

		mvc.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("errors", Matchers.hasSize(1)))
				.andExpect(jsonPath("errors[0]").value("livro ja emprestado"));
	}

	@Test
	@DisplayName("Deve retornar um livro")
	public void returnBookTest() throws Exception {
		ReturnedLoanDto dto = ReturnedLoanDto.builder().returned(true).build();
		String json = new ObjectMapper().writeValueAsString(dto);
		Loan loan = Loan.builder().id(1l).build();
		BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.of(loan));

		mvc.perform(MockMvcRequestBuilders.patch(LOAN_API.concat("/1")).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isOk());

		verify(loanService, Mockito.times(1)).update(loan);
	}

	@Test
	@DisplayName("Deve retornar 404 quando devolver um livro sem emprestimo")
	public void returnInexistentBookTest() throws Exception {
		ReturnedLoanDto dto = ReturnedLoanDto.builder().returned(true).build();
		String json = new ObjectMapper().writeValueAsString(dto);

		BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.empty());

		mvc.perform(MockMvcRequestBuilders.patch(LOAN_API.concat("/1")).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isNotFound());

		verify(loanService, Mockito.never()).update(Mockito.any(Loan.class));
	}

	@Test
	@DisplayName("Deve filtrar emprestimos")
	public void findLoanTest() throws Exception {
		Long id = 1l;

		Book book = Book.builder().id(2l).isbn("123").build();
		Loan loan = Loan.builder().id(id).book(book).customer("zé").loanDate(LocalDate.now()).returned(true).build();

		BDDMockito.given(loanService.find(Mockito.any(LoanFilterDto.class), Mockito.any(Pageable.class)))
				.willReturn(new PageImpl<Loan>(Collections.singletonList(loan), PageRequest.of(0, 100), 1));

		String queryString = String.format("?customer=%s&isbn=%s&page=0&size=100", loan.getCustomer(), book.getIsbn());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(LOAN_API.concat(queryString))
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request).andExpect(status().isOk()).andExpect(jsonPath("content", Matchers.hasSize(1)))
				.andExpect(jsonPath("totalElements").value(1)).andExpect(jsonPath("pageable.pageSize").value(100))
				.andExpect(jsonPath("pageable.pageNumber").value(0));
	}
}
