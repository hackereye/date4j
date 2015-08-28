package hirondelle.date4j;

import junit.framework.Test;
import junit.framework.TestSuite;

/** Run all JUnit tests. */
public final class TestAll {

  public static void main(String args[]) {
    String[] testCaseName = { TestAll.class.getName()};
    junit.textui.TestRunner.main(testCaseName);
 }
  
  public static Test suite ( ) {
    TestSuite suite= new TestSuite("All JUnit Tests");

    suite.addTest(new TestSuite(TestDateTime.class));
    suite.addTest(new TestSuite(TestDateTimeFormatter.class));
    suite.addTest(new TestSuite(TestDateTimeInterval.class));
    
    return suite;
  }
}
