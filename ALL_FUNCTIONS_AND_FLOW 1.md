# Complete Function and Method Documentation: inspire-vo-loader-config-service

This document covers **all Java files and all functions/methods** in the codebase, with a detailed chart for each. The flow is organized by runtime execution and logical grouping (controllers, services, domain, utilities, etc.).

---

## LoaderConfigApplication.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| main | static void main(String[] args) | Spring Boot application entry point. | Bootstraps the application, loading configuration and starting the context. |

---

## LoaderConfigurationController.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| LoaderConfigurationController | LoaderConfigurationController(LoaderConfigurationApplicationService loaderConfigurationApplicationService) | Constructor. Initializes the controller with the application service dependency. | Sets up the controller to delegate business logic to the service layer. |
| sanitize | static String sanitize(String input) | Utility method. Checks if input is not blank, trims whitespace, removes newlines. Throws BadRequestException if input is empty. | Ensures all path and request parameters are clean and safe before processing. |
| uploadConfig | ResponseEntity<SuccessResponse> uploadConfig(String partnerId, String industryType, String formatType, MultipartFile transformationConfig) | Handles POST request to upload loader transformation config. Validates and sanitizes input, delegates to service, returns success response. | Main endpoint for uploading loader configuration files for a partner/industry/format. |
| uploadLookup | ResponseEntity<SuccessResponse> uploadLookup(String partnerId, String industryType, MultipartFile lookupConfig) | Handles POST request to upload lookup config. Validates and sanitizes input, delegates to service, returns success response. | Main endpoint for uploading lookup configuration files for a partner/industry. |

---

## LoaderConfigurationControllerV2.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| LoaderConfigurationControllerV2 | LoaderConfigurationControllerV2(LoaderConfigurationApplicationService loaderConfigurationApplicationService) | Constructor. Initializes the controller with the application service dependency. | Sets up the controller to delegate business logic to the service layer. |
| sanitize | static String sanitize(String input) | Utility method. Checks if input is not blank, trims whitespace, removes newlines. Throws BadRequestException if input is empty. | Ensures all path and request parameters are clean and safe before processing. |
| uploadConfig | ResponseEntity<SuccessResponse> uploadConfig(String partnerId, String industryType, String formatType, MultipartFile transformationConfig) | Handles POST request to upload loader transformation config (V2). Validates and sanitizes input, delegates to service, returns success response. | Main endpoint for uploading loader configuration files for a partner/industry/format (V2 logic). |
| uploadLookup | ResponseEntity<SuccessResponse> uploadLookup(String partnerId, String industryType, MultipartFile lookupConfig) | Handles POST request to upload lookup config. Validates and sanitizes input, delegates to service, returns success response. | Main endpoint for uploading lookup configuration files for a partner/industry. |

---

## LoaderValidationService.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| validateWorkbook | List<Sheet> validateWorkbook(Workbook workbook) | Validates the uploaded workbook: checks mandatory sheets, collects transformation config sheets, validates each, checks generic master lookup, returns relevant sheets. | Ensures the uploaded Excel file is valid and ready for further processing. |
| validateLookup | void validateLookup(Workbook workbook) | Validates the uploaded workbook for lookup config: checks mandatory lookup sheets. | Ensures lookup Excel files have all required sheets. |
| getTransformationConfigSheets | List<Sheet> getTransformationConfigSheets(Workbook workbook) | Iterates all sheets, adds those whose name contains TARGET_SHEET to a list, returns the list. | Collects all transformation config sheets for validation and processing. |
| validateTransformationConfig | void validateTransformationConfig(Sheet transformedMemberSheet) | Validates a transformation config sheet: checks header, mandatory columns, validates each row for transformation type, loader column, assignment values, throws errors if issues found. | Ensures each transformation config sheet is correctly structured and filled. |
| checkMandatorySheets | void checkMandatorySheets(Workbook workbook) | Checks for presence of all required sheet names, throws error if any missing. | Ensures all required sheets are present in the workbook. |
| checkMandatoryLookupSheets | void checkMandatoryLookupSheets(Workbook workbook) | Checks for presence of all required lookup sheet names, throws error if all missing. | Ensures all required lookup sheets are present. |
| mandatoryTemplateColumnsMissing | BadRequestException mandatoryTemplateColumnsMissing(Row headerRow, List<String> mandatoryColumns) | Compares header row columns to required list, returns error if any missing or out of order, null if all present. | Ensures the header row matches the expected template. |
| validateGenericMasterLookup | void validateGenericMasterLookup(Workbook workbook) | Checks for the generic master lookup sheet, validates header, throws error if missing. | Ensures the generic master lookup sheet is present and valid. |
| checkMandatoryGenericLookupColumns | void checkMandatoryGenericLookupColumns(Row headerRow) | Checks for all required columns in the generic master lookup header, throws error if missing. | Ensures the generic master lookup sheet has all required columns. |

---

## LookupParserService.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| extractLookupConfigurations | Lookups extractLookupConfigurations(Workbook workbook) | Validates workbook, iterates sheets, validates and parses known lookup sheets, throws error if unknown or invalid, returns Lookups object. | Extracts and validates all lookup data from an Excel workbook. |
| validateAndParseLookupSheet | <T extends LookupRow> void validateAndParseLookupSheet(Sheet sheet, Consumer<CustomLookup<T>> setter, Class<T> clazz, List<String> errors) | Validates headers, parses rows if valid, sets result in Lookups, adds errors if invalid. | Ensures lookup sheets are well-formed before parsing. |
| parseLookupSheetRows | <ROW_TYPE extends LookupRow> CustomLookup<ROW_TYPE> parseLookupSheetRows(Sheet sheet, Class<ROW_TYPE> lookupClass) | Parses each data row into POJO, tracks which columns are filled, collects ignorable fields, returns CustomLookup. | Converts Excel rows into Java objects for lookups. |

---

## JSLTGenerator.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| JSLTGenerator | JSLTGenerator(InspireMappingProvider, TransformationFunctionsReader) | Constructor, stores mapping and function readers for JSLT generation. | Allows JSLTGenerator to access mapping and transformation function definitions. |
| sanitizeJSLT | static String sanitizeJSLT(String jsltExpression) | Removes function quotes, replaces escaped quotes and "null" strings. | Cleans up generated JSLT expressions for correct execution. |
| removeFunctionQuotes | static String removeFunctionQuotes(String expression) | Uses regex to find and remove quotes around function calls. | Ensures function calls in JSLT are not treated as strings. |
| addLookupInputColumns | static void addLookupInputColumns(String functionName, Map<String, Object> lookupAssociation, Map<String, String> lookup, List<String> functionParams) | Adds input columns for lookup functions to parameter list, handles single/multiple columns. | Formats lookup function parameters in JSLT. |
| customFunctionExpression | String customFunctionExpression(InspireColumnConfiguration, Map<String, Object>, List<Map<String, String>>, String, String) | Builds a custom function expression for a column, checks for overrides, falls back to lookup if none. | Generates the correct JSLT transformation for a column. |

---

## InspireMappingProvider.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| getInstance | static InspireMappingProvider getInstance() | Returns singleton instance, creates if needed. | Provides a single source of truth for Inspire mappings. |
| getInspirePath | InspireMapping getInspirePath(String gaColumn) | Looks up mapping for a given GA column. | Retrieves mapping details for a specific column. |
| loadInspireMapping | Map<String, InspireMapping> loadInspireMapping() | Reads mapping JSON, parses to list, builds map. | Loads and caches all Inspire mappings at startup. |
| getInspiredPath | String getInspiredPath(String inspireColumnName) | Looks up mapping for a given Inspire column name, returns path string or empty. | Gets the path string for a column, used in transformation logic. |

---

## TransformationFunctionsReader.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| TransformationFunctionsReader | TransformationFunctionsReader(InputStream fileInputStream) | Loads YAML config from input stream, stores all transformation function configs. | Provides access to transformation function definitions for JSLT generation. |
| mergedFunctions | Map<String, Object> mergedFunctions(List<Map<String, Object>> defaultConfigs, List<Map<String, Object>> customConfigs) | Merges default and custom transformation configs, custom overrides default. | Builds the final set of transformation functions for a context. |
| getFunctions | Map<String, Object> getFunctions(String partnerId, String industryType, String formatType) | Gets default and custom configs, merges, returns merged map. | Provides the correct set of transformation functions for a context. |
| getSpecialFunctions | List<Map<String, Object>> getSpecialFunctions() | Returns list of special functions from config. | Provides access to special-case transformation functions. |
| getMasterLookups | List<Map<String, String>> getMasterLookups() | Returns list of master lookups from config. | Provides access to master lookup definitions. |
| getLookups | List<Map<String, String>> getLookups() | Returns list of lookups from config. | Provides access to lookup definitions. |

---

## LoaderConfigurationService.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| LoaderConfigurationService | LoaderConfigurationService(LookupParserService, S3ApplicationService, LoaderValidationService) | Constructor, sets up dependencies, mapping provider, JSLT generator. | Prepares for processing config files and transformations. |
| createLoaderTransformationConfig | LoaderConfigRequest createLoaderTransformationConfig(MultipartFile, String, String, String) | Validates file, reads Excel, extracts configs, saves to S3, generates JSLT, extracts lookups, returns config request. | Processes and structures the uploaded config file. |
| ... | ... | ... | ... |

---

## CellTransformationUtil.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| getStringValue | static String getStringValue(Cell cell) | Calls getCellValue, converts to string, trims whitespace. | Safely extracts a string value from any Excel cell. |
| getCellValue | static String getCellValue(Cell cell) | Checks cell type, returns value as string, handles date/numeric/boolean/blank. | Extracts the raw value from an Excel cell. |
| toLowerCamelCase | static String toLowerCamelCase(String input) | Splits input into words, converts to lowerCamelCase. | Converts column names to a standard format. |
| trim | static String trim(String str) | Removes leading/trailing spaces (including non-breaking spaces). | Cleans up strings from Excel. |
| sanitize | static String sanitize(String input) | Trims input, removes newlines. | Sanitizes input strings for safe processing. |

---

## ObjectMapUtil.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| buildObjectStructure | static Map<String, Object> buildObjectStructure(SortedMap<String, Object> sortedMap) | Iterates keys, splits by '_', inserts value into nested map, returns built map. | Converts flat key-value data into a nested map structure. |
| setAttributeToPath | static void setAttributeToPath(Object, String[], int, Map<String, Object>) | Recursively walks key path, inserts value, handles lists. | Builds arbitrarily nested data structures from flat key paths. |
| getCurrentLevelDataForRepeatingAttributes | static Map<String, Object> getCurrentLevelDataForRepeatingAttributes(String, Map<String, Object>) | Handles repeated/nested attributes (like arrays/lists) in the nested map. | Supports lists/arrays in the nested object. |

---

## LoaderConfigServiceExceptionHandler.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| handleBadRequestException | ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException exception) | Handles BadRequestException, logs, returns error response with 400 status. | Centralizes error handling for bad requests. |
| handleSizeLimitException | ResponseEntity<ErrorResponse> handleSizeLimitException(MaxUploadSizeExceededException exception) | Handles file size limit errors, logs, returns error response with 400 status. | Handles file upload size errors. |
| handleConstraintViolationException | ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException exception) | Handles validation errors, logs, returns error response with appropriate status. | Handles bean validation errors. |
| handleGeneralException | ResponseEntity<com.hli.common.response.ErrorResponse> handleGeneralException(Exception exception) | Handles all uncaught exceptions, logs, returns error response with 500 status. | Catches and logs unexpected errors. |
| handleAccessDeniedException | ResponseEntity<com.hli.common.response.ErrorResponse> handleAccessDeniedException(UnauthorizedException exception) | Handles unauthorized access, logs, returns error response with 403 status. | Handles authorization errors. |
| handleBadRequestException (common) | ResponseEntity<com.hli.common.response.ErrorResponse> handleBadRequestException(com.hli.common.exceptions.BadRequestException exception) | Handles common bad request exceptions, logs, returns error response with 400 status. | Handles bad requests from common exception class. |

---

## ErrorResponse.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| ErrorResponse | ErrorResponse(PartnerErrorCode errorCode) | Constructor, sets error code and message. | Used to build error responses. |
| ErrorResponse | ErrorResponse(String errorCode, String errorMessage) | Constructor, sets error code and message. | Used to build error responses. |
| ErrorResponse | ErrorResponse(PartnerErrorCode errorCode, Object reasons) | Constructor, sets error code, message, and reasons. | Used to build error responses with reasons. |
| ErrorResponse | ErrorResponse(PartnerErrorCode errorCode, String message, Object reasons) | Constructor, sets error code, custom message, and reasons. | Used to build error responses with custom message and reasons. |

---

## Reason.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| Reason | Reason(String message) | Constructor, sets message. | Used to represent a reason for an error. |
| Reason | Reason(String fieldName, String message) | Constructor, sets field name and message. | Used to represent a field-specific error reason. |
| equals | boolean equals(Object o) | Checks equality based on field name and message. | For comparing Reason objects. |
| hashCode | int hashCode() | Generates hash code based on field name and message. | For use in hash-based collections. |

---

## AWSS3Client.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| addFileToS3 | void addFileToS3(String bucketName, MultipartFile file, String filepath) | Uploads a file to S3 using TransferManager. | Handles S3 file uploads. |
| getS3Url | String getS3Url(String bucketName, String docFilepath) | Returns the S3 URL for a given file. | Retrieves S3 file URLs. |
| shutdown | void shutdown() | Shuts down the TransferManager. | Cleans up S3 resources. |
| sanitize | String sanitize(String input) | Trims and removes newlines from input, throws if blank. | Cleans and validates file names. |

---

## PartnerClient.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| savePartnerLoaderConfiguration | SuccessResponse savePartnerLoaderConfiguration(LoaderTransformationJsltConfiguration config) | Calls partner service to save loader config. | Integrates with partner service for loader configs. |
| savePartnerLoaderConfigurationV2 | SuccessResponse savePartnerLoaderConfigurationV2(LoaderTransformationJsltConfigurationV2 config) | Calls partner service to save loader config v2. | Integrates with partner service for loader configs v2. |
| uploadDataValidationConfigs | SuccessResponse uploadDataValidationConfigs(String partnerId, String industryType, String formatType, DataValidationConfigRequest dataValidationConfigRequest) | Calls partner service to upload data validation configs. | Integrates with partner service for validation configs. |

---

## WebClientConfig.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| partnerWebClient | WebClient partnerWebClient(WebClient.Builder builder) | Builds a WebClient for partner service. | Configures HTTP client for partner service. |
| masterWebClient | WebClient masterWebClient(WebClient.Builder builder) | Builds a WebClient for master data service. | Configures HTTP client for master data service. |

---

## MasterDataClient.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| uploadGenericLookupConfigurationV1 | SuccessResponse uploadGenericLookupConfigurationV1(List<GenericLookup> genericLookupConfiguration) | Calls master data service to upload generic lookup config v1. | Integrates with master data service for generic lookups. |
| uploadGenericLookupConfiguration | SuccessResponse uploadGenericLookupConfiguration(List<GenericLookup> genericLookupConfiguration, String partnerId, String industryType, String formatType) | Calls master data service to upload generic lookup config. | Integrates with master data service for generic lookups. |

---

## GenericLookup.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| (interface) |  | Marker interface for generic lookup types. | Used for type safety and polymorphism. |

---

## GenericLookupRowV1.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| GenericLookupRowV1 | GenericLookupRowV1(List<FieldValuePairs> fieldValuePairs) | POJO for generic lookup row v1. | Represents a row in generic lookup v1. |

---

## GenericLookupRow.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| GenericLookupRow | GenericLookupRow(...) | POJO for generic lookup row. | Represents a row in generic lookup. |

---

## FieldValuePairs.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| FieldValuePairs | FieldValuePairs(String fieldName, String value) | Record for field name-value pairs. | Used for key-value pairs in lookups. |

---

## DataValidationConfig.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| DataValidationConfig | DataValidationConfig(String inspirePath, boolean dataValidation) | Record for data validation config. | Used for validation config in loader. |

---

## PlanNumberLookupRow.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| PlanNumberLookupRow | PlanNumberLookupRow(...) | Record for plan number lookup row. | Represents a row in plan number lookup. |

---

## LookupRow.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| (interface) |  | Marker interface for lookup row types. | Used for type safety and polymorphism. |

---

## CustomLookup.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| CustomLookup | CustomLookup(String lookupName, List<T> lookupRows, Set<String> ignorableFields) | POJO for custom lookup. | Represents a custom lookup with ignorable fields. |

---

## DataValidationConfigRequest.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| DataValidationConfigRequest | DataValidationConfigRequest(List<DataValidationConfig> validationConfigs) | Record for data validation config request. | Used to send validation configs to partner service. |

---

## Lookups.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| Lookups | Lookups(...) | POJO for all lookup types. | Aggregates all lookup requests for a partner. |

---

## PolicyNumberLookupRow.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| PolicyNumberLookupRow | PolicyNumberLookupRow(...) | Record for policy number lookup row. | Represents a row in policy number lookup. |

---

## LoanInterestRateLookupRow.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| LoanInterestRateLookupRow | LoanInterestRateLookupRow(...) | Record for loan interest rate lookup row. | Represents a row in loan interest rate lookup. |

---

## LookupConfig.java

| Function Name | Signature | What it Does | Why (Purpose) |
|--------------|-----------|--------------|---------------|
| LookupConfig | LookupConfig(String partner, String industryType, String s3Url, String folderPath, String fileName) | POJO for lookup config. | Holds metadata for a lookup config. |
