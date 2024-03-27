package dao;

import entity.Book;
import exception.DaoOperationException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BookDaoImpl implements BookDao {
  @Override
  public Book save(Book bookToSave) {
    var insertSql = "INSERT INTO book(name, author) VALUES(?, ?)";
    try (var connection = DBUtil.getConnection();
        var insertStatement =
            connection.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
      Objects.requireNonNull(bookToSave, "Cannot save null value book");
      insertStatement.setString(1, bookToSave.getName());
      insertStatement.setString(2, bookToSave.getAuthor());
      insertStatement.executeUpdate();
      var generatedId = insertStatement.getGeneratedKeys();
      if (generatedId.next()) {
        bookToSave.setId(generatedId.getLong(1));
      }
      return bookToSave;
    } catch (SQLException e) {
      throw new DaoOperationException(String.format("Error saving book: %s", bookToSave), e);
    } catch (NullPointerException e) {
      throw new DaoOperationException("Null pointer exception occurred while attempting to save the book. " +
              "Please ensure that the book object is not null.");
    }
  }

  @Override
  public void returnBook(long bookId) {
    var returnByIdSql = "UPDATE book SET reader_id = null WHERE id = ?";
    try (var connection = DBUtil.getConnection();
        var returnStatement = connection.prepareStatement(returnByIdSql)) {
      returnStatement.setLong(1, bookId);
      returnStatement.executeUpdate();
    } catch (SQLException e) {
      throw new DaoOperationException(String.format("Error returning book with id: %d", bookId), e);
    }
  }

  @Override
  public Optional<Book> findById(long bookId) {
    var selectByIdSql = "SELECT id, name, author, reader_id FROM book WHERE id = ?";
    try (var connection = DBUtil.getConnection();
        var selectByIdStatement = connection.prepareStatement(selectByIdSql)) {
      selectByIdStatement.setLong(1, bookId);
      var resultSet = selectByIdStatement.executeQuery();
      if (resultSet.next()) {
        var book = mapResultSetToBook(resultSet);
        return Optional.of(book);
      } else {
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw new DaoOperationException(
          String.format("Error finding book with bookId: %d", bookId), e);
    }
  }

  @Override
  public List<Book> findAll() {
    var selectAllSql = "SELECT id, name, author, reader_id FROM book";
    try (var connection = DBUtil.getConnection();
        var statement = connection.createStatement()) {
      var resultSet = statement.executeQuery(selectAllSql);
      return mapResultSetToBooksList(resultSet);
    } catch (SQLException e) {
      throw new DaoOperationException("Error finding all books", e);
    }
  }

  private List<Book> mapResultSetToBooksList(ResultSet resultSet) throws SQLException {
    List<Book> books = new ArrayList<>();
    while (resultSet.next()) {
      var book = mapResultSetToBook(resultSet);
      books.add(book);
    }
    return books;
  }

  private Book mapResultSetToBook(ResultSet resultSet) {
    try {
      var book = new Book();
      book.setId(resultSet.getLong("id"));
      book.setName(resultSet.getString("name"));
      book.setAuthor(resultSet.getString("author"));
      book.setReaderId(resultSet.getLong("reader_id"));
      return book;
    } catch (SQLException e) {
      throw new DaoOperationException("Cannot parse row to create book instance", e);
    }
  }

  @Override
  public void borrow(long bookId, long readerId) {
    var borrowByIdSql = "UPDATE book SET reader_id = ? WHERE id = ?";
    try (var connection = DBUtil.getConnection();
        var borrowStatement = connection.prepareStatement(borrowByIdSql)) {
      borrowStatement.setLong(1, readerId);
      borrowStatement.setLong(2, bookId);
      borrowStatement.executeUpdate();
    } catch (SQLException e) {
      throw new DaoOperationException(
          String.format("Error borrowing book with id: %d for reader id: %d", bookId, readerId), e);
    }
  }

  @Override
  public List<Book> findAllByReaderId(long readerId) {
    var selectBookByReaderIdSql =
        "SELECT id, name, author, reader_id FROM book WHERE reader_id = ?";
    try (var connection = DBUtil.getConnection();
        var selectByReaderIdStatement = connection.prepareStatement(selectBookByReaderIdSql)) {
      selectByReaderIdStatement.setLong(1, readerId);
      var resultSet = selectByReaderIdStatement.executeQuery();
      return mapResultSetToBooksList(resultSet);
    } catch (SQLException e) {
      throw new DaoOperationException(
          String.format("Error finding all books by reader id: %d", readerId), e);
    }
  }
}
