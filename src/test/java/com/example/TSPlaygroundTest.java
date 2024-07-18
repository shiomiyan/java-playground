package com.example;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.*;
import org.treesitter.*;

public class TSPlaygroundTest {

    @Nested
    public class TSPlaygroundForJavascript {
        TSPlayground playground = new TSPlayground();
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
        @DisplayName("サロゲートペア（絵文字）が混入してもコメントを削除できること（UTF-8）")
        void testRemoveCommentWithEmojiUtf8() throws IOException {
            String yoshi = Character.toString(0x20bb7);

            byte[] bytes = String.format("""
                    // ほっけ[%s]
                    alert("STRING HERE"); // 白い笑顔[%s]
                    /* はしご高[%s] */
                    function foo() {
                        var %s = "ﾖｼ";
                    }
                    """,
                    Character.toString(0x29E3D),
                    Character.toString(0x0263A),
                    Character.toString(0x09AD9),
                    yoshi
            ).getBytes(StandardCharsets.UTF_8);

            String code = new String(bytes, StandardCharsets.UTF_8);
            var result = playground.removeComment(javascript, code);

            String expect = String.format("""
                    alert("STRING HERE");
                    function foo() {
                        var %s = "ﾖｼ";
                    }""", yoshi);

            assertThat(result.trim()).isEqualTo(expect);
        }

        @Test
        @DisplayName("サロゲートペア（絵文字）が混入してもコメントを削除できること（UTF-16）")
        void testRemoveCommentWithEmojiUtf16() throws IOException {
            byte[] bytes = """
                    // 😭
                    alert("STRING HERE");
                    """.getBytes(StandardCharsets.UTF_16);

            String code = new String(bytes, StandardCharsets.UTF_16);
            var result = playground.removeComment(javascript, code);

            String expect = """
                    alert("STRING HERE");""";

            assertThat(result.trim()).isEqualTo(expect);
        }
    }

}
