package com.example.booklibrary.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.booklibrary.dao.BookDao;
import com.example.booklibrary.dao.ReaderDao;
import com.example.booklibrary.dto.BookWithReaderDto;
import com.example.booklibrary.dto.ReaderWithBooksDto;
import com.example.booklibrary.entity.Book;
import com.example.booklibrary.entity.Reader;
import com.example.booklibrary.exception.LibraryServiceException;
import java.util.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LibraryServiceTest {
  private static final String BOOK_NOT_FOUND = "This Book ID doesn't exist!";
  private static final String READER_NOT_FOUND = "This Reader ID doesn't exist!";
  @Mock BookDao bookDao;
  @Mock ReaderDao readerDao;

  @InjectMocks LibraryService libraryService;

  @Test
  void findAllBooks() {
    List<Book> expectedBooks =
        List.of(new Book(1L, "X", "X"), new Book(2L, "Y", "Y"), new Book(3L, "Z", "Z"));

    when(bookDao.findAll()).thenReturn(expectedBooks);

    List<Book> actualBooks = libraryService.findAllBooks();

    verify(bookDao).findAll();
    assertThat(actualBooks).isEqualTo(expectedBooks);
  }

  @Test
  void findAllBooksIfListIsEmpty() {
    List<Book> expectedBooks = new ArrayList<>();
    when(bookDao.findAll()).thenReturn(expectedBooks);

    List<Book> actualBooks = libraryService.findAllBooks();

    assertThat(actualBooks).isNotNull();
    assertThat(actualBooks).isEmpty();
  }

  @Test
  void findAllReader() {
    List<Reader> expectedReaders =
        List.of(new Reader(1L, "X"), new Reader(2L, "Y"), new Reader(3L, "Z"));
    when(readerDao.findAll()).thenReturn(expectedReaders);

    List<Reader> actualReaders = libraryService.findAllReader();

    assertThat(actualReaders).isEqualTo(expectedReaders);
  }

  @Test
  void findAllReaderIfListIsEmpty() {
    List<Reader> expectedReaders = new ArrayList<>();
    when(readerDao.findAll()).thenReturn(expectedReaders);

    List<Reader> actualReaders = libraryService.findAllReader();

    assertThat(actualReaders).isNotNull();
    assertThat(actualReaders).isEmpty();
  }

  @Test
  void showCurrentReaderOfBook() {
    var bookId = 1L;
    var expectedReader = new Reader(1L, "Yevhenii");
    when(bookDao.findById(bookId)).thenReturn(Optional.of(new Book()));
    when(readerDao.findReaderByBookId(bookId)).thenReturn(Optional.of(expectedReader));

    Optional<Reader> actualReader = libraryService.showCurrentReaderOfBook(bookId);

    assertThat(actualReader).isPresent();
    assertThat(actualReader).get().isEqualTo(expectedReader);
  }

  @Test
  void showCurrentReaderOfBookIfBookIsNotFound() {
    var bookId = 99999L;
    when(bookDao.findById(bookId)).thenReturn(Optional.empty());

    var exception =
        assertThrows(
            LibraryServiceException.class, () -> libraryService.showCurrentReaderOfBook(bookId));

    assertThat(exception.getClass()).isEqualTo(LibraryServiceException.class);
    assertThat(exception.getMessage()).isEqualTo(BOOK_NOT_FOUND);
    verifyNoInteractions(readerDao);
  }

  @Test
  void showBorrowedBooks() {
    var readerId = 1L;
    List<Book> expectedBooks =
        List.of(new Book(1L, "X", "X"), new Book(2L, "Y", "Y"), new Book(3L, "Z", "Z"));
    when(readerDao.findById(readerId)).thenReturn(Optional.of(new Reader()));
    when(bookDao.findAllByReaderId(readerId)).thenReturn(expectedBooks);

    List<Book> actualBooks = libraryService.showBorrowedBooks(readerId);

    assertThat(actualBooks).isEqualTo(expectedBooks);
  }

  @Test
  void showBorrowedBooksIfReaderIsNotFound() {
    var readerId = 99999L;
    when(readerDao.findById(readerId)).thenReturn(Optional.empty());

    var exception =
        assertThrows(
            LibraryServiceException.class, () -> libraryService.showBorrowedBooks(readerId));

    assertThat(exception.getClass()).isEqualTo(LibraryServiceException.class);
    assertThat(exception.getMessage()).isEqualTo(READER_NOT_FOUND);
    verifyNoInteractions(bookDao);
  }

  @Test
  void addNewReader() {
    var reader = new Reader("Yevhenii");

    libraryService.addNewReader(reader);

    verify(readerDao).save(reader);
  }

  @Test
  void addNewBook() {
    var book = new Book("Martin Eden", "Jack London");

    libraryService.addNewBook(book);

    verify(bookDao).save(book);
  }

  @Test
  void borrowBook() {
    var bookId = 1L;
    var readerId = 1L;
    var book = new Book(bookId, "Martin Eden", "Jack London");
    var reader = new Reader(readerId, "Yevhenii");
    when(bookDao.findById(bookId)).thenReturn(Optional.of(book));
    when(readerDao.findById(readerId)).thenReturn(Optional.of(reader));
    when(readerDao.findReaderByBookId(bookId)).thenReturn(Optional.empty());

    libraryService.borrowBook(bookId, readerId);
    verify(bookDao).borrow(bookId, readerId);
  }

  @Test
  void borrowBookIfBookIsNotFound() {
    var bookId = 99999L;
    var readerId = 1L;
    when(bookDao.findById(bookId)).thenReturn(Optional.empty());

    var exception =
        assertThrows(
            LibraryServiceException.class, () -> libraryService.borrowBook(bookId, readerId));

    assertThat(exception.getClass()).isEqualTo(LibraryServiceException.class);
    assertThat(exception.getMessage()).isEqualTo(BOOK_NOT_FOUND);
    verifyNoInteractions(readerDao);
    verify(bookDao, times(0)).borrow(bookId, readerId);
  }

  @Test
  void borrowBookIfReaderIsNotFound() {
    var bookId = 1L;
    var readerId = 99999L;
    var book = new Book(bookId, "Martin Eden", "Jack London");
    when(bookDao.findById(bookId)).thenReturn(Optional.of(book));
    when(readerDao.findById(readerId)).thenReturn(Optional.empty());

    var exception =
        assertThrows(
            LibraryServiceException.class, () -> libraryService.borrowBook(bookId, readerId));

    assertThat(exception.getClass()).isEqualTo(LibraryServiceException.class);
    assertThat(exception.getMessage()).isEqualTo(READER_NOT_FOUND);
    verify(bookDao, times(0)).borrow(bookId, readerId);
  }

  @Test
  void borrowBookIfBookIsBorrowed() {
    var bookId = 1L;
    var readerId = 1L;
    var book = new Book(bookId, "Martin Eden", "Jack London", readerId);
    var reader = new Reader(readerId, "Yevhenii");
    when(bookDao.findById(bookId)).thenReturn(Optional.of(book));
    when(readerDao.findById(readerId)).thenReturn(Optional.of(reader));
    when(readerDao.findReaderByBookId(bookId)).thenReturn(Optional.of(reader));

    var exception =
        assertThrows(
            LibraryServiceException.class, () -> libraryService.borrowBook(bookId, readerId));

    assertThat(exception.getClass()).isEqualTo(LibraryServiceException.class);
    assertThat(exception.getMessage()).isEqualTo("Cannot borrow already borrowed Book!");
    verify(bookDao, times(0)).borrow(bookId, readerId);
  }

  @Test
  void findAllReadersWithBooks() {
    Map<Reader, List<Book>> expectedResult =
        Map.of(
            new Reader(1L, "X"),
                List.of(new Book(1L, "dummy", "dummy"), new Book(2L, "dummy1", "dummy1")),
            new Reader(2L, "Y"), List.of(new Book(3L, "dummy2", "dummy2")));
    when(readerDao.findAllWithBooks()).thenReturn(expectedResult);

    List<ReaderWithBooksDto> actualResult = libraryService.findAllReadersWithBooks();

    assertThat(actualResult).isNotEmpty();
    assertThat(actualResult.size()).isEqualTo(2);

    actualResult.forEach(
        dto -> {
          if (dto.getId() == 1L) {
            assertThat(dto.getName()).isEqualTo("X");
            assertThat(dto.getBooks().size()).isEqualTo(2);
          } else {
            assertThat(dto.getName()).isEqualTo("Y");
            assertThat(dto.getBooks().size()).isEqualTo(1);
          }
        });
  }

  @Test
  void findAllReadersWithBooksIfMapIsEmpty() {
    Map<Reader, List<Book>> expectedResult = new HashMap<>();
    when(readerDao.findAllWithBooks()).thenReturn(expectedResult);

    List<ReaderWithBooksDto> actualResult = libraryService.findAllReadersWithBooks();

    assertThat(actualResult).isNotNull();
    assertThat(actualResult).isEmpty();
  }

  @Test
  void findAllBooksWithReaders() {
    Map<Book, Optional<Reader>> expectedResult =
        Map.of(
            new Book(1L, "dummy1", "dummy2"),
            Optional.of(new Reader("dummy")),
            new Book(2L, "dummy3", "dummy4"),
            Optional.of(new Reader("dummy1")));
    when(bookDao.findAllWithReaders()).thenReturn(expectedResult);

    List<BookWithReaderDto> actualResult = libraryService.findAllBooksWithReaders();

    assertThat(actualResult).isNotEmpty();
    assertThat(actualResult.size()).isEqualTo(expectedResult.size());

    actualResult.forEach(
        dto -> {
          if (dto.getId() == 1L) {
            assertThat(dto.getName()).isEqualTo("dummy1");
            assertThat(dto.getAuthor()).isEqualTo("dummy2");
            assertThat(dto.getReader().getName()).isEqualTo("dummy");
          } else {
            assertThat(dto.getName()).isEqualTo("dummy3");
            assertThat(dto.getAuthor()).isEqualTo("dummy4");
            assertThat(dto.getReader().getName()).isEqualTo("dummy1");
          }
        });
  }

  @Test
  void findAllBooksWithReadersIfMapIsEmpty() {
    Map<Book, Optional<Reader>> expectedResult = new HashMap<>();
    when(bookDao.findAllWithReaders()).thenReturn(expectedResult);

    List<BookWithReaderDto> actualResult = libraryService.findAllBooksWithReaders();

    assertThat(actualResult).isNotNull();
    assertThat(actualResult).isEmpty();
  }

  @Test
  void returnBookToLibrary() {
    var bookId = 1L;
    var reader = new Reader(1L, "Yevhenii");
    var book = new Book(bookId, "Martin Eden", "Jack London", reader.getId());
    when(bookDao.findById(bookId)).thenReturn(Optional.of(book));
    when(readerDao.findReaderByBookId(bookId)).thenReturn(Optional.of(reader));

    libraryService.returnBookToLibrary(bookId);
    verify(bookDao).returnBook(bookId);
  }

  @Test
  void returnBookToLibraryIfBookDoesNotExists() {
    var bookId = 99999L;
    when(bookDao.findById(bookId)).thenReturn(Optional.empty());

    var exception =
        assertThrows(
            LibraryServiceException.class, () -> libraryService.returnBookToLibrary(bookId));

    assertThat(exception.getClass()).isEqualTo(LibraryServiceException.class);
    assertThat(exception.getMessage()).isEqualTo(BOOK_NOT_FOUND);
    verifyNoInteractions(readerDao);
    verify(bookDao, times(0)).returnBook(bookId);
  }

  @Test
  void returnBookToLibraryIfBookIsInLibrary() {
    var bookId = 1L;
    var book = new Book(bookId, "Martin Eden", "Jack London");
    when(bookDao.findById(bookId)).thenReturn(Optional.of(book));
    when(readerDao.findReaderByBookId(bookId)).thenReturn(Optional.empty());

    var exception =
        assertThrows(
            LibraryServiceException.class, () -> libraryService.returnBookToLibrary(bookId));

    assertThat(exception.getClass()).isEqualTo(LibraryServiceException.class);
    assertThat(exception.getMessage())
        .isEqualTo("Cannot return Book. Book is already in the Library!");
    verify(bookDao, times(0)).returnBook(bookId);
  }
}
