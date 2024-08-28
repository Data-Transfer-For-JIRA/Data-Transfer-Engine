package com.utils;


import com.atlassian.adf.html.parser.HtmlParser;
import com.atlassian.adf.jackson2.AdfJackson2;
import com.atlassian.adf.model.node.Doc;
import com.atlassian.renderer.wysiwyg.converter.DefaultWysiwygConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConvertHtmlToADF {

    public static JsonNode converter(String html) throws Exception{

        DefaultWysiwygConverter 변환기 = new DefaultWysiwygConverter();
        String jiraMarkup = 변환기.convertXHtmlToWikiMarkup(html);


        HtmlParser htmlParser = new HtmlParser();
        Doc test = htmlParser.unmarshall(html);

        AdfJackson2 parser = new AdfJackson2();
        String json = parser.marshall(test);


        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(json);


        return jsonNode;

    }

}
