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
import org.treesitter.TreeSitterJavascript;

public class Main {
    public static void main(String[] args) throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        byte[] bytes = classloader.getResourceAsStream("main.js").readAllBytes();
        String codeContent = new String(bytes, StandardCharsets.UTF_8);

        TSParser parser = new TSParser();
        TSLanguage javascript = new TreeSitterJavascript();
        parser.setLanguage(javascript);

        TSTree tree = parser.parseStringEncoding(null, codeContent, TSInputEncoding.TSInputEncodingUTF8);
        TSNode rootNode = tree.getRootNode();

        var parts = new ByteArrayOutputStream();
        var result = traverse(rootNode, bytes, parts);
        System.out.println(new String(result));
    }

    private static int prevEndByte = 0;

    public static byte[] traverse(TSNode node, byte[] code, ByteArrayOutputStream parts) throws IOException {
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
