package gitlet;

import java.io.Serializable;

/** @author Naman Patel.**/
public class Branch implements Serializable {

    /** A constructor of Branch object which takes in a
     * name and the commit it is pointing to.
     * @param name name
     * @param recentCommit recent commit.**/
    public Branch(String name, Commit recentCommit) {
        myBranchName = name;
        myRecentCommit = recentCommit;
        myStagingArea = new StagingArea(recentCommit);
    }

    /** Setter method to move the branch to next Commit.
     * @param commit my recent commit**/
    public void setMyRecentCommit(Commit commit) {
        myRecentCommit = commit;
    }

    /** Setter method to move the branch to next Staging Area.
     * @param stage my staging area **/
    public void setMyStagingArea(StagingArea stage) {
        myStagingArea = stage;
    }

    /** Getter method to get the Staging Area of the Branch.
     * @return my staging area**/
    public StagingArea getStagingArea() {
        return myStagingArea;
    }

    /** Getter method to return the name of the Branch.
     * @return my branch name**/
    public String getMyBranchName() {
        return myBranchName;
    }

    /** Getter method to return the Commit pointed
     * to by the Branch.
     * @return my recent commit**/
    public Commit getMyRecentCommit() {
        return myRecentCommit;
    }

    /** A string for the Branch name.**/
    private String myBranchName;

    /** The head Commit object of this branch.**/
    private Commit myRecentCommit;

    /** The StagingArea for the Branch.**/
    private StagingArea myStagingArea;
}
