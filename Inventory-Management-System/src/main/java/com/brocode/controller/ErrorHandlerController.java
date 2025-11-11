package com.brocode.controller;

import com.brocode.service.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

@ControllerAdvice
public class ErrorHandlerController {

//    @ExceptionHandler(NoSuchElementException.class)
//    public ResponseEntity<ErrorResponseDto> handleElementNotFoundError(NoSuchElementException ex){
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseDto(ex.getMessage(), 404));
//    }

}
