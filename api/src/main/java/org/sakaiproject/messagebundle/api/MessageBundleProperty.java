package org.sakaiproject.messagebundle.api;


/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Mar 16, 2010
 * Time: 12:57:00 PM
 * To change this template use File | Settings | File Templates.
 */


public class MessageBundleProperty {
    private Long id;
    private String moduleName;
    private String baseName;
    private String propertyName;
    private String value;
    private String locale;
    private String defaultValue;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "MessageBundleProperty{" +
                "id=" + id +
                ", moduleName='" + moduleName + '\'' +
                ", baseName='" + baseName + '\'' +
                ", propertyName='" + propertyName + '\'' +
                ", value='" + value + '\'' +
                ", locale='" + locale + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }
}
