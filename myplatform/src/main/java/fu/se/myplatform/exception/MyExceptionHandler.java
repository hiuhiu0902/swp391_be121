package fu.se.myplatform.exception;

import fu.se.myplatform.exception.exception.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MyExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity handleAuthenticationException(AuthenticationException e) {
        return new ResponseEntity(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleBadRequestException(MethodArgumentNotValidException exception){
        System.out.println("Người dùng nhập chưa đúng thông tin");
        String responseMessage = "";

        for(FieldError fieldError: exception.getFieldErrors()){
            responseMessage += fieldError.getDefaultMessage() + "\n";
        }

        return new ResponseEntity(responseMessage, HttpStatus.BAD_REQUEST);
    }
}
