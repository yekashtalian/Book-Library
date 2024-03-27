package dao;

import entity.Reader;
import exception.DaoOperationException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ReaderDaoImpl implements ReaderDao {
  @Override
  public Reader save(Reader readerToSave) {
    var insertSql = "INSERT INTO reader(name) VALUES(?)";
    try (var connection = DBUtil.getConnection();
        var updateStatement =
            connection.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
      Objects.requireNonNull(readerToSave, "Cannot save null value reader");
      updateStatement.setString(1, readerToSave.getName());
      updateStatement.executeUpdate();
      var generatedId = updateStatement.getGeneratedKeys();
      if (generatedId.next()) {
        readerToSave.setId(generatedId.getLong(1));
      }
      return readerToSave;
    } catch (SQLException e) {
      throw new DaoOperationException(String.format("Error saving reader: %s", readerToSave), e);
    } catch (NullPointerException e) {
      throw new DaoOperationException("Null pointer exception occurred while attempting to save the reader. " +
              "Please ensure that the reader object is not null.");
    }
  }

  @Override
  public Optional<Reader> findById(long readerId) {
    var selectByIdSql = "SELECT id, name FROM reader WHERE id = ?";
    try (var connection = DBUtil.getConnection();
        var selectByIdStatement = connection.prepareStatement(selectByIdSql)) {
      selectByIdStatement.setLong(1, readerId);
      var resultSet = selectByIdStatement.executeQuery();
      if (resultSet.next()) {
        var reader = mapResultSetToReader(resultSet);
        return Optional.of(reader);
      } else {
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw new DaoOperationException(
          String.format("Error finding reader with id: %d", readerId), e);
    }
  }

  private Reader mapResultSetToReader(ResultSet resultSet) {
    try {
      var reader = new Reader();
      reader.setId(resultSet.getLong("id"));
      reader.setName(resultSet.getString("name"));
      return reader;
    } catch (SQLException e) {
      throw new DaoOperationException("Cannot parse row to create reader instance", e);
    }
  }

  @Override
  public List<Reader> findAll() {
    var selectAllSql = "SELECT id, name FROM reader";
    try (var connection = DBUtil.getConnection();
        var selectAllStatement = connection.createStatement()) {
      var resultSet = selectAllStatement.executeQuery(selectAllSql);
      return collectToList(resultSet);
    } catch (SQLException e) {
      throw new DaoOperationException("Error finding all readers", e);
    }
  }

  private List<Reader> collectToList(ResultSet resultSet) throws SQLException {
    List<Reader> readers = new ArrayList<>();
    while (resultSet.next()) {
      var reader = mapResultSetToReader(resultSet);
      readers.add(reader);
    }
    return readers;
  }

  @Override
  public Optional<Reader> findReaderByBookId(long bookId) {
    var selectByBookIdSql =
        "SELECT reader.id, reader.name FROM reader INNER JOIN book ON reader.id = book.reader_id WHERE book.id = ?";
    try (var connection = DBUtil.getConnection();
        var selectReaderByBookStatement = connection.prepareStatement(selectByBookIdSql)) {
      selectReaderByBookStatement.setLong(1, bookId);
      var resultSet = selectReaderByBookStatement.executeQuery();
      if (resultSet.next()) {
        var reader = mapResultSetToReader(resultSet);
        return Optional.of(reader);
      } else {
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw new DaoOperationException(
          String.format("Error finding reader by book id: %d", bookId), e);
    }
  }
}
