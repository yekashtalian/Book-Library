package com.example.booklibrary.dao;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.booklibrary.entity.Book;
import com.example.booklibrary.entity.Reader;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@JdbcTest
@Sql(scripts = "classpath:db/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@ComponentScan
class BookDaoIT {
  @Autowired private BookDao bookDao;
  @Autowired private ReaderDao readerDao;
  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void cleanData() {
    jdbcTemplate.execute("DELETE FROM book");
    jdbcTemplate.execute("DELETE FROM reader");
  }

  @Test
  void saveAndFindById() {
    var bookToSave = generateBook("Test1", "Test1");

    var savedBook = bookDao.save(bookToSave);
    Optional<Book> actualBook = bookDao.findById(savedBook.getId());

    assertThat(actualBook).isPresent();
    assertAll(
        () -> assertThat(savedBook.getId()).isNotNull(),
        () -> assertThat(actualBook.get().getName()).isEqualTo(bookToSave.getName()),
        () -> assertThat(actualBook.get().getAuthor()).isEqualTo(bookToSave.getAuthor()));
  }
  @Test
  void findAll() {
    var book1 = bookDao.save(generateBook("Test1", "Test1"));
    var book2 = bookDao.save(generateBook("Test2", "Test2"));
    var book3 = bookDao.save(generateBook("Test3", "Test3"));

    List<Book> actualBooks = bookDao.findAll();
    List<Long> bookIds = actualBooks.stream().map(Book::getId).toList();

    assertThat(actualBooks).hasSize(3);
    assertThat(bookIds).contains(book1.getId(), book2.getId(), book3.getId());
  }

  @Test
  void findAllByReaderId() {
    var book1 = bookDao.save(generateBook("Test1", "Test1"));
    var book2 = bookDao.save(generateBook("Test2", "Test2"));
    var book3 = bookDao.save(generateBook("Test3", "Test3"));
    var book4 = bookDao.save(generateBook("Test4", "Test4"));

    var reader1 = readerDao.save(generateReader("Test1"));
    var reader2 = readerDao.save(generateReader("Test2"));

    bookDao.borrow(book1.getId(), reader1.getId());
    bookDao.borrow(book2.getId(), reader1.getId());
    bookDao.borrow(book3.getId(), reader2.getId());

    Optional<Book> actualBook1 = bookDao.findById(book1.getId());
    Optional<Book> actualBook2 = bookDao.findById(book2.getId());
    List<Book> expectedBooks = List.of(actualBook1.get(), actualBook2.get());

    List<Book> actualBooks = bookDao.findAllByReaderId(reader1.getId());

    assertThat(actualBooks).isNotNull();
    assertThat(actualBooks).hasSameSizeAs(expectedBooks);
    assertThat(actualBooks).containsAll(expectedBooks);
  }

  @Test
  void findAllWithReaders() {
    var book1 = bookDao.save(generateBook("Test1", "Test1"));
    var book2 = bookDao.save(generateBook("Test2", "Test2"));
    var book3 = bookDao.save(generateBook("Test3", "Test3"));

    var reader1 = readerDao.save(generateReader("Test1"));
    var reader2 = readerDao.save(generateReader("Test2"));

    bookDao.borrow(book1.getId(), reader1.getId());
    bookDao.borrow(book2.getId(), reader2.getId());

    Optional<Book> borrowedBook1 = bookDao.findById(book1.getId());
    Optional<Book> borrowedBook2 = bookDao.findById(book2.getId());
    Optional<Book> emptyBook = bookDao.findById(book3.getId());
    Map<Book, Optional<Reader>> expectedMap =
        Map.of(
            borrowedBook1.get(), Optional.of(reader1),
            borrowedBook2.get(), Optional.of(reader2),
            emptyBook.get(), Optional.empty());
    Map<Book, Optional<Reader>> actualMap = bookDao.findAllWithReaders();

    assertAll(
        () -> assertThat(actualMap).hasSameSizeAs(expectedMap),
        () -> assertThat(actualMap).containsEntry(borrowedBook1.get(), Optional.of(reader1)),
        () -> assertThat(actualMap).containsEntry(borrowedBook2.get(), Optional.of(reader2)),
        () -> assertThat(actualMap).containsKey(emptyBook.get()),
        () -> assertThat(actualMap.get(emptyBook.get())).isEmpty());
  }

  @Test
  void returnBook() {
    var book = bookDao.save(generateBook("Test1", "Test1"));
    var reader = readerDao.save(generateReader("Test1"));
    bookDao.borrow(book.getId(), reader.getId());

    Optional<Book> actualBook = bookDao.findById(book.getId());
    assertThat(actualBook.get().getReaderId()).isEqualTo(reader.getId());

    bookDao.returnBook(book.getId());
    Optional<Book> returnedBook = bookDao.findById(book.getId());

    assertThat(returnedBook.get().getReaderId()).isNull();
  }

  private static Book generateBook(String name, String author) {
    return new Book(name, author);
  }

  private static Reader generateReader(String name) {
    return new Reader(name);
  }
}
