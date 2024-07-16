package com.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.treesitter.TSInputEncoding;
import org.treesitter.TSLanguage;
import org.treesitter.TSNode;
import org.treesitter.TSParser;
import org.treesitter.TSTree;

/**
 * tree-sitter playground.
 */
public class TSPlayground {

    public String removeComment(TSLanguage lang, String code) throws IOException {
        TSParser parser = new TSParser();
        parser.setLanguage(lang);
        TSTree tree = parser.parseStringEncoding(null, code, TSInputEncoding.TSInputEncodingUTF8);
        TSNode rootNode = tree.getRootNode();

        var result = traverse(rootNode, code.getBytes(StandardCharsets.UTF_8), new ByteArrayOutputStream());
        return new String(result, StandardCharsets.UTF_8);
    }

    private int prevEndByte = 0;

    private byte[] traverse(TSNode node, byte[] code, ByteArrayOutputStream parts) throws IOException {
        int currEndByte = node.getEndByte();
        boolean hasChild = node.getChildCount() > 0;

        if (hasChild) {
            for (var i = 0; i < node.getChildCount(); i++) {
                traverse(node.getChild(i), code, parts);
            }
        } else {
            if (!node.getType().equalsIgnoreCase("comment")) {
                parts.write(Arrays.copyOfRange(code, prevEndByte, currEndByte));
            }
        }
        prevEndByte = currEndByte;
        return parts.toByteArray();
    }
}
