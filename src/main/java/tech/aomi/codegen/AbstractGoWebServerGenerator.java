package tech.aomi.codegen;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.codegen.*;
import org.openapitools.codegen.languages.AbstractGoCodegen;
import org.openapitools.codegen.meta.features.*;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;
import org.openapitools.codegen.model.OperationMap;
import org.openapitools.codegen.model.OperationsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractGoWebServerGenerator extends AbstractGoCodegen {

    private final Logger LOGGER = LoggerFactory.getLogger(AbstractGoWebServerGenerator.class);

    public static final String MODULE_NAME = "moduleName";
    public static final String HANDLER_PACKAGE = "handlerPackage";
    public static final String CONTROLLER_PACKAGE = "controllerPackage";
    public static final String MODEL_FOLDER_FIELD_NAME = "modelFolderFieldName";
    public static final String SUPPORT_VALID_MULTIPLE_OF = "supportValidMultipleOf";
    public static final String SUPPORT_VALID_REGEXP = "supportValidRegexp";
    public static final String DATETIME_FORMAT = "datetimeFormat";
    public static final String DATE_FORMAT = "dateFormat";
    public static final String TIME_FORMAT = "timeFormat";
    public static final String PAGE_PACKAGE = "pagePackage";
    public static final String PAGE_PACKAGE_ALIAS = "pagePackageAlias";

    protected String apiVersion = "1.0.0";

    /**
     * go module 名称
     * eg: github.com/xxx/xxx
     */
    @Setter
    protected String moduleName = "openapi-server";

    @Setter
    protected String handlerPackage = "handler";
    @Setter
    protected String controllerPackage = "controller";

    /**
     * page 所在的包路径,通过接口的扩展属性 x-paginated 启用，启用后请求参数会继承 PageRequest. 响应结果使用 Page 包裹
     * 例如：github.com/xxx/xxx/page
     * 里面提供两个结构体
     * 1. Page 带泛型的分页查询结果对象
     * 2. PageRequest 分页查询请求对象
     */
    @Setter
    protected String pagePackage = "page";
    @Setter
    protected String pagePackageAlias = "page";
    /**
     * 自定义model路径取值key
     * 例如：x-apifox-folder
     */
    @Setter
    protected String modelFolderFieldName = "";

    /**
     * 是否支持倍数校验
     */
    @Setter
    protected Boolean supportValidMultipleOf = false;

    @Setter
    protected Boolean supportValidRegexp = false;

    @Setter
    @Getter
    protected String datetimeFormat = "2006-01-02T15:04:05Z";
    @Setter
    @Getter
    protected String dateFormat = "2006-01-02";
    @Setter
    @Getter
    protected String timeFormat = "15:04:05";

    protected Set<String> noCreateAliasPkgs = new HashSet<>();

    public AbstractGoWebServerGenerator() {
        super();
        this.apiNameSuffix = "";
        apiPackage = "api";
        modelPackage = "dto";

        // set the output folder here
        outputFolder = "generated/go";

        modifyFeatureSet(features -> features
                .includeDocumentationFeatures(DocumentationFeature.Readme)
                .wireFormatFeatures(EnumSet.of(WireFormatFeature.JSON, WireFormatFeature.XML))
                .securityFeatures(EnumSet.noneOf(
                        SecurityFeature.class
                ))
                .excludeGlobalFeatures(
                        GlobalFeature.XMLStructureDefinitions,
                        GlobalFeature.Callbacks,
                        GlobalFeature.LinkObjects,
                        GlobalFeature.ParameterStyling
                )
                .excludeSchemaSupportFeatures(
                        SchemaSupportFeature.Polymorphism
                )
                .excludeParameterFeatures(
                        ParameterFeature.Cookie
                )
        );

        typeMapping.put("File", "*multipart.FileHeader");
        typeMapping.put("file", "*multipart.FileHeader");
//        typeMapping.put("duration", "time.Duration");

        importMapping.put("*multipart.FileHeader", "mime/multipart");
//        importMapping.put("time.Time", "time");
//        importMapping.put("*time.Time", "time");
        languageSpecificPrimitives.add("nil");

        noCreateAliasPkgs.add("mime/multipart");
        noCreateAliasPkgs.add("time");


        cliOptions.add(CliOption.newString(MODULE_NAME, "go module name."));
        cliOptions.add(CliOption.newString(HANDLER_PACKAGE, "请求处理代码生成的包路径."));
        cliOptions.add(CliOption.newString(CONTROLLER_PACKAGE, "Controller代码生成的包路径."));
        cliOptions.add(CliOption.newString(MODEL_FOLDER_FIELD_NAME, "从那个字段获取MODEL的包路径."));
        cliOptions.add(CliOption.newString(DATETIME_FORMAT, "时间格式"));
        cliOptions.add(CliOption.newString(DATE_FORMAT, "日期格式"));
        cliOptions.add(CliOption.newString(TIME_FORMAT, "时间格式"));
        cliOptions.add(CliOption.newString(PAGE_PACKAGE, "分页包路径"));
        cliOptions.add(CliOption.newString(PAGE_PACKAGE_ALIAS, "分页包别名"));
        cliOptions.add(CliOption.newBoolean(SUPPORT_VALID_MULTIPLE_OF, "go valid support MULTIPLE_OF"));
        cliOptions.add(CliOption.newBoolean(SUPPORT_VALID_REGEXP, "go valid support regexp"));


        cliOptions.add(CliOption.newBoolean(CodegenConstants.ENUM_CLASS_PREFIX, CodegenConstants.ENUM_CLASS_PREFIX_DESC));
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (additionalProperties.containsKey(MODULE_NAME)) {
            this.setModuleName(additionalProperties.get(MODULE_NAME).toString());
        }

        if (additionalProperties.containsKey(HANDLER_PACKAGE)) {
            this.setHandlerPackage(additionalProperties.get(HANDLER_PACKAGE).toString());
        }

        if (additionalProperties.containsKey(CONTROLLER_PACKAGE)) {
            this.setControllerPackage(additionalProperties.get(CONTROLLER_PACKAGE).toString());
        }
        if (additionalProperties.containsKey(MODEL_FOLDER_FIELD_NAME)) {
            this.setModelFolderFieldName(additionalProperties.get(MODEL_FOLDER_FIELD_NAME).toString());
        }

        if (additionalProperties.containsKey(SUPPORT_VALID_MULTIPLE_OF)) {
            this.setSupportValidMultipleOf(Boolean.parseBoolean(additionalProperties.get(SUPPORT_VALID_MULTIPLE_OF).toString()));
        }
        if (additionalProperties.containsKey(SUPPORT_VALID_REGEXP)) {
            this.setSupportValidRegexp(Boolean.parseBoolean(additionalProperties.get(SUPPORT_VALID_REGEXP).toString()));
        }
        if (additionalProperties.containsKey(DATETIME_FORMAT)) {
            this.setDatetimeFormat(additionalProperties.get(DATETIME_FORMAT).toString());
        }
        if (additionalProperties.containsKey(DATE_FORMAT)) {
            this.setDateFormat(additionalProperties.get(DATE_FORMAT).toString());
        }
        if (additionalProperties.containsKey(TIME_FORMAT)) {
            this.setTimeFormat(additionalProperties.get(TIME_FORMAT).toString());
        }
        if (additionalProperties.containsKey(PAGE_PACKAGE)) {
            this.setPagePackage(additionalProperties.get(PAGE_PACKAGE).toString());
        }
        if (additionalProperties.containsKey(PAGE_PACKAGE_ALIAS)) {
            this.setPagePackageAlias(additionalProperties.get(PAGE_PACKAGE_ALIAS).toString());
        } else {
            this.setPagePackageAlias(sanitizeName(this.pagePackage, "_"));
        }


        /*
         * Additional Properties.  These values can be passed to the templates and
         * are available in models, apis, and supporting files
         */
        if (additionalProperties.containsKey("apiVersion")) {
            this.apiVersion = (String) additionalProperties.get("apiVersion");
        } else {
            additionalProperties.put("apiVersion", apiVersion);
        }


        if (additionalProperties.containsKey(CodegenConstants.ENUM_CLASS_PREFIX)) {
            setEnumClassPrefix(Boolean.parseBoolean(additionalProperties.get(CodegenConstants.ENUM_CLASS_PREFIX).toString()));
            if (enumClassPrefix) {
                additionalProperties.put(CodegenConstants.ENUM_CLASS_PREFIX, true);
            }
        }

    }


    @Override
    public OperationsMap postProcessOperationsWithModels(OperationsMap objs, List<ModelMap> allModels) {
        objs = super.postProcessOperationsWithModels(objs, allModels);

        OperationMap operations = objs.getOperations();
        List<CodegenOperation> operationList = operations.getOperation();

        boolean needAddPage = false;
        boolean hasAnyParams = false;
        for (CodegenOperation op : operationList) {
            if (op.path != null) {
                op.path = op.path.replaceAll("\\{(.*?)\\}", ":$1");
            }
            Optional.ofNullable(op.allParams).ifPresent(params -> params.forEach(p -> {
                if (p.isBodyParam) {
                    allModels.stream()
                            .filter(m -> m.getModel().getClassname().equals(p.dataType))
                            .findFirst()
                            .ifPresent(m -> p.vendorExtensions.put("importAlias", m.getOrDefault("alias", "")));
                    if (importMapping().containsKey(p.dataType)) {
                        if (!noCreateAliasPkgs.contains(importMapping().get(p.dataType))) {
                            p.vendorExtensions.put("importAlias", sanitizeName(importMapping().get(p.dataType), "_"));
                        }
                    }
                }
            }));

            if (!op.returnTypeIsPrimitive) {
                allModels.stream()
                        .filter(m -> m.getModel().getClassname().equals(op.returnBaseType))
                        .findFirst()
                        .ifPresent(m -> {
                            if (importMapping().containsKey(op.returnBaseType)) {
                                op.vendorExtensions.put("returnImportAlias", sanitizeName(importMapping().get(op.returnBaseType), "_"));
                            } else {
                                op.vendorExtensions.put("returnImportAlias", m.getOrDefault("alias", ""));
                            }
                        });
            }
            if (op.vendorExtensions.containsKey("x-paginated")) {
                needAddPage = true;
            }
            if (op.hasParams) {
                hasAnyParams = true;
            }
            if ("nil".equalsIgnoreCase(op.returnType)) {
                op.vendorExtensions.put("returnTypeIsNil", true);
            }

            Optional.ofNullable(op.allParams)
                    .flatMap(params -> params.stream().filter(item -> item.isFile).findFirst())
                    .ifPresent(p -> op.vendorExtensions.put("hasFileParam", true));
        }

        updateOperationsPkgInfo(objs, operations.getClassname());
        // 更新operations import 信息

        // 更新import 信息
        // interface.mustache 中导入dto的时候使用
        List<Map<String, String>> imports = Optional.ofNullable(objs.getImports()).orElse(new ArrayList<>()).stream().peek(item -> {
            String path = item.get("import");
            String classname = item.getOrDefault("classname", "");
            if (!noCreateAliasPkgs.contains(importMapping().get(classname))) {
                if (importMapping().containsKey(classname)) {
                    String alias = sanitizeName(path, "_");

                    item.put("alias", alias);
                    item.put("isModelImport", "true");
                }
            }

            allModels.stream().filter(m -> m.getOrDefault("importPath", "").equals(path)).findFirst().ifPresent(m -> {
                String alias = m.getOrDefault("alias", "").toString();
                if (Paths.get(path).getFileName().toString().equals(alias)) {
                    alias = "";
                }
                item.put("alias", alias);
                item.put("isModelImport", "true");
            });
        }).sorted(Comparator.comparing(o -> o.getOrDefault("import", ""))).collect(Collectors.toList());

        objs.setImports(imports);
        objs.put("pageEnabled", needAddPage);
        objs.put("pagePackageAlias", this.pagePackageAlias);
        objs.put("hasAnyParams", hasAnyParams);

        return objs;
    }

    @Override
    public ModelsMap postProcessModels(ModelsMap objs) {
        ModelsMap models = super.postProcessModels(objs);
        return postProcessModels(objs, models);
    }

    public ModelsMap postProcessModels(ModelsMap objs, ModelsMap models) {

        models.setModels(models.getModels().stream().peek(model -> {
            CodegenModel cmodel = model.getModel();
            if (!cmodel.oneOf.isEmpty()) {
                cmodel.vendorExtensions.put("isOneOf", true);
                List<String> oneOfs = cmodel.oneOf.stream().map(name -> {
                    String alias = getModelAlias(name);
                    if (alias.isEmpty()) {
                        return name;
                    }
                    return alias + "." + name;
                }).collect(Collectors.toList());
                cmodel.vendorExtensions.put("oneOfs", oneOfs);
            }

            Object importPath = model.getOrDefault("importPath", "");

            cmodel.vars.forEach(var -> {
                // 排除自己导入自己
                if (var.isModel && var instanceof ExtendedCodegenProperty) {
                    ((ExtendedCodegenProperty) var).needImport = !importPath.equals(((ExtendedCodegenProperty) var).importPath);
                }
                if (var.isArray && var.items.isModel && var instanceof ExtendedCodegenProperty) {
                    // 如果需要导入的情况下判断是不是等于自己
                    if (((ExtendedCodegenProperty) var).needImport) {
                        ((ExtendedCodegenProperty) var).needImport = !importPath.equals(((ExtendedCodegenProperty) var.items).importPath);
                    }
                }
            });


            String pkgName = getModelPkgName(cmodel.vendorExtensions);
            String alias = getModelAlias(cmodel.vendorExtensions);
            // 模块自己所在的包
            model.put("packageName", pkgName);
            // 模块自己对应的使用别名
            model.put("alias", alias);
        }).collect(Collectors.toList()));

        models.put("packageName", models.getModels().get(0).get("packageName"));
        Object selfImportPath = models.getModels().get(0).getOrDefault("importPath", "");
        models.setImports(models.getImportsOrEmpty().stream().filter(item -> {
            // 排除自己导入自己
            String path = item.get("import");
            return !selfImportPath.equals(path);
        }).peek(item -> {
            String path = item.get("import");
            // 别名设置
            String alias = "";
            if (path.startsWith(this.moduleName)) {
                path = path.replace(this.moduleName, "");
                alias = sanitizeName(path, "_");
            } else {
                alias = sanitizeName(path, "_");
            }
            String defaultAlias = Paths.get(path).getFileName().toString();
            if (alias.equalsIgnoreCase(defaultAlias)) {
                alias = "";
            }
            item.put("alias", alias); // 这里设置控制防止使用父级对象中的alias值
        }).collect(Collectors.toList()));

        models.setModels(models.getModels().stream().filter(item -> {
            CodegenModel model = item.getModel();
            // 过滤掉typeMapping ,不在生成typeMapping中定义的model
            if (typeMapping.containsValue(model.classname)) {
                return false;
            }
            return true;
        }).collect(Collectors.toList()));
        return models;
    }

    /**
     * Configures the type of generator.
     *
     * @return the CodegenType for this generator
     * @see org.openapitools.codegen.CodegenType
     */
    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    @Override
    public String modelFilename(String templateName, String modelName) {
        String suffix = this.modelTemplateFiles().get(templateName);

        String dir = getModelFolder(modelName);
        dir = dir.replaceAll("-", "_");

        return Paths.get(this.modelFileFolder(), dir, this.toModelFilename(modelName) + suffix).toString();

    }

    @Override
    public CodegenParameter fromParameter(Parameter parameter, Set<String> imports) {
        CodegenParameter cp = super.fromParameter(parameter, imports);
        ExtendedCodegenParameter ecp = new ExtendedCodegenParameter(cp);
        ecp.supportValidRegexp = supportValidRegexp;
        ecp.supportValidMultipleOf = supportValidMultipleOf;
        ecp.timeFormat = timeFormat;
        ecp.dateFormat = dateFormat;
        ecp.datetimeFormat = datetimeFormat;
        return ecp;
    }

    @Override
    public CodegenParameter fromFormProperty(String name, Schema propertySchema, Set<String> imports) {
        CodegenParameter cp = super.fromFormProperty(name, propertySchema, imports);
        ExtendedCodegenParameter ecp = new ExtendedCodegenParameter(cp);
        ecp.supportValidRegexp = supportValidRegexp;
        ecp.supportValidMultipleOf = supportValidMultipleOf;
        ecp.timeFormat = timeFormat;
        ecp.dateFormat = dateFormat;
        ecp.datetimeFormat = datetimeFormat;
        return ecp;
    }

    @Override
    public CodegenParameter fromRequestBody(RequestBody body, Set<String> imports, String bodyParameterName) {
        CodegenParameter cp = super.fromRequestBody(body, imports, bodyParameterName);
        ExtendedCodegenParameter ecp = new ExtendedCodegenParameter(cp);
        ecp.supportValidRegexp = supportValidRegexp;
        ecp.supportValidMultipleOf = supportValidMultipleOf;
        ecp.timeFormat = timeFormat;
        ecp.dateFormat = dateFormat;
        ecp.datetimeFormat = datetimeFormat;
        return ecp;
    }

    public ExtendedCodegenProperty fromProperty(String name, Schema p, boolean required) {
        CodegenProperty cp = super.fromProperty(name, p, required);
        ExtendedCodegenProperty ecp = new ExtendedCodegenProperty(cp, supportValidMultipleOf, supportValidRegexp);
        ecp.datetimeFormat = datetimeFormat;
        ecp.dateFormat = dateFormat;
        ecp.timeFormat = timeFormat;
        if (ecp.isModel) {
            ecp.packageName = getModelPkgName(ecp.vendorExtensions);
            ecp.alias = getModelAlias(ecp.vendorExtensions);
            ecp.importPath = toModelImport(ecp.dataType);
            ecp.needImport = !importMapping.containsKey(ecp.dataType);
        }
        if (ecp.isArray && ecp.items.isModel && ecp.items instanceof ExtendedCodegenProperty) {
            ecp.packageName = getModelPkgName(ecp.vendorExtensions);

            ExtendedCodegenProperty tmp = (ExtendedCodegenProperty) ecp.items;
            ecp.alias = tmp.alias;
            ecp.importPath = tmp.importPath;
            ecp.needImport = tmp.needImport;
        }

        return ecp;
    }

    @Override
    public String toModelName(String name) {
        if (typeMapping.containsKey(name)) {
            return typeMapping.get(name);
        }
        return super.toModelName(name);
    }

    @Override
    public String toModelImport(String name) {
        // 因为 AbstractGoCodegen 手动处理的time 这里只能手动排除
        if ("time.Time".equals(name)) {
            return null;
        }
        if (importMapping.containsKey(name)) {
            return importMapping.get(name);
        }
        String dir = getModelFolder(name);
        return Paths.get(this.moduleName, this.modelPackage().replace('.', File.separatorChar), dir).toString().replaceAll("-", "_");
    }

    protected String getFirstTagName(String tag) {
        Tag t = this.findTag(tag);
        String name = "";
        if (null != t) {
            name = t.getName();
            String[] tmp = name.split("/");
            name = tmp[tmp.length - 1];
            name = sanitizeName(name);
            tmp[tmp.length - 1] = name;
            name = String.join("/", tmp);
        }
        return name;
    }

    protected Tag findTag(String tag) {
        if (null == this.openAPI.getTags()) {
            return null;
        }
        return this.openAPI.getTags().stream().filter(item -> {
            String name = this.sanitizeTag(item.getName());
            return tag.equals(name);
        }).findFirst().orElse(null);
    }


    protected void updateOperationsPkgInfo(OperationsMap operation, String classname) {

        String tag = this.getFirstTagName(classname);

        Path api = Paths.get(this.moduleName, this.apiPackage, tag);
        String goApiAlias = sanitizeName(Paths.get(this.apiPackage, tag).toString(), "_");
        String goApiInterfaceFullPackage = api.toString().replaceAll("-", "_");

        String goHandlerAlias = sanitizeName(Paths.get(this.handlerPackage, tag).toString(), "_");
        String goHandlerFullPackage = Paths.get(this.moduleName, this.handlerPackage, tag).toString().replaceAll("-", "_");

        String goControllerAlias = sanitizeName(Paths.get(this.controllerPackage, tag).toString(), "_");
        String goControllerFullPackage = Paths.get(this.moduleName, this.controllerPackage, tag).toString().replaceAll("-", "_");

        String goPackage = api.getName(api.getNameCount() - 1).toString();
        if (goPackage.isEmpty()) {
            goPackage = "main";
        }

        operation.put("package", goPackage);
        operation.put("goApiAlias", goApiAlias);
        operation.put("goApiInterfaceFullPackage", goApiInterfaceFullPackage);
        operation.put("goHandlerAlias", goHandlerAlias);
        operation.put("goHandlerFullPackage", goHandlerFullPackage);
        operation.put("goControllerAlias", goControllerAlias);
        operation.put("goControllerFullPackage", goControllerFullPackage);

        operation.put("tag", tag);
        operation.put("filename", this.toApiFilename(classname));
    }


    private String getModelPkgName(Map<String, Object> vendorExtensions) {
        String folder = getModelFolder(vendorExtensions);
        Path path = Paths.get(this.modelPackage().replace('.', File.separatorChar), folder);
        return path.getName(path.getNameCount() - 1).toString().replaceAll("-", "_");
    }

    private String getModelAlias(String name) {
        String folder = getModelFolder(name);
        Path path = Paths.get(this.modelPackage().replace('.', File.separatorChar), folder);
        return sanitizeName(path.toString(), "_");
    }

    private String getModelAlias(Map<String, Object> vendorExtensions) {
        String folder = getModelFolder(vendorExtensions);
        Path path = Paths.get(this.modelPackage().replace('.', File.separatorChar), folder);
        return sanitizeName(path.toString(), "_");
    }

    protected String getModelFolder(String name) {
        Schema<?> schema = this.openAPI.getComponents()
                .getSchemas()
                .keySet()
                .stream()
                .filter(name::equalsIgnoreCase)
                .map(key -> this.openAPI.getComponents().getSchemas().get(key))
                .findFirst()
                .orElse(null);
        if (null == schema) {
            return "";
        }
        return this.getModelFolder(schema.getExtensions());
    }

    private String getModelFolder(Map<String, Object> vendorExtensions) {
        if (null == vendorExtensions) {
            return "";
        }
        String folder = vendorExtensions.getOrDefault(this.modelFolderFieldName, "").toString();
        // 去重叠部分
        return folder.replace(this.modelPackage, "");
    }

}
