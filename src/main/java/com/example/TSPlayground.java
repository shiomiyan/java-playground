package com.example;

import org.treesitter.TSLanguage;
import org.treesitter.TSNode;
import org.treesitter.TSParser;
import org.treesitter.TSTree;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * tree-sitter playground.
 */
public class TSPlayground {

    public String removeComment(TSLanguage lang, String code) throws IOException {
        TSParser parser = new TSParser();
        parser.setLanguage(lang);
        TSTree tree = parser.parseString(null, code);
        TSNode rootNode = tree.getRootNode();

        try (var result = new ByteArrayOutputStream()) {
            traverse(rootNode, code.getBytes(StandardCharsets.UTF_8), result, 0);
            return result.toString(StandardCharsets.UTF_8);
        }
    }

    private int traverse(TSNode node, byte[] code, ByteArrayOutputStream parts, int prevEndByte) throws IOException {
        int currEndByte = node.getEndByte();
        boolean hasChild = node.getChildCount() > 0;

        if (hasChild) {
            for (var i = 0; i < node.getChildCount(); i++) {
                prevEndByte = traverse(node.getChild(i), code, parts, prevEndByte);
            }
        } else if (!node.getType().equalsIgnoreCase("comment")) {
            // https://github.com/bonede/tree-sitter-ng/issues/19#issuecomment-2130987620
            parts.write(Arrays.copyOfRange(code, prevEndByte, currEndByte));
        }

        return currEndByte;
    }
}
