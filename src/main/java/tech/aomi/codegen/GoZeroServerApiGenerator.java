package tech.aomi.codegen;

import io.swagger.v3.parser.util.SchemaTypeUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openapitools.codegen.CliOption;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.ModelsMap;
import org.openapitools.codegen.model.OperationsMap;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GoZeroServerApiGenerator extends AbstractGoWebServerGenerator {

    public static final String SERVICE_NAME = "serviceName";

    public static final String GROUP_CONFIG = "groupConfig";


    @Setter
    protected String serviceName;

    /**
     * 分组配置
     * key 为 tag
     * value @see {@link GoZeroServerApiGenerator.GroupConfig}
     */
    @Setter
    protected Map<String, Map<String, String>> groupConfig;

    public GoZeroServerApiGenerator() {
        super();
        // 不支持时间格式
        typeMapping.put("DateTime", "string");
        serviceName = "app-api";
        groupConfig = new HashMap<>();

        modelPackage = this.apiPackage;
        /*
         * Models.  You can write model files using the modelTemplateFiles map.
         * if you want to create one template for file, you can do so here.
         * for multiple files for model, just put another entry in the `modelTemplateFiles` with
         * a different extension
         */
        modelTemplateFiles.put("model.mustache", ".api");

        /*
         * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
         * as with models, add multiple entries with different extensions for multiple files per
         * class
         */
        apiTemplateFiles.put(
                "api.mustache",   // the template to use
                ".api");       // the extension for each file to write
        /*
         * Template Location.  This is the location which templates will be read from.  The generator
         * will use the resource stream to attempt to read the templates.
         */
        embeddedTemplateDir = templateDir = "go-zero-server-api";

        cliOptions.add(CliOption.newString(SERVICE_NAME, "go zero service name"));
        cliOptions.add(new CliOption(SERVICE_NAME, "go zero service name", SchemaTypeUtil.OBJECT_TYPE));

    }

    @Override
    public void processOpts() {
        super.processOpts();
        if (additionalProperties.containsKey(SERVICE_NAME)) {
            this.setServiceName(additionalProperties.get(SERVICE_NAME).toString());
        }
        if (additionalProperties.containsKey(GROUP_CONFIG)) {
            this.setGroupConfig((Map<String, Map<String, String>>) additionalProperties.get(GROUP_CONFIG));
        }
        /*
         * Supporting Files.  You can write single files for the generator with the
         * entire object tree available.  If the input file has a suffix of `.mustache
         * it will be processed by the template engine.  Otherwise, it will be copied
         */
        supportingFiles.add(new SupportingFile("root_api.mustache", this.apiPackage, "app.api"));
    }

    @Override
    public ModelsMap postProcessModels(ModelsMap objs, ModelsMap models) {
        return models;
    }

    @Override
    public OperationsMap postProcessOperationsWithModels(OperationsMap objs, List<ModelMap> allModels) {
        OperationsMap operationsMap = super.postProcessOperationsWithModels(objs, allModels);
        String selfPkg = operationsMap.getOrDefault("goApiInterfaceFullPackage", "").toString();

        operationsMap.setImports(operationsMap.getImports().stream().peek(item -> {
            String i = item.get("import");
            if (i.startsWith(this.moduleName)) {
                String newI = getRelativePath(selfPkg, i).toString();
                item.put("import", newI);
            }
        }).collect(Collectors.toList()));

        Map<String, String> config = this.groupConfig.get(operationsMap.get("tag").toString());
        if (null != config) {
            operationsMap.putAll(config);
        }
        // gozero 不支持数组参数
        operationsMap.getOperations().getOperation().forEach(item -> {
            if (item.allParams.size() == 1 && item.allParams.get(0).isBodyParam && item.allParams.get(0).isArray) {
//                item.hasParams = false;
                item.allParams.clear();
            }
        });

        return operationsMap;
    }

    @Override
    public String toModelImport(String name) {
        if (Arrays.asList("set", "array", "time.Time").contains(name)) {
            // 让AbstractGoCodegen#postProcessOperationsWithModels方法进行移除
            return apiPackage();
        }
        return this.toModelFilename(name);
    }

    @Override
    public String modelFilename(String templateName, String modelName) {
        String suffix = modelTemplateFiles().get(templateName);
        return modelFileFolder() + File.separator + toModelFilename(modelName) + suffix;
    }

    /**
     * Configures a friendly name for the generator.  This will be used by the generator
     * to select the library with the -g flag.
     *
     * @return the friendly name for the generator
     */
    @Override
    public String getName() {
        return "go-zero-server-api";
    }

    /**
     * Returns human-friendly help for the generator.  Provide the consumer with help
     * tips, parameters here
     *
     * @return A string value for the help message
     */
    @Override
    public String getHelp() {
        return "Generates a Go zero server library with the gin framework using OpenAPI-Generator.";

    }

    protected Path getRelativePath(String basePath, String targetPath) {
        Path base = Paths.get(basePath).getParent();
        Path target = Paths.get(targetPath);
        return base.relativize(target);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupConfig {
        /**
         * 启动签名
         */
        private Boolean signature;

        /**
         * jwt 值
         */
        private String jwt;

        /**
         * 中间件
         */
        private String middleware;
    }
}
