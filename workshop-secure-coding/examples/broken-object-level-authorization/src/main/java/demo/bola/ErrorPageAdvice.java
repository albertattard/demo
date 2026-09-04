package demo.bola;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ErrorPageAdvice {

    @ExceptionHandler(OrderNotFoundException.class)
    ModelAndView missingOrder() {
        final ModelAndView view = new ModelAndView("error");
        view.setStatus(HttpStatus.NOT_FOUND);
        view.addObject("status", HttpStatus.NOT_FOUND.value());
        return view;
    }
}
