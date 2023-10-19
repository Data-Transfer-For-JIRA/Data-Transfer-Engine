package com.mining.issue.service;

import java.util.List;
import java.util.Map;

public interface MiningIssue {

    public List<Map<String, String>> miningscheduler() throws Exception;

    public int miningissuedata(String project_key, String issue_key) throws Exception;
}
