package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/** @author Naman Patel.**/
public class StagingArea implements Serializable {

    /** A constructor of a Staging Area object which take
     * in a Commit object.
     * @param currCommit  current commit**/
    public StagingArea(Commit currCommit) {
        myCommit = currCommit;
        newlyStagedFiles = new ArrayList<>();
        parentTrackedFiles = new ArrayList<>();
        untrackedFiles = new ArrayList<>();
        tobeRemovedFiles = new ArrayList<>();
        if (myCommit.getUpdatedFilesHashMap() != null) {
            setStage();
        }
    }

    /** Setter method for Staging Area.**/
    public void setStage() {
        Set<String> previousFiles = myCommit.getUpdatedFilesHashMap().keySet();
        if (!previousFiles.isEmpty()) {
            parentTrackedFiles.addAll(previousFiles);
        }
    }

    /** Clear the Staging Area.**/
    public void clearStagingArea() {
        newlyStagedFiles.clear();
        parentTrackedFiles.clear();
        untrackedFiles.clear();
        tobeRemovedFiles.clear();
    }

    /** Adds a file to the Staging Area.
     * @param fileName file name**/
    public void addFile(String fileName) {
        File fileHandle = new File(fileName);
        String sha1ToBeAdded = Utils.sha1(
                Utils.readContentsAsString(fileHandle));
        HashMap<String, String> allfilesHashMap = myCommit.getAllfilesHashMap();
        boolean fileExists = allfilesHashMap.containsKey(fileName);
        boolean fileNotChanged = false;
        if (fileExists) {
            fileNotChanged =
                    myCommit.getAllfilesHashMap().
                            get(fileName).equals(sha1ToBeAdded);
        }
        if (fileExists && fileNotChanged) {
            if (newlyStagedFiles.contains(fileName)) {
                newlyStagedFiles.remove(fileName);
            }
        } else {
            if (!newlyStagedFiles.contains(fileName)) {
                newlyStagedFiles.add(fileName);
            }
        }
        if (tobeRemovedFiles.contains(fileName)) {
            tobeRemovedFiles.remove(fileName);
        }
    }

    /** Checks for untracked files and prints the given
     * error message if there are any.
     * @param userDir  user directory**/
    public void checkUntrackedFiles(File userDir) {
        HashMap<String, String> commitedFiles =
                myCommit.getUpdatedFilesHashMap();
        for (File file : userDir.listFiles()) {
            if (commitedFiles == null) {
                if (userDir.listFiles().length > 1) {
                    try {
                        Utils.message("There is an "
                                + "untracked file in the way; delete "
                                + "it or add it first.");
                        throw new GitletException("There is an "
                                + "untracked file in the way; "
                                + "delete it or add it first.");
                    } catch (GitletException excp) {
                        System.exit(0);
                    }
                }
            } else {
                boolean notCommited =
                        !commitedFiles.containsKey(file.getName());
                boolean notStaged =
                        !myCommit.getMyStagingArea().
                                getNewlyStagedFiles().contains(file.getName());
                if (notCommited && notStaged
                        && !file.getName().equals(".gitlet")) {
                    try {
                        Utils.message("There is an untracked file in the way; "
                                + "delete it or add it first.");
                        throw new GitletException("There is "
                                + "an untracked file in the way; "
                                + "delete it or add it first.");
                    } catch (GitletException excp) {
                        System.exit(0);
                    }
                }
            }
        }
    }

    /** Populate untracked files.
     * @param userDir user directory**/
    public void populateUntrackedFiles(File userDir) {
        HashMap<String, String> commitedFiles =
                myCommit.getUpdatedFilesHashMap();
        for (File file : userDir.listFiles()) {
            boolean notCommited = !commitedFiles.containsKey(file.getName());
            boolean notStaged = !myCommit.getMyStagingArea().
                    getNewlyStagedFiles().contains(file.getName());
            if (notCommited && notStaged
                    && !file.getName().equals(".gitlet")) {
                untrackedFiles.add(file.getName());
            }
        }
    }

    /** Populate modified files.
     * @param userDir user directory**/
    public void populateModifiedFiles(File userDir) {
        HashMap<String, String> commitedFiles =
                myCommit.getUpdatedFilesHashMap();
        for (File file : userDir.listFiles()) {
            boolean commited = commitedFiles.containsKey(file.getName());
            boolean notStaged = !myCommit.getMyStagingArea().
                    getNewlyStagedFiles().contains(file.getName());
            if (commited && notStaged && !file.getName().equals(".gitlet")) {
                String id = commitedFiles.get(file.getName());
                boolean changed =
                        Utils.sha1(Utils.readContentsAsString(file)).equals(id);
                if (changed) {
                    modifiedFiles.add(file.getName());
                }
            }
            if (!notStaged) {
                String cwdID = Utils.sha1(Utils.readContentsAsString(file));
                boolean deletedCWD = !file.exists();
            }
            if (commited && notStaged && !file.getName().equals(".gitlet")) {
                String id;
            }
        }
    }

    /** Removes a file from the Staging Area.
     * @param fileName file name**/
    public void removeFile(String fileName) {
        tobeRemovedFiles.add(fileName);
    }



    /** Getter method for Commit of Staging Area object.
     * @return my commit **/
    public Commit getMyCommit() {
        return myCommit;
    }

    /** Getter method for newly staged files of Staging Area object.
     * @return newly staged files**/
    public ArrayList<String> getNewlyStagedFiles() {
        return newlyStagedFiles;
    }

    /** Getter method for untracked files of Staging Area object.
     * @return untracked files**/
    public ArrayList<String> getUntrackedFiles() {
        return untrackedFiles;
    }

    /** Getter method for untracked files of Staging Area object.
     * @return modified files**/
    public ArrayList<String> getModifiedFiles() {
        return modifiedFiles;
    }

    /** Getter method for to be removed of Staging Area object.
     * @return to be removed files **/
    public ArrayList<String> getTobeRemovedFiles() {
        return tobeRemovedFiles;
    }

    /** Getter method for commited files of Staging Area object.
     * @return parent tracked files**/
    public ArrayList<String> getParentTrackedFiles() {
        return parentTrackedFiles;
    }

    /** The created Commit object which allows for a new StagingArea
     * to be set up for the next Commit to come -- also the parameter
     * for constructing a new StagingArea.**/
    private Commit myCommit;

    /** An Arraylist of all newly staged files.**/
    private ArrayList<String> newlyStagedFiles;

    /** An Arraylist of all parent tracked files.**/
    private ArrayList<String> parentTrackedFiles;

    /** An Arraylist of all untracked files.**/
    private ArrayList<String> untrackedFiles;

    /** An Arraylist of all modified files but untracked
     * files.**/
    private ArrayList<String> modifiedFiles;

    /** An Arraylist of the files to be removed before
     * next Commit.**/
    private ArrayList<String> tobeRemovedFiles;

}
