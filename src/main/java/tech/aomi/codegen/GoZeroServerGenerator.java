package tech.aomi.codegen;

import lombok.Getter;
import lombok.Setter;
import org.openapitools.codegen.CliOption;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.OperationsMap;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.openapitools.codegen.utils.StringUtils.underscore;

public class GoZeroServerGenerator extends AbstractGoWebServerGenerator {

    public static final String SERVICE_NAME = "serviceName";
    public static final String SVG_PACKAGE = "svcPackage";


    @Setter
    protected String serviceName;

    @Getter
    @Setter
    protected String svcPackage;

    public GoZeroServerGenerator() {
        super();
        // 不支持时间格式
        typeMapping.put("DateTime", "string");
        serviceName = "app-api";
        modelPackage = "types";
        svcPackage = moduleName + "/svc";
        /*
         * Models.  You can write model files using the modelTemplateFiles map.
         * if you want to create one template for file, you can do so here.
         * for multiple files for model, just put another entry in the `modelTemplateFiles` with
         * a different extension
         */
//        modelTemplateFiles.put("model.mustache", ".go");

        /*
         * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
         * as with models, add multiple entries with different extensions for multiple files per
         * class
         */
//        apiTemplateFiles.put(
//                "api.mustache",   // the template to use
//                ".go");       // the extension for each file to write
        apiTemplateFiles.put(
                "handler.mustache",
                ".go"
        );
        /*
         * Template Location.  This is the location which templates will be read from.  The generator
         * will use the resource stream to attempt to read the templates.
         */
        embeddedTemplateDir = templateDir = "go-zero-server";

        cliOptions.add(CliOption.newString(SERVICE_NAME, "go zero service name"));
        cliOptions.add(CliOption.newString(SVG_PACKAGE, "svc package."));

    }

    @Override
    public void processOpts() {
        super.processOpts();
        if (additionalProperties.containsKey(SERVICE_NAME)) {
            this.setServiceName(additionalProperties.get(SERVICE_NAME).toString());
        } else {
            additionalProperties.put(SERVICE_NAME, this.serviceName);
        }
        if (additionalProperties.containsKey(SVG_PACKAGE)) {
            this.setSvcPackage(additionalProperties.get(SVG_PACKAGE).toString());
        } else {
            additionalProperties.put(SVG_PACKAGE, this.moduleName + "/svc");
        }
        /*
         * Supporting Files.  You can write single files for the generator with the
         * entire object tree available.  If the input file has a suffix of `.mustache
         * it will be processed by the template engine.  Otherwise, it will be copied
         */
        supportingFiles.add(new SupportingFile("routes.mustache", this.handlerPackage, "routes.go"));
        supportingFiles.add(new SupportingFile("model.mustache", this.modelPackage, "types.go"));
    }

    @Override
    public OperationsMap postProcessOperationsWithModels(OperationsMap objs, List<ModelMap> allModels) {
        // gozero 不支持数组参数
        objs.getOperations().getOperation().forEach(item -> {
            if (item.allParams.size() == 1 && item.allParams.get(0).isBodyParam && (item.allParams.get(0).isArray || item.allParams.get(0).isMap)) {
                item.allParams.get(0).nameInPascalCase = "RequestBody";
                item.allParams.get(0).vendorExtensions.put("isBaseTypeBody", true);
                item.vendorExtensions.put("isBaseTypeBody", true);
                List<Map<String, String>> imports = objs.getImports();
                if (imports.stream().noneMatch(i -> i.get("import").equalsIgnoreCase("encoding/json"))) {
                    imports.add(Collections.singletonMap("import", "encoding/json"));
                }
                if (imports.stream().noneMatch(i -> i.get("import").equalsIgnoreCase("io"))) {
                    imports.add(Collections.singletonMap("import", "io"));
                }
                objs.setImports(imports);
            }
        });

        return super.postProcessOperationsWithModels(objs, allModels);
    }

    @Override
    public String apiFilename(String templateName, String tag) {
        String dir = File.separator;
        if ("handler.mustache".equalsIgnoreCase(templateName)) {
            dir += handlerPackage.replace('.', File.separatorChar) + File.separator;
        }
        String t = this.getFirstTagName(tag);
        if (null != t && !t.isEmpty()) {
            dir += t.replace('/', File.separatorChar) + File.separatorChar;
        }
        dir = dir.replaceAll("-", "_");

        String suffix = this.apiTemplateFiles().get(templateName);
        return Paths.get(outputFolder, dir, this.toApiFilename(tag) + suffix).toString();
    }

    @Override
    public String toApiFilename(String name) {
        final String apiName;
        // replace - with _ e.g. created-at => created_at
        String api = name.replaceAll("-", "_");
        // e.g. PetApi.go => pet_api.go
        api = underscore(api);
        if (isReservedFilename(api)) {
            api += "_";
        }
        apiName = api;
        return apiName;
    }


    /**
     * Configures a friendly name for the generator.  This will be used by the generator
     * to select the library with the -g flag.
     *
     * @return the friendly name for the generator
     */
    @Override
    public String getName() {
        return "go-zero-server";
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

}
