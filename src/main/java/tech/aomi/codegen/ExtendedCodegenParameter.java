package tech.aomi.codegen;

import lombok.Getter;
import org.openapitools.codegen.CodegenParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExtendedCodegenParameter extends CodegenParameter {

    /**
     * 支持校验数字是否是某个数字的倍数
     */
    public boolean supportValidMultipleOf;

    /**
     * 支持校验正则表达式
     */
    public boolean supportValidRegexp;

    public String datetimeFormat;
    public String dateFormat;
    public String timeFormat;

    @Getter
    private List<Map<String, Object>> conditions;

    public boolean hasConditions;

    /**
     * 该属性的导入完整路径
     */
    public String importPath;
    /**
     * 该属性对应的包名j
     */
    public String packageName;
    /**
     * 该属性导入包的别名
     */
    public String alias;
    /**
     * 表明该属性的类型是否需要导入
     */
    public boolean needImport = false;

    public ExtendedCodegenParameter(CodegenParameter cp) {
        super();

        this.nameInPascalCase = cp.nameInPascalCase;
        this.nameInSnakeCase = cp.nameInSnakeCase;
        this.nameInCamelCase = cp.nameInCamelCase;

        this.isFormParam = cp.isFormParam;
        this.isQueryParam = cp.isQueryParam;
        this.isPathParam = cp.isPathParam;
        this.isHeaderParam = cp.isHeaderParam;
        this.isCookieParam = cp.isCookieParam;
        this.isBodyParam = cp.isBodyParam;
        this.isContainer = cp.isContainer;
        this.isCollectionFormatMulti = cp.isCollectionFormatMulti;
        this.isPrimitiveType = cp.isPrimitiveType;
        this.isModel = cp.isModel;
        this.isExplode = cp.isExplode;
        this.baseName = cp.baseName;
        this.paramName = cp.paramName;
        this.dataType = cp.dataType;
        this.datatypeWithEnum = cp.datatypeWithEnum;
        this.dataFormat = cp.dataFormat;
        this.contentType = cp.contentType;
        this.collectionFormat = cp.collectionFormat;
        this.description = cp.description;
        this.unescapedDescription = cp.unescapedDescription;
        this.baseType = cp.baseType;
        this.defaultValue = cp.defaultValue;
        this.enumName = cp.enumName;
        this.style = cp.style;
        this.nameInLowerCase = cp.nameInLowerCase;
        this.example = cp.example;
        this.jsonSchema = cp.jsonSchema;
        this.isString = cp.isString;
        this.isNumeric = cp.isNumeric;
        this.isInteger = cp.isInteger;
        this.isLong = cp.isLong;
        this.isNumber = cp.isNumber;
        this.isFloat = cp.isFloat;
        this.isDouble = cp.isDouble;
        this.isDecimal = cp.isDecimal;
        this.isByteArray = cp.isByteArray;
        this.isBinary = cp.isBinary;
        this.isBoolean = cp.isBoolean;
        this.isDate = cp.isDate;
        this.isDateTime = cp.isDateTime;
        this.isUuid = cp.isUuid;
        this.isUri = cp.isUri;
        this.isEmail = cp.isEmail;
        this.isFreeFormObject = cp.isFreeFormObject;
        this.isAnyType = cp.isAnyType;
        this.isArray = cp.isArray;
        this.isMap = cp.isMap;
        this.isFile = cp.isFile;
        this.isEnum = cp.isEnum;
        this._enum = cp._enum;
        this.allowableValues = cp.allowableValues;
        this.items = cp.items;
        this.additionalProperties = cp.additionalProperties;
        this.vars = cp.vars;
        this.requiredVars = cp.requiredVars;
        this.mostInnerItems = cp.mostInnerItems;
        this.vendorExtensions = cp.vendorExtensions;
        this.hasValidation = cp.hasValidation;
        this.isNullable = cp.isNullable;
        this.required = cp.required;
        this.maximum = cp.maximum;
        this.exclusiveMaximum = cp.exclusiveMaximum;
        this.minimum = cp.minimum;
        this.exclusiveMinimum = cp.exclusiveMinimum;
        this.maxLength = cp.maxLength;
        this.minLength = cp.minLength;
        this.pattern = cp.pattern;
        this.maxItems = cp.maxItems;
        this.minItems = cp.minItems;
        this.uniqueItems = cp.uniqueItems;
        this.multipleOf = cp.multipleOf;
        this.setHasVars(cp.getHasVars());
        this.setHasRequired(cp.getHasRequired());
        this.setMaxProperties(cp.getMaxProperties());
        this.setMinProperties(cp.getMinProperties());

        this.initConditions();

    }

    @Override
    public ExtendedCodegenParameter copy() {
        CodegenParameter superCopy = super.copy();
        return new ExtendedCodegenParameter(superCopy);
    }


    public boolean isIpv4() {
        return isNotEmpty(this.getFormat()) && "ipv4".equalsIgnoreCase(this.getFormat());
    }

    public boolean isIpv6() {
        return isNotEmpty(this.getFormat()) && "ipv6".equalsIgnoreCase(this.getFormat());
    }

    public boolean isHostname() {
        return isNotEmpty(this.getFormat()) && "hostname".equalsIgnoreCase(this.getFormat());
    }

    public boolean isTime() {
        return isNotEmpty(this.getFormat()) && "time".equalsIgnoreCase(this.getFormat());
    }

    public boolean isAnyTime() {
        return this.isDateTime || this.isDate || this.isTime();
    }

    private void initConditions() {
        List<Map<String, Object>> conditions = new ArrayList<>();

        if (null != this.maxLength) {
            conditions.add(new Condition("maxLength", this.maxLength));
        }
        if (null != this.minLength) {
            conditions.add(new Condition("minLength", this.minLength));
        }
        if (supportValidRegexp && isNotEmpty(this.pattern)) {
            conditions.add(new Condition("pattern", this.pattern));
        }

        if (null != this.maxLength) {
            conditions.add(new Condition("maxLength", this.maxLength));
        }

        if (isNotEmpty(this.minimum)) {
            conditions.add(new Condition("minimum", this.minimum));
        }
        if (isNotEmpty(this.maximum)) {
            conditions.add(new Condition("maximum", this.maximum));
        }
        if (this.supportValidMultipleOf && null != this.multipleOf) {
            conditions.add(new Condition("multipleOf", this.multipleOf));
        }
        if (null != this.maxItems) {
            conditions.add(new Condition("maxItems", this.maxItems));
        }
        if (null != this.minItems) {
            conditions.add(new Condition("minItems", this.minItems));
        }
        if (this.getUniqueItems()) {
            conditions.add(new Condition("uniqueItems", this.getUniqueItems()));
        }
        if (this.isEnum) {
            conditions.add(new Condition("isEnum", true));
        }
        if (this.required) {
            conditions.add(new Condition("required", true));
        }
        if (this.isEmail) {
            conditions.add(new Condition("isEmail", true));
        }
        if (this.isUri) {
            conditions.add(new Condition("isUri", true));
        }
        if (this.isPassword) {
            conditions.add(new Condition("isPassword", true));
        }
        if (this.isUuid) {
            conditions.add(new Condition("isUuid", true));
        }
        if (this.isIpv4()) {
            conditions.add(new Condition("isIpv4", true));
        }
        if (this.isIpv6()) {
            conditions.add(new Condition("isIpv6", true));
        }
        if (this.isHostname()) {
            conditions.add(new Condition("isHostname", true));
        }

        this.conditions = conditions;
        this.hasConditions = !conditions.isEmpty();

    }

    static boolean isNotEmpty(String str) {
        return null != str && !str.isEmpty();
    }

}
