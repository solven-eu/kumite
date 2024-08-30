package eu.solven.kumite.app.webflux;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties.Resources;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;

// https://docs.spring.io/spring-boot/reference/web/reactive.html#web.reactive.webflux.error-handling
// https://router.vuejs.org/guide/essentials/history-mode#HTML5-Mode

public class KumiteErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {

	// https://stackoverflow.com/questions/29740078/how-to-call-super-constructor-in-lombok
	// There is no lombok way to generator this constructor
	public KumiteErrorWebExceptionHandler(ErrorAttributes errorAttributes,
			Resources resources,
			ErrorProperties errorProperties,
			ApplicationContext applicationContext) {
		super(errorAttributes, resources, errorProperties, applicationContext);
	}

}
