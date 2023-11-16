package ui;

import entity.Book;
import entity.Reader;
import exception.LibraryServiceException;
import service.LibraryService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Menu {
  private static final String SET_GREEN_TEXT_COLOR = "\u001B[32m";
  private static final String SET_DEFAULT_TEXT_COLOR = "\u001B[0m";
  private static final String SEPARATOR =
      "-------------------------------------------------------------------";
  private final Scanner scanner = new Scanner(System.in);
  private final LibraryService libraryService = new LibraryService();

  public void displayMenu() {
    System.out.println(SEPARATOR);
    System.out.println("WELCOME TO THE LIBRARY!");
    while (true) {
      System.out.println(
          """
            PLEASE, SELECT ONE OF THE FOLLOWING ACTIONS BY TYPING THE OPTION’S NUMBER AND PRESSING ENTER KEY:
                [1] SHOW ALL BOOKS IN THE LIBRARY
                [2] SHOW ALL READERS REGISTERED IN THE LIBRARY
                [3] REGISTER NEW READER
                [4] ADD NEW BOOK
                [5] BORROW A BOOK TO A READER
                [6] RETURN A BOOK TO THE LIBRARY
                [7] SHOW ALL BORROWED BOOK BY USER ID
                [8] SHOW CURRENT READER OF A BOOK WITH ID
            TYPE “EXIT” TO STOP THE PROGRAM AND EXIT!
                    """);
      try {
        switch (scanner.nextLine().toLowerCase()) {
          case "1" -> showAllBooks();
          case "2" -> showAllReaders();
          case "3" -> addNewReader();
          case "4" -> addNewBook();
          case "5" -> borrowBook();
          case "6" -> returnBookToLibrary();
          case "7" -> showBorrowedBooksByReaderId();
          case "8" -> showCurrentReaderOfBookById();
          case "exit" -> exitFromMenu();
          default -> System.err.println(
              "Invalid option, please write correct option from the menu.");
        }
      } catch (LibraryServiceException ex) {
        System.err.println(ex.getMessage());
      }
      System.out.println(SEPARATOR);
    }
  }

  private void showAllBooks() {
    System.out.print(SET_GREEN_TEXT_COLOR);
    libraryService.findAllBooks().forEach(System.out::println);
    System.out.print(SET_DEFAULT_TEXT_COLOR);
  }

  private void showAllReaders() {
    System.out.print(SET_GREEN_TEXT_COLOR);
    libraryService.findAllReader().forEach(System.out::println);
    System.out.print(SET_DEFAULT_TEXT_COLOR);
  }

  private void addNewReader() throws LibraryServiceException {
    System.out.println("Please enter new reader full name!");

    var readerName = scanner.nextLine().trim();
    libraryService.addNewReader(readerName);

    System.out.println(
        SET_GREEN_TEXT_COLOR
            + "Reader "
            + readerName
            + "has successfully added!"
            + SET_DEFAULT_TEXT_COLOR);
  }

  private void addNewBook()
      throws LibraryServiceException {
    System.out.println("Please enter new book name and author separated by “/”");

    var book = scanner.nextLine().trim();
    libraryService.addNewBook(book);

    System.out.println(
        SET_GREEN_TEXT_COLOR
            + "Book "
            + book
            + " has successfully added!"
            + SET_DEFAULT_TEXT_COLOR);
  }

  private void borrowBook() throws LibraryServiceException {
    System.out.println("Please enter book ID and reader ID separated by “/” to borrow a book.");

    String bookIdAndReaderId = scanner.nextLine().trim();
    libraryService.borrowBook(bookIdAndReaderId);

    System.out.println(
        SET_GREEN_TEXT_COLOR
            + "Book has successfully borrowed by reader, with "
            + bookIdAndReaderId
            + " ID's respectively!"
            + SET_DEFAULT_TEXT_COLOR);
  }

  private void returnBookToLibrary() throws LibraryServiceException {
    System.out.println("Please enter book ID to return a book.");

    String bookToReturn = scanner.nextLine().trim();
    libraryService.returnBookToLibrary(bookToReturn);

    System.out.println(
        SET_GREEN_TEXT_COLOR
            + "Book with ID = "
            + bookToReturn
            + " has successfully returned to the library!"
            + SET_DEFAULT_TEXT_COLOR);
  }

  private void showBorrowedBooksByReaderId() throws LibraryServiceException {
    System.out.println("Please enter reader ID to show all his borrowed books");

    var readerId = scanner.nextLine().trim();
    List<Book> borrowedBooks = libraryService.showBorrowedBooks(readerId);

    if (borrowedBooks.isEmpty()) {
      System.err.println("There is no borrowed books by Reader ID = " + readerId);
    } else {
      System.out.println(
          SET_GREEN_TEXT_COLOR
              + "Borrowed books by reader ID"
              + readerId
              + " : "
              + libraryService.showBorrowedBooks(readerId)
              + SET_DEFAULT_TEXT_COLOR);
    }
  }

  private void showCurrentReaderOfBookById() throws LibraryServiceException {
    System.out.println("Please enter book ID to show all his readers");

    var bookId = scanner.nextLine().trim();
    Optional<Reader> reader = libraryService.showCurrentReaderOfBook(bookId);

    if (reader.isEmpty()) {
      System.err.println("Book is not borrowed yet!");
    } else {
      System.out.println(
          SET_GREEN_TEXT_COLOR
              + "Reader of Book ID "
              + bookId
              + " = "
              + reader.get()
              + SET_DEFAULT_TEXT_COLOR);
    }
  }

  private void exitFromMenu() {
    scanner.close();
    System.out.println(SET_GREEN_TEXT_COLOR);
    System.out.println("GOODBYE!");
    System.exit(0);
  }
}
