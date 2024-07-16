package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.treesitter.TSInputEncoding;
import org.treesitter.TSLanguage;
import org.treesitter.TSNode;
import org.treesitter.TSParser;
import org.treesitter.TSTree;
import org.treesitter.TreeSitterJavascript;

public class ExampleTest {
    @Test
    void emojiTest() {
        TSParser parser = new TSParser();
        TSLanguage javascript = new TreeSitterJavascript();
        parser.setLanguage(javascript);

        String code = """
                // 😭
                foo();
                """;

        TSTree tree = parser.parseStringEncoding(null, code, TSInputEncoding.TSInputEncodingUTF8);
        TSNode rootNode = tree.getRootNode();

        TSNode commentNode = rootNode.getChild(0);
        int startByte = commentNode.getStartByte();
        int endByte = commentNode.getEndByte();

        byte[] codeBytes = code.getBytes(StandardCharsets.UTF_8);
        byte[] commentNodeBytes = Arrays.copyOfRange(codeBytes, startByte, endByte);
        String commentString = new String(commentNodeBytes, StandardCharsets.UTF_8);

        assertThat(commentString).isEqualTo("// 😭");
    }

}
