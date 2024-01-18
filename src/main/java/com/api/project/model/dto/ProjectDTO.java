package com.api.project.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectDTO {
    private String self;

    private String id;

    private String key;

    private String description;

    private Lead Lead;

    private ProjectCategory projectCategory;

    private String name;
    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Lead{
        private String self;
        private String accountId;
        private String displayName;
    }
    @Getter
    @Setter
    @Builder
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProjectCategory{
        private String self;
        private String id;
        private String name;
    }

}
