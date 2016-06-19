/*
 * bq.com
 *
 * @author Alberto Vilches (alberto.vilches@bq.com)
 * @date 15/6/16
 * Copyright. All Rights Reserved.
 */
package org.hs5tb.groospin.common;

class Ini {

    static String defaultSection = "#"

    Ini parent
    Map sections = [:]

    Ini load(Map map) {
        return load(defaultSection, map)
    }

    Ini load(String section, Map map) {
        map.each { key, val ->
            put(section, key, val)
        }
        return this
    }

    Ini parse(String iniRaw, String section = null, Collection<String> keys = null) {
        return parse(new StringReader(iniRaw), section, keys)
    }

    Ini parse(File iniFile, String section = null, Collection<String> keys = null) {
        return iniFile.exists() ? parse(iniFile.newReader(), section, keys) : this
    }
    /*
    RocketLauncher INI policy is:
    Ignore duplicated sections (only the first section is used)
    Ignore duplicated keys in the same section (only the first key is used)
     */
    static boolean ignoreDuplicatedSections = true
    static boolean ignoreDuplicatedKeys = true
    Ini parse(Reader iniFile, String section = null, Collection<String> includeOnlyKeys = null) {
        Set<String> sectionsParsed = new HashSet<>()
        Set<String> keysParsed  = new HashSet<>()
        boolean ignoreDuplicatedSection = false
        String currentSection = Ini.defaultSection
        section = section?.trim()?.toLowerCase()
        Ini ini = new Ini()
        includeOnlyKeys = includeOnlyKeys ? includeOnlyKeys.collect { String k -> k.trim().toLowerCase() } : null
        iniFile.eachLine { String line ->
            line = line.trim()
            if (!line || line.startsWith("#") || line.startsWith(";")) {
                return // Ignore comments
            } else if (line.startsWith("[")) {
                int pos = line.indexOf("]")
                if (pos > 1) {
                    currentSection = line.substring(1, pos).trim().toLowerCase()
                    keysParsed.clear()
                    if (currentSection in sectionsParsed) {
                        ignoreDuplicatedSection = true
                    } else {
                        if (ignoreDuplicatedSections) sectionsParsed << currentSection
                        ignoreDuplicatedSection = false
                    }
                }
            } else {
                if (line.contains("=") && !ignoreDuplicatedSection && (section == null || section == currentSection)) {
                    int equalsPos = line.indexOf("=")
                    String key = line.substring(0, equalsPos).trim().toLowerCase()
                    if (!keysParsed.contains(key) && (!includeOnlyKeys || key in includeOnlyKeys)) {
                        if (ignoreDuplicatedKeys) keysParsed << key
                        String value = line.substring(equalsPos + 1).trim()
                        ini.put(currentSection, key, value)
                    }
                }
            }
        }
        return ini
    }

    Object getAt(String property) {
        return get(property)
    }

    void putAt(String property, Object newValue) {
        put(property, newValue as String)
    }

    Object put(String key, String value) {
        return put(defaultSection, key as String, value as String)
    }

    String put(String section, String key, String value) {
        Map values = sections[section.trim().toLowerCase()]
        if (!values) {
            values = sections[section.trim().toLowerCase()] = [:]
        }
        return values.put(key.trim().toLowerCase(), value)
    }

    Object get(String key) {
        return get(defaultSection, key as String)
    }

    Map getDefaultSection() {
        return getSection(defaultSection)
    }

    Map getSection(String section) {
        Map map = this.sections[section.trim().toLowerCase()] ?: [:]
        if (parent) {
            parent.getSection(section).each { String key, String value ->
                if (!map.containsKey(key)) {
                    map[key] = value
                }
            }
        }
        return map
    }

    String get(String section, String key) {
        Map values = this.sections[section.trim().toLowerCase()]
        if (values) {
            String value = values[key.trim().toLowerCase()]
            if (value) return value
        }
        // No section or no value in section
        if (parent) {
            return parent.get(section, key)
        }
        return null
    }

}

