<#--第一行DOCTYPE是声明-->
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>支付</title>
</head>
<body>
<div id="myQrcode"></div>
<div id="orderId" hidden>${orderId}</div>
<div id="returnUrl" hidden>${returnUrl}</div><#--完成后在payController修改map。且会在网页上渲染出来-->
<#--引入js，js不用下载，在BootCDN网站上找标签，选择1.5.1的压缩版本-->
<script src="https://cdn.bootcss.com/jquery/1.5.1/jquery.min.js"></script>
<#--还需引入qrcode-->
<script src="https://cdn.bootcss.com/jquery.qrcode/1.0/jquery.qrcode.min.js"></script>
<#--以下是js的用法-->
<script>
    jQuery('#myQrcode').qrcode({
        text     : "${codeUrl}"    // 对该字符串生成二维码
        });
    <#--支付成功后跳转页面，不停请求后端api-->
    $(function () {
        //定时器,2秒一次请求
        setInterval(function () {
            console.log("开始查询支付状态...")
            $.ajax({
                url: 'http://localhost:8080/pay/queryByOrderId',
                data: {
                    'orderId': $('#orderId').text() //要把订单Id传进来。通过渲染传进来,见上面的div语句
                },
                success: function (result) {
                    console.log(result)
                    //对支付状态进行判断  两个等号不会判断类型
                    if (result.platformStatus!=null && result.platformStatus==='SUCCESS'){
                        //如果想在网页地址加入orderId信息，可以在下行加入。怎么加入网上查，这里没写
                        location.href=$('#returnUrl').text()
                    }
                },
                error: function (result) {
                    alert(result)
                }
            })
        },2000)
        });
</script>
</body>
</html>