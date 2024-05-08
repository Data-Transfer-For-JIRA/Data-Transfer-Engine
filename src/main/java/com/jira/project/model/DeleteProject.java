package com.jira.project.model;

public enum DeleteProject {
    ALL,
    TRASH;

    public String isALL() {
        if (this == ALL) {
            return "true";
        }
        return "false";
    }
}
