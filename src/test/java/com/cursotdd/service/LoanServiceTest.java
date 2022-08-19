package com.cursotdd.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

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
import com.cursotdd.model.entity.Loan;
import com.cursotdd.model.repository.LoanRepository;
import com.cursotdd.service.impl.LoanServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

	@MockBean
	LoanRepository repository;

	LoanService service;

	@BeforeEach
	public void setup() {
		this.service = new LoanServiceImpl(repository);
	}

	@Test
	@DisplayName("Deve salvar um emprestimo")
	public void saveLoanTest() {
		Book book = Book.builder().id(1L).build();
		Loan loanToSave = Loan.builder().customer("Fulano").book(book).loanDate(LocalDate.now()).build();

		Loan savedLoan = Loan.builder().id(1L).loanDate(LocalDate.now()).customer("Fulano").book(book).build();

		when(repository.existsByBookAndNotReturned(book)).thenReturn(false);
		when(repository.save(loanToSave)).thenReturn(savedLoan);

		Loan loan = service.save(loanToSave);

		assertThat(loan.getId()).isEqualTo(savedLoan.getId());
		assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
		assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
	}

	@Test
	@DisplayName("Deve lancar erro ao salvar um emprestimo com livro ja emprestado")
	public void loanedBookSaveTest() {
		Book book = Book.builder().id(1L).build();
		Loan loanToSave = Loan.builder().customer("Fulano").book(book).loanDate(LocalDate.now()).build();

		when(repository.existsByBookAndNotReturned(book)).thenReturn(true);

		Throwable ex = Assertions.catchThrowable(() -> service.save(loanToSave));

		assertThat(ex).isInstanceOf(BusinessException.class).hasMessage("livro ja emprestado");

		Mockito.verify(repository, Mockito.never()).save(loanToSave);
	}

	@Test
	@DisplayName("deve obter informacoes de um emprestimo por id")
	public void getLoanDetailsTest() {
		long id = 1L;
		Loan loan = createLoan(id);

		Mockito.when(repository.findById(id)).thenReturn(Optional.of(loan));

		Optional<Loan> returnedLoan = service.getById(id);

		assertThat(returnedLoan.isPresent()).isTrue();
		assertThat(returnedLoan.get().getId()).isEqualTo(loan.getId());
		assertThat(returnedLoan.get().getCustomer()).isEqualTo(loan.getCustomer());

		verify(repository).findById(id);
	}

	@Test
	@DisplayName("deve atualizar um emprestimo")
	public void updateLoanTest() {
		Long id = 1l;
		Loan loanToUpdate = createLoan(id);
		loanToUpdate.setReturned(true);

		Mockito.when(repository.save(loanToUpdate)).thenReturn(loanToUpdate);

		Loan updatedLoan = service.update(loanToUpdate);

		assertThat(updatedLoan.getReturned()).isTrue();
		verify(repository).save(loanToUpdate);
	}

	private Loan createLoan(long id) {
		return Loan.builder().id(id).book(Book.builder().id(1l).build()).customer("fulano").loanDate(LocalDate.now())
				.build();
	}
}
