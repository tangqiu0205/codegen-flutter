package com.tangqiu.cloud.codegen

import io.swagger.codegen.v3.generators.DefaultCodegenConfig
import io.swagger.codegen.v3.*
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.MapSchema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.codegen.v3.generators.handlebars.ExtensionHelper
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

class FlutterClientCodegen : DefaultCodegenConfig() {
    private var browserClient = true
    private var pubName: String? = "swagger"
    private var pubClassName: String? = null
    private var pubVersion: String? = "1.0.0"
    private var pubDescription: String? = "Swagger API client"
    private var apiLibHost: String? = "http://127.0.0.1"
    private var apiLibVersion: String? = "1.0.0"
    private var basePath: String? = null
    private var useEnumExtension = false
    private var sourceFolder: String? = ""
    private var apiDocPath = "docs/"
    private var modelDocPath = "docs/"

    override fun getTag(): CodegenType = CodegenType.CLIENT

    override fun getName(): String = "flutter"

    override fun getHelp(): String = "Generates a Dart client library."

    override fun processOpts() {
        super.processOpts()
        if (additionalProperties.containsKey(BROWSER_CLIENT)) {
            browserClient = convertPropertyToBooleanAndWriteBack(BROWSER_CLIENT)
        } else {
            //not set, use to be passed to template
            additionalProperties[BROWSER_CLIENT] = browserClient
        }
        if (additionalProperties.containsKey(PUB_NAME)) {
            pubName = additionalProperties[PUB_NAME] as String?
        } else {
            //not set, use to be passed to template
            additionalProperties[PUB_NAME] = pubName
        }
        if (additionalProperties.containsKey(PUB_CLASS_NAME)) {
            pubClassName = additionalProperties[PUB_CLASS_NAME] as String?
        } else {
            //not set, use to be passed to template
            additionalProperties[PUB_CLASS_NAME] =
                if (pubClassName == null) pubName else pubClassName
        }
        if (additionalProperties.containsKey(PUB_VERSION)) {
            pubVersion = additionalProperties[PUB_VERSION] as String?
        } else {
            //not set, use to be passed to template
            additionalProperties[PUB_VERSION] = pubVersion
        }
        if (additionalProperties.containsKey(PUB_DESCRIPTION)) {
            pubDescription = additionalProperties[PUB_DESCRIPTION] as String?
        } else {
            //not set, use to be passed to template
            additionalProperties[PUB_DESCRIPTION] = pubDescription
        }
        if (additionalProperties.containsKey(USE_ENUM_EXTENSION)) {
            useEnumExtension = convertPropertyToBooleanAndWriteBack(USE_ENUM_EXTENSION)
        } else {
            // Not set, use to be passed to template.
            additionalProperties[USE_ENUM_EXTENSION] = useEnumExtension
        }
        if (additionalProperties.containsKey(API_LIB_HOST)) {
            apiLibHost = additionalProperties[API_LIB_HOST] as String?
        } else {
            //not set, use to be passed to template
            additionalProperties[API_LIB_HOST] = apiLibHost
        }
        if (additionalProperties.containsKey(API_LIB_VERSION)) {
            apiLibVersion = additionalProperties[API_LIB_VERSION] as String?
        } else {
            //not set, use to be passed to template
            additionalProperties[API_LIB_VERSION] = apiLibVersion
        }
        if (additionalProperties.containsKey(BASE_PATH)) {
            basePath = additionalProperties[BASE_PATH] as String?
        } else {
            //not set, use to be passed to template
            additionalProperties[BASE_PATH] = basePath
        }
        if (additionalProperties.containsKey(CodegenConstants.SOURCE_FOLDER)) {
            sourceFolder = additionalProperties[CodegenConstants.SOURCE_FOLDER] as String?
        }

        // make api and model doc path available in mustache template
        additionalProperties["apiDocPath"] = apiDocPath
        additionalProperties["modelDocPath"] = modelDocPath
        val libFolder = sourceFolder + File.separator + "lib"
        supportingFiles.add(SupportingFile("pubspec.mustache", "", "pubspec.yaml"))
        supportingFiles.add(SupportingFile("api_deserializer.mustache", libFolder, "api_deserializer.dart"))
        supportingFiles.add(SupportingFile("apilib.mustache", libFolder, "api.dart"))
        supportingFiles.add(SupportingFile("git_push.sh.mustache", "", "git_push.sh"))
        supportingFiles.add(SupportingFile("gitignore.mustache", "", ".gitignore"))
        supportingFiles.add(SupportingFile("README.mustache", "", "README.md"))
    }

    override fun preprocessOpenAPI(openAPI: OpenAPI) {
        super.preprocessOpenAPI(openAPI)
        if (openAPI.servers?.isNotEmpty() == true) {
            additionalProperties["nodePath"] = openAPI.servers[0].url.removeSuffix("/")
        }
    }

    override fun escapeReservedWord(name: String): String {
        return name + "_"
    }

    override fun apiFileFolder(): String {
        return outputFolder + "/" + sourceFolder + "/" + apiPackage().replace('.', File.separatorChar)
    }

    override fun modelFileFolder(): String {
        return outputFolder + "/" + sourceFolder + "/" + modelPackage().replace('.', File.separatorChar)
    }

    override fun apiTestFileFolder(): String {
        return outputFolder + "/" + sourceFolder + "/" + testPackage().replace('/', File.separatorChar)
    }

    override fun apiDocFileFolder(): String {
        return "$outputFolder/$apiDocPath".replace('/', File.separatorChar)
    }

    override fun modelDocFileFolder(): String {
        return "$outputFolder/$modelDocPath".replace('/', File.separatorChar)
    }

    override fun toVarName(name: String): String {
        // replace - with _ e.g. created-at => created_at
        var name1 = name.replace(
            "-".toRegex(),
            "_"
        ) // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.

        // if it's all uppper case, do nothing
        if (name1.matches("^[A-Z_]*$".toRegex())) {
            return name1
        }

        // camelize (lower first character) the variable name
        // pet_id => petId
        name1 = camelize(name1, true)
        if (name1.matches("^\\d.*".toRegex())) {
            name1 = "n$name1"
        }
        if (isReservedWord(name1)) {
            name1 = escapeReservedWord(name1)
        }
        return name1
    }

    override fun toParamName(name: String): String {
        // should be the same as variable name
        return toVarName(name)
    }

    override fun toModelName(name: String): String {
        // model name cannot use reserved keyword, e.g. return
        var name1 = name
        if (isReservedWord(name1)) {
            LOGGER.warn(
                "$name1 (reserved word) cannot be used as model filename. Renamed to " + camelize(
                    "model_$name1"
                )
            )
            name1 = "model_$name1" // e.g. return => ModelReturn (after camelize)
        }
        if (Character.isDigit(name1[0])) {
            LOGGER.warn(
                "$name1 start with number. Renamed to " + camelize(
                    "model_$name1"
                )
            )
            name1 = "model_$name1" // e.g. return => ModelReturn (after camelize)
        }

        // camelize the model name
        // phone_number => PhoneNumber
        return camelize(name1)
    }

    override fun toModelFilename(name: String): String = underscore(toModelName(name))

    override fun toApiFilename(name: String): String = underscore(toApiName(name))

    override fun toApiTestFilename(name: String): String = underscore(toApiName(name)) + "_test"

    override fun toDefaultValue(schema: Schema<*>): String {
        when (schema) {
            is MapSchema -> return "{}"
            is ArraySchema -> return "[]"
            is StringSchema -> if (schema.default != null) {
                val s = schema.default.toString()
                return if (schema.enum == null) {
                    String.format("\"%s\"", escapeText(s))
                } else {
                    // convert to enum var name later in postProcessModels
                    s
                }
            }
        }
        return super.toDefaultValue(schema)
    }

    override fun getTypeDeclaration(schema: Schema<*>?): String {
        if (schema is ArraySchema) {
            val inner = schema.items
            return getSchemaType(schema) + "<" + getTypeDeclaration(inner) + ">"
        } else if (schema is MapSchema) {
            val innerObject = schema.additionalProperties
            if (innerObject is Schema<*>) {
                return getSchemaType(schema) + "<String, " + getTypeDeclaration(innerObject) + ">"
            }
        }
        return super.getTypeDeclaration(schema)
    }

    override fun getSchemaType(schema: Schema<*>?): String {
        val swaggerType = super.getSchemaType(schema)
        var type: String? = null
        if (typeMapping.containsKey(swaggerType)) {
            type = typeMapping[swaggerType]
            if (languageSpecificPrimitives.contains(type)) {
                return type!!
            }
        } else {
            type = swaggerType
        }
        if (type == null) {
            type = "Object"
        }
        return toModelName(type)
    }

    override fun postProcessModels(objs: Map<String, Any>): Map<String, Any> {
        return postProcessModelsEnum(objs)
    }

    override fun postProcessModelsEnum(objs: Map<String, Any>): Map<String, Any> {
        val models = objs["models"] as List<Any>?
        for (_mo in models!!) {
            val modelMap = _mo as Map<String, Any>
            val codegenModel = modelMap["model"] as CodegenModel?
            val succes = buildEnumFromVendorExtension(codegenModel) || buildEnumFromValues(codegenModel)
            for (`var` in codegenModel!!.vars) {
                updateCodegenPropertyEnum(`var`)
            }
        }
        return objs
    }

    /**
     * Builds the set of enum members from their declared value.
     *
     * @return `true` if the enum was built
     */
    private fun buildEnumFromValues(codegenModel: CodegenModel?): Boolean {
        if (!ExtensionHelper.getBooleanValue(
                codegenModel,
                CodegenConstants.IS_ENUM_EXT_NAME
            ) || codegenModel!!.allowableValues == null
        ) {
            return false
        }
        val allowableValues = codegenModel.allowableValues
        val values = allowableValues["values"] as List<Any>?
        val enumVars: MutableList<Map<String, String>> = ArrayList()
        val commonPrefix = findCommonPrefixOfVars(values)
        val truncateIdx = commonPrefix.length
        for (value in values!!) {
            val enumVar: MutableMap<String, String> = HashMap()
            var enumName: String
            if (truncateIdx == 0) {
                enumName = value.toString()
            } else {
                enumName = value.toString().substring(truncateIdx)
                if ("" == enumName) {
                    enumName = value.toString()
                }
            }
            enumVar["name"] = toEnumVarName(enumName, codegenModel.dataType)
            enumVar["value"] = toEnumValue(value.toString(), codegenModel.dataType)
            enumVars.add(enumVar)
        }
        codegenModel.allowableValues["enumVars"] = enumVars
        return true
    }

    /**
     * Builds the set of enum members from a vendor extension.
     *
     * @return `true` if the enum was built
     */
    private fun buildEnumFromVendorExtension(codegenModel: CodegenModel?): Boolean {
        if (!ExtensionHelper.getBooleanValue(
                codegenModel,
                CodegenConstants.IS_ENUM_EXT_NAME
            ) || codegenModel!!.allowableValues == null ||
            !useEnumExtension ||
            !codegenModel.vendorExtensions.containsKey("x-enum-values")
        ) {
            return false
        }
        val extension = codegenModel.vendorExtensions["x-enum-values"]
        val values = extension as List<Map<String, Any>>?
        val enumVars: MutableList<Map<String, String>> = ArrayList()
        for (value in values!!) {
            val enumVar: MutableMap<String, String> = HashMap()
            var name = camelize(value["identifier"] as String?, true)
            if (isReservedWord(name)) {
                name = escapeReservedWord(name)
            }
            enumVar["name"] = name
            enumVar["value"] = toEnumValue(
                value["numericValue"].toString(), codegenModel.dataType
            )
            if (value.containsKey("description")) {
                enumVar["description"] = value["description"].toString()
            }
            enumVars.add(enumVar)
        }
        codegenModel.allowableValues["enumVars"] = enumVars
        return true
    }

    override fun toEnumVarName(value: String, datatype: String): String {
        if (value.isEmpty()) {
            return "empty"
        }
        var `var` = value.replace("\\W+".toRegex(), "_")
        if ("number".equals(datatype, ignoreCase = true) ||
            "int".equals(datatype, ignoreCase = true)
        ) {
            `var` = "Number$`var`"
        }
        return escapeReservedWord(camelize(`var`, true))
    }

    override fun toEnumValue(value: String, datatype: String): String {
        return if ("number".equals(datatype, ignoreCase = true) ||
            "int".equals(datatype, ignoreCase = true)
        ) {
            value
        } else {
            "\"" + escapeText(value) + "\""
        }
    }

    override fun toOperationId(operationId: String): String {
        // method name cannot use reserved keyword, e.g. return
        if (isReservedWord(operationId)) {
            val newOperationId = camelize("call_$operationId", true)
            LOGGER.warn("$operationId (reserved word) cannot be used as method name. Renamed to $newOperationId")
            return newOperationId
        }
        return camelize(operationId, true)
    }

    override fun escapeQuotationMark(input: String): String = // remove " to avoid code injection
        input.replace("\"", "")

    override fun escapeUnsafeCharacters(input: String): String = input.replace("*/", "*_/").replace("/*", "/_*")

    override fun getDefaultTemplateDir(): String = "flutter"

    companion object {
        const val BROWSER_CLIENT = "browserClient"
        const val PUB_NAME = "pubName"
        const val PUB_CLASS_NAME = "pubClassName"
        const val PUB_VERSION = "pubVersion"
        const val PUB_DESCRIPTION = "pubDescription"
        const val USE_ENUM_EXTENSION = "useEnumExtension"
        const val API_LIB_HOST = "apiLibHost"
        const val API_LIB_VERSION = "apiLibVersion"
        const val BASE_PATH = "basePath"
        private val LOGGER = LoggerFactory.getLogger(FlutterClientCodegen::class.java)
    }

    init {

        // clear import mapping (from default generator) as dart does not use it
        // at the moment
        importMapping.clear()
        outputFolder = "generated-code/dart"
        modelTemplateFiles["model.mustache"] = ".dart"
        apiTemplateFiles["api.mustache"] = ".dart"
        apiTestTemplateFiles["api_test.mustache"] = ".dart"
        templateDir = "dart"
        embeddedTemplateDir = templateDir
        apiPackage = "lib.api"
        modelPackage = "lib.model"
        testPackage = "tests"
        modelDocTemplateFiles["object_doc.mustache"] = ".md"
        apiDocTemplateFiles["api_doc.mustache"] = ".md"

        // default HIDE_GENERATION_TIMESTAMP to true
        hideGenerationTimestamp = java.lang.Boolean.TRUE
        setReservedWordsLowerCase(
            listOf(
                "abstract", "as", "assert", "async", "async*", "await",
                "break", "case", "catch", "class", "const", "continue",
                "default", "deferred", "do", "dynamic", "else", "enum",
                "export", "external", "extends", "factory", "false", "final",
                "finally", "for", "get", "if", "implements", "import", "in",
                "is", "library", "new", "null", "operator", "part", "rethrow",
                "return", "set", "static", "super", "switch", "sync*", "this",
                "throw", "true", "try", "typedef", "var", "void", "while",
                "int", "double", "with", "yield", "yield*"
            )
        )
        languageSpecificPrimitives = hashSetOf(
            "String",
            "bool",
            "int",
            "num",
            "double",
            "dynamic"
        )
        instantiationTypes["array"] = "List"
        instantiationTypes["map"] = "Map"
        typeMapping = HashMap()
        typeMapping["Array"] = "List"
        typeMapping["array"] = "List"
        typeMapping["List"] = "List"
        typeMapping["boolean"] = "bool"
        typeMapping["string"] = "String"
        typeMapping["char"] = "String"
        typeMapping["int"] = "int"
        // long仅用于ID
        typeMapping["long"] = "string"
        typeMapping["short"] = "int"
        typeMapping["number"] = "num"
        typeMapping["float"] = "double"
        typeMapping["double"] = "double"
        typeMapping["BigDecimal"] = "double"
        typeMapping["object"] = "dynamic"
        typeMapping["integer"] = "int"
        typeMapping["Date"] = "DateTime"
        typeMapping["date"] = "DateTime"
        typeMapping["File"] = "MultipartFile"
        typeMapping["UUID"] = "String"
        //TODO binary should be mapped to byte array
        // mapped to String as a workaround
        typeMapping["binary"] = "String"
        typeMapping["ByteArray"] = "String"
        cliOptions.add(CliOption(BROWSER_CLIENT, "Is the client browser based"))
        cliOptions.add(CliOption(PUB_NAME, "Name in generated pubspec"))
        cliOptions.add(CliOption(PUB_VERSION, "Version in generated pubspec"))
        cliOptions.add(CliOption(PUB_DESCRIPTION, "Description in generated pubspec"))
        cliOptions.add(CliOption(USE_ENUM_EXTENSION, "Allow the 'x-enum-values' extension for enums"))
        cliOptions.add(CliOption(CodegenConstants.SOURCE_FOLDER, "source folder for generated code"))
    }
}
