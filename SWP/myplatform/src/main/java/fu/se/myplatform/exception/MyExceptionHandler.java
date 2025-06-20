package fu.se.myplatform.exception;

import fu.se.myplatform.exception.exception.AuthenticationException;
import fu.se.myplatform.service.LogEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class MyExceptionHandler {
    @Autowired
    private LogEventService logEventService;

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(AuthenticationException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleBadRequestException(MethodArgumentNotValidException exception){
        System.out.println("Người dùng nhập chưa đúng thông tin");
        StringBuilder responseMessage = new StringBuilder();
        for(FieldError fieldError: exception.getFieldErrors()){
            responseMessage.append(fieldError.getDefaultMessage()).append("\n");
        }
        return new ResponseEntity<>(responseMessage.toString(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e) {
        // Ghi log lỗi hệ thống
        logEventService.logError(e.getMessage(), e.toString());
        return new ResponseEntity<>("Đã xảy ra lỗi hệ thống!", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(MyException.class)
    public ResponseEntity<String> handleMyException(MyException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
