package com.example.booklibrary.controllers;

import com.example.booklibrary.dto.BookWithReaderDto;
import com.example.booklibrary.entity.Book;
import com.example.booklibrary.entity.Reader;
import com.example.booklibrary.exception.ReaderNotFoundException;
import com.example.booklibrary.exception.SaveBookException;
import com.example.booklibrary.service.LibraryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1")
public class BookController {
  private final LibraryService libraryService;

  public BookController(LibraryService libraryService) {
    this.libraryService = libraryService;
  }

  @GetMapping("/books")
  public ResponseEntity<List<Book>> getBooks() {
    var books = libraryService.findAllBooks();
    return ResponseEntity.ok(books);
  }

  @PostMapping("/books")
  public ResponseEntity<Book> saveBook(@Valid @RequestBody Book book) {
    if (book.getId() != null){
      throw new SaveBookException("Request body should not contain book id value, only name and author!");
    }
    var savedBook = libraryService.addNewBook(book);
    return ResponseEntity.ok(savedBook);
  }

  @PostMapping("/books/{bookId}/readers/{readerId}")
  public ResponseEntity<Void> borrowBookToReader(
      @PathVariable("bookId") @NotNull @Positive(message = "Please use only positive bookId")
          Long bookId,
      @PathVariable("readerId") @NotNull @Positive(message = "Please use only positive readerId")
          Long readerId) {
    libraryService.borrowBook(bookId, readerId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/books/{bookId}")
  public ResponseEntity<Void> returnBook(@PathVariable("bookId") @NotNull @Positive Long bookId) {
    libraryService.returnBookToLibrary(bookId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/books/{bookId}/reader")
  public ResponseEntity<Reader> getReaderByBookId(
      @PathVariable("bookId") @NotNull @Positive Long bookId) {
    return libraryService
        .showCurrentReaderOfBook(bookId)
        .map(ResponseEntity::ok)
        .orElseThrow(
            () ->
                new ReaderNotFoundException(
                    String.format(
                        "Cannot find reader by book = %d id! Book has not any reader", bookId)));
  }

  @GetMapping("/books/readers")
  public ResponseEntity<List<BookWithReaderDto>> getBooksWithReaders() {
    var booksWithReaders = libraryService.findAllBooksWithReaders();
    return ResponseEntity.ok(booksWithReaders);
  }
}
