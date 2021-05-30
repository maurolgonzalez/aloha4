package com.salesforce.tests.fs;

import org.junit.Test;

/**
 * Place holder for your unit tests
 */
public class YourUnitTest extends BaseTest{
    @Test
    public void myTest() {
        String[] expectedResults = {
                "myfile1\n",
                "myfile2\n",
                "/root\n",
                "/root/sub1\n",
                "myfile1\n",
                "myfile2\n",
        };
        runTest(expectedResults,    "mkdir sub1",
                                    "cd sub1",
                                    "ls",
                                    "touch myfile1",                                    
                                    "touch myfile2",
                                    "ls",
                                    "cd ..",
                                    "ls -r",
                                    "quit");
    }
}
