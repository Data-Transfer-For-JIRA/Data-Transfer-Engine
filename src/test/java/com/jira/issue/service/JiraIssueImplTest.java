package com.jira.issue.service;

import com.Application;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("수동 실행용 - Jira API 실제 호출")
@SpringBootTest(classes = Application.class)
class JiraIssueImplTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JiraIssue jiraIssue;

    @Test
    void addMentionAndComment_단일멘션_테스트() throws Exception {
        // given
        String issueKey = "EDPP-338"; // 테스트할 이슈키로 변경
        String targetUser = "최수현";
        String contents = "단일 멘션 테스트 댓글입니다.";

        // when
        Boolean result = jiraIssue.addMentionAndComment(issueKey, targetUser, contents);

        // then
        logger.info("단일 멘션 결과: " + result);
        assertTrue(result);
    }

    @Test
    void addMentionAndCommentWithCc_CC멘션_테스트() throws Exception {
        // given
        String issueKey = "EDPP-338"; // 테스트할 이슈키로 변경
        String targetUser = "최수현";
        String ccUser = "최정오";
        String contents = "CC 멘션 테스트 댓글입니다.";

        // when
        Boolean result = jiraIssue.addMentionAndCommentWithCc(issueKey, targetUser, ccUser, contents);

        // then
        logger.info("CC 멘션 결과: " + result);
        assertTrue(result);
    }
}
