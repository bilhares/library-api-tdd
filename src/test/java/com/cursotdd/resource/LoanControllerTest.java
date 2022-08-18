package com.cursotdd.resource;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.cursotdd.dto.LoanDto;
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
		LoanDto dto = LoanDto.builder().customer("Fulano").isbn("123").build();
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
}
