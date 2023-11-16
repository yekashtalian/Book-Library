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
  private static final String SELECT_ALL_SQL = "SELECT id, name FROM reader";
  private static final String INSERT_SQL = "INSERT INTO reader(name) VALUES(?)";
  private static final String SELECT_BY_ID_SQL = "SELECT id, name FROM reader WHERE id = ?";
  private static final String SELECT_BY_BOOK_ID_SQL = "SELECT reader.id, reader.name FROM reader INNER JOIN book ON reader.id = book.reader_id WHERE book.id = ?";

  @Override
  public Reader save(Reader readerToSave) {
      Objects.requireNonNull(readerToSave);
    try(var connection = DBUtil.getConnection();
        var updateStatement = connection.prepareStatement(INSERT_SQL, PreparedStatement.RETURN_GENERATED_KEYS)
    ){
      updateStatement.setString(1, readerToSave.getName());
      updateStatement.executeUpdate();
      var generatedId = updateStatement.getGeneratedKeys();
      if(generatedId.next()){
        if (generatedId.getLong(1) == 0){
          throw new DaoOperationException("Book ID cannot be 0");
        }
        readerToSave.setId(generatedId.getLong(1));
      }else {
        throw new DaoOperationException("Cannot obtain reader ID");
      }
      return readerToSave;
    }catch (SQLException e){
      throw new DaoOperationException(String.format("Error saving reader: %s", readerToSave), e);
    }
  }

  @Override
  public Optional<Reader> findById(long readerId) {
    try(var connection = DBUtil.getConnection();
        var selectByIdStatement = connection.prepareStatement(SELECT_BY_ID_SQL)
    ){
      selectByIdStatement.setLong(1, readerId);
      var resultSet = selectByIdStatement.executeQuery();
      if (resultSet.next()){
        var reader = parseRow(resultSet);
        return Optional.of(reader);
      }else {
        return Optional.empty();
      }
    }catch (SQLException e){
      throw new DaoOperationException(String.format("Error finding reader with id: %d", readerId), e);
    }
  }

  private Reader parseRow(ResultSet resultSet) {
    try{
      var reader = new Reader();
      reader.setId(resultSet.getLong("id"));
      reader.setName(resultSet.getString("name"));
      return reader;
    }catch (SQLException e){
      throw new DaoOperationException("Cannot parse row to create reader instance", e);
    }
  }

  @Override
  public List<Reader> findAll() {
    try(var connection = DBUtil.getConnection();
        var selectAllStatement = connection.createStatement()
    ){
      var resultSet = selectAllStatement.executeQuery(SELECT_ALL_SQL);
      return collectToList(resultSet);
    }catch (SQLException e){
      throw new DaoOperationException("Error finding all readers", e);
    }
  }
  private List<Reader> collectToList(ResultSet resultSet) throws SQLException {
    List<Reader> readers = new ArrayList<>();
    while(resultSet.next()){
      var reader = parseRow(resultSet);
      readers.add(reader);
    }
    return readers;
  }

  @Override
  public Optional<Reader> findReaderByBookId(long bookId) {
    try(var connection = DBUtil.getConnection();
        var selectReaderByBookStatement = connection.prepareStatement(SELECT_BY_BOOK_ID_SQL)
    ){
      selectReaderByBookStatement.setLong(1, bookId);
      var resultSet = selectReaderByBookStatement.executeQuery();
      if (resultSet.next()){
        var reader = parseRow(resultSet);
        return Optional.of(reader);
      }else {
        return Optional.empty();
      }
    }catch (SQLException e){
      throw new DaoOperationException(String.format("Error finding reader by book id: %d", bookId), e);
    }
  }
}
