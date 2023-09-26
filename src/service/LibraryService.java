package service;

import dao.BookDao;
import dao.BookDaoImpl;
import dao.ReaderDao;
import dao.ReaderDaoImpl;
import entity.Book;
import entity.Reader;
import exception.InvalidBookTitleException;
import exception.InvalidIdException;
import exception.InvalidInputFormatException;
import exception.InvalidNameException;

import java.util.List;
import java.util.Optional;

public class LibraryService {
  private final static String BOOK_NOT_FOUND = "This Book ID doesn't exist!";
  private final static String READER_NOT_FOUND = "This Reader ID doesn't exist!";
  private final Validator validator = new Validator();
  private final BookDao bookDao = new BookDaoImpl();
  private final ReaderDao readerDao = new ReaderDaoImpl();

  public List<Book> findAllBooks() {
    return bookDao.findAll();
  }

  public List<Reader> findAllReader() {
    return readerDao.findAll();
  }


  public Optional<Reader> showCurrentReaderOfBook(String bookIdToCheck) throws InvalidIdException {
    validator.validateSingleId(bookIdToCheck);

    long bookId = Long.parseLong(bookIdToCheck);
    bookDao.findById(bookId).orElseThrow(() -> new InvalidIdException(BOOK_NOT_FOUND));

    long readerId = bookDao.findReaderIdByBookId(bookId);
    if (readerId == 0L){
      return Optional.empty();
    }
    readerDao.findById(readerId).orElseThrow(() -> new InvalidIdException(READER_NOT_FOUND));

    return readerDao.findById(readerId);
  }

  public List<Book> showBorrowedBooks(String readerIdToCheck) throws InvalidIdException {
    validator.validateSingleId(readerIdToCheck);

    long readerId = Long.parseLong(readerIdToCheck);
    readerDao.findById(readerId).orElseThrow(() -> new InvalidIdException(READER_NOT_FOUND));

    return bookDao.findAllByReaderId(readerId);
  }

  public void addNewReader(String readerName) throws InvalidNameException {
    validator.validateName(readerName);
    readerDao.save(new Reader(readerName));
  }

  public void addNewBook(String book)
      throws InvalidBookTitleException, InvalidNameException, InvalidInputFormatException {
    validator.validateNewBookInputFormat(book);

    String[] bookAndAuthor = book.split("/");
    String bookTitle = bookAndAuthor[0];
    String authorName = bookAndAuthor[1];

    validator.validateBookTitle(bookTitle);
    validator.validateName(authorName);
    bookDao.save(new Book(bookTitle, authorName, 0));
  }

  public void borrowBook(String bookIdAndReaderId)
      throws InvalidInputFormatException, InvalidIdException {
    validator.validateIdToBorrowBook(bookIdAndReaderId);

    String[] ids = bookIdAndReaderId.split("/");

    long bookId = Long.parseLong(ids[0]);
    bookDao.findById(bookId).orElseThrow(()-> new InvalidIdException(BOOK_NOT_FOUND));

    long readerId = Long.parseLong(ids[1]);
    readerDao.findById(readerId).orElseThrow(() -> new InvalidIdException(READER_NOT_FOUND));

    if (bookDao.findReaderIdByBookId(readerId) != 0){
      throw new InvalidIdException("Cannot borrow already borrowed Book!");
    }
    bookDao.borrowBook(bookId, readerId);
  }

  public void returnBookToLibrary(String bookIdToReturn) throws InvalidIdException {
    validator.validateSingleId(bookIdToReturn);

    long bookId = Long.parseLong(bookIdToReturn);
    Book bookToReturn =
        bookDao.findById(bookId).orElseThrow(() -> new InvalidIdException(BOOK_NOT_FOUND));

    if (bookToReturn.getReaderId() == 0) {
      throw new InvalidIdException("Cannot return Book. Book is already in the Library!");
    }

    bookDao.returnBookToLibrary(bookId);
  }
}