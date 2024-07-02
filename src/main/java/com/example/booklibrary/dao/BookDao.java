package com.example.booklibrary.dao;

import com.example.booklibrary.entity.Book;
import com.example.booklibrary.entity.Reader;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
public interface BookDao {
  Book save(Book bookToSave);

  void returnBook(long bookId);

  Optional<Book> findById(long id);

  List<Book> findAll();

  void borrow(long bookId, long readerId);

  List<Book> findAllByReaderId(long readerId);

  Map<Book, Optional<Reader>> findAllWithReaders();
}
