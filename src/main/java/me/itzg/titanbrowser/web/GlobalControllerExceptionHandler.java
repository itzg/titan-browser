package me.itzg.titanbrowser.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ResponseStatus(value= HttpStatus.BAD_REQUEST, reason = "Missing/malformed parameter was provided.")
    @ExceptionHandler(IllegalArgumentException.class)
    public void handleIllegalArg(IllegalArgumentException e) {
        // nothing specific needed
    }
}
