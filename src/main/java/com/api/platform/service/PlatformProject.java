package com.api.platform.service;

import com.api.platform.dto.BaseDTO;
import com.api.platform.dto.ReturnMessage;
import com.api.scheduler.backup.model.entity.BACKUP_ISSUE_Entity;
import com.jira.issue.model.dto.create.CustomFieldDTO;
import com.jira.issue.model.dto.FieldDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PlatformProject {

//    Map<String, String> platformCreateProject(BaseDTO baseDTO) throws Exception;

    BaseDTO platformGetProject(String projectFlag, String jiraKey) throws Exception;

    BaseDTO platformGetIssue(String jiraIssueKey) throws Exception;

    Map<String, String> platformCreateProject(String jiraProjectCode, String projectFlag, String projectName, String projectCode, String assignees, String salesManager) throws Exception;

    Map<String, String> platformService(BaseDTO baseDTO) throws Exception;

    String setAssignees(String assignee, String subAssignee);

    <B extends CustomFieldDTO.CustomFieldDTOBuilder<?, ?>> B setCommonFields(B customBuilder, String jiraProjectCode, BaseDTO.CommonDTO commonDTO) throws Exception;

    List<FieldDTO.Field> setProductInfo(String category, List<String> productList);

    void setBuilder(String info, Consumer<String> consumer);

    <T> void setBuilder(Supplier<T> supplier, Consumer<T> consumer);

    <T, R> void setBuilder(Supplier<T> supplier, Function<T, R> function, Consumer<R> consumer);

    ReturnMessage platformWeblink(String mainJiraKey, String subJiraKey) throws Exception;

    Map<String, String> upDateProjectInfo(String jiraKey, BaseDTO baseDTO) throws Exception;

    Map<String, String> updateBaseIssue(String issueKey, BaseDTO baseDTO) throws Exception;

    Map<String, String> createTicket(String summary, String description) throws Exception;

    Optional<BACKUP_ISSUE_Entity> 티켓_정보_조회(String jiraIssueKey);

    Page<BACKUP_ISSUE_Entity> 프로젝트에_생성된_티켓_정보_조회(String jiraProjectKey , int page, int size);

}
