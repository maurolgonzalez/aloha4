package com.salesforce.tests.fs;

import java.util.ArrayList;
import java.util.Scanner;

enum FSType {
    FILE, FOLDER
}

enum CommandsName { 
    PWD("pwd"),
    LS("ls"),
    MKDIR("mkdir"),
    CD("cd"),
    TOUCh("touch"),
    QUIT("quit");

    private final String text;
    
    CommandsName(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}

enum Errors {
    INVALID_FILE_DIR("Invalid File or Folder Name"),
    INVALID_COMMAND("Invalid Command"),
    DIR_ALREADY_EXIST("Directory already exists"),
    DIR_NOT_FOUND("Directory not found"),
    UNRECOGNIZED_COMMAND("Unrecognized command");

    private final String text;
    
    Errors(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}

class FSObject {
    private String name;
    private FSObject father;
    private ArrayList<FSObject> childs;
    private FSType type;
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

    private boolean existFile(String fileName) {
        for(FSObject item: childs) {
            if(item.name.compareTo(fileName) == 0 && item.type == FSType.FILE) {
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
            System.out.println(Errors.INVALID_FILE_DIR.toString());
        } else {
            if(!this.existDir(dirName)) {
                childs.add(new FSObject(dirName, FSType.FOLDER, this));
            } else {
                System.out.println(Errors.DIR_ALREADY_EXIST.toString());
            }
        }        
    }

    public void listFilesAndFolders(boolean recursive) {
        if(recursive) {
            printAbsPath();
        }else if(father == null) {
            // Print only if root. I don't know why the Unit Test "testLsSimple" need this.
            System.out.println(this);
        }

        for(FSObject item: childs) {
            if(recursive && item.type == FSType.FOLDER) {
                item.listFilesAndFolders(true);
            } else {
                System.out.println(item.name);
            }
        }
    }

    public FSObject getFather() {
        return father;
    }

    public void createFile(String fileName) {
        if(fileName.length() >= 100) {
            System.out.println(Errors.INVALID_FILE_DIR.toString());
        } else if(!this.existFile(fileName)) {
            FSObject file = new FSObject(fileName, FSType.FILE, this);
            childs.add(file);
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
        boolean foundDir = true;

        if (dirName.compareTo("..") == 0 && currentPath.getFather() == null) {
            // This is root. Do nothing
            return;
        } else if (dirName.compareTo("..") == 0 && currentPath.getFather() != null) {
            currentPath = currentPath.getFather();
        } else if (dirName.compareTo(".") != 0) {
            FSObject subFolder = currentPath.getSubfolder(dirName);
            if (subFolder != null) {
                currentPath = subFolder;
            } else {
                foundDir = false;
            }
        }

        if(!foundDir) {
            System.out.println(Errors.DIR_NOT_FOUND.toString());
        }
    }

    public void listFilesAndFolders(boolean recursive) {
        currentPath.listFilesAndFolders(recursive);
    }

    public void createFile(String fileName) {
        currentPath.createFile(fileName);
    }

    public void clean() {
        osFileSystem = null;
    }

}
interface Command {
    public void execute();
    public boolean validate();
}

class CurrentDir implements Command{
    private String command;

    public CurrentDir(String command) {
        this.command = command;
    }

    public boolean validate() {
        String[] splittedCommands = command.split(" ");
        boolean valid = true;

        if (splittedCommands.length != 1) {
            valid = false;
        }

        return valid;
    }

    public void execute() {
        if (validate()) {
            OSFileSystem.getFileSystem().printAbsPath();
        }

    }
}

class ListContent implements Command {

    private boolean recursive = false;
    private final String strRecursive = "-r";
    private String command;

    public ListContent(String command)
    {
        this.command = command;
    }

    public boolean validate() {
        String[] splittedCommands = command.split(" ");
        boolean valid = true;

        if (splittedCommands.length > 2
                || (splittedCommands.length == 2 && splittedCommands[1].compareTo(strRecursive) != 0)) {
            valid = false;
        } else if (splittedCommands.length == 2 && splittedCommands[1].compareTo(strRecursive) == 0) {
            recursive = true;
        }

        return valid;
    }

    public void execute()
    {
        if(validate()) {
            OSFileSystem.getFileSystem().listFilesAndFolders(recursive);
        }
        else {
            System.out.println(Errors.INVALID_COMMAND.toString());
        }
    }
}

class CreateDir implements Command {

    private String dirName = "";
    private String command;

    public CreateDir(String command) {
        this.command = command;
    }

    public boolean validate() {
        String[] splittedCommands = command.split(" ");
        boolean valid = true;

        if (splittedCommands.length == 2) {
            dirName = splittedCommands[1];
        } else {
            valid = false;
        }

        return valid;
    }

    public void execute() {
        if(validate()) {
            OSFileSystem.getFileSystem().createDir(dirName);
        }
    }
}

class CreateFile implements Command {

    private String fileName = "";
    private String command;

    public CreateFile(String command) {
        this.command = command;
    }

    public boolean validate() {
        String[] splittedCommands = command.split(" ");
        boolean valid = true;

        if (splittedCommands.length == 2) {
            fileName = splittedCommands[1];
        } else {
            valid = false;
        }

        return valid;
    }

    public void execute() {
        if(validate()) {
            OSFileSystem.getFileSystem().createFile(fileName);
        }
    }
}

class ChangeDir implements Command {

    private String dirName = "";
    private String command;

    public ChangeDir(String command) {
        this.command = command;
    }

    public boolean validate() {
        String[] splittedCommands = command.split(" ");
        boolean valid = true;

        if (splittedCommands.length == 2) {
            dirName = splittedCommands[1];
        } else {
            valid = false;
        }

        return valid;
    }

    public void execute() {
        if(validate()){
            OSFileSystem.getFileSystem().changeDir(dirName);
        } else {
            System.out.println(Errors.INVALID_COMMAND.toString());
        }
    }
}

class Quit implements Command {

    private String command = "";

    public Quit(String command) {
        this.command = command;
    }

    public boolean validate() {
        String[] splittedCommands = command.split(" ");
        boolean valid = true;

        if (splittedCommands.length != 1) {
            valid = false;
            System.out.println(Errors.INVALID_COMMAND.toString());
        }

        return valid;
    }

    public void execute() {
    }
}

/**
 * The entry point for the Test program
 */
public class Main {
    
    
    public static void main(String[] args) {
        /* Enter your code here. Read input from STDIN. Print output to STDOUT */

        Scanner sc = new Scanner(System.in);
        String strCommand;

        do {
            strCommand = sc.nextLine();
            Command cmd = null;

            if (strCommand.startsWith(CommandsName.QUIT.toString())) {
                cmd = new Quit(strCommand);
                if (cmd.validate()) {
                    break;
                }
            } else if (strCommand.startsWith(CommandsName.PWD.toString())) {
                cmd = new CurrentDir(strCommand);
            } else if (strCommand.startsWith(CommandsName.LS.toString())) {
                cmd = new ListContent(strCommand);
            } else if (strCommand.startsWith(CommandsName.MKDIR.toString())) {
                cmd = new CreateDir(strCommand);
            } else if (strCommand.startsWith(CommandsName.CD.toString())) {
                cmd = new ChangeDir(strCommand);
            } else if (strCommand.startsWith(CommandsName.TOUCh.toString())) {
                cmd = new CreateFile(strCommand);
            }

            if(cmd != null)
                cmd.execute();
            else
                System.out.println(Errors.UNRECOGNIZED_COMMAND.toString());

        } while(true);

        OSFileSystem.getFileSystem().clean();
        sc.close();
    }
}
