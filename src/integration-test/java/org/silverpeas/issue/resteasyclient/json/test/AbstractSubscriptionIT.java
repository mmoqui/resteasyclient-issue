package org.silverpeas.issue.resteasyclient.json.test;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Rule;
import org.silverpeas.issue.resteasyclient.json.model.SubscriptionPersistenceIT;

import java.io.File;
import java.util.stream.Stream;

public abstract class AbstractSubscriptionIT {

  private static final String DB_CREATION_SCRIPT = "create-tables.sql";
  private static final String DB_DATASET_SCRIPT = "create-dataset.sql";

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom("/" + DB_CREATION_SCRIPT)
          .loadInitialDataSetFrom("/" + DB_DATASET_SCRIPT);

  protected static <T extends AbstractSubscriptionIT> WebArchive createBaseDeploymentFor(
      Class<T> testClass) {
    WebArchive war = ShrinkWrap.create(WebArchive.class, testClass.getSimpleName() + "-test.war");
    war.addAsResource("META-INF/MANIFEST.MF")
        .addAsResource("META-INF/persistence.xml")
        .addAsResource("META-INF/beans.xml")
        .setWebXML("web.xml")
        .addAsResource(DB_CREATION_SCRIPT)
        .addAsResource(DB_DATASET_SCRIPT)
        .addPackages(true, "org.silverpeas.issue.resteasyclient.json.test")
        .addPackages(true, "org.silverpeas.issue.resteasyclient.json.util")
        .addPackages(true, "org.silverpeas.issue.resteasyclient.json.model")
        .addPackages(true, "org.silverpeas.issue.resteasyclient.json.repository");

    File[] dependencies = Stream.of(Maven.resolver()
            .loadPomFromFile("pom.xml")
            .resolve("com.ninja-squad:DbSetup", "commons-io:commons-io")
            .withTransitivity()
            .asFile())
        .toArray(File[]::new);
    war.addAsLibraries(dependencies);

    return war;
  }
}
