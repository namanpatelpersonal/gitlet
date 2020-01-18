package gitlet;


import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/** @author Naman Patel.**/
public class Commit implements Serializable {

    /** A constructor for commit class.
     * @param message message
     * @param stage  stage **/
    public Commit(String message, StagingArea stage) {
        myMessage = message;
        myStagingArea = stage;
        updatedFilesHashMap = new HashMap<>();
        allfilesHashMap = new HashMap<>();
        sHAID = "6be3acaa81987a291d2d4109897857bcd517a4c0";
        if (stage != null) {
            myParent = stage.getMyCommit();
            updatedFilesHashMap.putAll(myParent.updatedFilesHashMap);
            if (myStagingArea.getTobeRemovedFiles() != null) {
                ArrayList<String> filestobeRemoved =
                        myStagingArea.getTobeRemovedFiles();
                for (String removefile : filestobeRemoved) {
                    if (updatedFilesHashMap.containsKey(removefile)) {
                        updatedFilesHashMap.remove(removefile);
                    }
                }
            }
            allfilesHashMap.putAll(myParent.allfilesHashMap);
            Date myDate = new Date();
            simpleDateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy");
            committimeStamp = simpleDateFormat.format(myDate) + " -0800";
            sHAID = getcommitID();
            writeCommit(stage);
        }
    }

    /** Method to write the Commit object to directory.
     * @param stage  stage **/
    public void writeCommit(StagingArea stage) {
        if (stage != null) {
            for (String file : stage.getNewlyStagedFiles()) {
                File fileHandle = new File(file);
                String id2 = Utils.sha1(Utils.readContentsAsString(fileHandle));
                if (allfilesHashMap.containsKey(file)) {
                    String id1 = allfilesHashMap.get(file);
                    if (!id1.equals(id2)) {
                        updatedFilesHashMap.put(file, id2);
                        allfilesHashMap.replace(file, id2);
                    }
                } else {
                    updatedFilesHashMap.put(file, id2);
                    allfilesHashMap.put(file, id2);
                }
            }
        }
        if (!checkCommitChanges()) {
            String commitObjectFileName = ".gitlet/commits/" + getSHAID();
            String commitedFilesDirectory = ".gitlet/commitedfiles/";
            if (!myMessage.equals("initial commit")) {
                File commitObjectFile = new File(commitObjectFileName);
                if (stage != null) {
                    for (String file : stage.getNewlyStagedFiles()) {
                        File fileHandle1 = new File(file);
                        try {
                            String fileContents =
                                    Utils.readContentsAsString(fileHandle1);
                            File fileHandle2 = new File(commitedFilesDirectory
                                    + updatedFilesHashMap.get(file));
                            Utils.writeContents(fileHandle2, fileContents);
                        } catch (ClassCastException e) {
                            throw new GitletException();
                        }
                    }
                    Utils.writeObject(commitObjectFile, this);
                }
            }
        } else {
            System.out.println("No changes added to the commit.");
        }
    }

    /** Checks for changes in commit.
     * @return boolean**/
    public boolean checkCommitChanges() {
        if (!(myParent == null)) {
            if (getUpdatedFilesHashMap().size()
                    != myParent.getUpdatedFilesHashMap().size()) {
                return false;
            }
            for (String name : updatedFilesHashMap.keySet()) {
                if (!myParent.getUpdatedFilesHashMap().
                        keySet().contains(name)) {
                    return false;
                } else {
                    if (!getAllfilesHashMap().get(name).
                            equals(myParent.getAllfilesHashMap().get(name))) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /** Creates the first commit.
     * @return first commit**/
    public static Commit firstCommit() {
        String initialtimeStamp = "Wed Dec 31 16:00:00 1969 -0800";
        Commit firstCommit = new Commit("initial commit", null);
        firstCommitID = Utils.sha1(firstCommit.myMessage, initialtimeStamp);
        return firstCommit;
    }

    /** Creates a distinct commit ID for each Commit object.
     * @return commit id **/
    public String getcommitID() {
        if (myMessage.equals("initial commit")) {
            return getFirstCommitID();
        } else {
            if (myParent.myMessage.equals("initial commit")) {
                return Utils.sha1(myMessage, committimeStamp,
                        myStagingArea.getNewlyStagedFiles().toString(),
                        "6be3acaa81987a291d2d4109897857bcd517a4c0");
            } else {
                return Utils.sha1(myMessage, committimeStamp,
                        myStagingArea.getNewlyStagedFiles().toString(),
                        myParent.getcommitID());
            }
        }
    }

    /** Getter method for parent of Commit object.
     * @return my parent**/
    public Commit getMyParent() {
        return myParent;
    }

    /** Getter method for parent of first commit ID.
     * @return first commit ID**/
    public String getFirstCommitID() {
        return firstCommitID;
    }

    /** Getter method for SHA ID of Commit object.
     * @return SHA ID**/
    public String getSHAID() {
        return sHAID;
    }

    /**Getter method for staging area of Commit object.
     * @return my staging area**/
    public StagingArea getMyStagingArea() {
        return myStagingArea;
    }

    /** Getter method for time stamp of Commit object.
     * @return commit time stamp **/
    public String getCommittimeStamp() {
        if (myMessage.equals("initial commit")) {
            return "Wed Dec 31 16:00:00 1969 -0800";
        } else {
            return committimeStamp;
        }
    }

    /** Getter method for updated files of Commit object.
     * @return updated files hashmap**/
    public HashMap<String, String> getUpdatedFilesHashMap() {
        return updatedFilesHashMap;
    }

    /** Getter method for all files of Commit object.
     * @return all files hashmap**/
    public HashMap<String, String> getAllfilesHashMap() {
        return allfilesHashMap;
    }

    /** Getter method for message of Commit object.
     * @return my message**/
    public String getMyMessage() {
        return myMessage;
    }

    /** A string that stores Commit message.**/
    private String myMessage;

    /**
     * A HashMap of all files that has the keys as file name
     * and values as the full SHA1 ID of the file
     * -- maps file name to full SHA ID.
     **/
    private HashMap<String, String> allfilesHashMap;

    /**
     * A HashMap of newly updated files that has the keys
     * as file name and values as the full Commit SHA1 ID
     * -- maps file name to full SHA ID.
     **/
    private HashMap<String, String> updatedFilesHashMap;

    /**
     * A Commit object of the parent of this Commit.
     **/
    private Commit myParent;

    /**
     * The StagingArea of this Commit -- this is passed in
     * when creating a Commit object.
     **/
    private StagingArea myStagingArea;

    /**
     * A string that stores the firstCommit's SHAID.
     **/
    private static String firstCommitID;

    /**
     * A string that stores the current Commit's SHAID.
     **/
    private String sHAID;

    /**
     * A string that stores this Commit timestamp.
     **/
    private String committimeStamp;

    /**
     * The desired date format for Commit objects.
     **/
    private static SimpleDateFormat simpleDateFormat;

}
