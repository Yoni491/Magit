package Objects.Commit;

import Objects.Api.MagitObject;
import Objects.Date.*;

public class Commit extends MagitObject implements Comparable<Commit> {
    private String rootFolderSha1;
    private String previousCommitSha1;
    private String previousCommit2Sha1;
    private String CommitPurposeMSG;

    public String getNameOfModifier() {
        return NameOfModifier;
    }

    private String NameOfModifier;

    public DateAndTime getDateAndTime() {
        return dateAndTime;
    }

    private DateAndTime dateAndTime;

    public Commit(String _rootFolderSha1, String _previousCommitSha1,
                  String _previousCommit2Sha1, String _CommitPurposeMSG, String _NameOfModifier, DateAndTime _dateAndTime) {
        super(_rootFolderSha1 + _previousCommitSha1
                + _previousCommit2Sha1 + _CommitPurposeMSG + _NameOfModifier);
        rootFolderSha1 = _rootFolderSha1;
        previousCommitSha1 = _previousCommitSha1;
        previousCommit2Sha1 = _previousCommit2Sha1;
        CommitPurposeMSG = _CommitPurposeMSG;
        NameOfModifier = _NameOfModifier;
        dateAndTime = _dateAndTime;
    }

    public Commit(String _rootFolderSha1, String _previousCommitSha1,
                  String _previousCommit2Sha1, String _CommitPurposeMSG, String _NameOfModifier) {
        super(_rootFolderSha1 + _previousCommitSha1
                + _previousCommit2Sha1 + _CommitPurposeMSG + _NameOfModifier);
        rootFolderSha1 = _rootFolderSha1;
        previousCommitSha1 = _previousCommitSha1;
        previousCommit2Sha1 = _previousCommit2Sha1;
        CommitPurposeMSG = _CommitPurposeMSG;
        NameOfModifier = _NameOfModifier;
        dateAndTime = new DateAndTime();
    }

    public Commit(String _rootFolderSha1, String _CommitPurposeMSG, String _NameOfModifier) {
        super(_rootFolderSha1 + _CommitPurposeMSG + _NameOfModifier);
        rootFolderSha1 = _rootFolderSha1;
        previousCommitSha1 = null;
        previousCommit2Sha1 = null;
        CommitPurposeMSG = _CommitPurposeMSG;
        NameOfModifier = _NameOfModifier;
        dateAndTime = new DateAndTime();
    }


    public String getPreviousCommitSha1() {
        return previousCommitSha1;
    }

    public String getRootFolderSha1() {
        return rootFolderSha1;
    }

    public String getCommitPurposeMSG() {
        return CommitPurposeMSG;
    }


    @Override
    public int compareTo(Commit o) {
        return this.dateAndTime.getDateInSeconds() < o .dateAndTime.getDateInSeconds() ? 1 : -1;
    }
}
