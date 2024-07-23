package com.api.platform.service;

import com.api.platform.dto.BaseDTO;
import com.api.platform.dto.ReturnMessage;
import com.jira.issue.model.dto.create.CustomFieldDTO;
import com.jira.issue.model.dto.FieldDTO;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PlatformProject {

    Map<String, String> platformCreateProject(BaseDTO baseDTO) throws Exception;

    BaseDTO platformGetProject(String projectFlag, String jiraKey) throws Exception;

    BaseDTO platformGetIssue(String jiraIssueKey) throws Exception;

    Map<String, String> platformCreateProject(String jiraProjectCode, String projectFlag, String projectName, String projectCode, String assignees) throws Exception;

    Map<String, String> platformService(BaseDTO baseDTO) throws Exception;

    String setAssignees(String assignee, String subAssignee);

    <B extends CustomFieldDTO.CustomFieldDTOBuilder<?, ?>> B setCommonFields(B customBuilder, String jiraProjectCode, BaseDTO.CommonDTO commonDTO) throws Exception;

    List<FieldDTO.Field> setProductInfo(String category, List<String> productList);

    void setBuilder(String info, Consumer<String> consumer);

    <T> void setBuilder(Supplier<T> supplier, Consumer<T> consumer);

    <T, R> void setBuilder(Supplier<T> supplier, Function<T, R> function, Consumer<R> consumer);

    ReturnMessage platformWeblink(String mainJiraKey, String subJiraKey) throws Exception;

    void upDateProjectInfo(String jiraKey, BaseDTO baseDTO) throws Exception;

    Map<String, String> updateBaseIssue(String issueKey, BaseDTO baseDTO) throws Exception;
}
