package org.silverpeas.issue.resteasyclient.json.test;

import org.silverpeas.issue.resteasyclient.json.util.BeanContainer;
import org.silverpeas.issue.resteasyclient.json.util.Provider;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.sql.DataSource;

@Provider
public class DataSourceProvider {

  @Resource(lookup = "java:jboss/datasources/ExampleDS")
  private DataSource dataSource;

  private static DataSourceProvider getInstance() {
    return BeanContainer.getInstance().getBeanByType(DataSourceProvider.class);
  }

  @Produces
  public static DataSource getDataSource() {
    return getInstance().dataSource;
  }
}
