package tech.aomi.codegen;

import io.swagger.v3.oas.models.media.Schema;
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

    public AbstractGoWebServerGenerator() {
        super();
        this.apiNameSuffix = "";

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

        apiPackage = "api";
        modelPackage = "dto";

        // set the output folder here
        outputFolder = "generated/go";

        /*
         * Reserved words.  Override this with reserved words specific to your language
         */
        setReservedWordsLowerCase(
                Arrays.asList(
                        // data type
                        "string", "bool", "uint", "uint8", "uint16", "uint32", "uint64",
                        "int", "int8", "int16", "int32", "int64", "float32", "float64",
                        "complex64", "complex128", "rune", "byte", "uintptr",

                        "break", "default", "func", "interface", "select",
                        "case", "defer", "go", "map", "struct",
                        "chan", "else", "goto", "package", "switch",
                        "const", "fallthrough", "if", "range", "type",
                        "continue", "for", "import", "return", "var", "error", "nil")
                // Added "error" as it's used so frequently that it may as well be a keyword
        );

//        this.typeMapping.put("duration", "time.Duration");
//        this.typeMapping.put("time", "time.Time");
//        this.typeMapping.put("date", "time.Time");

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
                }
            }));

            if (!op.returnTypeIsPrimitive) {
                allModels.stream()
                        .filter(m -> m.getModel().getClassname().equals(op.returnBaseType))
                        .findFirst()
                        .ifPresent(m -> op.vendorExtensions.put("returnImportAlias", m.getOrDefault("alias", "")));
            }
            if (op.vendorExtensions.containsKey("x-paginated")) {
                needAddPage = true;
            }
            if (op.hasParams) {
                hasAnyParams = true;
            }
        }

        updateOperationsPkgInfo(objs, operations.getClassname());
        // 更新operations import 信息

        // 更新import 信息
        // interface.mustache 中导入dto的时候使用
        List<Map<String, String>> imports = Optional.ofNullable(objs.getImports()).orElse(new ArrayList<>()).stream().peek(item -> {
            String path = item.get("import");
            allModels.stream().filter(m -> m.getOrDefault("importPath", "").equals(path)).findFirst().ifPresent(m -> {
                item.put("alias", m.getOrDefault("alias", "").toString());
                item.put("isModelImport", "true");
            });
        }).collect(Collectors.toList());

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
        models.setModels(models.getModels().stream().map(this::updateModelInfo).collect(Collectors.toList()));
        models.setImports(models.getImportsOrEmpty().stream().peek(item -> {
            String path = item.get("import");
            item.put("alias", ""); // 这里设置控制防止使用父级对象中的alias值
            if (path.startsWith(this.moduleName)) {
                path = path.replace(this.moduleName, "");
                String alias = sanitizeName(path, "_");
                item.put("alias", alias);
            }
        }).collect(Collectors.toList()));
        models.put("packageName", models.getModels().get(0).get("packageName"));
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
    public CodegenModel fromModel(String name, Schema schema) {
        CodegenModel model = super.fromModel(name, schema);
        return fromModel(model, name, schema);
    }

    public CodegenModel fromModel(CodegenModel model, String name, Schema schema) {
        String selfPath = toModelImport(name);
        model.setImports(model.getImports().stream().filter(item -> {
            String iPath = toModelImport(item);
            return !selfPath.equals(iPath);
        }).collect(Collectors.toSet()));

        return model;
    }

    public ExtendedCodegenProperty fromProperty(String name, Schema p, boolean required) {
        CodegenProperty cp = super.fromProperty(name, p, required);
        ExtendedCodegenProperty ecp = new ExtendedCodegenProperty(cp, supportValidMultipleOf, supportValidRegexp);
        ecp.datetimeFormat = datetimeFormat;
        ecp.dateFormat = dateFormat;
        ecp.timeFormat = timeFormat;
        if (!cp.isPrimitiveType) {
            String type = cp.dataType;
            if (cp.isArray) {
                type = cp.items.dataType;
            }
            if (!this.typeMapping().containsValue(type)) {
                ecp.packageName = getModelPkgName(type);
                ecp.alias = getModelAlias(type);
                ecp.importPath = this.toModelImport(type);
            }
        }
        return ecp;
    }

    @Override
    public String toModelImport(String name) {
        if ("time.Time".equals(name)) {
            return null;
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

    protected ModelMap updateModelInfo(ModelMap model) {
        CodegenModel cmodel = model.getModel();
        String importPath = model.get("importPath").toString();

        cmodel.vars.forEach(var -> {
            if (isModel(var) && var instanceof ExtendedCodegenProperty) {
                ((ExtendedCodegenProperty) var).needImport = !importPath.equals(((ExtendedCodegenProperty) var).importPath);
            }
        });

        model.put("packageName", getModelPkgName(cmodel.getVendorExtensions()));
        model.put("alias", getModelAlias(cmodel.getVendorExtensions()));
        return model;
    }


    private String getModelPkgName(String name) {
        String folder = getModelFolder(name);
        Path path = Paths.get(this.modelPackage().replace('.', File.separatorChar), folder);
        return path.getName(path.getNameCount() - 1).toString().replaceAll("-", "_");
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

    private boolean isModel(CodegenProperty property) {
        return !property.isPrimitiveType && !this.typeMapping().containsValue(property.dataType);

    }
}
