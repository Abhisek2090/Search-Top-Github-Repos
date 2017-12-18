package com.abhisek.mapprr_github;

import java.io.Serializable;

/**
 * Created by bapu on 2/11/2017.
 */

public class RepositoryData implements Serializable {

    public String repoName;
    public String fullRepoName;
    public String repoId;
    public String logo;
    public String repowatchers;
    public String commitsCount;


    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getFullRepoName() {
        return fullRepoName;
    }

    public void setFullRepoName(String fullRepoName) {
        this.fullRepoName = fullRepoName;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getRepowatchers() {
        return repowatchers;
    }

    public void setRepowatchers(String repowatchers) {
        this.repowatchers = repowatchers;
    }

    public String getCommitsCount() {
        return commitsCount;
    }

    public void setCommitsCount(String commitsCount) {
        this.commitsCount = commitsCount;
    }
}
