package ecom.ecomException.handler;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ecom.ecomException.exceptions.ObjectNotValidException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	
	@ExceptionHandler(ObjectNotValidException.class)
	public ResponseEntity<Set<String>> objectNotValidExceptionHandler(ObjectNotValidException exception) {
		return ResponseEntity.badRequest().body(exception.getErrorMessages());
	}
}
