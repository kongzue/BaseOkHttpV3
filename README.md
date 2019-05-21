# BaseOkHttp V3

<a href="https://github.com/kongzue/BaseOkHttp/">
<img src="https://img.shields.io/badge/BaseOkHttp-3.1.0-green.svg" alt="BaseOkHttp">
</a>
<a href="https://bintray.com/myzchh/maven/BaseOkHttp_v3/3.1.0/link">
<img src="https://img.shields.io/badge/Maven-3.1.0-blue.svg" alt="Maven">
</a>
<a href="http://www.apache.org/licenses/LICENSE-2.0">
<img src="https://img.shields.io/badge/License-Apache%202.0-red.svg" alt="License">
</a>
<a href="http://www.kongzue.com">
<img src="https://img.shields.io/badge/Homepage-Kongzue.com-brightgreen.svg" alt="Homepage">
</a>

## 简介
- BaseOkHttp V3是基于BaseOkHttp V2( https://github.com/kongzue/BaseOkHttp )的升级版本，基于能够快速创建常用请求链接而封装的库。
- 本库中自带 OkHttp 库，并对其关联的 okio 库进行了包名的修改和封装，因此不会影响到您项目中的其他版本的 okHttp 库，亦不会产生冲突。
- 若请求来自于一个 Activity，结束请求后自动回归主线程操作，不需要再做额外处理。
- 提供统一返回监听器ResponseListener处理返回数据，避免代码反复臃肿。
- 强大的全局方法和事件让您的请求得心应手。

## Maven仓库或Gradle的引用方式
Maven仓库：
```
<dependency>
  <groupId>com.kongzue.baseokhttp_v3</groupId>
  <artifactId>baseokhttp_v3</artifactId>
  <version>3.1.0</version>
  <type>pom</type>
</dependency>
```
Gradle：

在dependencies{}中添加引用：
```
implementation 'com.kongzue.baseokhttp_v3:baseokhttp_v3:3.1.0'
```

![BaseOkHttpV3 Demo](https://github.com/kongzue/Res/raw/master/app/src/main/res/mipmap-xxxhdpi/baseokhttpv3demo.png)

试用版可以前往 https://fir.im/BaseOkHttp3 下载

## 目录
· <a href="#一般请求">一般请求</a>

· <a href="#json请求">JSON请求</a>

· <a href="#文件上传">文件上传</a>

· <a href="#文件下载">文件下载</a>

· <a href="#putdelete">PUT&DELETE</a>

· <a href="#websocket">WebSocket</a>

· <a href="#json解析">JSON解析</a>

· <a href="#额外功能">额外功能</a>

···· <a href="#全局日志">全局日志</a>

···· <a href="#全局请求地址">全局请求地址</a>

···· <a href="#全局-Header-请求头">全局 Header 请求头</a>

···· <a href="#全局请求返回拦截器">全局请求返回拦截器</a>

···· <a href="#HTTPS-支持">HTTPS 支持</a>

···· <a href="#全局参数拦截器">全局参数拦截器</a>

···· <a href="#请求超时">请求超时</a>

···· <a href="#停止请求">停止请求</a>

· <a href="#开源协议">开源协议</a>

· <a href="#更新日志">更新日志</a>

## 一般请求
BaseOkHttp V3 提供两种请求写法，范例如下：

以参数形式创建请求：
```
progressDialog = ProgressDialog.show(context, "请稍候", "请求中...");
HttpRequest.POST(context, "http://你的接口地址", new Parameter().add("page", "1"), new ResponseListener() {
    @Override
    public void onResponse(String response, Exception error) {
        progressDialog.dismiss();
        if (error == null) {
            resultHttp.setText(response);
        } else {
            resultHttp.setText("请求失败");
            Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
        }
    }
});
```
一般请求中，使用 HttpRequest.POST(...) 方法可直接创建 POST 请求，相应的，HttpRequest.GET(...) 可创建 GET 请求，另外可选额外的方法增加 header 请求头：
```
HttpRequest.POST(Context context, String url, Parameter headers, Parameter parameter, ResponseListener listener);
HttpRequest.GET(Context context, String url, Parameter headers, Parameter parameter, ResponseListener listener);
```

或者也可以以流式代码创建请求：
```
progressDialog = ProgressDialog.show(context, "请稍候", "请求中...");
HttpRequest.build(context,"http://你的接口地址")
        .addHeaders("Charset", "UTF-8")
        .addParameter("page", "1")
        .addParameter("token", "A128")
        .setResponseListener(new ResponseListener() {
            @Override
            public void onResponse(String response, Exception error) {
                progressDialog.dismiss();
                if (error == null) {
                    resultHttp.setText(response);
                } else {
                    resultHttp.setText("请求失败");
                    Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
                }
            }
        })
        .doPost();
```
返回回调监听器只有一个，请在其中对 error 参数判空，若 error 不为空，则为请求失败，反之则为请求成功，请求成功后的数据存放在 response 参数中。

之所以将请求成功与失败放在一个回调中主要目的是方便无论请求成功或失败都需要执行的代码，例如上述代码中的 progressDialog 等待对话框都需要关闭（dismiss掉），这样的写法更为方便。

3.1.0 版本起提供直接解析返回值为 jsonMap 对象，可使用 JsonResponseListener 监听器返回：
```
HttpRequest.POST(context, "/femaleNameApi", new Parameter().add("page", "1"), new JsonResponseListener() {
    @Override
    public void onResponse(JsonMap main, Exception error) {
        if (error == null) {
            resultHttp.setText(main.getString("msg"));
        } else {
            resultHttp.setText("请求失败");
            Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
        }
    }
});
```

## JSON请求
有时候我们需要使用已经处理好的json文本作为请求参数，此时可以使用 HttpRequest.JSONPOST(...) 方法创建 json 请求。

json 请求中，参数为文本类型，创建请求方式如下：
```
progressDialog = ProgressDialog.show(context, "请稍候", "请求中...");
HttpRequest.JSONPOST(context, "http://你的接口地址", "{\"key\":\"DFG1H56EH5JN3DFA\",\"token\":\"124ASFD53SDF65aSF47fgT211\"}", new ResponseListener() {
    @Override
    public void onResponse(String response, Exception error) {
        progressDialog.dismiss();
        if (error == null) {
            resultHttp.setText(response);
        } else {
            resultHttp.setText("请求失败");
            Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
        }
    }
});
```
Json请求中，可使用 HttpRequest.JSONPOST(...) 快速创建 Json 请求，另外可选额外的方法增加 header 请求头：
```
HttpRequest.JSONPOST(Context context, String url, Parameter headers, String jsonParameter, ResponseListener listener)
```

也可以使用流式代码创建请求：
```
progressDialog = ProgressDialog.show(context, "请稍候", "请求中...");
HttpRequest.build(context,"http://你的接口地址")
        .setJsonParameter("{\"key\":\"DFG1H56EH5JN3DFA\",\"token\":\"124ASFD53SDF65aSF47fgT211\"}")
        .setResponseListener(new ResponseListener() {
            @Override
            public void onResponse(String response, Exception error) {
                progressDialog.dismiss();
                if (error == null) {
                    resultHttp.setText(response);
                } else {
                    resultHttp.setText("请求失败");
                    Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
                }
            }
        })
        .doPost();
```
因需要封装请求体，Json请求只能以非 GET 请求的方式进行。

## 文件上传
要使用文件上传就需要将 File 类型的文件作为参数传入 Parameter，此时参数中亦可以传入其他文本类型的参数。

因需要封装请求体，文件上传只能以非 GET 请求的形式发送。

范例代码如下：
```
progressDialog = ProgressDialog.show(context, "请稍候", "请求中...");
HttpRequest.POST(context, "http://你的接口地址", new Parameter()
                         .add("key", "DFG1H56EH5JN3DFA")
                         .add("imageFile1", file1)
                         .add("imageFile2", file2)
        , new ResponseListener() {
            @Override
            public void onResponse(String response, Exception error) {
                progressDialog.dismiss();
                if (error == null) {
                    resultHttp.setText(response);
                } else {
                    resultHttp.setText("请求失败");
                    Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
```

也可以使用流式代码创建请求：
```
HttpRequest.build(context,"http://你的接口地址")
        .addHeaders("Charset", "UTF-8")
        .addParameter("page", "1")
        .addParameter("imageFile1", file1)
        .addParameter("imageFile2", file2)
        .setResponseListener(new ResponseListener() {
            @Override
            public void onResponse(String response, Exception error) {
                progressDialog.dismiss();
                if (error == null) {
                    resultHttp.setText(response);
                } else {
                    resultHttp.setText("请求失败");
                    Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
                }
            }
        })
        .doPost();
```

默认上传文件使用的 mediaType 为 "image/png"，可使用以下代码进行修改：
```
.setMediaType(MediaType.parse("application/pdf"))       //设置为pdf类型
```

类型参考如下：

内容 | 含义
---|---
text/html | HTML格式
text/plain | 纯文本格式
text/xml |  XML格式
image/gif | gif图片格式
image/jpeg | jpg图片格式
image/png | png图片格式
application/xhtml+xml | XHTML格式
application/xml     |  XML数据格式
application/atom+xml  | Atom XML聚合格式
application/json    |  JSON数据格式
application/pdf       | pdf格式
application/msword  |  Word文档格式
application/octet-stream | 二进制流数据
multipart/form-data | 表单数据

## 文件下载
首先请确保您的 APP 已经在 AndroidManifest.xml 声明读写权限：
```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
并确保您以获得该权限许可。

您可以使用以下代码启动下载进程：
```
HttpRequest.DOWNLOAD(
        MainActivity.this,
        "http://cdn.to-future.net/apk/tofuture.apk",
        new File(new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "BaseOkHttpV3"), "to-future.apk"),
        new OnDownloadListener() {
            @Override
            public void onDownloadSuccess(File file) {
                Toast.makeText(context, "文件已下载完成：" + file.getAbsolutePath(), Toast.LENGTH_LONG);
            }
            @Override
            public void onDownloading(int progress) {
                psgDownload.setProgress(progress);
            }
            @Override
            public void onDownloadFailed(Exception e) {
                Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT);
            }
        }
);
```

也可以使用build创建：
```
httpRequest = HttpRequest.build(MainActivity.this, "http://cdn.to-future.net/apk/tofuture.apk");
httpRequest.doDownload(
        new File(new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "BaseOkHttpV3"), "to-future.apk"),
        new OnDownloadListener() {
            @Override
            public void onDownloadSuccess(File file) {
                Toast.makeText(context, "文件已下载完成：" + file.getAbsolutePath(), Toast.LENGTH_LONG);
            }
            
            @Override
            public void onDownloading(int progress) {
                psgDownload.setProgress(progress);
            }
            
            @Override
            public void onDownloadFailed(Exception e) {
                Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT);
            }
        }
);

//停止下载：
httpRequest.stop();
```

额外的，若存储文件的父文件夹不存在，会自动创建。

## PUT&DELETE
从 3.0.3 版本起新增了 PUT 和 DELETE 请求方式，使用方法和一般请求一致，可以通过以下两种方法创建：
```
//PUT 请求：
HttpRequest.PUT(context, "http://你的接口地址", new Parameter().add("page", "1"), new ResponseListener() {...});
//DELETE 请求：
HttpRequest.DELETE(context, "http://你的接口地址", new Parameter().add("page", "1"), new ResponseListener() {...});
```
也可适用流式代码创建：
```
HttpRequest.build(context,"http://你的接口地址")
        .addHeaders("Charset", "UTF-8")
        .addParameter("page", "1")
        .addParameter("token", "A128")
        .setResponseListener(new ResponseListener() {
            @Override
            public void onResponse(String response, Exception error) {
                ...
            }
        })
        .doPut();
        //.doDelete();
```

## WebSocket
从 3.0.6 版本起新增了 WebSocket 封装工具类 BaseWebSocket，用于快速实现 WebSocket 请求连接。

请先前往 AndroidManifest.xml 中添加检查网络连接状态权限：
```
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

使用方法：
```
//通过 BUILD 方法获取 baseWebSocket 实例化对象，参数 context 为上下文索引，url 为 websocket服务器地址：
baseWebSocket = BaseWebSocket.BUILD(context, url)
        //实现监听方法
        .setWebSocketStatusListener(new WebSocketStatusListener() {
            @Override
            public void connected(Response response) {
                //连接上时触发
            }
            @Override
            public void onMessage(String message) {
                //处理收到的消息 message
            }
            @Override
            public void onMessage(ByteString message) {
            }
            @Override
            public void onReconnect() {
                //重新连接时触发
            }
            @Override
            public void onDisconnected(int breakStatus) {
                //断开连接时触发，breakStatus 值为 BREAK_NORMAL 时为正常断开，值为 BREAK_ABNORMAL  时为异常断开
            }
            @Override
            public void onConnectionFailed(Throwable t) {
                //连接错误处理
                t.printStackTrace();
            }
        })
        .startConnect();        //开始连接

//发送消息
baseWebSocket.send("Test!");

//断开连接
baseWebSocket.disConnect();

//重新连接
baseWebSocket.reConnect();
```

## JSON解析
从 3.0.7 版本起，新增 Json 解析功能，此功能基于 org.json 库二次实现，基本实现了无惧空指针异常的特性。

因原始 org.json 库提供的 JsonObject 和 JsonArray 框架使用起来相对麻烦，我们对其进行了二次封装和完善，且因 BaseOkHttpV3提供的 Json 解析框架底层使用的是 Map 和 List，与适配器具有更好的兼容性。

使用 BaseOkHttpV3提供的 Json 解析框架无需判断 Json 转换异常，可以直接将 Json 文本字符串传入解析。

另外，JsonMap 和 JsonList 的 `toString()` 方法可输出该对象原始 json 文本；

从 3.1.0 版本起提供直接解析返回值为 jsonMap 对象，详见 <a href="#一般请求">一般请求</a>

### 对于未知文本

```
Object obj = JsonUtil.deCodeJson(JsonStr);
```
deCodeJson 方法提供的是未知目标字符串是 JsonArray 还是 JsonObject 的情况下使用，返回对象为 Object，此时判断：
```
if(obj != null){
    if (obj instanceof JsonMap){
        //此对象为JsonObject，使用get(...)相关方法获取其中的值
    }
    if (obj instanceof JsonList){
        //此对象为JsonArray，使用get(index)相关方法获取其中的子项
    }
}
```
请注意对 obj 进行判空处理，若解析失败，则会返回 null。

### 对于已知JsonObject文本
```
JsonMap map = JsonUtil.deCodeJsonObject(JsonStr);        //直接解析为Map对象，JsonMap继承自LinkedHashMap，按照入栈顺序存储键值对集合
```
请注意对 map 进行判空处理，若解析失败，则会返回 null。

### 对于已知JsonArray文本
```
JsonList list = JsonUtil.deCodeJsonArray(JsonStr);        //直接解析为List对象，JsonList继承自ArrayList
```
请注意对 list 进行判空处理，若解析失败，则会返回 null。

### 额外说明
为方便解析使用，JsonMap 和 JsonList 都提供对应的如下方法来获取内部元素的值：
```
getString(...)
getInt(...)
getBoolean(...)
getLong(...)
getShort(...)
getDouble(...)
getFloat(...)
getList(...)
getJsonMap(...)
```

请注意，您亦可使用 Map、List 自带的 get(...) 方法获取元素的值，但 JsonMap 和 JsonList 提供的额外方法对于空指针元素，会返回一个默认值，例如对于实际是 null 的 String，会返回空字符串“”，对于实际是 null 的元素，获取其int值则为0,。

若您需要空值判断，可以通过例如 `getInt(String key, int emptyValue)` 来进行，若为空值会返回您提供的 emptyValue。

这确实不够严谨，但更多的是为了提升开发效率，适应快速开发的生产要求。

## 额外功能

### 全局日志
全局日志开关（默认是关闭态，需要手动开启）：
```
BaseOkHttp.DEBUGMODE = true;
```
BaseOkHttp V3支持增强型日志，使用输出日志内容是 json 字符串时，会自动格式化输出，方便查看。

![BaseOkHttp Logs](https://github.com/kongzue/Res/raw/master/app/src/main/res/mipmap-xxxhdpi/img_okhttp_logs.png)

在您使用 BaseOkHttp 时可以在 Logcat 的筛选中使用字符 “>>>” 对日志进行筛选（Logcat日志界面上方右侧的搜索输入框）。

您可以在 Android Studio 的 File -> Settings 的 Editor -> Color Scheme -> Android Logcat 中调整各类型的 log 颜色，我们推荐如下图方式设置颜色：

![Kongzue's log settings](https://github.com/kongzue/Res/raw/master/app/src/main/res/mipmap-xxxhdpi/baseframework_logsettings.png)

### 全局请求地址
设置全局请求地址后，所有接口都可以直接使用相对地址进行，例如设置全局请求地址：
```
BaseOkHttp.serviceUrl = "https://www.example.com";
```
发出一个请求：
```
HttpRequest.POST(context, "/femaleNameApi", new Parameter().add("page", "1"), new ResponseListener() {...});
```
那么实际请求地址即 https://www.example.com/femaleNameApi ，使用更加轻松方便。

注意，设置全局请求地址后，若 HttpRequest 的请求参数地址为“http”开头，则不会拼接全局请求地址。

### 全局 Header 请求头
使用如下代码设置全局 Header 请求头：
```
BaseOkHttp.overallHeader = new Parameter()
        .add("Charset", "UTF-8")
        .add("Content-Type", "application/json")
        .add("Accept-Encoding", "gzip,deflate")
;
```

### 全局请求返回拦截器
使用如下代码可以设置全局返回数据监听拦截器，return true 可返回请求继续处理，return false 即拦截掉不会继续返回原请求进行处理；
```
BaseOkHttp.responseInterceptListener = new ResponseInterceptListener() {
    @Override
    public boolean onResponse(Context context, String url, String response, Exception error) {
        if (error != null) {
            return true;
        } else {
            Log.i("!!!", "onResponse: " + response);
            return true;
        }
    }
};
```

### HTTPS 支持
1) 请将SSL证书文件放在assets目录中，例如“ssl.crt”；
2) 以附带SSL证书名的方式创建请求：
```
BaseOkHttp.SSLInAssetsFileName = "ssl.crt";
...
```
即可使用Https请求方式。

另外，可使用 BaseOkHttp.httpsVerifyServiceUrl=(boolean) 设置是否校验请求主机地址与设置的 HttpRequest.serviceUrl 一致；

### 全局参数拦截器
使用如下代码可以设置全局参数监听拦截器，此参数拦截器可以拦截并修改、新增所有请求携带的参数。

此方法亦适用于需要对参数进行加密的场景：
```
BaseOkHttp.parameterInterceptListener = new ParameterInterceptListener() {
    @Override
    public Parameter onIntercept(Parameter parameter) {
        parameter.add("key", "DFG1H56EH5JN3DFA");
        parameter.add("sign", makeSign(parameter.toParameterString()));
        return parameter;
    }
};

private String makeSign(String parameterString){
    //加密逻辑
    ...
}
```

### 请求超时
使用以下代码设置请求超时时间（单位：秒）
```
BaseOkHttp.TIME_OUT_DURATION = 10;
```

### 停止请求
可使用以下方法停止请求过程：
```
stop();     //停止请求
```

## 开源协议
```
Copyright Kongzue BaseOkHttp

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

本项目中使用的网络请求底层框架为square.okHttp3(https://github.com/square/okhttp )，感谢其为开源做出的贡献。

相关协议如下：
```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 更新日志
v3.1.0：
- 新增 setJsonResponseListener 返回监听器，可直接返回已解析的 jsonMap，新增解析 Json 异常：DecodeJsonException；
- 新增文件下载功能，以及下载进度监听器 OnDownloadListener；
- 修复参数拦截器 parameter 为空的问题；
- 新增 stop() 方法可以停止请求进程（但请注意已发出的请求无法撤回）；
- JsonMap 和 JsonList 新增 toString() 可输出该对象原始 json 文本；
- 修改部分日志文案；

v3.0.9.1：
- 修正 JsonUtil 解析过程中误将所有空格剔除的 bug；

v3.0.9：
- JsonList 新增方法 set(Object) 可使用流式代码添加内容；
- JsonMap 新增方法 set(String, Object) 可使用流式代码添加内容；

v3.0.8：
- 修改 JsonUtil 的子方法为静态方法，可直接使用，提升使用便利性；

v3.0.7：
- 新增 JSON 解析框架，包含 JsonUtil、JsonMap和JsonList 三个工具类。

v3.0.6：
- 新增 BaseWebSocket 封装类，可快速实现 WebSocket 请求与连接。
- （此版本为小更新）新增 StringPOST 请求方式，可以丢任意文本封装为请求体发送给服务端，MediaType 默认为“text/plain”；
- 升级 OkHttp 底层框架至 3.9.1 版本；

v3.0.5：
- 新增了 skipSSLCheck() 方法用于临时忽略使用 HTTPS 证书；
- 删除了自定义异常 NetworkErrorException 的使用；

v3.0.4：
- 默认禁止了网络环境差的重复请求；
- 修复其他请求无法正常执行的 bug；

v3.0.3：
- 新增 put、delete 请求方法；
- 完善了请求的创建逻辑；

v3.0.2：
- 日志新增打印请求头；
- 日志请求参数打印增强；
![BaseOkHttp Logs2.0](https://github.com/kongzue/Res/raw/master/app/src/main/res/mipmap-xxxhdpi/baseokhttp_log2.0.png)
- 修改完善了 OkHttplient 创建方式以及默认未设置证书时对 HTTPS 的验证忽略；
- 修复了文件上传的相关 bug；

v3.0.1：
- 修复了一些bug；