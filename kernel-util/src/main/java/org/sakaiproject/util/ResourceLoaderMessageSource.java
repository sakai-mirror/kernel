package org.sakaiproject.util;

import org.springframework.context.support.AbstractMessageSource;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jan 7, 2010
 * Time: 11:49:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class ResourceLoaderMessageSource extends AbstractMessageSource {
    ResourceLoader resourceLoader;

    public void setBasename(String baseName) {
        if (baseName.startsWith("classpath:")) {
            baseName = baseName.replaceFirst("classpath:", "");
            baseName = baseName.replaceAll("/",".");
        }
        this.resourceLoader= new ResourceLoader(baseName);
    }

    protected MessageFormat resolveCode(String code, Locale locale) {
        resourceLoader.setContextLocale(locale);
        String msg = resourceLoader.getString(code);
        return createMessageFormat(msg, locale);
    }

    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        resourceLoader.setContextLocale(locale);
        return resourceLoader.getString(code);
    }

    public void setCacheSeconds(int secs) {
        
    }
}