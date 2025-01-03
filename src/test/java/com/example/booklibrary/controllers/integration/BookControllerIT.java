package com.example.booklibrary.controllers.integration;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.booklibrary.controllers.integration.annotation.ControllerIT;
import com.example.booklibrary.dao.BookDao;
import com.example.booklibrary.dao.ReaderDao;
import com.example.booklibrary.entity.Book;
import com.example.booklibrary.entity.Reader;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@ControllerIT
class BookControllerIT {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private BookDao bookDao;
  @Autowired private ReaderDao readerDao;

  @Test
  void getBooks() throws Exception {
    var book1 = bookDao.save(generateBook("1984", "Tony Morrison"));
    var book2 = bookDao.save(generateBook("Home", "George Orwell'"));
    var book3 = bookDao.save(generateBook("Glue", "Irvine Welsh"));

    mockMvc
        .perform(get("/api/v1/books"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(book1.getId()))
        .andExpect(jsonPath("$[0].name").value(book1.getName()))
        .andExpect(jsonPath("$[0].author").value(book1.getAuthor()))
        .andExpect(jsonPath("$[1].id").value(book2.getId()))
        .andExpect(jsonPath("$[1].name").value(book2.getName()))
        .andExpect(jsonPath("$[1].author").value(book2.getAuthor()))
        .andExpect(jsonPath("$[2].id").value(book3.getId()))
        .andExpect(jsonPath("$[2].name").value(book3.getName()))
        .andExpect(jsonPath("$[2].author").value(book3.getAuthor()));
  }

  @Test
  void saveBook() throws Exception {
    var book = generateBook("Martin Eden", "Jack London");

    var isBookPresent =
            bookDao.findAll().stream().anyMatch(currentBook -> currentBook.equals(book));
    assertThat(isBookPresent).isFalse();

    var result = mockMvc
            .perform(
                    post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(book)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(book.getName()))
            .andExpect(jsonPath("$.author").value(book.getAuthor()))
            .andReturn();

    var jsonResponse = result.getResponse().getContentAsString();
    var createdBook = objectMapper.readValue(jsonResponse, Book.class);

    assertAll(
            () -> assertThat(bookDao.findById(createdBook.getId())).isPresent(),
            () -> assertThat(createdBook).isEqualTo(bookDao.findById(createdBook.getId()).get())
    );
  }

  @Test
  void saveBookWithInvalidRequestBody() throws Exception {
    var book = Book.builder().id(4L).name("Jack London").author("Martin Eden").build();

    mockMvc
        .perform(
            post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book)))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.errorMessage").value("Request body should not contain book id value"));

    Optional<Book> maybeBook = bookDao.findById(book.getId());
    assertThat(maybeBook).isNotPresent();
  }

  @Test
  void borrowBook() throws Exception {
    var book = bookDao.save(generateBook("Martin Eden", "Jack London"));
    var reader = readerDao.save(generateReader("Jonny"));

    mockMvc
        .perform(post("/api/v1/books/{bookId}/readers/{readerId}", book.getId(), reader.getId()))
        .andExpect(status().isOk());

    Optional<Book> maybeBook = bookDao.findById(book.getId());
    assertThat(maybeBook.get().getReaderId()).isEqualTo(reader.getId());
  }

  @Test
  void returnBook() throws Exception {
    var book = bookDao.save(generateBook("Martin Eden", "Jack London"));
    var reader = readerDao.save(generateReader("Jonny"));

    bookDao.borrow(book.getId(), reader.getId());

    mockMvc.perform(delete("/api/v1/books/{bookId}", book.getId())).andExpect(status().isOk());

    Optional<Book> maybeBook = bookDao.findById(book.getId());
    assertThat(maybeBook.get().getReaderId()).isNull();
  }

  @Test
  void getReaderByBookId() throws Exception {
    var book = bookDao.save(generateBook("Martin Eden", "Jack London"));
    var reader = readerDao.save(generateReader("Jonny"));

    bookDao.borrow(book.getId(), reader.getId());

    mockMvc
        .perform(get("/api/v1/books/{bookId}/reader", book.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(reader.getId()))
        .andExpect(jsonPath("$.name").value(reader.getName()));
  }

  @Sql(statements = "TRUNCATE TABLE book RESTART IDENTITY")
  @Test
  void getBooksWithReaders() throws Exception {
    var book1 = bookDao.save(generateBook("Martin Eden", "Jack London"));
    var book2 = bookDao.save(generateBook("1984", "George Orwell"));
    var book3 = bookDao.save(generateBook("Home", "Tony Morrison"));
    var reader1 = readerDao.save(generateReader("Jonny"));
    var reader2 = readerDao.save(generateReader("Yevhenii"));

    bookDao.borrow(book1.getId(), reader1.getId());
    bookDao.borrow(book2.getId(), reader2.getId());

    mockMvc
        .perform(get("/api/v1/books/readers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[1].id").value(book1.getId()))
        .andExpect(jsonPath("$[1].name").value(book1.getName()))
        .andExpect(jsonPath("$[1].author").value(book1.getAuthor()))
        .andExpect(jsonPath("$[1].reader").value(reader1))
        .andExpect(jsonPath("$[0].id").value(book2.getId()))
        .andExpect(jsonPath("$[0].name").value(book2.getName()))
        .andExpect(jsonPath("$[0].author").value(book2.getAuthor()))
        .andExpect(jsonPath("$[0].reader").value(reader2));
  }

  private static Book generateBook(String name, String author) {
    return Book.builder().name(name).author(author).build();
  }

  private static Reader generateReader(String name) {
    return Reader.builder().name(name).build();
  }
}
