function keyLogin() {
    if (event.keyCode == 13 && $('#userId').val() !== "" && $('#password').val() !== "") {
        document.getElementById("oncgi").click();
    }
}

$('#oncgi').click(function () {
    $.post("/index.html",
        {userId: $('#userId').val(), password: $('#password').val()},
        function (data, status) {
            console.log(("【已发送POST请求】\n返回数据为: \n" + data + "\n状态为: " + status));
            var res = data.split(" ");
            console.log(res);
            alert(
                "上海大学学号为【" + res[0] + "】的基本信息于下：" +
                "\n学号： " + res[0] +
                "\n姓名： " + res[1] +
                "\n职业： " + res[2] +
                "\n所属学院： " + res[3]
            )
        });
});

