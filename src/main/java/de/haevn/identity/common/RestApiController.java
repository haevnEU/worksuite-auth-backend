package de.haevn.identity.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Custom meta-annotation combining {@link RestController} and {@link RequestMapping}.
 *
 * <p>Conveniently declares a Spring REST controller bean while configuring the base
 * request path mapping via an alias.
 */
@Documented
@Component
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@RestController
@RequestMapping
public @interface RestApiController {

    /**
     * The primary path mapping URI for the annotated REST controller.
     *
     * <p>Acts as an alias for {@link RequestMapping#value()}.
     *
     * @return the array of base URL path mappings
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "value") String[] value();
}