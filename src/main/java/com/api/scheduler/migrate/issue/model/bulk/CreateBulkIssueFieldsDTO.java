package com.api.scheduler.migrate.issue.model.bulk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jira.issue.model.dto.FieldDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder // 상속관계 빌더 충돌 해결
@ToString
@NoArgsConstructor //파라메터 없는 생성자
@AllArgsConstructor // 모든 필드를 파라메터로 받는 생성자
@JsonInclude(JsonInclude.Include.NON_NULL) // json 직렬화시 null 값 제거
public class CreateBulkIssueFieldsDTO extends FieldDTO {
    private FieldDTO fields;
}
