package com.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.treesitter.TSLanguage;
import org.treesitter.TreeSitterJavascript;

public class TSPlaygroundTest {

    @Nested
    public class TSPlaygroundForJavascript {

        TSLanguage javascript = new TreeSitterJavascript();

        @Test
        @DisplayName("単一行コメントを削除できる")
        void testRemoveSingleLineComment() throws IOException {
            byte[] bytes = """
                // COMMENT HERE
                const s = "STRING HERE"; // COMMENT HERE""".getBytes(StandardCharsets.UTF_8);

            String code = new String(bytes, StandardCharsets.UTF_8);
            var result = TSPlayground.removeComment(javascript, code);

            String expect = """
                const s = "STRING HERE";""";

            assertThat(result.trim()).isEqualTo(expect);
        }

        @Test
        @DisplayName("複数行コメントを削除できる")
        void testRemoveMultiLineComment() throws IOException {
            byte[] bytes = """
                /*
                COMMENT HERE
                COMMENT HERE
                */
                const s = "STRING HERE"; /* COMMENT HERE */""".getBytes(StandardCharsets.UTF_8);

            String code = new String(bytes, StandardCharsets.UTF_8);
            var result = TSPlayground.removeComment(javascript, code);

            String expect = """
                const s = "STRING HERE";""";

            assertThat(result.trim()).isEqualTo(expect);
        }

        @Test
        @DisplayName("イカれたコメントを削除できる")
        void testRemoveCrazyComment() throws IOException {
            byte[] bytes = """
                var add = function(a, b) {
                    return a +
                    // CRAZY COMMENT
                    b;
                }""".getBytes(StandardCharsets.UTF_8);

            String code = new String(bytes, StandardCharsets.UTF_8);
            var result = TSPlayground.removeComment(javascript, code);

            String expect = """
                var add = function(a, b) {
                    return a +
                    b;
                }""";

            assertThat(result.trim()).isEqualTo(expect);
        }

        @Test
        @DisplayName("構文エラーを含むコードでもコメントを削除できる")
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
            var result = TSPlayground.removeComment(javascript, code);

            String expect = """
                alert("foo;
                function ( { console.log("foo"); }""";

            assertThat(result.trim()).isEqualTo(expect);
        }

        @Test
        @DisplayName("サロゲートペア（絵文字）が混入してもコメントを削除できる（UTF-8）")
        void testRemoveCommentWithEmojiUtf8() throws IOException {
            String yoshi = Character.toString(0x20bb7);
            byte[] bytes = String.format("""
                    // ほっけ[%s]
                    alert("STRING HERE"); // 白い笑顔[%s]
                    /* はしご高[%s] */
                    function foo() {
                        var %s = "ﾖｼ";
                    }
                    """, Character.toString(0x29E3D), Character.toString(0x0263A),
                Character.toString(0x09AD9), yoshi).getBytes(StandardCharsets.UTF_8);

            String code = new String(bytes, StandardCharsets.UTF_8);
            var result = TSPlayground.removeComment(javascript, code);

            String expect = String.format("""
                alert("STRING HERE");
                function foo() {
                    var %s = "ﾖｼ";
                }""", yoshi);

            assertThat(result.trim()).isEqualTo(expect);
        }

        @Test
        @DisplayName("サロゲートペア（絵文字）が混入してもコメントを削除できる（UTF-16）")
        void testRemoveCommentWithEmojiUtf16() throws IOException {
            String yoshi = Character.toString(0x20bb7);
            byte[] bytes = String.format("""
                    // ほっけ[%s]
                    alert("STRING HERE"); // 白い笑顔[%s]
                    /* はしご高[%s] */
                    function foo() {
                        var %s = "ﾖｼ";
                    }
                    """, Character.toString(0x29E3D), Character.toString(0x0263A),
                Character.toString(0x09AD9), yoshi).getBytes(StandardCharsets.UTF_16);

            String code = new String(bytes, StandardCharsets.UTF_16);
            var result = TSPlayground.removeComment(javascript, code);

            String expect = String.format("""
                alert("STRING HERE");
                function foo() {
                    var %s = "ﾖｼ";
                }""", yoshi);

            assertThat(result.trim()).isEqualTo(expect);
        }

        @Test
        @DisplayName("サロゲートペア（絵文字）が混入してもコメントを削除できる（Shift_JIS）")
        void testRemoveCommentWithEmojiShiftJIS() throws IOException {
            String shikaru = Character.toString(0x20b9f);
            byte[] bytes = String.format("""
                    // ほっけ[%s]
                    alert("STRING HERE"); // 白い笑顔[%s]
                    /* はしご高[%s] */
                    function foo() {
                        var %s = "ｼｶﾙ";
                    }
                    """, Character.toString(0x29E3D), Character.toString(0x0263A),
                Character.toString(0x09AD9), shikaru).getBytes("SJIS");

            String code = new String(bytes, "SJIS");
            var result = TSPlayground.removeComment(javascript, code);

            String expect = new String("""
                alert("STRING HERE");
                function foo() {
                    var %s = "ｼｶﾙ";
                }""".formatted(shikaru).getBytes("SJIS"), "SJIS");

            assertThat(result.trim()).isEqualTo(expect);
        }


        @Test
        @DisplayName("だいたいいい感じにコメントを削除できる")
        void testRemoveCommentBrokenSyntax1() throws IOException {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            byte[] bytes = classloader.getResourceAsStream("broken-syntax-1.js").readAllBytes();

            String code = new String(bytes, StandardCharsets.UTF_8);
            var result = TSPlayground.removeComment(javascript, code);

            String expect = """
                const s = "STRING HERE";
                人類社会のすべての構成員の固有の尊厳と平等で譲ることのできない権利とを承認することは
                alert();
                alert(";
                function foo( {
                    console.log("// This is not comment.");
                }""";

            assertThat(result.trim()).isEqualTo(expect);
        }

        @Test
        @DisplayName("だいたいいい感じにコメントを削除できる")
        void testRemoveCommentBrokenSyntax2() throws IOException {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            byte[] bytes = classloader.getResourceAsStream("broken-syntax-2.js").readAllBytes();

            String code = new String(bytes, StandardCharsets.UTF_8);
            var result = TSPlayground.removeComment(javascript, code);

            String expect = """
                (function() {
                    var x = 42
                        y = (function() {
                            return x * 2
                        })()
                                
                        z = (() => {
                            try {
                                return JSON.parse(
                                    '{"a": 1, "b": "2", "c": [3, 4, {"d": 5}]}'
                                ).c[2].d
                            catch (e) {
                                return 0
                            }
                        })()
                                
                    var result = y +
                        z;
                                
                    setTimeout(() => {
                        console.log(result);
                                
                        new Promise((resolve, reject) => {
                            Math.random() > 0.5 ?
                                resolve('Success') :
                                reject('Failure')
                        }).then(msg => {
                            console.log(msg);
                        }).catch(err => {
                            console.warn(err);
                        }
                    }, 100)
                                
                    var a = Math.random() > 0.5 ?
                        (Math.random() > 0.5 ? 'A' : 'B') : 'C'
                                
                    console.log(a);
                                
                    var factorial = n =>
                        n <= 1 ? 1 :
                        n * factorial(n - 1
                    console.log(factorial(5))
                })();""";

            assertThat(result.trim()).isEqualTo(expect);
        }

        @Test
        @DisplayName("JQueryでもいけちゃうよ")
        void testRemoveCommentFromJQuery() throws IOException {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            byte[] bytes = classloader.getResourceAsStream("comment-jquery.js").readAllBytes();

            String code = new String(bytes, StandardCharsets.UTF_8);
            var result = TSPlayground.removeComment(javascript, code);

            String expect = """
                $(document).ready(function() {
                    $("#changeTextButton").click(
                        function() {
                        $("#message").text(
                            "AWESOME MESSAGE!");
                    });
                });""";

            assertThat(result.trim()).isEqualTo(expect);
        }
    }
}
