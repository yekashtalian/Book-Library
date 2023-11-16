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
  private static final String SELECT_ALL_SQL = "SELECT id, name, author, reader_id FROM book";
  private static final String INSERT_SQL = "INSERT INTO book(name, author) VALUES(?, ?)";
  private static final String SELECT_BY_ID_SQL = "SELECT id, name, author, reader_id FROM book WHERE id = ?";
  private static final String BORROW_BY_ID_SQL = "UPDATE book SET reader_id = ? WHERE id = ?";
  private static final String RETURN_BY_ID_SQL = "UPDATE book SET reader_id = null WHERE id = ?";
  private static final String SELECT_BOOK_BY_READER_ID_SQL =
      "SELECT id, name, author, reader_id FROM book WHERE reader_id = ?";

  @Override
  public Book save(Book bookToSave) {
    Objects.requireNonNull(bookToSave);
    try (var connection = DBUtil.getConnection();
         var insertStatement = connection.prepareStatement(INSERT_SQL, PreparedStatement.RETURN_GENERATED_KEYS)
    ) {
      insertStatement.setString(1, bookToSave.getName());
      insertStatement.setString(2, bookToSave.getAuthor());
      insertStatement.executeUpdate();
      try(var generatedId = insertStatement.getGeneratedKeys()){
        if (generatedId.next()){
          if (generatedId.getLong(1) == 0){
            throw new DaoOperationException("Book ID cannot be 0");
          }
          bookToSave.setId(generatedId.getLong(1));
          return bookToSave;
        }else {
          throw new DaoOperationException("Cannot obtain book ID");
        }
      }
    } catch (SQLException e) {
      throw new DaoOperationException(String.format("Error saving book: %s", bookToSave), e);
    }
  }

  @Override
  public void returnBook(long bookId) {
    try (var connection = DBUtil.getConnection();
         var returnStatement = connection.prepareStatement(RETURN_BY_ID_SQL)
    ) {
      returnStatement.setLong(1, bookId);
      var affectedRows = returnStatement.executeUpdate();
      if (affectedRows == 0){
        throw new DaoOperationException(String.format("Book with id = %d doesn't exists", bookId));
      }
    } catch (SQLException e) {
      throw new DaoOperationException(String.format("Error returning book with id: %d", bookId), e);
    }
  }

  @Override
  public Optional<Book> findById(long bookId) {
    try (var connection = DBUtil.getConnection();
         var selectByIdStatement = connection.prepareStatement(SELECT_BY_ID_SQL)
    ) {
      selectByIdStatement.setLong(1, bookId);
      try(var resultSet = selectByIdStatement.executeQuery()){
        if (resultSet.next()){
          var book = parseRow(resultSet);
          return Optional.of(book);
        }else {
          return Optional.empty();
        }
      }
    } catch (SQLException e) {
      throw new DaoOperationException(
          String.format("Error finding book with bookId: %d", bookId), e);
    }
  }

  @Override
  public List<Book> findAll() {
    try (var connection = DBUtil.getConnection();
         var statement = connection.createStatement()
    ) {
      try(var resultSet = statement.executeQuery(SELECT_ALL_SQL)){
        return collectToList(resultSet);
      }
    } catch (SQLException e) {
      throw new DaoOperationException("Error finding all books", e);
    }
  }

  private List<Book> collectToList(ResultSet resultSet) throws SQLException {
    List<Book> books = new ArrayList<>();
    while (resultSet.next()) {
      var book = parseRow(resultSet);
      books.add(book);
    }
    return books;
  }

  private Book parseRow(ResultSet resultSet) {
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
    try (var connection = DBUtil.getConnection();
         var borrowStatement = connection.prepareStatement(BORROW_BY_ID_SQL)
    ) {
      borrowStatement.setLong(1, readerId);
      borrowStatement.setLong(2, bookId);
      var affectedRows = borrowStatement.executeUpdate();
      if (affectedRows == 0){
        throw new DaoOperationException(String.format("Book with id = %d doesn't exists", bookId));
      }
    } catch (SQLException e) {
      throw new DaoOperationException(
          String.format("Error borrowing book with id: %d for reader id: %d", bookId, readerId), e);
    }
  }

  @Override
  public List<Book> findAllByReaderId(long readerId) {
    try (var connection = DBUtil.getConnection();
         var selectByReaderIdStatement = connection.prepareStatement(SELECT_BOOK_BY_READER_ID_SQL)
    ) {
      selectByReaderIdStatement.setLong(1, readerId);
      var resultSet = selectByReaderIdStatement.executeQuery();
      return collectToList(resultSet);
    } catch (SQLException e) {
      throw new DaoOperationException(
          String.format("Error finding all books by reader id: %d", readerId), e);
    }
  }
}
