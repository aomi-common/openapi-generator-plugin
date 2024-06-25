package tech.aomi.codegen;

import lombok.Setter;
import org.openapitools.codegen.CliOption;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.OperationsMap;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MyGoGinServerGenerator extends AbstractGoWebServerGenerator {
    public static final String ROUTER_PACKAGE = "routerPackage";

    @Setter
    protected String routerPackage;

    public MyGoGinServerGenerator() {
        super();
        routerPackage = "main";

        /*
         * Models.  You can write model files using the modelTemplateFiles map.
         * if you want to create one template for file, you can do so here.
         * for multiple files for model, just put another entry in the `modelTemplateFiles` with
         * a different extension
         */
        modelTemplateFiles.put("model.mustache", ".go");

        /*
         * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
         * as with models, add multiple entries with different extensions for multiple files per
         * class
         */
        apiTemplateFiles.put(
                "interface.mustache",   // the template to use
                ".go");       // the extension for each file to write
        apiTemplateFiles.put(
                "handler.mustache",   // the template to use
                ".go");       // the extension for each file to write

        /*
         * Template Location.  This is the location which templates will be read from.  The generator
         * will use the resource stream to attempt to read the templates.
         */
        embeddedTemplateDir = templateDir = "my-go-gin-server";

        cliOptions.add(CliOption.newString(ROUTER_PACKAGE, "[routers.go]包路径."));
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (additionalProperties.containsKey(ROUTER_PACKAGE)) {
            this.setRouterPackage(additionalProperties.get(ROUTER_PACKAGE).toString());
        }

        /*
         * Supporting Files.  You can write single files for the generator with the
         * entire object tree available.  If the input file has a suffix of `.mustache
         * it will be processed by the template engine.  Otherwise, it will be copied
         */
        supportingFiles.add(new SupportingFile("routers.mustache", this.routerPackage, "routers.go"));
    }

    @Override
    public OperationsMap postProcessOperationsWithModels(OperationsMap objs, List<ModelMap> allModels) {
        OperationsMap operationsMap = super.postProcessOperationsWithModels(objs, allModels);

        operationsMap.setImports(operationsMap.getImports().stream().filter(item -> {
            if ("strings".equalsIgnoreCase(item.get("import"))) {
                return false;
            }
            return true;
        }).collect(Collectors.toList()));

        return operationsMap;
    }

    @Override
    public String apiFilename(String templateName, String tag) {
        String dir = File.separator;
        if ("interface.mustache".equalsIgnoreCase(templateName)) {
            dir += apiPackage.replace('.', File.separatorChar) + File.separator;
        } else if ("controller.mustache".equalsIgnoreCase(templateName)) {
            dir += controllerPackage.replace('.', File.separatorChar) + File.separator;
        } else if ("handler.mustache".equalsIgnoreCase(templateName)) {
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

    /**
     * Configures a friendly name for the generator.  This will be used by the generator
     * to select the library with the -g flag.
     *
     * @return the friendly name for the generator
     */
    @Override
    public String getName() {
        return "my-go-gin-server";
    }

    /**
     * Returns human-friendly help for the generator.  Provide the consumer with help
     * tips, parameters here
     *
     * @return A string value for the help message
     */
    @Override
    public String getHelp() {
        return "Generates a Go server library with the gin framework using OpenAPI-Generator." +
                "By default, it will also generate service classes.";

    }
}
