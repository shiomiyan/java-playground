package com.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        // System.out.println(codeContent);

        TSParser parser = new TSParser();
        TSLanguage javascript = new TreeSitterJavascript();
        parser.setLanguage(javascript);

        TSTree tree = parser.parseStringEncoding(null, codeContent, TSInputEncoding.TSInputEncodingUTF8);
        TSNode rootNode = tree.getRootNode();

        var parts = new ByteArrayOutputStream();
        var result = traverse(rootNode, bytes, parts);
        System.out.println(new String(result));

        //for (int i = 0; i < rootNode.getChildCount(); i++) {
        //    var node = rootNode.getChild(i);
        //    var type = node.getType();

        //    if (type.equalsIgnoreCase("comment")) {
        //        var startByte = node.getStartByte();
        //        var endByte = node.getEndByte();
        //        System.out.println(type);
        //        System.out.println("startByte: " + startByte);                
        //        System.out.println("endByte: " + endByte);

        //        var commentBytes = Arrays.copyOfRange(bytes, startByte, endByte);
        //        var comment = new String(commentBytes, StandardCharsets.UTF_8);

        //        System.out.println(comment);
        //    }
        //}
    }

    public static byte[] traverse(TSNode node, byte[] code, ByteArrayOutputStream parts) throws IOException {
        int startByte = node.getStartByte();
        int endByte = node.getEndByte();
        boolean hasChild = node.getChildCount() > 0;

        if (hasChild) {
            for (var i = 0; i < node.getChildCount(); i++) {
                traverse(node.getChild(i), code, parts);
            }
        } else {
            if (!node.getType().equalsIgnoreCase("comment")) {
                parts.write(Arrays.copyOfRange(code, startByte, endByte));
            }
        }

        return parts.toByteArray();
    }
}