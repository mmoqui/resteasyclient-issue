package org.silverpeas.issue.resteasyclient.json.util;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class BeanContainer {

  public static BeanContainer getInstance() {
    return new BeanContainer();
  }

  private BeanContainer() {
  }

  @SuppressWarnings("unchecked")
  public <T> T getBeanByName(final String name) throws IllegalStateException {
    BeanManager beanManager = CDI.current().getBeanManager();
    Bean<T> bean = (Bean<T>) beanManager.resolve(beanManager.getBeans(name));
    if (bean == null) {
      throw new IllegalStateException("Cannot find an instance of name " + name);
    }
    CreationalContext<T> ctx = beanManager.createCreationalContext(bean);
    Type type = bean.getTypes()
        .stream()
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "The bean " + name + " doesn't satisfy any managed type"));

    return (T) beanManager.getReference(bean, type, ctx);
  }

  @SuppressWarnings("unchecked")
  public <T> T getBeanByType(final Class<T> type, Annotation... qualifiers)
      throws IllegalStateException {
    BeanManager beanManager = CDI.current().getBeanManager();
    Bean<T> bean = (Bean<T>) beanManager.resolve(beanManager.getBeans(type, qualifiers));
    if (bean == null) {
      throw new IllegalStateException("Cannot find an instance of type " + type.getName());
    }
    CreationalContext<T> ctx = beanManager.createCreationalContext(bean);

    return (T) beanManager.getReference(bean, type, ctx);
  }

}
