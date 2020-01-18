package gitlet;

import java.io.File;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Naman Patel
 */

public class Main {


/** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */

    public static void main(String... args) {
        if (args.length == 0) {
            try {
                Utils.message("Please enter a command.");
                throw new GitletException("Please enter a command.");
            } catch (GitletException excp) {
                System.exit(0);
            }
        }
        String command = args[0];
        if (checkforValidcommand(command)) {
            if (noArgsCommand(args) && command.equals("init")) {
                if (checkInit()) {
                    try {
                        Utils.message("A Gitlet version-control system already "
                                + "exists in the current directory.");
                        throw new GitletException();
                    } catch (GitletException excp) {
                        System.exit(0);
                    }
                } else {
                    Commands commandBoard = new Commands();
                    commandBoard.init();
                    Utils.writeObject(new File(".gitlet/commandboard"),
                            commandBoard);
                }
            } else {
                doCommands(command, args);
            }
        } else {
            try {
                Utils.message("No command with that name exists.");
                throw new GitletException("No command with that name exists.");
            } catch (GitletException excp) {
                System.exit(0);
            }
        }
    }

    /** Execute the commands.
     * @param args args
     * @param command command **/
    public static void doCommands(String command, String... args) {
        if (checkInit()) {
            Commands commandBoard = getCommandBoard();
            if (noArgsCommand(command)) {
                switch (command) {
                case "log":
                    commandBoard.log();
                    break;
                case "global-log":
                    commandBoard.globalLog();
                    break;
                case "status":
                    commandBoard.status();
                    break;
                default:
                    break;
                }
            } else if (legaloneArgCommand(args)) {
                String parameter1 = args[1];
                switch (command) {
                case "add":
                    commandBoard.add(parameter1);
                    break;
                case "branch":
                    commandBoard.branch(parameter1);
                    break;
                case "commit":
                    commandBoard.commit(parameter1);
                    break;
                case "find":
                    commandBoard.find(parameter1);
                    break;
                case "rm":
                    commandBoard.remove(parameter1);
                    break;
                case "rm-branch":
                    commandBoard.removeBranch(parameter1);
                    break;
                case "reset":
                    commandBoard.reset(parameter1);
                    break;
                case "merge":
                    commandBoard.merge(parameter1);
                    break;
                default:
                    break;
                }
            } else if (command.equals("checkout")) {
                commandBoard.checkoutWrapper(args);
            }
            Utils.writeObject(new File(".gitlet/commandboard"), commandBoard);
        } else {
            try {
                Utils.message(" Not in an initialized Gitlet directory.");
                throw new GitletException();
            } catch (GitletException excp) {
                System.exit(0);
            }
        }
    }

    /** Checks if .gitlet has already been initialized.
     * @return boolean checking for .gitlet**/
    public static boolean checkInit() {
        File init = new File(".gitlet/");
        if (init.exists()) {
            return true;
        }
        return false;
    }


    /** Checks if command is legal.
     * @param command command
     * @return boolean checking for legal command **/
    public static boolean checkforValidcommand(String command) {
        for (String allowed : legalCommands) {
            if (allowed.equals(command)) {
                return true;
            }
        }
        return false;
    }

    /** Checks if args is legal no argument command format.
     * @param parameters parameters
     * @return boolean checking for no args**/
    public static boolean noArgsCommand(String... parameters) {
        boolean noArgsCommand = false;
        boolean legalCommand = false;
        for (String allowed : noArgCommands) {
            if (allowed.equals(parameters[0])) {
                noArgsCommand = true;
                break;
            }
        }
        if (noArgsCommand) {
            if (parameters.length == 1) {
                legalCommand = true;
            } else {
                try {
                    Utils.message("Incorrect operands.");
                    throw new GitletException("Incorrect operands.");
                } catch (GitletException excp) {
                    System.exit(0);
                }
            }
        }
        return legalCommand;
    }

    /** Checks if args is legal one argument command format.
     * @param parameters  parameters
     * @return boolean checking for legal one args command**/
    public static boolean legaloneArgCommand(String... parameters) {
        boolean oneArgCommand = false;
        boolean legalCommand = false;
        for (String allowed : oneArgCommands) {
            if (allowed.equals(parameters[0])) {
                oneArgCommand = true;
                break;
            }
        }
        if (oneArgCommand) {
            if (parameters.length == 2) {
                legalCommand = true;
            } else {
                try {
                    Utils.message("Incorrect operands.");
                    throw new GitletException("Incorrect operands.");
                } catch (GitletException excp) {
                    System.exit(0);
                }
            }
        }
        return legalCommand;
    }

    /** Gets our command board with information written to it
     * from previous commands to continue gitlet execution.
     * @return command board**/
    public static Commands getCommandBoard() {
        return Utils.readObject(new File(".gitlet/commandboard"),
                Commands.class);
    }

    /** A string array of all legal commands.**/
    private static String[] legalCommands = {"init", "add", "commit",
        "rm", "log", "global-log",
        "find", "status", "checkout", "branch",
        "rm-branch", "reset", "merge"};

    /** A string array of all legal commands that take no arguments.**/
    private static String[] noArgCommands = {"init", "log", "global-log",
        "status"};

    /** A string array of all legal commands that take one argument.**/
    private static String[] oneArgCommands = {"add", "branch", "commit",
        "find", "rm", "rm-branch",
        "reset", "merge"};
}

