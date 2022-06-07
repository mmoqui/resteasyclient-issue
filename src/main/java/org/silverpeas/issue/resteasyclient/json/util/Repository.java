package org.silverpeas.issue.resteasyclient.json.util;

import javax.enterprise.inject.Stereotype;
import javax.inject.Singleton;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Singleton
@Stereotype
public @interface Repository {
}
