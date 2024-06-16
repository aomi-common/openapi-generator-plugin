# openapi generator


### generator

#### go gin 服务端代码生成
使用模板`resources/my-go-gin-server`

配置如下:
```yaml
# plugin options
moduleName: github.com/xx/xx
# 处理请求代码目录
handlerPackage: web/handler
# controller 代码目录(该目录下的代码需要自行定义，实现对应的接口即可)
controllerPackage: controller
# model 目录配置所在的字段名称. 
modelFolderFieldName: "x-apifox-folder"
# 路径文件生成的路径
routerPackage: "web"
# 支持数字的倍数校验
supportValidMultipleOf: false
# 支持正则校验
supportValidRegexp: false
datetimeFormat: xxx
dateFormat: xxx
timeFormat: xxx
```

### templates

#### go http 客户端代码生成

使用模板`resources/go`

配置如下:
```yaml
generatorName: go

isGoSubmodule: true
packageName: api

generateAliasAsModel: true
withGoMod: false
generateInterfaces: false

globalProperties:
  generateAliasAsModel: true
  apis: ""
  models: ""
  apiDocs: false
  modelDocs: false
  apiTests: false
  modelTests: false
```