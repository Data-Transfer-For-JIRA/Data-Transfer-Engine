package com.api.platform.service;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.api.platform.service.PlatformProjectImpl.removeHtmlTags;
import static com.api.platform.service.PlatformProjectImpl.replaceText;

class PlatformProjectImplTest {

    @Test
    void setDescription() {
        String description = "<ol>\n" +
                "\t<li>deep1</li>\n" +
                "\t\t<li class=\"ql-indent-1\">deep1-1</li>\n" +
                "\t\t<li class=\"ql-indent-1\">deep1-2</li>\n" +
                "\t\t<li class=\"ql-indent-1\">deep1-3</li>\n" +
                "\t<li>deep2</li>\n" +
                "\t\t<li class=\"ql-indent-1\">deep2-1</li>\n" +
                "\t\t<li class=\"ql-indent-1\">deep2-2</li>\n" +
                "\t\t<li class=\"ql-indent-1\">deep2-3</li>\n" +
                "\t<li>deep3</li>\n" +
                "\t\t<li class=\"ql-indent-1\">deep3-1</li>\n" +
                "\t\t\t<li class=\"ql-indent-2\">depp3-1-1</li>\n" +
                "\t\t\t<li class=\"ql-indent-2\">depp3-1-2</li>\n" +
                "\t\t\t<li class=\"ql-indent-2\">depp3-1-3</li>\n" +
                "</ol>\n" +
                "<ul>\n" +
                "\t<li>deep1</li>\n" +
                "\t\t<li class=\"ql-indent-1\">deep1-1</li>\n" +
                "\t\t<li class=\"ql-indent-1\">deep1-2</li>\n" +
                "\t\t<li class=\"ql-indent-2\">deep1-2-1</li>\n" +
                "</ul>";

        String 설명 = Optional.ofNullable(description)
                .map(String::trim)
                .orElse(StringUtils.EMPTY);

        if (!StringUtils.isEmpty(설명)) {
            설명 = processHtml(설명);
            String text = replaceText(removeHtmlTags(Jsoup.clean(설명, Whitelist.basic())), "&nbsp;", " ");
            System.out.println(text);
        }
    }

    public static String processHtml(String html) {
        Document doc = Jsoup.parse(html);

        // ol 태그 처리
        Elements olElements = doc.select("ol");
        for (Element ol : olElements) {
            processOlElement(ol);
        }

        // ul 태그 처리
        Elements ulElements = doc.select("ul");
        for (Element ul : ulElements) {
            processUlElement(ul);
        }

        return doc.body().html();
    }

    public static void processOlElement(Element ol) {
        int counter = 1;
        char indentCounter = 'a';

        Elements liElements = ol.children();
        for (Element li : liElements) {
            if (li.hasClass("ql-indent-1")) {
                li.prepend("&nbsp;&nbsp;" + indentCounter++ + ". ");
            } else {
                li.prepend(counter++ + ". ");
                indentCounter = 'a'; // 새 li 만나면 초기화
            }
        }
    }

    public static void processUlElement(Element ul) {
        char bullet = '•';

        Elements liElements = ul.children();
        for (Element li : liElements) {
            if (li.hasClass("ql-indent-1")) {
                li.prepend("&nbsp;&nbsp;" + bullet + " ");
            } else {
                li.prepend(bullet + " ");
            }
        }
    }
}