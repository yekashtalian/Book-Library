package dao;

import entity.Reader;

import java.util.List;
import java.util.Optional;

public interface ReaderDao {
  Reader save(Reader readerToSave);

  Optional<Reader> findById(long id);

  List<Reader> findAll();
  Optional<Reader> findReaderByBookId(long bookId);
}
