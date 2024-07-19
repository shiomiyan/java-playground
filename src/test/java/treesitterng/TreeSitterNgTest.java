package treesitterng;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.treesitter.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class TreeSitterNgTest {
    @Test
    void emojiOverflowTest() {
        // https://github.com/bonede/tree-sitter-ng/issues/36

        TSParser parser = new TSParser();
        TSLanguage javascript = new TreeSitterJavascript();
        parser.setLanguage(javascript);

        String code = """
                // 😭
                a();
                // 😭
                b();
                // 😭
                c();
                // 😭
                d();""";

        TSTree tree = parser.parseString(null, code);
        TSNode rootNode = tree.getRootNode();
        int startByte = rootNode.getStartByte();
        int endByte = rootNode.getEndByte();

        byte[] codeBytes = code.getBytes(StandardCharsets.UTF_8);
        byte[] rootNodeBytes = Arrays.copyOfRange(codeBytes, startByte, endByte);

        assertThat(new String(rootNodeBytes, StandardCharsets.UTF_8)).isEqualTo(code);
    }

    @Test
    void emojiTest() {
        // https://github.com/bonede/tree-sitter-ng/issues/36

        TSParser parser = new TSParser();
        TSLanguage javascript = new TreeSitterJavascript();
        parser.setLanguage(javascript);

        String code = """
                // 😭
                foo();""";

        TSTree tree = parser.parseString(null, code);
        TSNode rootNode = tree.getRootNode();

        TSNode commentNode = rootNode.getChild(0);
        int startByte = commentNode.getStartByte();
        int endByte = commentNode.getEndByte();

        byte[] codeBytes = code.getBytes(StandardCharsets.UTF_8);
        byte[] commentNodeBytes = Arrays.copyOfRange(codeBytes, startByte, endByte);
        String commentString = new String(commentNodeBytes, StandardCharsets.UTF_8);

        assertThat(commentString).isEqualTo("// 😭");
    }

    @Test
    void jsonEmojiTest() {
        // https://github.com/bonede/tree-sitter-ng/issues/36

        TSParser parser = new TSParser();
        TSLanguage javascript = new TreeSitterJson();
        parser.setLanguage(javascript);

        String code = """
                ["😭", "foo"]""";

        TSTree tree = parser.parseString(null, code);
        TSNode rootNode = tree.getRootNode();

        TSNode commentNode = rootNode.getChild(0);
        int startByte = commentNode.getStartByte();
        int endByte = commentNode.getEndByte();

        byte[] codeBytes = code.getBytes(StandardCharsets.UTF_8);
        byte[] commentNodeBytes = Arrays.copyOfRange(codeBytes, startByte, endByte);
        String commentString = new String(commentNodeBytes, StandardCharsets.UTF_8);

        assertThat(commentString).isEqualTo("[\"😭\", \"foo\"]");
    }


    @Nested
    public class GettingStarted {
        TSParser parser = new TSParser();
        TSLanguage javascript = new TreeSitterJavascript();

        @BeforeEach
        void init() {
            parser.setLanguage(javascript);
        }

        @Test
        void testGetCommentNode() {
            String code = """
                    // COMMENT HERE
                    var s;
                    """;
            TSTree tree = parser.parseStringEncoding(null, code, TSInputEncoding.TSInputEncodingUTF8);
            TSNode rootNode = tree.getRootNode();

            assertThat(rootNode.getChildCount()).isEqualTo(2);

            String syntaxTree = "(program (comment) (variable_declaration (variable_declarator name: (identifier))))";
            assertThat(rootNode.toString()).isEqualTo(syntaxTree);

            // 最初のノード、つまりはコメントノードを取得する
            TSNode commentNode = rootNode.getChild(0);
            assertThat(commentNode.getStartByte()).isEqualTo(0);
            assertThat(commentNode.getEndByte()).isEqualTo(15);

            // バイト配列から部分文字列を取り出す
            byte[] codeBytes = code.getBytes(StandardCharsets.UTF_8);
            byte[] commentBytes = Arrays.copyOfRange(codeBytes, commentNode.getStartByte(), commentNode.getEndByte());
            assertThat(new String(commentBytes, StandardCharsets.UTF_8)).isEqualTo("// COMMENT HERE");
        }
    }
}
