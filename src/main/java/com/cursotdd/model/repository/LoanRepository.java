package com.cursotdd.model.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cursotdd.model.entity.Book;
import com.cursotdd.model.entity.Loan;

public interface LoanRepository extends JpaRepository<Loan, Long> {

	@Query("select case when(count(l.id) > 0) then true else false end "
			+ "from Loan l where l.book =:book and ( l.returned is null or l.returned is false)")
	boolean existsByBookAndNotReturned(@Param("book") Book book);

	@Query(value = "select l from Loan l join l.book b where b.isbn = :isbn or l.customer = :customer")
	Page<Loan> findByBookIsbnOrCustomer(@Param("isbn") String isbn, @Param("customer") String customer,
			Pageable pageRequest);

	Page<Loan> findByBook(Book book, Pageable pageable);

	@Query("select l from Loan l where l.loanDate <= :threeDaysAgo and ( l.returned is null or l.returned is false)")
	List<Loan> findByLoanDateLessThanAndNotReturned(@Param("threeDaysAgo") LocalDate threeDaysAgo);

}
