package com.example;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterJavascript;

public class TSPlaygroundTest {
    TSPlayground playground = new TSPlayground();

    @Nested
    public class TSPlaygroundForJavascript {
        TSLanguage javascript = new TreeSitterJavascript();

        @Test
        @DisplayName("単一行コメントを削除できること")
        void testRemoveSingleLineComment() throws IOException {
            byte[] bytes = """
                    // COMMENT HERE
                    const s = "STRING HERE"; // COMMENT HERE""".getBytes(StandardCharsets.UTF_8);

            String code = new String(bytes, StandardCharsets.UTF_8);
            var result = playground.removeComment(javascript, code);

            String expect = """
                    const s = "STRING HERE";""";

            assertThat(result.trim()).isEqualTo(expect);
        }
        
        @Test
        @DisplayName("複数行コメントを削除できること")
        void testRemoveMultiLineComment() throws IOException {
            byte[] bytes = """
                    /*
                    COMMENT HERE
                    COMMENT HERE
                    */
                    const s = "STRING HERE"; /* COMMENT HERE */""".getBytes(StandardCharsets.UTF_8);

            String code = new String(bytes, StandardCharsets.UTF_8);
            var result = playground.removeComment(javascript, code);

            String expect = """
                    const s = "STRING HERE";""";

            assertThat(result.trim()).isEqualTo(expect);
        }

        @Test
        @DisplayName("イカれたコメントを削除できること")
        void testRemoveCrazyComment() throws IOException {
            byte[] bytes = """
                    var add = function(a, b) {
                        return a +
                        // CRAZY COMMENT
                        b;
                    }""".getBytes(StandardCharsets.UTF_8);

            String code = new String(bytes, StandardCharsets.UTF_8);
            var result = playground.removeComment(javascript, code);

            String expect = """
                    var add = function(a, b) {
                        return a +
                        b;
                    }""";

            assertThat(result.trim()).isEqualTo(expect);
        }
        
        @Test
        @DisplayName("構文エラーを含むコードでもコメントを削除できること")
        void testRemoveCommentWithInvalidSyntax() throws IOException {
            byte[] bytes = """
                    // 文字列が閉じられていない + 閉じカッコがない
                    alert("foo;
                    /*
                    閉じカッコを忘れたメソッド
                    */
                    function ( { console.log("foo"); }
                    """.getBytes(StandardCharsets.UTF_8);

            String code = new String(bytes, StandardCharsets.UTF_8);
            var result = playground.removeComment(javascript, code);

            String expect = """
                    alert("foo;
                    function ( { console.log("foo"); }""";

            assertThat(result.trim()).isEqualTo(expect);
        }

        @Test
        @DisplayName("サロゲートペア（絵文字）が混入してもコメントを削除できること")
        void testRemoveCommentWithEmoji() throws IOException {
            byte[] bytes = """
                    // 😭
                    alert("STRING HERE");
                    """.getBytes(StandardCharsets.UTF_8);

            String code = new String(bytes, StandardCharsets.UTF_8);
            var result = playground.removeComment(javascript, code);

            String expect = """
                    alert("STRING HERE");""";

            assertThat(result.trim()).isEqualTo(expect);
        }
    }

}
