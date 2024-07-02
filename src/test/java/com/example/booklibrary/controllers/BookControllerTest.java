package com.example.booklibrary.controllers;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.booklibrary.dto.BookWithReaderDto;
import com.example.booklibrary.entity.Book;
import com.example.booklibrary.entity.Reader;
import com.example.booklibrary.exception.ReaderNotFoundException;
import com.example.booklibrary.service.LibraryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = BookController.class)
@ExtendWith(MockitoExtension.class)
class BookControllerTest {
  @Autowired BookController bookController;
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @MockBean LibraryService libraryService;

  @Test
  void getBooksShouldReturnBookList() throws Exception {
    var bookList =
        List.of(
            generateBookWithId(1L, "Test1", "Test1"),
            generateBookWithId(2L, "Test2", "Test2"),
            generateBookWithId(3L, "Test3", "Test3"));

    when(libraryService.findAllBooks()).thenReturn(bookList);

    mockMvc
        .perform(get("/api/v1/books"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0].id").value(bookList.get(0).getId()))
        .andExpect(jsonPath("$[0].name").value(bookList.get(0).getName()))
        .andExpect(jsonPath("$[0].author").value(bookList.get(0).getAuthor()))
        .andExpect(jsonPath("$[1].id").value(bookList.get(1).getId()))
        .andExpect(jsonPath("$[1].name").value(bookList.get(1).getName()))
        .andExpect(jsonPath("$[1].author").value(bookList.get(1).getAuthor()))
        .andExpect(jsonPath("$[2].id").value(bookList.get(2).getId()))
        .andExpect(jsonPath("$[2].name").value(bookList.get(2).getName()))
        .andExpect(jsonPath("$[2].author").value(bookList.get(2).getAuthor()));
  }

  @Test
  void getBooksShouldReturnEmptyList() throws Exception {
    List<Book> bookList = new ArrayList<>();

    when(libraryService.findAllBooks()).thenReturn(bookList);

    mockMvc
        .perform(get("/api/v1/books"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void saveBook() throws Exception {
    var book = generateBook("Test name", "Test author");
    var bookJson = objectMapper.writeValueAsString(book);
    when(libraryService.addNewBook(book)).thenReturn(book);

    mockMvc
        .perform(post("/api/v1/books").contentType(MediaType.APPLICATION_JSON).content(bookJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value(book.getName()))
        .andExpect(jsonPath("$.author").value(book.getAuthor()));

    verify(libraryService, times(1)).addNewBook(book);
  }

  @Test
  void saveBookShouldThrowsExceptionIfRequestBodyContainsId() throws Exception {
    var book = generateBookWithId(1L, "dummy", "dummy");
    var bookJson = objectMapper.writeValueAsString(book);

    mockMvc
        .perform(post("/api/v1/books").contentType(MediaType.APPLICATION_JSON).content(bookJson))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.errorMessage")
                .value("Request body should not contain book id value, only name and author!"));

    verify(libraryService, never()).addNewBook(book);
  }

  private static Stream<Arguments> provideInvalidFields() {
    return Stream.of(
        Arguments.of(
            "name", "x", "Book name must be longer than 5 characters, shorter than 100 characters"),
        Arguments.of(
            "name",
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghij",
            "Book name must be longer than 5 characters, shorter than 100 characters"),
        Arguments.of(
            "name",
            "dummy|",
            "Book name must not contain the following characters: |/\\#%=+*_><, and must be written using ENGLISH letters"),
        Arguments.of(
            "name",
            "dummy/",
            "Book name must not contain the following characters: |/\\#%=+*_><, and must be written using ENGLISH letters"),
        Arguments.of(
            "name",
            "dummy\\",
            "Book name must not contain the following characters: |/\\#%=+*_><, and must be written using ENGLISH letters"),
        Arguments.of(
            "name",
            "dummy#",
            "Book name must not contain the following characters: |/\\#%=+*_><, and must be written using ENGLISH letters"),
        Arguments.of(
            "name",
            "dummy%",
            "Book name must not contain the following characters: |/\\#%=+*_><, and must be written using ENGLISH letters"),
        Arguments.of(
            "name",
            "dummy=",
            "Book name must not contain the following characters: |/\\#%=+*_><, and must be written using ENGLISH letters"),
        Arguments.of(
            "name",
            "dummy+",
            "Book name must not contain the following characters: |/\\#%=+*_><, and must be written using ENGLISH letters"),
        Arguments.of(
            "name",
            "dummy*",
            "Book name must not contain the following characters: |/\\#%=+*_><, and must be written using ENGLISH letters"),
        Arguments.of(
            "name",
            "dummy_",
            "Book name must not contain the following characters: |/\\#%=+*_><, and must be written using ENGLISH letters"),
        Arguments.of(
            "name",
            "dummy>",
            "Book name must not contain the following characters: |/\\#%=+*_><, and must be written using ENGLISH letters"),
        Arguments.of(
            "name",
            "dummy<",
            "Book name must not contain the following characters: |/\\#%=+*_><, and must be written using ENGLISH letters"),
        Arguments.of(
            "author",
            "x",
            "Book author must be longer than 5 characters, shorter than 30 characters"),
        Arguments.of(
            "author",
            "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz",
            "Book author must be longer than 5 characters, shorter than 30 characters"),
        Arguments.of(
            "author",
            "dummy/",
            "Book author must contain only ENGLISH letters, spaces, dashes, apostrophes"));
  }

  private static void setField(Book book, String field, String value) {
    switch (field) {
      case "name" -> {
        book.setName(value);
        book.setAuthor("Valid Author");
      }
      case "author" -> {
        book.setName("Valid Name");
        book.setAuthor(value);
      }
    }
  }

  @ParameterizedTest
  @MethodSource("provideInvalidFields")
  void saveBookShouldThrowsExceptionIfInvalidArguments(
      String field, String value, String errorMessage) throws Exception {
    var book = new Book();
    setField(book, field, value);
    var bookJson = objectMapper.writeValueAsString(book);

    mockMvc
        .perform(post("/api/v1/books").contentType(MediaType.APPLICATION_JSON).content(bookJson))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.errorMessage")
                .value("Failed to create a new book, the request contains invalid fields"))
        .andExpect(jsonPath("$.errors[0].field").value(field))
        .andExpect(jsonPath("$.errors[0].message").value(errorMessage));

    verify(libraryService, never()).addNewBook(book);
  }

  @Test
  void borrowBookToReader() throws Exception {
    var bookId = 1L;
    var readerId = 1L;

    doNothing().when(libraryService).borrowBook(bookId, readerId);

    mockMvc
        .perform(post("/api/v1/books/{bookId}/readers/{readerId}", bookId, readerId))
        .andExpect(status().isOk());

    verify(libraryService, times(1)).borrowBook(bookId, readerId);
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = ';',
      value = {
        "-1 ; 1 ; Please use only positive bookId",
        "1 ; -1 ; Please use only positive readerId",
        "-1 ; -1 ; Please use only positive bookId, Please use only positive readerId"
      })
  void borrowBookToReaderShouldThrowsExceptionIfInvalidArguments(
      Long bookId, Long readerId, String expectedMessage) throws Exception {

    mockMvc
        .perform(post("/api/v1/books/{bookId}/readers/{readerId}", bookId, readerId))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorMessage").value(expectedMessage));

    verify(libraryService, never()).borrowBook(bookId, readerId);
  }

  @Test
  void returnBook() throws Exception {
    var bookId = 1L;

    doNothing().when(libraryService).returnBookToLibrary(bookId);

    mockMvc.perform(delete("/api/v1/books/{bookId}", bookId)).andExpect(status().isOk());

    verify(libraryService, times(1)).returnBookToLibrary(bookId);
  }

  @Test
  void getReaderByBookId() throws Exception {
    var bookId = 1L;
    var reader = generateReader(1L, "Test1");

    when(libraryService.showCurrentReaderOfBook(bookId)).thenReturn(Optional.of(reader));

    mockMvc
        .perform(get("/api/v1/books/{bookId}/reader", bookId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("Test1"));

    verify(libraryService, times(1)).showCurrentReaderOfBook(bookId);
  }

  @Test
  void getReaderByBookIdShouldThrowsExceptionIfBookHasNotReader() throws Exception {
    var bookId = 1L;
    var errorMessage = "Cannot find reader by book = 1 id! Book has not any reader";
    when(libraryService.showCurrentReaderOfBook(bookId))
        .thenThrow(new ReaderNotFoundException(errorMessage));

    mockMvc
        .perform(get("/api/v1/books/{bookId}/reader", bookId))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorMessage").value(errorMessage));

    verify(libraryService, times(1)).showCurrentReaderOfBook(bookId);
  }

  @Test
  void getBooksWithReader() throws Exception {
    List<BookWithReaderDto> booksWithReader =
        List.of(
            new BookWithReaderDto(1L, "Test1", "Test1", generateReader(1L, "Reader1")),
            new BookWithReaderDto(2L, "Test2", "Test2", generateReader(2L, "Reader2")));

    when(libraryService.findAllBooksWithReaders()).thenReturn(booksWithReader);

    mockMvc
        .perform(get("/api/v1/books/readers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value(booksWithReader.get(0).getId()))
        .andExpect(jsonPath("$[0].author").value(booksWithReader.get(0).getAuthor()))
        .andExpect(jsonPath("$[0].name").value(booksWithReader.get(0).getName()))
        .andExpect(jsonPath("$[0].reader.id").value(booksWithReader.get(0).getReader().getId()))
        .andExpect(jsonPath("$[0].reader.name").value(booksWithReader.get(0).getReader().getName()))
        .andExpect(jsonPath("$[1].id").value(booksWithReader.get(1).getId()))
        .andExpect(jsonPath("$[1].author").value(booksWithReader.get(1).getAuthor()))
        .andExpect(jsonPath("$[1].name").value(booksWithReader.get(1).getName()))
        .andExpect(jsonPath("$[1].reader.id").value(booksWithReader.get(1).getReader().getId()))
        .andExpect(
            jsonPath("$[1].reader.name").value(booksWithReader.get(1).getReader().getName()));
  }

  private static Book generateBookWithId(Long id, String name, String author) {
    return new Book(id, name, author);
  }

  private static Book generateBook(String name, String author) {
    return new Book(name, author);
  }

  private static Reader generateReader(Long id, String name) {
    return new Reader(id, name);
  }
}
