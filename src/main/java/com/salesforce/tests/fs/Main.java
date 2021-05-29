package com.salesforce.tests.fs;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;


interface Command {
    public void execute();
}

class CurrentDir implements Command{

    public void execute()
    {
        Path path = Paths.get(".");
        System.out.println(path.toAbsolutePath());
    }
}

class ListContent implements Command {

    private boolean recursive = false;

    public ListContent(String command)
    {
        String[] splittedCommands = command.split(" ");
        if(splittedCommands.length == 2 && splittedCommands[1].compareTo("-r") == 0)        
        {
            recursive = true;
        }
    }

    public void execute()
    {        
        listFiles(".");
    }

    private void listFiles(String path)
    {
        File files = new File(path);
        File[] fileList = files.listFiles();

        if(recursive)
        {
            Path current_full_path = Paths.get(path);
            System.out.println(current_full_path.toAbsolutePath());
        }
        
        for (File current_file: fileList)
        {
            System.out.println(current_file);
            if(recursive && current_file.isDirectory())
            {
                listFiles(current_file.toString());
            }
        }
        
    }
}

class CreateDir implements Command{

    private String dirName = "";

    public CreateDir(String command)
    {
        String[] splittedCommands = command.split(" ");
        if(splittedCommands.length == 2)        
        {
            dirName = splittedCommands[1];
        }
    }

    public void execute()
    {
        File dir = new File(dirName);
        dir.mkdir();
    }
}

/**
 * The entry point for the Test program
 */
public class Main {

    public static void main(String[] args) {
        /* Enter your code here. Read input from STDIN. Print output to STDOUT */

        Scanner sc = new Scanner(System.in);
        String command;

        do {
            command = sc.nextLine();
            Command cmd = null;

            if(command.compareTo("quit") == 0)
            {
                break;
            } else if(command.compareTo("pwd") == 0)
            {
                cmd = new CurrentDir();
            }                
            else if(command.startsWith("ls"))
            {
                cmd = new ListContent(command);
            } else if(command.startsWith("mkdir"))
            {
                cmd = new CreateDir(command);
            } 

            if(cmd != null)
                cmd.execute();
            else
                System.out.println("Unrecognized command");

        } while(true);

        sc.close();
    }
}
