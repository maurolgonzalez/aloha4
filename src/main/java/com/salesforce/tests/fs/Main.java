package com.salesforce.tests.fs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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

class Logger {
    static public void log(String s) {
        System.out.println(s);
    }

    static public void log(Errors e) {
        System.out.println(e.toString());
    }

    private Logger() {}
}
/**
 * FSObject: Represent a file or folder into a File System
 */
class FSObject implements Serializable {
    private String name;
    private FSObject father;
    private ArrayList<FSObject> childs;
    private FSType type;
    public static final String SEPARATOR = "/";
    public static final int MAX_CHARS = 100;

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
        Logger.log(getAbsPath());
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

    public void createDir(String dirName) {
        if (!this.existDir(dirName)) {
            childs.add(new FSObject(dirName, FSType.FOLDER, this));
        } else {
            Logger.log(Errors.DIR_ALREADY_EXIST);
        }
    }

    public void listFilesAndFolders(boolean recursive) {
        if(recursive) {
            printAbsPath();
        }else if(father == null) {
            // Print only if root. I don't know why the Unit Test "testLsSimple" need this.
            Logger.log(this.toString());
        }

        for(FSObject item: childs) {
            if(recursive && item.type == FSType.FOLDER) {
                item.listFilesAndFolders(true);
            } else {
                Logger.log(item.name);
            }
        }
    }

    public FSObject getFather() {
        return father;
    }

    public void createFile(String fileName) {
        if(!this.existFile(fileName)) {
            FSObject file = new FSObject(fileName, FSType.FILE, this);
            childs.add(file);
        }
    }
}

/**
 * OSFileSystem: Represent a file system to operate on it
 */
class OSFileSystem implements Serializable{
    private static OSFileSystem INSTANCE = new OSFileSystem();
    private static final String FS_SERIALIZATION_NAME = "FileSystem.txt";
    private FSObject root;
    private FSObject currentPath;

    public static OSFileSystem getFileSystem() {
        
        return INSTANCE;
    }

    private OSFileSystem() {
        initialize();
    }

    private void initialize() {
        this.root = new FSObject("root", FSType.FOLDER, null);
        currentPath = root;
    }

    public void clean() {
        initialize();
    }

    protected Object readResolve() {
        return INSTANCE;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        INSTANCE = this;
    }

    public void printAbsPath() {
        currentPath.printAbsPath();
    }

    public void createDir(String dirName) {
        currentPath.createDir(dirName);
    }

    private boolean changeDir(String dirName) {
        boolean foundDir = true;

        if (dirName.compareTo("..") == 0 && currentPath.getFather() == null) {
            // This is root. Do nothing
            return true;
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
            Logger.log(Errors.DIR_NOT_FOUND);
        }

        return foundDir;
    }

    /**
    * Use a multi-faceted dir list. Could receive 1 or more dirs.
    * Ex. subdir1/subdir1-1/subdir3
    */
    public boolean changeDir(String[] dirList) {
        // Save current folder
        FSObject currentFolderTemp = currentPath;
        boolean success = true;

        for(String dir: dirList) {
            if(!this.changeDir(dir))
            {
                success = false;
                break;
            }
        }

        if(!success) {
            // Restore current folder
            currentPath = currentFolderTemp;
        }

        return success;
    }

    public void listFilesAndFolders(boolean recursive, String dirName) {
        FSObject currentFolderTemp = null;
        
        // This is a ls command over a subdirectory, so save current folder
        if (dirName.length() > 0) {
            currentFolderTemp = currentPath;
            if(!this.changeDir(dirName.split(FSObject.SEPARATOR))) {
                return;
            }
        }

        currentPath.listFilesAndFolders(recursive);

        // Restore current folder
        if (dirName.length() > 0) {
            currentPath = currentFolderTemp;
        }
    }

    public void createFile(String fileName) {
        currentPath.createFile(fileName);
    }

    public static void serialize() {
        
        FileOutputStream file;
        ObjectOutputStream out;

        try {
            file = new FileOutputStream(FS_SERIALIZATION_NAME);
            out = new ObjectOutputStream(file);
            
            out.writeObject(OSFileSystem.getFileSystem());

            out.close();
            file.close();
        }
        catch (IOException ex) {
            Logger.log(ex.toString());
        }
    }

    public static void deserialize() {
        FileInputStream file;
        ObjectInputStream input;

        try {
            file = new FileInputStream(FS_SERIALIZATION_NAME);
            input = new ObjectInputStream(file);
            
            INSTANCE = (OSFileSystem) input.readObject();

            input.close();
            file.close();
        }
        catch (IOException ex) {
            Logger.log(ex.toString());
        } catch (ClassNotFoundException c) {
            Logger.log(c.toString());
        }
    }
}

interface Command {
    public static final String ARG_DELIMITER = " ";
    public static final String ARG_RECURSIVE = "-r";

    public void execute();
    public boolean validate();
}

class CurrentDir implements Command{
    private String command;

    public CurrentDir(String command) {
        this.command = command;
    }

    public boolean validate() {
        String[] splittedCommands = command.split(Command.ARG_DELIMITER);
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
    private String command;
    private String dirName;

    public ListContent(String command)
    {
        this.command = command;
        this.dirName = "";
    }

    public boolean validate() {
        String[] splittedCommands = command.split(Command.ARG_DELIMITER);
        boolean valid = true;

        if (splittedCommands.length > 3) {
            valid = false;
        } else if (splittedCommands.length == 2 && splittedCommands[1].compareTo(Command.ARG_RECURSIVE) == 0) {
            recursive = true;
        } else if (splittedCommands.length == 2 && splittedCommands[1].compareTo(Command.ARG_RECURSIVE) != 0) {
            dirName = splittedCommands[1];
        } else if (splittedCommands.length == 3 && splittedCommands[1].compareTo(Command.ARG_RECURSIVE) == 0) {
            recursive = true;
            dirName = splittedCommands[2];
        }

        return valid;
    }

    public void execute()
    {
        if(validate()) {
            OSFileSystem.getFileSystem().listFilesAndFolders(recursive, dirName);
        }
        else {
            Logger.log(Errors.INVALID_COMMAND);
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
        String[] splittedCommands = command.split(Command.ARG_DELIMITER);
        boolean valid = true;

        if (splittedCommands.length == 2) {
            dirName = splittedCommands[1];

            if(dirName.length() >= FSObject.MAX_CHARS) {
                Logger.log(Errors.INVALID_FILE_DIR);
                valid = false;
            }
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
        String[] splittedCommands = command.split(Command.ARG_DELIMITER);
        boolean valid = true;

        if (splittedCommands.length == 2) {
            fileName = splittedCommands[1];

            if(fileName.length() >= FSObject.MAX_CHARS) {
                Logger.log(Errors.INVALID_FILE_DIR);
                valid = false;
            }
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

    private String[] multiDir;
    private String command;

    public ChangeDir(String command) {
        this.command = command;
    }

    public boolean validate() {
        String[] splittedCommands = command.split(Command.ARG_DELIMITER);
        boolean valid = true;

        if (splittedCommands.length == 2) {
            String dirName = splittedCommands[1];
            multiDir = dirName.split(FSObject.SEPARATOR);
        } else {
            valid = false;
        }

        return valid;
    }

    public void execute() {
        if(validate()){
            OSFileSystem.getFileSystem().changeDir(multiDir);
        } else {
            Logger.log(Errors.INVALID_COMMAND);
        }
    }
}

class Quit implements Command {

    private String command = "";

    public Quit(String command) {
        this.command = command;
    }

    public boolean validate() {
        String[] splittedCommands = command.split(Command.ARG_DELIMITER);
        boolean valid = true;

        if (splittedCommands.length != 1) {
            valid = false;
            Logger.log(Errors.INVALID_COMMAND);
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
        
        /* Adding this check due if I serialize/desserialize the FileSystem it contains garbage
        * from previous tests. The correct way should be call clean() before to run the every test.
        */
        boolean runUnitTests = (args.length > 0 && args[0].compareTo("RunUnitTests") == 0);

        Scanner sc = new Scanner(System.in);
        String strCommand;
        
        if(!runUnitTests) {
            OSFileSystem.deserialize();
        }

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
                Logger.log(Errors.UNRECOGNIZED_COMMAND);

        } while(true);

        if(!runUnitTests) {
            OSFileSystem.serialize();
        } else {
            OSFileSystem.getFileSystem().clean();
        }

        sc.close();
    }
}
