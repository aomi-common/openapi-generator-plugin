package tech.aomi.codegen;

import io.swagger.v3.oas.models.media.Schema;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.model.ModelsMap;

import java.io.File;
import java.nio.file.Paths;

public class GoZeroServerGenerator extends AbstractGoWebServerGenerator {

    public GoZeroServerGenerator() {
        super();
        modelPackage = this.apiPackage + "/" + "model";
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
        embeddedTemplateDir = templateDir = "go-zero-server";

    }

    @Override
    public void processOpts() {
        super.processOpts();
        /*
         * Supporting Files.  You can write single files for the generator with the
         * entire object tree available.  If the input file has a suffix of `.mustache
         * it will be processed by the template engine.  Otherwise, it will be copied
         */
//        supportingFiles.add(new SupportingFile("routers.mustache", this.routerPackage, "routers.go"));
    }

    @Override
    public ModelsMap postProcessModels(ModelsMap objs, ModelsMap models) {
        return models;
    }

    @Override
    public String apiFilename(String templateName, String tag) {
        String dir = File.separator + apiPackage + File.separator;

        String t = this.getFirstTagName(tag);
        if (null != t && !t.isEmpty()) {
            dir += t.replace('/', File.separatorChar) + File.separatorChar;
        }
        dir = dir.replaceAll("-", "_");

        String suffix = this.apiTemplateFiles().get(templateName);
        return this.apiFileFolder() + dir + this.toApiFilename(tag) + suffix;
    }

    @Override
    public CodegenModel fromModel(CodegenModel model, String name, Schema schema) {
        return model;
    }

    @Override
    public String toModelImport(String name) {
        String i = super.toModelImport(name);
        String filename = this.toModelFilename(name);

        return Paths.get(i, filename).toString();
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
