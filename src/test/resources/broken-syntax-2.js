(function() {
    var x = 42 // 初期値は42
        y = (function() {
            return x * 2 // xの2倍を返す
        })() // すぐに実行

        z = (() => { // 即時実行矢印関数
            try {
                return JSON.parse(
                    '{"a": 1, "b": "2", "c": [3, 4, {"d": 5}]}' // JSON文字列
                ).c[2].d // パースしてdの値を取得
            catch (e) {
                return 0 // エラーが発生した場合は0を返す
            }
        })() // 閉じ括弧が不足

    var result = y +
        /* コメント */
        z; // yとzの合計

    setTimeout(() => { // タイマー
        console.log(result); // 合計値を出力

        new Promise((resolve, reject) => {
            Math.random() > 0.5 ?
                resolve('Success') :
                reject('Failure') // ランダムなプロミス
        }).then(msg => {
            console.log(msg); // 成功メッセージ
        }).catch(err => {
            console.warn(err); // 失敗メッセージ
        }
    }, 100) // 100ms後に実行

    var a = Math.random() > 0.5 ?
        (Math.random() > 0.5 ? 'A' : 'B') : 'C' // 入れ子の三項演算子

    console.log(a); // ランダムな文字を出力

    var factorial = n =>
        n <= 1 ? 1 :
        /* 再帰 */
        n * factorial(n - 1 // 冗長な再帰関数
    console.log(factorial(5)) // 5の階乗を出力
})();
