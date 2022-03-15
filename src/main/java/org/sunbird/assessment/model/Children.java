
package org.sunbird.assessment.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "lastStatusChangedOn",
    "parent",
    "children",
    "name",
    "navigationMode",
    "createdOn",
    "generateDIALCodes",
    "lastUpdatedOn",
    "showTimer",
    "identifier",
    "description",
    "containsUserData",
    "allowSkip",
    "compatibilityLevel",
    "trackable",
    "primaryCategory",
    "setType",
    "languageCode",
    "attributions",
    "scoreCutoffType",
    "versionKey",
    "mimeType",
    "code",
    "license",
    "maxQuestions",
    "version",
    "prevStatus",
    "showHints",
    "subTitle",
    "language",
    "showFeedback",
    "objectType",
    "status",
    "requiresSubmit",
    "keywords",
    "shuffle",
    "minimumPassPercentage",
    "contentEncoding",
    "depth",
    "allowAnonymousAccess",
    "contentDisposition",
    "visibility",
    "showSolutions",
    "index"
})

public class Children {

    @JsonProperty("lastStatusChangedOn")
    private String lastStatusChangedOn;
    @JsonProperty("parent")
    private String parent;
    @JsonProperty("children")
    private List<Child> children = null;
    @JsonProperty("name")
    private String name;
    @JsonProperty("navigationMode")
    private String navigationMode;
    @JsonProperty("createdOn")
    private String createdOn;
    @JsonProperty("generateDIALCodes")
    private String generateDIALCodes;
    @JsonProperty("lastUpdatedOn")
    private String lastUpdatedOn;
    @JsonProperty("showTimer")
    private String showTimer;
    @JsonProperty("identifier")
    private String identifier;
    @JsonProperty("description")
    private String description;
    @JsonProperty("containsUserData")
    private String containsUserData;
    @JsonProperty("allowSkip")
    private String allowSkip;
    @JsonProperty("compatibilityLevel")
    private Long compatibilityLevel;
    @JsonProperty("trackable")
    private Trackable trackable;
    @JsonProperty("primaryCategory")
    private String primaryCategory;
    @JsonProperty("setType")
    private String setType;
    @JsonProperty("languageCode")
    private List<String> languageCode = null;
    @JsonProperty("attributions")
    private List<Object> attributions = null;
    @JsonProperty("scoreCutoffType")
    private String scoreCutoffType;
    @JsonProperty("versionKey")
    private String versionKey;
    @JsonProperty("mimeType")
    private String mimeType;
    @JsonProperty("code")
    private String code;
    @JsonProperty("license")
    private String license;
    @JsonProperty("maxQuestions")
    private Long maxQuestions;
    @JsonProperty("version")
    private Long version;
    @JsonProperty("prevStatus")
    private String prevStatus;
    @JsonProperty("showHints")
    private String showHints;
    @JsonProperty("subTitle")
    private String subTitle;
    @JsonProperty("language")
    private List<String> language = null;
    @JsonProperty("showFeedback")
    private String showFeedback;
    @JsonProperty("objectType")
    private String objectType;
    @JsonProperty("status")
    private String status;
    @JsonProperty("requiresSubmit")
    private String requiresSubmit;
    @JsonProperty("keywords")
    private List<String> keywords = null;
    @JsonProperty("shuffle")
    private Boolean shuffle;
    @JsonProperty("minimumPassPercentage")
    private Long minimumPassPercentage;
    @JsonProperty("contentEncoding")
    private String contentEncoding;
    @JsonProperty("depth")
    private Long depth;
    @JsonProperty("allowAnonymousAccess")
    private String allowAnonymousAccess;
    @JsonProperty("contentDisposition")
    private String contentDisposition;
    @JsonProperty("visibility")
    private String visibility;
    @JsonProperty("showSolutions")
    private String showSolutions;
    @JsonProperty("index")
    private Long index;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Children() {
    }

    /**
     * 
     * @param parent
     * @param code
     * @param allowSkip
     * @param keywords
     * @param containsUserData
     * @param prevStatus
     * @param description
     * @param language
     * @param mimeType
     * @param showHints
     * @param createdOn
     * @param objectType
     * @param scoreCutoffType
     * @param subTitle
     * @param children
     * @param primaryCategory
     * @param contentDisposition
     * @param lastUpdatedOn
     * @param contentEncoding
     * @param generateDIALCodes
     * @param showSolutions
     * @param trackable
     * @param allowAnonymousAccess
     * @param identifier
     * @param lastStatusChangedOn
     * @param requiresSubmit
     * @param visibility
     * @param showTimer
     * @param maxQuestions
     * @param index
     * @param setType
     * @param languageCode
     * @param minimumPassPercentage
     * @param version
     * @param versionKey
     * @param showFeedback
     * @param license
     * @param depth
     * @param compatibilityLevel
     * @param name
     * @param navigationMode
     * @param shuffle
     * @param attributions
     * @param status
     */
    public Children(String lastStatusChangedOn, String parent, List<Child> children, String name, String navigationMode, String createdOn, String generateDIALCodes, String lastUpdatedOn, String showTimer, String identifier, String description, String containsUserData, String allowSkip, Long compatibilityLevel, Trackable trackable, String primaryCategory, String setType, List<String> languageCode, List<Object> attributions, String scoreCutoffType, String versionKey, String mimeType, String code, String license, Long maxQuestions, Long version, String prevStatus, String showHints, String subTitle, List<String> language, String showFeedback, String objectType, String status, String requiresSubmit, List<String> keywords, Boolean shuffle, Long minimumPassPercentage, String contentEncoding, Long depth, String allowAnonymousAccess, String contentDisposition, String visibility, String showSolutions, Long index) {
        super();
        this.lastStatusChangedOn = lastStatusChangedOn;
        this.parent = parent;
        this.children = children;
        this.name = name;
        this.navigationMode = navigationMode;
        this.createdOn = createdOn;
        this.generateDIALCodes = generateDIALCodes;
        this.lastUpdatedOn = lastUpdatedOn;
        this.showTimer = showTimer;
        this.identifier = identifier;
        this.description = description;
        this.containsUserData = containsUserData;
        this.allowSkip = allowSkip;
        this.compatibilityLevel = compatibilityLevel;
        this.trackable = trackable;
        this.primaryCategory = primaryCategory;
        this.setType = setType;
        this.languageCode = languageCode;
        this.attributions = attributions;
        this.scoreCutoffType = scoreCutoffType;
        this.versionKey = versionKey;
        this.mimeType = mimeType;
        this.code = code;
        this.license = license;
        this.maxQuestions = maxQuestions;
        this.version = version;
        this.prevStatus = prevStatus;
        this.showHints = showHints;
        this.subTitle = subTitle;
        this.language = language;
        this.showFeedback = showFeedback;
        this.objectType = objectType;
        this.status = status;
        this.requiresSubmit = requiresSubmit;
        this.keywords = keywords;
        this.shuffle = shuffle;
        this.minimumPassPercentage = minimumPassPercentage;
        this.contentEncoding = contentEncoding;
        this.depth = depth;
        this.allowAnonymousAccess = allowAnonymousAccess;
        this.contentDisposition = contentDisposition;
        this.visibility = visibility;
        this.showSolutions = showSolutions;
        this.index = index;
    }

    @JsonProperty("lastStatusChangedOn")
    public String getLastStatusChangedOn() {
        return lastStatusChangedOn;
    }

    @JsonProperty("lastStatusChangedOn")
    public void setLastStatusChangedOn(String lastStatusChangedOn) {
        this.lastStatusChangedOn = lastStatusChangedOn;
    }

    public Children withLastStatusChangedOn(String lastStatusChangedOn) {
        this.lastStatusChangedOn = lastStatusChangedOn;
        return this;
    }

    @JsonProperty("parent")
    public String getParent() {
        return parent;
    }

    @JsonProperty("parent")
    public void setParent(String parent) {
        this.parent = parent;
    }

    public Children withParent(String parent) {
        this.parent = parent;
        return this;
    }

    @JsonProperty("children")
    public List<Child> getChildren() {
        return children;
    }

    @JsonProperty("children")
    public void setChildren(List<Child> children) {
        this.children = children;
    }

    public Children withChildren(List<Child> children) {
        this.children = children;
        return this;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    public Children withName(String name) {
        this.name = name;
        return this;
    }

    @JsonProperty("navigationMode")
    public String getNavigationMode() {
        return navigationMode;
    }

    @JsonProperty("navigationMode")
    public void setNavigationMode(String navigationMode) {
        this.navigationMode = navigationMode;
    }

    public Children withNavigationMode(String navigationMode) {
        this.navigationMode = navigationMode;
        return this;
    }

    @JsonProperty("createdOn")
    public String getCreatedOn() {
        return createdOn;
    }

    @JsonProperty("createdOn")
    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public Children withCreatedOn(String createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    @JsonProperty("generateDIALCodes")
    public String getGenerateDIALCodes() {
        return generateDIALCodes;
    }

    @JsonProperty("generateDIALCodes")
    public void setGenerateDIALCodes(String generateDIALCodes) {
        this.generateDIALCodes = generateDIALCodes;
    }

    public Children withGenerateDIALCodes(String generateDIALCodes) {
        this.generateDIALCodes = generateDIALCodes;
        return this;
    }

    @JsonProperty("lastUpdatedOn")
    public String getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    @JsonProperty("lastUpdatedOn")
    public void setLastUpdatedOn(String lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    public Children withLastUpdatedOn(String lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
        return this;
    }

    @JsonProperty("showTimer")
    public String getShowTimer() {
        return showTimer;
    }

    @JsonProperty("showTimer")
    public void setShowTimer(String showTimer) {
        this.showTimer = showTimer;
    }

    public Children withShowTimer(String showTimer) {
        this.showTimer = showTimer;
        return this;
    }

    @JsonProperty("identifier")
    public String getIdentifier() {
        return identifier;
    }

    @JsonProperty("identifier")
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Children withIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public Children withDescription(String description) {
        this.description = description;
        return this;
    }

    @JsonProperty("containsUserData")
    public String getContainsUserData() {
        return containsUserData;
    }

    @JsonProperty("containsUserData")
    public void setContainsUserData(String containsUserData) {
        this.containsUserData = containsUserData;
    }

    public Children withContainsUserData(String containsUserData) {
        this.containsUserData = containsUserData;
        return this;
    }

    @JsonProperty("allowSkip")
    public String getAllowSkip() {
        return allowSkip;
    }

    @JsonProperty("allowSkip")
    public void setAllowSkip(String allowSkip) {
        this.allowSkip = allowSkip;
    }

    public Children withAllowSkip(String allowSkip) {
        this.allowSkip = allowSkip;
        return this;
    }

    @JsonProperty("compatibilityLevel")
    public Long getCompatibilityLevel() {
        return compatibilityLevel;
    }

    @JsonProperty("compatibilityLevel")
    public void setCompatibilityLevel(Long compatibilityLevel) {
        this.compatibilityLevel = compatibilityLevel;
    }

    public Children withCompatibilityLevel(Long compatibilityLevel) {
        this.compatibilityLevel = compatibilityLevel;
        return this;
    }

    @JsonProperty("trackable")
    public Trackable getTrackable() {
        return trackable;
    }

    @JsonProperty("trackable")
    public void setTrackable(Trackable trackable) {
        this.trackable = trackable;
    }

    public Children withTrackable(Trackable trackable) {
        this.trackable = trackable;
        return this;
    }

    @JsonProperty("primaryCategory")
    public String getPrimaryCategory() {
        return primaryCategory;
    }

    @JsonProperty("primaryCategory")
    public void setPrimaryCategory(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public Children withPrimaryCategory(String primaryCategory) {
        this.primaryCategory = primaryCategory;
        return this;
    }

    @JsonProperty("setType")
    public String getSetType() {
        return setType;
    }

    @JsonProperty("setType")
    public void setSetType(String setType) {
        this.setType = setType;
    }

    public Children withSetType(String setType) {
        this.setType = setType;
        return this;
    }

    @JsonProperty("languageCode")
    public List<String> getLanguageCode() {
        return languageCode;
    }

    @JsonProperty("languageCode")
    public void setLanguageCode(List<String> languageCode) {
        this.languageCode = languageCode;
    }

    public Children withLanguageCode(List<String> languageCode) {
        this.languageCode = languageCode;
        return this;
    }

    @JsonProperty("attributions")
    public List<Object> getAttributions() {
        return attributions;
    }

    @JsonProperty("attributions")
    public void setAttributions(List<Object> attributions) {
        this.attributions = attributions;
    }

    public Children withAttributions(List<Object> attributions) {
        this.attributions = attributions;
        return this;
    }

    @JsonProperty("scoreCutoffType")
    public String getScoreCutoffType() {
        return scoreCutoffType;
    }

    @JsonProperty("scoreCutoffType")
    public void setScoreCutoffType(String scoreCutoffType) {
        this.scoreCutoffType = scoreCutoffType;
    }

    public Children withScoreCutoffType(String scoreCutoffType) {
        this.scoreCutoffType = scoreCutoffType;
        return this;
    }

    @JsonProperty("versionKey")
    public String getVersionKey() {
        return versionKey;
    }

    @JsonProperty("versionKey")
    public void setVersionKey(String versionKey) {
        this.versionKey = versionKey;
    }

    public Children withVersionKey(String versionKey) {
        this.versionKey = versionKey;
        return this;
    }

    @JsonProperty("mimeType")
    public String getMimeType() {
        return mimeType;
    }

    @JsonProperty("mimeType")
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Children withMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    @JsonProperty("code")
    public void setCode(String code) {
        this.code = code;
    }

    public Children withCode(String code) {
        this.code = code;
        return this;
    }

    @JsonProperty("license")
    public String getLicense() {
        return license;
    }

    @JsonProperty("license")
    public void setLicense(String license) {
        this.license = license;
    }

    public Children withLicense(String license) {
        this.license = license;
        return this;
    }

    @JsonProperty("maxQuestions")
    public Long getMaxQuestions() {
        return maxQuestions;
    }

    @JsonProperty("maxQuestions")
    public void setMaxQuestions(Long maxQuestions) {
        this.maxQuestions = maxQuestions;
    }

    public Children withMaxQuestions(Long maxQuestions) {
        this.maxQuestions = maxQuestions;
        return this;
    }

    @JsonProperty("version")
    public Long getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(Long version) {
        this.version = version;
    }

    public Children withVersion(Long version) {
        this.version = version;
        return this;
    }

    @JsonProperty("prevStatus")
    public String getPrevStatus() {
        return prevStatus;
    }

    @JsonProperty("prevStatus")
    public void setPrevStatus(String prevStatus) {
        this.prevStatus = prevStatus;
    }

    public Children withPrevStatus(String prevStatus) {
        this.prevStatus = prevStatus;
        return this;
    }

    @JsonProperty("showHints")
    public String getShowHints() {
        return showHints;
    }

    @JsonProperty("showHints")
    public void setShowHints(String showHints) {
        this.showHints = showHints;
    }

    public Children withShowHints(String showHints) {
        this.showHints = showHints;
        return this;
    }

    @JsonProperty("subTitle")
    public String getSubTitle() {
        return subTitle;
    }

    @JsonProperty("subTitle")
    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public Children withSubTitle(String subTitle) {
        this.subTitle = subTitle;
        return this;
    }

    @JsonProperty("language")
    public List<String> getLanguage() {
        return language;
    }

    @JsonProperty("language")
    public void setLanguage(List<String> language) {
        this.language = language;
    }

    public Children withLanguage(List<String> language) {
        this.language = language;
        return this;
    }

    @JsonProperty("showFeedback")
    public String getShowFeedback() {
        return showFeedback;
    }

    @JsonProperty("showFeedback")
    public void setShowFeedback(String showFeedback) {
        this.showFeedback = showFeedback;
    }

    public Children withShowFeedback(String showFeedback) {
        this.showFeedback = showFeedback;
        return this;
    }

    @JsonProperty("objectType")
    public String getObjectType() {
        return objectType;
    }

    @JsonProperty("objectType")
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public Children withObjectType(String objectType) {
        this.objectType = objectType;
        return this;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    public Children withStatus(String status) {
        this.status = status;
        return this;
    }

    @JsonProperty("requiresSubmit")
    public String getRequiresSubmit() {
        return requiresSubmit;
    }

    @JsonProperty("requiresSubmit")
    public void setRequiresSubmit(String requiresSubmit) {
        this.requiresSubmit = requiresSubmit;
    }

    public Children withRequiresSubmit(String requiresSubmit) {
        this.requiresSubmit = requiresSubmit;
        return this;
    }

    @JsonProperty("keywords")
    public List<String> getKeywords() {
        return keywords;
    }

    @JsonProperty("keywords")
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public Children withKeywords(List<String> keywords) {
        this.keywords = keywords;
        return this;
    }

    @JsonProperty("shuffle")
    public Boolean getShuffle() {
        return shuffle;
    }

    @JsonProperty("shuffle")
    public void setShuffle(Boolean shuffle) {
        this.shuffle = shuffle;
    }

    public Children withShuffle(Boolean shuffle) {
        this.shuffle = shuffle;
        return this;
    }

    @JsonProperty("minimumPassPercentage")
    public Long getMinimumPassPercentage() {
        return minimumPassPercentage;
    }

    @JsonProperty("minimumPassPercentage")
    public void setMinimumPassPercentage(Long minimumPassPercentage) {
        this.minimumPassPercentage = minimumPassPercentage;
    }

    public Children withMinimumPassPercentage(Long minimumPassPercentage) {
        this.minimumPassPercentage = minimumPassPercentage;
        return this;
    }

    @JsonProperty("contentEncoding")
    public String getContentEncoding() {
        return contentEncoding;
    }

    @JsonProperty("contentEncoding")
    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public Children withContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    @JsonProperty("depth")
    public Long getDepth() {
        return depth;
    }

    @JsonProperty("depth")
    public void setDepth(Long depth) {
        this.depth = depth;
    }

    public Children withDepth(Long depth) {
        this.depth = depth;
        return this;
    }

    @JsonProperty("allowAnonymousAccess")
    public String getAllowAnonymousAccess() {
        return allowAnonymousAccess;
    }

    @JsonProperty("allowAnonymousAccess")
    public void setAllowAnonymousAccess(String allowAnonymousAccess) {
        this.allowAnonymousAccess = allowAnonymousAccess;
    }

    public Children withAllowAnonymousAccess(String allowAnonymousAccess) {
        this.allowAnonymousAccess = allowAnonymousAccess;
        return this;
    }

    @JsonProperty("contentDisposition")
    public String getContentDisposition() {
        return contentDisposition;
    }

    @JsonProperty("contentDisposition")
    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    public Children withContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
        return this;
    }

    @JsonProperty("visibility")
    public String getVisibility() {
        return visibility;
    }

    @JsonProperty("visibility")
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public Children withVisibility(String visibility) {
        this.visibility = visibility;
        return this;
    }

    @JsonProperty("showSolutions")
    public String getShowSolutions() {
        return showSolutions;
    }

    @JsonProperty("showSolutions")
    public void setShowSolutions(String showSolutions) {
        this.showSolutions = showSolutions;
    }

    public Children withShowSolutions(String showSolutions) {
        this.showSolutions = showSolutions;
        return this;
    }

    @JsonProperty("index")
    public Long getIndex() {
        return index;
    }

    @JsonProperty("index")
    public void setIndex(Long index) {
        this.index = index;
    }

    public Children withIndex(Long index) {
        this.index = index;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Children.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("lastStatusChangedOn");
        sb.append('=');
        sb.append(((this.lastStatusChangedOn == null)?"<null>":this.lastStatusChangedOn));
        sb.append(',');
        sb.append("parent");
        sb.append('=');
        sb.append(((this.parent == null)?"<null>":this.parent));
        sb.append(',');
        sb.append("children");
        sb.append('=');
        sb.append(((this.children == null)?"<null>":this.children));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append(',');
        sb.append("navigationMode");
        sb.append('=');
        sb.append(((this.navigationMode == null)?"<null>":this.navigationMode));
        sb.append(',');
        sb.append("createdOn");
        sb.append('=');
        sb.append(((this.createdOn == null)?"<null>":this.createdOn));
        sb.append(',');
        sb.append("generateDIALCodes");
        sb.append('=');
        sb.append(((this.generateDIALCodes == null)?"<null>":this.generateDIALCodes));
        sb.append(',');
        sb.append("lastUpdatedOn");
        sb.append('=');
        sb.append(((this.lastUpdatedOn == null)?"<null>":this.lastUpdatedOn));
        sb.append(',');
        sb.append("showTimer");
        sb.append('=');
        sb.append(((this.showTimer == null)?"<null>":this.showTimer));
        sb.append(',');
        sb.append("identifier");
        sb.append('=');
        sb.append(((this.identifier == null)?"<null>":this.identifier));
        sb.append(',');
        sb.append("description");
        sb.append('=');
        sb.append(((this.description == null)?"<null>":this.description));
        sb.append(',');
        sb.append("containsUserData");
        sb.append('=');
        sb.append(((this.containsUserData == null)?"<null>":this.containsUserData));
        sb.append(',');
        sb.append("allowSkip");
        sb.append('=');
        sb.append(((this.allowSkip == null)?"<null>":this.allowSkip));
        sb.append(',');
        sb.append("compatibilityLevel");
        sb.append('=');
        sb.append(((this.compatibilityLevel == null)?"<null>":this.compatibilityLevel));
        sb.append(',');
        sb.append("trackable");
        sb.append('=');
        sb.append(((this.trackable == null)?"<null>":this.trackable));
        sb.append(',');
        sb.append("primaryCategory");
        sb.append('=');
        sb.append(((this.primaryCategory == null)?"<null>":this.primaryCategory));
        sb.append(',');
        sb.append("setType");
        sb.append('=');
        sb.append(((this.setType == null)?"<null>":this.setType));
        sb.append(',');
        sb.append("languageCode");
        sb.append('=');
        sb.append(((this.languageCode == null)?"<null>":this.languageCode));
        sb.append(',');
        sb.append("attributions");
        sb.append('=');
        sb.append(((this.attributions == null)?"<null>":this.attributions));
        sb.append(',');
        sb.append("scoreCutoffType");
        sb.append('=');
        sb.append(((this.scoreCutoffType == null)?"<null>":this.scoreCutoffType));
        sb.append(',');
        sb.append("versionKey");
        sb.append('=');
        sb.append(((this.versionKey == null)?"<null>":this.versionKey));
        sb.append(',');
        sb.append("mimeType");
        sb.append('=');
        sb.append(((this.mimeType == null)?"<null>":this.mimeType));
        sb.append(',');
        sb.append("code");
        sb.append('=');
        sb.append(((this.code == null)?"<null>":this.code));
        sb.append(',');
        sb.append("license");
        sb.append('=');
        sb.append(((this.license == null)?"<null>":this.license));
        sb.append(',');
        sb.append("maxQuestions");
        sb.append('=');
        sb.append(((this.maxQuestions == null)?"<null>":this.maxQuestions));
        sb.append(',');
        sb.append("version");
        sb.append('=');
        sb.append(((this.version == null)?"<null>":this.version));
        sb.append(',');
        sb.append("prevStatus");
        sb.append('=');
        sb.append(((this.prevStatus == null)?"<null>":this.prevStatus));
        sb.append(',');
        sb.append("showHints");
        sb.append('=');
        sb.append(((this.showHints == null)?"<null>":this.showHints));
        sb.append(',');
        sb.append("subTitle");
        sb.append('=');
        sb.append(((this.subTitle == null)?"<null>":this.subTitle));
        sb.append(',');
        sb.append("language");
        sb.append('=');
        sb.append(((this.language == null)?"<null>":this.language));
        sb.append(',');
        sb.append("showFeedback");
        sb.append('=');
        sb.append(((this.showFeedback == null)?"<null>":this.showFeedback));
        sb.append(',');
        sb.append("objectType");
        sb.append('=');
        sb.append(((this.objectType == null)?"<null>":this.objectType));
        sb.append(',');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null)?"<null>":this.status));
        sb.append(',');
        sb.append("requiresSubmit");
        sb.append('=');
        sb.append(((this.requiresSubmit == null)?"<null>":this.requiresSubmit));
        sb.append(',');
        sb.append("keywords");
        sb.append('=');
        sb.append(((this.keywords == null)?"<null>":this.keywords));
        sb.append(',');
        sb.append("shuffle");
        sb.append('=');
        sb.append(((this.shuffle == null)?"<null>":this.shuffle));
        sb.append(',');
        sb.append("minimumPassPercentage");
        sb.append('=');
        sb.append(((this.minimumPassPercentage == null)?"<null>":this.minimumPassPercentage));
        sb.append(',');
        sb.append("contentEncoding");
        sb.append('=');
        sb.append(((this.contentEncoding == null)?"<null>":this.contentEncoding));
        sb.append(',');
        sb.append("depth");
        sb.append('=');
        sb.append(((this.depth == null)?"<null>":this.depth));
        sb.append(',');
        sb.append("allowAnonymousAccess");
        sb.append('=');
        sb.append(((this.allowAnonymousAccess == null)?"<null>":this.allowAnonymousAccess));
        sb.append(',');
        sb.append("contentDisposition");
        sb.append('=');
        sb.append(((this.contentDisposition == null)?"<null>":this.contentDisposition));
        sb.append(',');
        sb.append("visibility");
        sb.append('=');
        sb.append(((this.visibility == null)?"<null>":this.visibility));
        sb.append(',');
        sb.append("showSolutions");
        sb.append('=');
        sb.append(((this.showSolutions == null)?"<null>":this.showSolutions));
        sb.append(',');
        sb.append("index");
        sb.append('=');
        sb.append(((this.index == null)?"<null>":this.index));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
