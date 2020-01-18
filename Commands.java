package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/** @author Naman Patel.**/
public class Commands implements Serializable {

    /** The init command.**/
    public void init() {
        File gitlet = new File(".gitlet");
        gitlet.mkdir();
        Commit first = Commit.firstCommit();
        masterHead = "master";
        activeHead = masterHead;
        File commits = new File(".gitlet/commits");
        commits.mkdir();
        File staging = new File(".gitlet/commitedfiles");
        staging.mkdir();
        String firstHashID = first.getFirstCommitID();
        File firstFile = new File(".gitlet/commits/" + firstHashID);
        Utils.writeObject(firstFile, first);
        masterBranch = new Branch(masterHead, first);
        activeBranch = masterBranch;
        commitIDS = new ArrayList<>();
        commitIDS.add(firstHashID);
        findMessageHashmap = new HashMap<>();
        findMessageHashmap.put(first.getMyMessage(), addFirstID(firstHashID));
        nametoBranchHashmap = new HashMap<>();
        nametoBranchHashmap.put(activeHead, activeBranch);
    }

    /** Returns the Commit object corresponding
     * to given SHAID.
     * @param shaID SHAID**/
    public Commit getCommitFromDir(String shaID) {
        File commitFile = new File(".gitlet/commits/" + shaID);
        if (!commitFile.exists()) {
            try {
                Utils.message("No commit with that id exists.");
                throw new GitletException("No commit with that id exists.");
            } catch (GitletException excp) {
                System.exit(0);
            }
            return null;
        } else {
            return Utils.readObject(commitFile, Commit.class);
        }
    }
    /** Adds first ID to HashMap for the init command.
     * @param id id
     * @return array list**/
    private ArrayList<String> addFirstID(String id) {
        ArrayList<String> first = new ArrayList<String>();
        first.add(id);
        return first;
    }

    /** The add command.
     * @param fileName file name**/
    public void add(String fileName) {
        File filetobeAdded = new File(fileName);
        if (!filetobeAdded.exists()) {
            try {
                Utils.message("File does not exist.");
                throw new GitletException("File does not exist.");
            } catch (GitletException excp) {
                System.exit(0);
            }
        } else {
            activeBranch.getStagingArea().addFile(fileName);
        }
    }

    /** The commit command.
     * @param commitMessage commit message.**/
    public void commit(String commitMessage) {
        if (commitMessage.length() == 0) {
            Utils.message("Please enter a commit message.");
            return;
        }
        Commit nextCommit = new Commit(commitMessage,
                activeBranch.getStagingArea());

        if (!nextCommit.checkCommitChanges()) {
            activeBranch.setMyRecentCommit(nextCommit);
            StagingArea nextStage = new StagingArea(nextCommit);
            activeBranch.setMyStagingArea(nextStage);
            String thiscommitID = nextCommit.getSHAID();
            commitIDS.add(thiscommitID);
            if (findMessageHashmap.get(commitMessage) == null) {
                ArrayList<String> initializedArrayList = new ArrayList<>();
                initializedArrayList.add(nextCommit.getSHAID());
                findMessageHashmap.put(commitMessage, initializedArrayList);
            } else {
                ArrayList<String> existingArrayList =
                        findMessageHashmap.get(commitMessage);
                existingArrayList.add(nextCommit.getSHAID());
                findMessageHashmap.put(commitMessage, existingArrayList);
            }
        }
    }

    /** The remove command.
     * @param fileName filename**/
    public void remove(String fileName) {
        Commit commit = activeBranch.getMyRecentCommit();
        StagingArea stage = activeBranch.getStagingArea();
        if (stage.getParentTrackedFiles().contains(fileName)
                || stage.getNewlyStagedFiles().contains(fileName)) {
            if (commit.getUpdatedFilesHashMap().containsKey(fileName)) {
                Utils.restrictedDelete(fileName);
                stage.getTobeRemovedFiles().add(fileName);
            }
            stage.getParentTrackedFiles().remove(fileName);
            stage.getNewlyStagedFiles().remove(fileName);
        } else {
            try {
                Utils.message("No reason to remove the file.");
                throw new GitletException("No reason to remove the file.");
            } catch (GitletException excp) {
                System.exit(0);
            }
        }
    }

    /** The log command.**/
    public void log() {
        Commit mostRecent = activeBranch.getMyRecentCommit();
        while (mostRecent != null) {
            printCommit(mostRecent);
            mostRecent = mostRecent.getMyParent();
        }
    }

    /** Helps print out the Commit in specified format for
     *  log command.
     *  @param commit commit**/
    public void printCommit(Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commit.getSHAID());
        System.out.println("Date: " + commit.getCommittimeStamp());
        System.out.println(commit.getMyMessage());
        System.out.println();
    }

    /** The global log command.**/
    public void globalLog() {
        for (String id : commitIDS) {
            printCommit(getCommitFromDir(id));
        }
    }

    /** The find command.
     * @param message message**/
    public void find(String message) {
        if (!(findMessageHashmap.containsKey(message))) {
            try {
                Utils.message("Found no commit with that message.");
                throw new GitletException("Found no commit with that message.");
            } catch (GitletException excp) {
                System.exit(0);
            }
        }
        ArrayList<String> ids = findMessageHashmap.get(message);
        for (String id : ids) {
            System.out.println(id);
        }
    }

    /** The status command.**/
    public void status() {
        System.out.println("=== Branches ===");
        Object[] branchArray = nametoBranchHashmap.keySet().toArray();
        Arrays.sort(branchArray);
        for (Object branch : branchArray) {
            if (activeBranch.getMyBranchName() == branch) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        Object[] stagedArray = activeBranch.getStagingArea().
                getNewlyStagedFiles().toArray();
        Arrays.sort(stagedArray);
        for (Object stagedFile : stagedArray) {
            System.out.println(stagedFile);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        Object[] removedArray = activeBranch.getStagingArea().
                getTobeRemovedFiles().toArray();
        Arrays.sort(removedArray);
        for (Object removedFile : removedArray) {
            System.out.println(removedFile);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
    }

    /** Helps determine which format of checkout to use
     * based on parameters received from args in Main.
     * @param parameters parameters**/
    public void checkoutWrapper(String... parameters) {
        switch (parameters.length - 1) {
        case 1:
            checkout(parameters[1]);
            break;
        case 2:
            if (!parameters[1].equals("--")) {
                try {
                    Utils.message("Incorrect operands.");
                    throw new GitletException("Incorrect operands.");
                } catch (GitletException excp) {
                    System.exit(0);
                }
            }
            checkout(parameters[1], parameters[2]);
            break;
        case 3:
            if (!parameters[2].equals("--")) {
                try {
                    Utils.message("Incorrect operands.");
                    throw new GitletException("Incorrect operands.");
                } catch (GitletException excp) {
                    System.exit(0);
                }
            }
            checkout(parameters[1], parameters[2], parameters[3]);
            break;
        default:
            try {
                Utils.message("Incorrect operands.");
                throw new GitletException("Incorrect operands.");
            } catch (GitletException excp) {
                System.exit(0);
            }
        }
    }

    /** The branch name checkout command.
     * @param branchName branchname**/
    public void checkout(String branchName) {
        if (!nametoBranchHashmap.containsKey(branchName)) {
            try {
                Utils.message("No such branch exists.");
                throw new GitletException("No such branch exists.");
            } catch (GitletException excp) {
                System.exit(0);
            }
        }
        if (activeBranch.getMyBranchName().equals(branchName)) {
            try {
                Utils.message("No need to checkout the current branch.");
                throw new GitletException("No need"
                        + " to checkout the current branch.");
            } catch (GitletException excp) {
                System.exit(0);
            }
        }
        Commit otherBranchCommit =
                nametoBranchHashmap.get(branchName).getMyRecentCommit();
        HashMap<String, String> filesMapping =
                otherBranchCommit.getUpdatedFilesHashMap();
        File userDir = new File(System.getProperty("user.dir"));
        activeBranch.getStagingArea().checkUntrackedFiles(userDir);
        for (File file : userDir.listFiles()) {
            if (filesMapping == null) {
                Utils.restrictedDelete(file);
            } else {
                boolean contains = !filesMapping.containsKey(file.getName());
                if (contains && !file.getName().equals(".gitlet")) {
                    Utils.restrictedDelete(file);
                }
            }
        }
        if (filesMapping != null) {
            for (String file : filesMapping.keySet()) {
                String fileName = ".gitlet/commitedfiles/"
                        + otherBranchCommit.getAllfilesHashMap().get(file);
                File fileHandle = new File(fileName);
                String fileContents = Utils.readContentsAsString(fileHandle);
                Utils.writeContents(new File(file), fileContents);
            }
        }
        activeBranch = nametoBranchHashmap.get(branchName);
    }

    /** The file name checkout command.
     * @param fileName file name
     * @param fixedString  fixed string**/
    public void checkout(String fixedString, String fileName) {
        if (activeBranch.getMyRecentCommit().
                getUpdatedFilesHashMap().containsKey(fileName)) {
            File fileHandle1 = new File(fileName);
            File fileHandle2 = new File(".gitlet/commitedfiles/"
                    + activeBranch.getMyRecentCommit().
                    getAllfilesHashMap().get(fileName));
            try {
                Utils.writeContents(fileHandle1,
                        Utils.readContentsAsString(fileHandle2));
            } catch (GitletException e) {
                System.out.println(e.getMessage());
            }
        } else {
            try {
                Utils.message("File does not exist in that commit.");
                throw new GitletException("File does "
                        + "not exist in that commit.");
            } catch (GitletException excp) {
                System.exit(0);
            }
        }
    }

    /** The commit ID and file name checkout command.
     * @param fixedString fixed string
     * @param commitID commit ID
     * @param fileName file name**/
    public void checkout(String commitID, String fixedString, String fileName) {
        if (commitID.length() < Utils.UID_LENGTH) {
            commitID = shortToLongID(commitID);
        }
        Commit commit = getCommitFromDir(commitID);
        if (!commit.getUpdatedFilesHashMap().containsKey(fileName)) {
            try {
                Utils.message("File does not exist in that commit.");
                throw new GitletException("File does "
                        + "not exist in that commit.");
            } catch (GitletException excp) {
                System.exit(0);
            }
        }
        File fileHandle1 = new File(fileName);
        File fileHandle2 = new File(".gitlet/commitedfiles/"
                + commit.getAllfilesHashMap().get(fileName));
        Utils.writeContents(fileHandle1,
                Utils.readContentsAsString(fileHandle2));
    }

    /** Convert any commit ID that is shorter than 40
     * characters into its full commit ID.
     * @param commitID commit ID
     * @return id **/
    public String shortToLongID(String commitID) {
        for (String id : commitIDS) {
            if (id.startsWith(commitID)) {
                return id;
            }
        }
        try {
            Utils.message("No commit with that id exists.");
            throw new GitletException("No commit with that id exists.");
        } catch (GitletException excp) {
            System.exit(0);
        }
        return commitID;
    }
    /** The branch command.
     * @param branchName branch name**/
    public void branch(String branchName) {
        if (nametoBranchHashmap.containsKey(branchName)) {
            try {
                Utils.message("A branch with that name already exists.");
                throw new GitletException("A branch with "
                        + "that name already exists.");
            } catch (GitletException excp) {
                System.exit(0);
            }
        } else {
            secondBranch = new Branch(branchName,
                    activeBranch.getMyRecentCommit());
            nametoBranchHashmap.put(branchName, secondBranch);
        }
    }

    /** The remove branch command.
     * @param branchName branch name**/
    public void removeBranch(String branchName) {
        if (activeBranch.getMyBranchName().equals(branchName)) {
            try {
                Utils.message("Cannot remove the current branch.");
                throw new GitletException("Cannot remove the current branch.");
            } catch (GitletException excp) {
                System.exit(0);
            }
        }
        if (nametoBranchHashmap.containsKey(branchName)) {
            nametoBranchHashmap.remove(branchName);

        } else {
            try {
                Utils.message("A branch with that name does not exist.");
                throw new GitletException("A branch with "
                        + "that name does not exist.");
            } catch (GitletException excp) {
                System.exit(0);
            }
        }
    }

    /** The reset command.
     * @param commitID commit id **/
    public void reset(String commitID) {
        if (commitID.length() < Utils.UID_LENGTH) {
            commitID = shortToLongID(commitID);
        }
        if (commitIDS.contains(commitID)) {
            Commit resetCommit = getCommitFromDir(commitID);
            File userDir = new File(System.getProperty("user.dir"));
            activeBranch.getStagingArea().checkUntrackedFiles(userDir);
            for (File f : userDir.listFiles()) {
                String fileName = f.getName();
                if (!resetCommit.getUpdatedFilesHashMap().
                        containsKey(fileName)) {
                    Utils.restrictedDelete(f);
                }
            }
            for (String filename : resetCommit.
                    getUpdatedFilesHashMap().keySet()) {
                File fileHandle = new File(".gitlet/commitedfiles/"
                        + resetCommit.getAllfilesHashMap().get(filename));
                String fileContent = Utils.readContentsAsString(fileHandle);
                Utils.writeContents(new File(filename), fileContent);
            }
            activeBranch.setMyRecentCommit(resetCommit);
            activeBranch.getStagingArea().clearStagingArea();
        } else {
            try {
                Utils.message("No commit with that id exists.");
                throw new GitletException("No commit with that id exists.");
            } catch (GitletException excp) {
                System.exit(0);
            }
        }
    }

    /** The merge command.
     * @param branchName branch name**/
    public void merge(String branchName) {
        if (!activeBranch.getStagingArea().getNewlyStagedFiles().isEmpty()
                || !activeBranch.getStagingArea().
                getTobeRemovedFiles().isEmpty()) {
            try {
                Utils.message("You have uncommitted changes.");
                throw new GitletException("You have uncommitted changes.");
            } catch (GitletException excp) {
                System.exit(0);
            }
        }
        if (!nametoBranchHashmap.containsKey(branchName)) {
            try {
                Utils.message("A branch with that name does not exist.");
                throw new GitletException("A branch with that name "
                        + "does not exist.");
            } catch (GitletException excp) {
                System.exit(0);
            }
        }
        if (branchName.equals(activeBranch.getMyBranchName())) {
            try {
                Utils.message("Cannot merge a branch with itself.");
                throw new GitletException("Cannot merge a branch with itself.");
            } catch (GitletException excp) {
                System.exit(0);
            }
        }
        String ancestor = findAncestor(branchName,
                activeBranch.getMyBranchName());
        String branchNameSHAID = nametoBranchHashmap.get(branchName).
                getMyRecentCommit().getSHAID();
        if (ancestor.equals(branchNameSHAID)) {
            try {
                Utils.message("Given branch is an ancestor of "
                        + "the current branch.");
                throw new GitletException("Given branch is an ancestor"
                        + " of the current branch.");
            } catch (GitletException excp) {
                System.exit(0);
            }
        }

        String activeBranchNameSHAID = activeBranch.
                getMyRecentCommit().getSHAID();
        if (ancestor.equals(activeBranchNameSHAID)) {
            activeBranch.setMyRecentCommit(getCommitFromDir(branchNameSHAID));
            try {
                Utils.message("Current branch fast-forwarded.");
                throw new GitletException("Current branch fast-forwarded.");
            } catch (GitletException excp) {
                System.exit(0);
            }
        }
    }

    /** Find ancestor.
     * @param branchName branch name
     * @param myBranchName my branch name
     * @return ancestor **/
    private String findAncestor(String branchName, String myBranchName) {
        return null;
    }

    /** A string that contains the name of the "master",
     * the initial branch.**/
    private String masterHead;

    /** A string that contains the name of the other,
     * possible branch.**/
    private String secondHead;

    /** A string that contains the name of the currently
     * active branch pointing to most recent commit.**/
    private String activeHead;

    /** A Branch type that contains the information of name,
     * recent commit, and staging area for the commit
     * being pointed to by masterBranch.**/
    private Branch masterBranch;

    /** A Branch type that contains the information of name,
     * recent commit, and staging area for the commit
     * being pointed to by the other possible branch.**/
    private Branch secondBranch;

    /** A Branch type that contains the information of name,
     * recent commit, and staging area for the commit
     * being pointed to by the activeBranch.**/
    private Branch activeBranch;

    /** A HashMap that has the keys as the name of the
     * branch and values as the Branch object with
     * that particular name -- maps name to Branch.**/
    private HashMap<String, Branch> nametoBranchHashmap;

    /** An ArrayList containing all commitIDS that have ever
     * been assigned to a commit by SHA1.**/
    private ArrayList<String> commitIDS;

    /** A HashMap that has the keys as the message of commit
     *  and values as an ArrayList of full commit IDS with
     * that particular message -- maps message to
     * all possible Commit IDs.**/
    private HashMap<String, ArrayList<String>> findMessageHashmap;

}
