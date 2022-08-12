package com.cursotdd.dto;

import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {
	private long id;

	@NotEmpty
	private String title;

	@NotEmpty
	private String author;

	@NotEmpty
	private String isbn;

}
