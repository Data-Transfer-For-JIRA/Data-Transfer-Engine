package com.transfer.issue.model.dto;


import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldDTO {

    private Project project;

    private Assignee assignee;

    private Status status;

    private IssueType issueType;

    private Description description;

    private String summary;

    @Getter
    @Setter
    private class Project{
        String id;
    }
    @Getter
    @Setter
    private class Assignee{
        String accountId;
    }
    @Getter
    @Setter
    private class Status{
        String id;
    }
    @Getter
    @Setter
    private class IssueType{
        String id;
    }
    @Getter
    @Setter
    private class Description{
        private List<ContentDTO> content;
        @Getter
        @Setter
        public class ContentDTO {
            private String text;
        }

    }

}
