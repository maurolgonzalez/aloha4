package com.salesforce.tests.fs;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

enum FSType {
    FILE, FOLDER
}

class FSObject {
    String name;
    FSObject father;
    ArrayList<FSObject> childs;
    FSType type;
    private final String SEPARATOR = "/";

    public FSObject(String name, FSType type, FSObject father) {
        this.name = name;
        this.type = type;
        this.father = father;
        this.childs = new ArrayList<FSObject>();
    }

    @Override
    public String toString() {
        return SEPARATOR + name;
    }

    public String getAbsPath() {
        if(father == null) {
            return this.toString();
        }
        return father.getAbsPath() + this;
    }

    public void printAbsPath() {
        System.out.println(getAbsPath());
    }

    public boolean existDir(String dirName) {
        for(FSObject item: childs) {
            if(item.name.compareTo(dirName) == 0 && item.type == FSType.FOLDER) {
                return true;
            }
        }
        return false;
    }

    public FSObject getSubfolder(String dirName) {
        for(FSObject item: childs) {
            if(item.name.compareTo(dirName) == 0 && item.type == FSType.FOLDER) {
                return item;
            }
        }

        return null;
    }


    public boolean equals(FSObject otherNode) {
        return this.name == otherNode.name 
            && this.type == otherNode.type 
            && this.father == otherNode.father;
    }

    public void createDir(String dirName) {
        if(dirName.length() >= 100) {
            System.out.println("Invalid File or Folder Name");
        } else {
            if(!this.existDir(dirName)) {
                childs.add(new FSObject(dirName, FSType.FOLDER, this));
            } else {
                System.out.println("Directory already exists");
            }
        }        
    }
}

class OSFileSystem {
    private static OSFileSystem osFileSystem;
    private FSObject root;
    private FSObject currentPath;

    public static OSFileSystem getFileSystem() {
        if (osFileSystem == null) {
            osFileSystem = new OSFileSystem();
        }

        return osFileSystem;
    }

    private OSFileSystem() {
        this.root = new FSObject("root", FSType.FOLDER, null);
        currentPath = root;
    }

    public void printAbsPath() {
        currentPath.printAbsPath();
    }

    public void createDir(String dirName) {
        currentPath.createDir(dirName);
    }

    public void changeDir(String dirName) {
        FSObject subFolder = currentPath.getSubfolder(dirName);
        if(subFolder == null) {
            System.out.println("Directory not found");
        } else {
            currentPath = subFolder;
        }
    }

}
interface Command {
    public void execute();
}

class CurrentDir implements Command{

    public void execute()
    {
        OSFileSystem.getFileSystem().printAbsPath();
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
        OSFileSystem.getFileSystem().createDir(dirName);
    }
}

class ChangeDir implements Command{

    private String dirName = "";

    public ChangeDir(String command)
    {
        String[] splittedCommands = command.split(" ");
        if(splittedCommands.length == 2)
        {
            dirName = splittedCommands[1];
        }
    }

    public void execute() {

        OSFileSystem.getFileSystem().changeDir(dirName);        
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
            } else if(command.startsWith("cd"))
            {
                cmd = new ChangeDir(command);
            } 

            if(cmd != null)
                cmd.execute();
            else
                System.out.println("Unrecognized command");

        } while(true);

        sc.close();
    }
}
