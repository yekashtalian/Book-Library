package dao;

import entity.Book;

import java.util.List;
import java.util.Optional;

public interface BookDao {
  Book save(Book bookToSave);

  void returnBook(long bookId);

  Optional<Book> findById(long id);

  List<Book> findAll();

  void borrow(long bookId, long readerId);

  List<Book> findAllByReaderId(long readerId);
}
