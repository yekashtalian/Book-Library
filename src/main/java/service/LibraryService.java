package service;

import dao.BookDao;
import dao.BookDaoImpl;
import dao.ReaderDao;
import dao.ReaderDaoImpl;
import entity.Book;
import entity.Reader;
import exception.LibraryServiceException;

import java.util.List;
import java.util.Optional;

public class LibraryService {
  private static final String BOOK_NOT_FOUND = "This Book ID doesn't exist!";
  private static final String READER_NOT_FOUND = "This Reader ID doesn't exist!";
  private final Validator validator = new Validator();
  private final BookDao bookDao = new BookDaoImpl();
  private final ReaderDao readerDao = new ReaderDaoImpl();

  public List<Book> findAllBooks() {
    return bookDao.findAll();
  }

  public List<Reader> findAllReader() {
    return readerDao.findAll();
  }

  public Optional<Reader> showCurrentReaderOfBook(String bookIdToCheck) {
    validator.validateSingleId(bookIdToCheck);

    var bookId = Long.parseLong(bookIdToCheck.trim());
    bookDao.findById(bookId).orElseThrow(() -> new LibraryServiceException(BOOK_NOT_FOUND));

    return readerDao.findReaderByBookId(bookId);
  }

  public List<Book> showBorrowedBooks(String readerIdToCheck) {
    validator.validateSingleId(readerIdToCheck);

    var readerId = Long.parseLong(readerIdToCheck.trim());
    readerDao.findById(readerId).orElseThrow(() -> new LibraryServiceException(READER_NOT_FOUND));

    return bookDao.findAllByReaderId(readerId);
  }

  public void addNewReader(String readerName) {
    validator.validateName(readerName);
    readerDao.save(new Reader(readerName));
  }

  public void addNewBook(String book) {
    validator.validateNewBookInputFormat(book);

    String[] bookAndAuthor = book.split("/");
    var bookTitle = bookAndAuthor[0].trim();
    var authorName = bookAndAuthor[1].trim();

    validator.validateBookTitle(bookTitle);
    validator.validateName(authorName);
    bookDao.save(new Book(bookTitle, authorName));
  }

  public void borrowBook(String bookIdAndReaderId) {
    validator.validateIdToBorrowBook(bookIdAndReaderId);

    String[] ids = bookIdAndReaderId.split("/");
    var bookId = Long.parseLong(ids[0].trim());
    var readerId = Long.parseLong(ids[1].trim());
    readerDao.findById(readerId).orElseThrow(() -> new LibraryServiceException(READER_NOT_FOUND));
    readerDao.findReaderByBookId(bookId).ifPresent(reader -> {
      throw new LibraryServiceException("Cannot borrow already borrowed Book!");
    });

    bookDao.borrow(bookId, readerId);
  }

  public void returnBookToLibrary(String bookIdToReturn) {
    validator.validateSingleId(bookIdToReturn);

    var bookId = Long.parseLong(bookIdToReturn.trim());
    if (readerDao.findReaderByBookId(bookId).equals(Optional.empty())) {
      throw new LibraryServiceException("Cannot return Book. Book is already in the Library!");
    }

    bookDao.returnBook(bookId);
  }
}
