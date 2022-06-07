package org.silverpeas.issue.resteasyclient.json.test;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;


public class DbSetupRule implements TestRule {

  private final List<Connection> safeConnectionPool = new ArrayList<>();

  private final String[] sqlTableScripts;
  private String[] sqlInsertScripts = null;
  private final DbSetupTracker dbSetupTracker = new DbSetupTracker();
  private Operation tableCreation = null;
  private Operation dataSetLoading = Operations.sql("");

  public static DbSetupRule createTablesFrom(String... sqlScripts) {
    Objects.requireNonNull(sqlScripts);
    return new DbSetupRule(sqlScripts);
  }

  public DbSetupRule loadInitialDataSetFrom(String... sqlScripts) {
    sqlInsertScripts = sqlScripts;
    return this;
  }

  protected DbSetupRule(String... sqlScripts) {
    sqlTableScripts = new String[sqlScripts.length];
    System.arraycopy(sqlScripts, 0, sqlTableScripts, 0, sqlScripts.length);
  }

  @Override
  public final Statement apply(final Statement test, final Description description) {
    final DbSetupRule theRuleInstance = this;
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          me.set(theRuleInstance);
          setUpDataSource(description);
          try {
            performBefore(description);

            // The test
            test.evaluate();

          } finally {
            performAfter(description);
          }
        } finally {
          try {
            cleanUpDataSource(description);
          } finally {
            me.remove();
          }
        }
      }
    };
  }

  protected void performBefore(Description description) {
    // For now, this method is useful for extension rules.
  }

  protected void performAfter(Description description) {
    // For now, this method is useful for extension rules.
  }

  private void setUpDataSource(Description description) {
    if (tableCreation == null) {
      // Initialization on the first test
      tableCreation = loadOperationFromSqlScripts(description.getTestClass(), sqlTableScripts);
      if (sqlInsertScripts != null && sqlInsertScripts.length > 0) {
        dataSetLoading = Operations.sequenceOf(dataSetLoading,
            loadOperationFromSqlScripts(description.getTestClass(), sqlInsertScripts));
      }
    }

    Operation preparation = Operations.sequenceOf(tableCreation, dataSetLoading);
    DataSource dataSource = DataSourceProvider.getDataSource();
    DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), preparation);
    dbSetupTracker.launchIfNecessary(dbSetup);
    Logger.getLogger(this.getClass().getName())
        .info("Database structure loaded successfully with DbSetup framework.");

  }

  @SuppressWarnings("ConstantConditions")
  private Operation loadOperationFromSqlScripts(Class<?> classOfTest, String[] scripts) {
    List<Operation> statements = new ArrayList<>();
    if (scripts != null) {
      Stream.of(scripts)
          .filter(s -> FilenameUtils.getExtension(s).equalsIgnoreCase("sql"))
          .forEach(s -> {
            try (InputStream sqlScriptInput = classOfTest.getResourceAsStream(s)) {
              if (sqlScriptInput != null) {
                StringWriter sqlScriptContent = new StringWriter();
                IOUtils.copy(sqlScriptInput, sqlScriptContent, StandardCharsets.UTF_8);
                if (sqlScriptContent.toString() != null && !sqlScriptContent.toString().isEmpty()) {
                  String[] sql = sqlScriptContent.toString().split(";");
                  statements.add(Operations.sql(sql));
                }
              }
            } catch (IOException e) {
              Logger.getLogger(getClass().getSimpleName())
                  .log(Level.SEVERE, "Error while loading the SQL script {0}!", s);
            }
          });
    }
    return Operations.sequenceOf(statements);
  }

  private void cleanUpDataSource(Description description) {
    try {
      try (Connection connection = getSafeConnection();
           PreparedStatement statement = connection.prepareStatement("SHOW TABLES");
           ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          String tableName = rs.getString(1);
          if (!tableName.startsWith("QRTZ_")) {
            try (PreparedStatement dropStatement = connection.prepareStatement(
                "DROP  TABLE " + tableName)) {
              dropStatement.execute();
            }
          }
        }
        Logger.getLogger(this.getClass().getName())
            .info("Database structure dropped successfully" + ".");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } finally {
      closeConnectionsQuietly(description);
    }
  }

  protected Connection openSafeConnection() throws SQLException {
    try {
      DataSource dataSource = DataSourceProvider.getDataSource();
      Connection connection = dataSource.getConnection();
      safeConnectionPool.add(connection);
      Logger.getLogger(DbSetupRule.class.getName()).info("Get a new connection successfully.");
      return connection;
    } catch (SQLException e) {
      Logger.getLogger(DbSetupRule.class.getName())
          .log(Level.WARNING, "Get a new connection error...");
      throw e;
    }
  }

  private void closeConnectionsQuietly(Description description) {
    if (safeConnectionPool.isEmpty()) {
      Logger.getLogger(DbSetupRule.class.getName()).info("No database safe connection to close.");
      return;
    }
    Iterator<Connection> connectionIterator = safeConnectionPool.iterator();
    int total = safeConnectionPool.size();
    int nbAlreadyClosed = 0;
    int nbCloseErrors = 0;
    int nbCloseSuccessfully = 0;
    while (connectionIterator.hasNext()) {
      Connection connection = connectionIterator.next();
      try {
        if (!connection.isClosed()) {
          connection.close();
          nbCloseSuccessfully++;
        } else {
          nbAlreadyClosed++;
        }
      } catch (Exception e) {
        nbCloseErrors++;
        Logger.getLogger(DbSetupRule.class.getName())
            .log(Level.WARNING, "Close connection error...", e);
      }
      connectionIterator.remove();
    }
    Logger.getLogger(DbSetupRule.class.getName()).log(Level.INFO,
        "# Quiet database connection close report #\n" +
            "On {0} opened safe {0,choice, 1#connection| 1<connections }:\n" +
            " - {1} closed successfully (could be the user of getActualDataSet or something wrong),\n" +
            " - {2} already closed,\n" +
            " - {3} closed in error",
        new Object[]{total, nbCloseSuccessfully, nbAlreadyClosed, nbCloseErrors});
    final String reason = nbCloseSuccessfully + " connection(s) not closed, please review the test performed by: " +
        description.getTestClass() + "#" + description.getMethodName();
    assertThat(reason, nbCloseSuccessfully, lessThanOrEqualTo(0));
  }

  /*
  CURRENT ME
   */

  private static final ThreadLocal<DbSetupRule> me = new ThreadLocal<>();

  /**
   * Gets the current instance of the rule.
   * @return the instance of the rule.
   * @throws java.lang.IllegalStateException if no rule is currently instanced.
   */
  private static DbSetupRule getCurrentRuleInstance() {
    DbSetupRule theCurrentRuleInstance = me.get();
    if (theCurrentRuleInstance == null) {
      String message =
          "Calling getSafeConnection method requires that the test must use directly DbSetupRule " +
              "or extends DataSetTest.\n";
      message += "Maybe is the method called from a Thread instantiated from a Test method. " +
          "Please call instead getSafeConnectionFromDifferentThread method if it is the case.";
      Logger.getLogger(DbSetupRule.class.getName()).severe(message);
      throw new IllegalStateException(message);
    }
    return theCurrentRuleInstance;
  }

  /*
  TOOLS
   */

  /**
   * Gets a new connection to the database that will be closed automatically closed at the end of
   * test if it is not already done.
   * @return a connection to the database
   * @throws SQLException on SQL error
   */
  public static Connection getSafeConnection() throws SQLException {
    return getCurrentRuleInstance().openSafeConnection();
  }

}
