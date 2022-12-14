package com.cursotdd.model.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.cursotdd.model.entity.Book;
import com.cursotdd.model.entity.Loan;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

	@Autowired
	TestEntityManager entityManager;

	@Autowired
	LoanRepository repository;

	@Test
	@DisplayName("deve verificar se existe emprestimo nao devolvido para o livro")
	public void existsByBookAndNotReturnedTest() {

		Book book = Book.builder().author("autor").isbn("123").title("titulo").build();
		Loan loan = Loan.builder().book(book).customer("Fulano").loanDate(LocalDate.now()).build();

		entityManager.persist(book);
		entityManager.persist(loan);

		boolean exists = repository.existsByBookAndNotReturned(book);

		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("deve buscar emprestimo por isbn ou customer")
	public void findByBookIsbnOrCustomerTest() {

		Book book = Book.builder().author("autor").isbn("123").title("titulo").build();
		Loan loan = Loan.builder().book(book).customer("Fulano").loanDate(LocalDate.now()).build();

		entityManager.persist(book);
		entityManager.persist(loan);

		Page<Loan> result = repository.findByBookIsbnOrCustomer(book.getIsbn(), loan.getCustomer(),
				PageRequest.of(0, 10));

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent()).contains(loan);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getTotalElements()).isEqualTo(1);
	}

	@Test
	@DisplayName("Deve obter emprestimos atrasados 3 dias")
	public void findByLoanDateLessThanAndNotReturnedTest() {
		Book book = Book.builder().author("autor").isbn("123").title("titulo").build();
		Loan loan = Loan.builder().book(book).customer("Fulano").loanDate(LocalDate.now().minusDays(5)).build();

		entityManager.persist(book);
		entityManager.persist(loan);

		List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

		assertThat(result).hasSize(1).contains(loan);
	}

	@Test
	@DisplayName("Deve retornar vazio quando nao existir emprestimos atrasados")
	public void notFindByLoanDateLessThanAndNotReturnedTest() {
		Book book = Book.builder().author("autor").isbn("123").title("titulo").build();
		Loan loan = Loan.builder().book(book).customer("Fulano").loanDate(LocalDate.now()).build();

		entityManager.persist(book);
		entityManager.persist(loan);

		List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

		assertThat(result).isEmpty();
	}
}
