package org.silverpeas.issue.resteasyclient.json.web;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import static org.silverpeas.issue.resteasyclient.json.web.WebApplication.RS_BASE_PATH;

@ApplicationPath(RS_BASE_PATH)
public class WebApplication extends Application {

  public static final String RS_BASE_PATH = "/resources";
}
