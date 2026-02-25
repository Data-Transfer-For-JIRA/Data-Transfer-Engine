package com.api.scheduler.wss.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WssSchedulerImplTest {

    private String removeImageTags(String html) {
        if (html == null || html.isEmpty()) return html;

        Document doc = Jsoup.parse(html);
        doc.select("span.image-wrap").remove();
        doc.select("img").remove();
        doc.select("p:empty").remove();

        return doc.body().html();
    }

    private void 비교출력(String 테스트명, String input, String output) {
        System.out.println("┌─────────────────────────────────────────────");
        System.out.println("│ [" + 테스트명 + "]");
        System.out.println("├─────────────────────────────────────────────");
        System.out.println("│ INPUT  : " + input);
        System.out.println("│ OUTPUT : " + output);
        System.out.println("└─────────────────────────────────────────────");
        System.out.println();
    }

    @Test
    void 이미지태그_포함된_HTML에서_이미지_제거() {
        String input = "<p><span class=\"image-wrap\" style=\"\"><img src=\"https://mail.google.com/mail/u/0?ui=2&amp;ik=088a83a9b3&amp;attid=0.0.3\" width=\"372\" style=\"border: 0px solid black\" /></span></p>"
                + "<p><span class=\"image-wrap\" style=\"\"><img src=\"/rest/api/3/attachment/content/45142\" alt=\"image-20260211-045221.png\" width=\"714\" style=\"border: 0px solid black\" /></span></p>"
                + "<p>이것은 텍스트입니다.</p>";

        String result = removeImageTags(input);
        비교출력("이미지 포함 HTML", input, result);

        assertFalse(result.contains("<img"));
        assertFalse(result.contains("image-wrap"));
        assertTrue(result.contains("이것은 텍스트입니다"));
    }

    @Test
    void 이미지_없는_HTML은_그대로_유지() {
        String input = "<p>일반 텍스트</p><p><strong>굵은 글씨</strong></p>";

        String result = removeImageTags(input);
        비교출력("이미지 없는 HTML", input, result);

        assertTrue(result.contains("일반 텍스트"));
        assertTrue(result.contains("굵은 글씨"));
    }

    @Test
    void 단독_img_태그도_제거() {
        String input = "<p>텍스트 앞</p><img src=\"/some/image.png\" /><p>텍스트 뒤</p>";

        String result = removeImageTags(input);
        비교출력("단독 img 태그", input, result);

        assertFalse(result.contains("<img"));
        assertTrue(result.contains("텍스트 앞"));
        assertTrue(result.contains("텍스트 뒤"));
    }

    @Test
    void null과_빈문자열_처리() {
        String nullResult = removeImageTags(null);
        String emptyResult = removeImageTags("");
        비교출력("null 입력", "null", String.valueOf(nullResult));
        비교출력("빈 문자열", "\"\"", "\"" + emptyResult + "\"");

        assertNull(nullResult);
        assertEquals("", emptyResult);
    }
}
