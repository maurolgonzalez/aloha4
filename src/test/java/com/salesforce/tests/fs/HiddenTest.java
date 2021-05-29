package com.salesforce.tests.fs;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * The default test class to be executed always
 */
public class HiddenTest extends BaseTest {


    @Test
    public void testCd() throws IOException {
        String[] expectedResults = {
                "Directory not found\n"
        };
        runTest(expectedResults, "cd ..", "cd abc", "quit");
    }

    @Test
    public void testMkdirAndCd() throws IOException {
        String[] expectedResults = {
                "Directory already exists\n",
                "/root/abc\n"
        };
        runTest(expectedResults, "mkdir abc", "mkdir abc", "cd abc", "pwd", "quit");
    }

    @Test
    public void testLsSimple() {
        String[] expectedResults = {
                "/root\n",
                "file1\n",
        };
        runTest(expectedResults, "touch file1", "ls", "quit");
    }

    @Test
    public void testLsMultiLevel() {
        String[] expectedResults = {
                "/root\n",
                "root-file\n",
                "/root/sub1\n",
                "sub1-file\n",
                "/root/sub1/sub2\n",
                "sub2-file1\n",
                "sub2-file2\n",
                "/root/sub1/sub2/sub3\n",
                "/root/sub1/sub2/sub3/sub4\n",
                "sub4-file1\n",
                "sub4-file2\n",
                "sub4-file3\n"
        };
        runTest(expectedResults, "touch root-file", "mkdir sub1", "cd sub1", "touch sub1-file",
                "mkdir sub2", "cd sub2", "touch sub2-file1", "touch sub2-file2", "touch sub2-file2", "mkdir sub3",
                "cd sub3", "mkdir sub4", "cd sub4", "touch sub4-file1", "touch sub4-file2", "touch sub4-file3",
                "cd ..", "cd ..", "cd ..", "cd .."/*extra cd on root should do nothing*/,
                "ls -r", "quit");
    }


    @Test
    public void testCommandErrors() {
        String[] expectedResults = {
                "Unrecognized command\n",
                "Invalid Command\n",
                "Invalid Command\n",
                "Invalid Command\n"
        };
        runTest(expectedResults, "vi abc", "ls efg", "cd", "quit dummy", "quit");
    }

    @Test
    public void testPathNameTooLong() {
        String[] expectedResults = {
                "Invalid File or Folder Name\n",
                "Invalid File or Folder Name\n"
        };
        String longName = makeLongName();
        runTest(expectedResults, "mkdir " + longName, "touch " + longName, "quit");
    }

    private String makeLongName() {
        StringBuilder sb = new StringBuilder("name");
        for (int i = 0; i < 100; i++) {
            sb.append("0");
        }

        return sb.toString();
    }

    @Test
    public void testIfUnitTestsAdded() throws ClassNotFoundException {
        String className = YourUnitTest.class.getName();
        Class c = Class.forName(className);

        for (Method method : c.getDeclaredMethods()) {
            Test testAnno = method.getAnnotation(Test.class);
            if (testAnno != null) {
                return;
            }
        }

        Assert.fail("You did not add any unit tests in " + className);
    }


}
