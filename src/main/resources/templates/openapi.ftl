<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>openapi</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/4.5.2/css/bootstrap.min.css">
    <style>
        body {
            display: flex;
            justify-content: center;
            align-items: center;
            background-color: #caf1cc;
            color: #0d1f0d;
        }

        pre {
            background-color: #f4f4f4;
            padding: 20px;
            border-radius: 10px;
            white-space: pre-wrap;
            word-wrap: break-word;
            font-family: Consolas, monospace;
        }
    </style>
</head>
<body>
<div>
    <pre id="json-data"></pre>
</div>

<script>
    var openapi = ${openapi};

    // 格式化JSON数据
    var formattedJson = JSON.stringify(openapi, null, 4);

    // 将格式化后的JSON渲染到页面
    document.getElementById("json-data").textContent = formattedJson;
</script>
</body>
</html>
