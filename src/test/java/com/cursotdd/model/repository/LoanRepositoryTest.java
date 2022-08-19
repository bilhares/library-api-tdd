package com.cursotdd.model.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
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
}
