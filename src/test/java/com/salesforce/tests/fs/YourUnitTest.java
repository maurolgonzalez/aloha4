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

    @Test
    public void multiFacetedDirs() {
        String[] expectedResults = {
                "/root/sub1/sub2\n",    // pwd
                
                "Directory not found\n",// cd sub1/sub/sub2
                
                "/root\n",             // ls
                "sub1\n",
                
                "sub1-file1\n",         // ls sub1
                "sub2\n",
                
                "sub2-file1\n",        // ls sub1/sub2 TODO: Fail
                
                "/root/sub1\n",         // ls -r sub1
                "sub1-file1\n",
                "/root/sub1/sub2\n",
                "sub2-file1\n",

                "/root\n",              // ls -r
                "/root/sub1\n",
                "sub1-file1\n",
                "/root/sub1/sub2\n",
                "sub2-file1\n",

                "/root\n",              // pwd
        };
        runTest(expectedResults,    "mkdir sub1",
                                    "cd sub1",
                                    "touch sub1-file1",
                                    "mkdir sub2",
                                    "cd ..", 
                                    "cd sub1/sub2",
                                    "pwd",
                                    "touch sub2-file1",
                                    "cd ..", 
                                    "cd ..", 
                                    "cd sub1/sub/sub2",
                                    "ls",
                                    "ls sub1",
                                    "ls sub1/sub2",
                                    "ls -r sub1",
                                    "ls -r",
                                    "pwd",
                                    "quit");
    }
}
