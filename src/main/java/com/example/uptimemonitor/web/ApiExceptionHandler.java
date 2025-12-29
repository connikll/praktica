package com.example.uptimemonitor.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(NoSuchElementException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorDto notFound(NoSuchElementException ex) {
    return new ErrorDto("NOT_FOUND", ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorDto badRequest(IllegalArgumentException ex) {
    return new ErrorDto("BAD_REQUEST", ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorDto validation(MethodArgumentNotValidException ex) {
    return new ErrorDto("VALIDATION_ERROR", ex.getMessage());
  }

  public record ErrorDto(String code, String message) {}
}
