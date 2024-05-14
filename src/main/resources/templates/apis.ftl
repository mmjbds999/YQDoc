<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="Pragma" content="no-cache"/>
    <meta http-equiv="Cache-Control" content="no-cache"/>
    <meta http-equiv="Expires" content="0"/>
    <meta http-equiv="Cache-Control" content="must-revalidate"/>
    <title>YQDoc - API文档</title>
    <link rel="stylesheet" type="text/css" href="/webjars/bootstrap/3.3.6/css/bootstrap.min.css"/>
    <script src="/webjars/jquery/1.11.1/jquery.min.js" type="text/javascript"></script>
    <script src="/webjars/bootstrap/3.3.6/js/bootstrap.min.js" type="text/javascript"></script>
    <style>

        pre {
            overflow-x: auto;
            white-space: pre-wrap;
            white-space: -moz-pre-wrap;
            white-space: -o-pre-wrap;
            word-wrap: break-word;
        }

    </style>
</head>
<body>
<div class="container">
    <div class="row clearfix">
        <div class="col-lg-12 column">
            <div class="page-header">
                <h1>
                    <strong>YQDoc-API 文档</strong>
                        <small>请搜索您的授权方法获取token填入下方</small>
                    </strong>
                </h1>
                <input class="form-control" value="${token}" type="text" id="token" placeholder="token">
                <p>
                    <strong>
                        说明：<color style="color: green">YQCode-Doc</color>是无侵入式的API文档，只要正常写注释即可得到文档和相关参数的描述
                    </strong>
                </p>
            </div>
            <div class="tabbable" id="tabs-367884">
                <div class="row" style="margin-bottom: 20px">
                    <div class="col-md-4">
                        <div class="input-group">
                            <input class="form-control" value="${keyWord}" type="text" id="searchInput" placeholder="输入关键字查询">
                            <span class="input-group-btn">
                                <button class="btn btn-info" id="searchBtn">搜索</button>
                                <button class="btn btn-default" id="resetBtn">重置</button>
                            </span>
                        </div>
                    </div>
                </div>
                <ul class="nav nav-tabs">
                    <#list allApi?keys as a>
                        <li <#if a_index==0>class="active"</#if>>
                            <a rel="nofollow" href="#panel-${a}" data-toggle="tab">${a?replace('microservice-','')}</a>
                        </li>
                    </#list>
                </ul>
                <div class="tab-content" style="margin-top: 5px">
                    <#list allApi?keys as a>
                        <div class="tab-pane<#if a_index==0> active</#if>" id="panel-${a}">
                            <div class="panel-group" id="panel-${a}-1">
                                <#list allApi[a] as k>
                                    <div class="panel panel-default">
                                        <div class="panel-heading">
                                            <div class="container-fluid" style="padding-left:0px">
                                                <div class="col-md-4" style="padding-left:0px">
                                                    <a rel="nofollow" class="panel-title" data-toggle="collapse"
                                                       data-parent="#panel-${a}-1"
                                                       href="#panel-element-${a}-${k_index}">${k.requestUrl!}</a>
                                                </div>
                                                <div class="col-md-8" style="padding-left:0px">
                                                    <span style="color: darkgoldenrod">【${k.description}】</span>
                                                </div>
                                            </div>
                                        </div>
                                        <div id="panel-element-${a}-${k_index}" class="panel-collapse collapse">
                                            <div class="panel-body">
                                                <form>
                                                    <label><span>调用方式：</span></label>
                                                    <select id="methodSelect_${a}-${k_index}" class="span2">
                                                        <#list k.requestType as ty>
                                                            <option value="${ty}">${ty}</option>
                                                        </#list>
                                                    </select>
                                                </form>
                                                <form role="form" id="form_${a}-${k_index}">
                                                    <#list k.methodParams as param>
                                                        <div class="form-group">
                                                            <label><span style="color: #99ccaa">【${param.comment}】</span><span>${param.name}：</span></label>
                                                            <label style="color: chocolate;float: right">
                                                                <span>${param.type}</span>
                                                            </label>
                                                            <#if param.type=='org.springframework.web.multipart.MultipartFile'>
                                                                <input id="upfile_${a}-${k_index}" type="file" name="file"/>
                                                            <#else>
                                                                <#if param.name?contains(".")>
                                                                    <#assign paramName = param.name?substring(param.name?last_index_of(".") + 1)>
                                                                <#else>
                                                                    <#assign paramName = param.name>
                                                                </#if>
                                                                <input type="text" class="form-control" id="${paramName}" name="${paramName}" placeholder="${param.comment}"/>
                                                            </#if>
                                                        </div>
                                                    </#list>
                                                </form>
                                                <input class="btn btn-primary" type="button" value="执行" onclick="doApi('${a}-${k_index}', '', '${a}', '${k.requestUrl}', false, '${k.contentType}');">
<#--                                                <input style="color: green" class="btn btn-default" type="button" value="mock" onclick="doApi('${a}-${k_index}', '', '${a}', '${k.requestUrl}', true);">-->
                                                <br/><br/>
                                                <span>返回结果：</span>
                                                <br/>
                                                <div class="row clearfix">
                                                    <div class="col-md-12 column">
                                                            <pre id="result_${a}-${k_index}">

                                                            </pre>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </#list>
                            </div>
                        </div>
                    </#list>
                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>

<script type="text/javascript">
    $.fn.serializeObject = function () {
        let o = {};
        let a = this.serializeArray();
        $.each(a, function () {
            if (o[this.name]) {
                if (!o[this.name].push) {
                    o[this.name] = [o[this.name]];
                }
                o[this.name].push(this.value || '');
            } else {
                o[this.name] = this.value || '';
            }
        });
        return o;
    };

    $("#searchInput").keydown(function(event) {
        if (event.key === "Enter") {
            event.preventDefault();
            document.getElementById("searchBtn").click();
        }
    });

    $("#searchBtn").click(function () {
        window.location.href = '${baseUrl}/doc?token=' + $("#token").val() + '&keyWord=' + $("#searchInput").val();
    })

    $("#resetBtn").click(function() {
        window.location.href = '${baseUrl}/doc?token=' + $("#token").val();
    });


    function doApi(index, method, server, api, isMock, contentType) {
        if($("#token").val()){
            $.ajax({
                type: "GET",
                url: "${baseUrl}/doc/saveToken?token=" + $("#token").val(),
                headers: {
                    Authorization: 'Bearer ' + $("#token").val()
                }
            });
        }
        $("#result_" + index).text("系统正在执行，请稍后。。。");
        let url = '${baseUrl}' + api;

        if ($('#upfile_' + index).length > 0) {
            //alert("upfile");
            let form = $('#form_' + index)[0];
            let data = new FormData(form);

            $('#form_' + index).find("input[type='button']").prop("disabled", true);
            $.ajax({
                type: "POST",
                enctype: 'multipart/form-data;charset=utf-8',
                url: url,
                data: data,
                processData: false,
                contentType: false,
                cache: false,
                timeout: 600000,
                headers: {
                    Authorization: 'Bearer ' + $("#token").val()
                },
                success: function (data) {
                    $("#result_" + index).text(JSON.stringify(data, null, 4));
                    $('#form_' + index).find("input[type='button']").prop("disabled", false);
                },
                error: function (e) {
                    $("#result_" + index).text(e.responseText);
                    console.log("ERROR : ", e);
                    $('#form_' + index).find("input[type='button']").prop("disabled", false);
                }
            });
        } else {
            let data = $("#form_" + index).serialize();

            let jo = $("#form_" + index).serializeObject();

            method = $("#methodSelect_" + index).val();
            for (var key in jo) {
                url = url.replace("{" + key + "}", jo[key]);
            }

            if(contentType === "application/json"){
                data = JSON.stringify(jo);
            }

            $.ajax({
                url: url,
                contentType: contentType,
                type: method,
                data: data,
                timeout: 600000,
                dataType: "json",
                headers: {
                    Authorization: 'Bearer ' + $("#token").val()
                },
                success: function (d) {
                    $("#result_" + index).text(JSON.stringify(d, null, 4));
                },
                error: function (data) {
                    $("#result_" + index).text(JSON.stringify(data, null, 4));
                }
            });
        }
    }

</script>