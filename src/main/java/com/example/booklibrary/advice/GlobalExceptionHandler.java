package com.example.booklibrary.advice;

import com.example.booklibrary.exception.*;
import com.example.booklibrary.util.ErrorDetail;
import com.example.booklibrary.util.ErrorResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.yaml.snakeyaml.reader.ReaderException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(value = MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleInvalidArguments(MethodArgumentNotValidException ex) {
    List<ErrorDetail> errors =
        ex.getBindingResult().getFieldErrors().stream().map(ErrorDetail::new).toList();

    var errorResponse =
        new ErrorResponse(
            LocalDateTime.now(),
            "Failed to create a new book, the request contains invalid fields",
            errors);

    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ExceptionHandler({
    BookNotFoundException.class,
    ReaderNotFoundException.class,
    BookNotBorrowedException.class,
    DaoOperationException.class,
    LibraryServiceException.class,
    SaveBookException.class
  })
  public ResponseEntity<ErrorResponse> handleBadRequestSituations(RuntimeException ex) {
    var errorResponse = new ErrorResponse(LocalDateTime.now(), ex.getMessage());
    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handlePathVariablesInvalidArguments(
      ConstraintViolationException ex) {
    var errorResponse =
        new ErrorResponse(
            LocalDateTime.now(),
            ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.joining(", ")));
    return ResponseEntity.badRequest().body(errorResponse);
  }
}
