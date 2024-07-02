package com.example.booklibrary.dao;

import com.example.booklibrary.entity.Book;
import com.example.booklibrary.entity.Reader;
import com.example.booklibrary.exception.DaoOperationException;
import java.util.*;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class BookDaoImpl implements BookDao {
  private JdbcTemplate jdbcTemplate;

  public BookDaoImpl(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Book save(Book bookToSave) {
    var query = "INSERT INTO book(name, author) VALUES(?, ?)";
    KeyHolder keyHolder = new GeneratedKeyHolder();
    try {
      Objects.requireNonNull(bookToSave, "Cannot save null value book");
      jdbcTemplate.update(
          con -> {
            var preparedStatement = con.prepareStatement(query, new String[] {"id"});
            preparedStatement.setString(1, bookToSave.getName());
            preparedStatement.setString(2, bookToSave.getAuthor());
            return preparedStatement;
          },
          keyHolder);
      if (keyHolder.getKey() != null) {
        bookToSave.setId(keyHolder.getKey().longValue());
      }
      return bookToSave;
    } catch (DataAccessException ex) {
      throw new DaoOperationException(String.format("Error saving book: %s", bookToSave), ex);
    } catch (NullPointerException ex) {
      throw new DaoOperationException(
          "Null pointer exception occurred while attempting to save the book. "
              + "Please ensure that the book object is not null.");
    }
  }

  @Override
  public void returnBook(long bookId) {
    var query = "UPDATE book SET reader_id = null WHERE id = ?";
    try {
      jdbcTemplate.update(query, bookId);
    } catch (DataAccessException ex) {
      throw new DaoOperationException(
          String.format("Error returning book with id: %d", bookId), ex);
    }
  }

  @Override
  public Optional<Book> findById(long bookId) {
    var query =
        "SELECT id, name, author, reader_id FROM book WHERE id = ?";
    try {
      //noinspection DataFlowIssue
      return Optional.of(
          jdbcTemplate.queryForObject(query, new BeanPropertyRowMapper<>(Book.class), bookId));
    } catch (EmptyResultDataAccessException ex) {
      return Optional.empty();
    } catch (DataAccessException ex) {
      throw new DaoOperationException(
          String.format("Error finding book with bookId: %d", bookId), ex);
    }
  }

  @Override
  public List<Book> findAll() {
    var query = "SELECT id, name, author, reader_id AS readerId FROM book";
    try {
      return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(Book.class));
    } catch (DataAccessException ex) {
      throw new DaoOperationException("Error finding all books", ex);
    }
  }

  @Override
  public void borrow(long bookId, long readerId) {
    var query = "UPDATE book SET reader_id = ? WHERE id = ?";
    try {
      jdbcTemplate.update(query, readerId, bookId);
    } catch (DataAccessException ex) {
      throw new DaoOperationException(
          String.format("Error borrowing book with id: %d for reader id: %d", bookId, readerId),
          ex);
    }
  }

  @Override
  public List<Book> findAllByReaderId(long readerId) {
    var query =
        """
                SELECT id,
                  name,
                  author,
                  reader_id
                FROM book
                  WHERE reader_id = ?
                """;
    try {
      return jdbcTemplate.query(query, new BeanPropertyRowMapper<>(Book.class), readerId);
    } catch (DataAccessException ex) {
      throw new DaoOperationException(
          String.format("Error finding all books by reader id: %d", readerId), ex);
    }
  }

  @Override
  public Map<Book, Optional<Reader>> findAllWithReaders() {
    var query =
        """
                SELECT
                  book.id AS bookId,
                  book.name AS bookName,
                  book.author AS bookAuthor,
                  book.reader_id,
                  reader.id AS readerId,
                  reader.name AS readerName
                FROM book
                  LEFT JOIN reader ON book.reader_id = reader.id
                     """;
    try {
      return jdbcTemplate.query(query, DaoUtils.getBookReaderExtractor());
    } catch (DataAccessException ex) {
      throw new DaoOperationException("Error finding books with their readers!");
    }
  }
}
