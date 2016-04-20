package main.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import org.javatuples.Pair;

public class PropertyReader {
    public PropertyReader() {
        readAllProperties(null);
    }

    public PropertyReader(String filename) {
        readAllProperties(filename);
    }

    public Map<String, String> getPropertyMap() {
        if (propertyMap == null)
            gatherPropertiesToMap();
        return propertyMap;
    }

    public Set<Pair<String, Boolean>> getPropertiesNotPresent() {
        if (propertyMap == null)
            gatherPropertiesToMap();
        return notPresentPropertiesSet;
    }

    private void readAllProperties(@Nullable String filename) {
        if (filename == null)
            readPropertyFile(PROPERTIES_FILENAME_DEFAULT);
        else
            readPropertyFile(filename);
        readDefaultPropertyFile();
    }

    private void readPropertyFile(String filename) {
        try (final FileInputStream fis = new FileInputStream(filename)) {
            properties.load(fis);
        } catch (FileNotFoundException e) {
            LOGGER.error("Could not find \"" + filename + "\" in root folder!", e);
        } catch (IOException e) {
            LOGGER.error("Weird error during loading \"" + filename + "\"!", e);
        }
    }

    private void readDefaultPropertyFile() {
        try (final FileInputStream fis = new FileInputStream(DEFAULT_PROPERTIES_FILENAME_DEFAULT)) {
            defaultProperties.load(fis);
        } catch (FileNotFoundException e) {
            LOGGER.error("Could not find \"" + DEFAULT_PROPERTIES_FILENAME_DEFAULT + "\" in root folder!", e);
        } catch (IOException e) {
            LOGGER.error("Weird error during loading \"" + DEFAULT_PROPERTIES_FILENAME_DEFAULT + "\"!", e);
        }
    }

    private void makeAllPropertiesNotPresent() {
        for (String requiredPropertyName : NECESSARY_FIELDS)
            notPresentPropertiesSet.add(new Pair<>(requiredPropertyName, true));

        for (String optionalPropertyName : UNNECESSARY_FIELDS)
            notPresentPropertiesSet.add(new Pair<>(optionalPropertyName, false));

        // do the same for additional properties
    }

    private void fillPropertyMap(Properties fromFile, @Nullable String sourceName) {
        if (sourceName == null)
            sourceName = "";

        for (Pair<String, Boolean> property : notPresentPropertiesSet) {
            final String propertyName = property.getValue0();
            final boolean isNecessary = property.getValue1();

            final String propertyValue = fromFile.getProperty(propertyName);

            if (propertyValue != null)
            {
                propertyMap.put(propertyName, propertyValue);
                notPresentPropertiesSet.remove(property);
                LOGGER.info("Property " + propertyName + " loaded as " + propertyValue + " from " + sourceName);
            }
            else if (isNecessary)
                LOGGER.warn("Could not load property " + propertyName + " from " + sourceName);
            else
                LOGGER.info("Could not load property " + propertyName + " from " + sourceName);
        }
    }

    private void checkIfAllPropertiesPresent() {
        for (Pair<String, Boolean> property : notPresentPropertiesSet) {
            final String log = "Property  " + property.getValue0() + " was not loaded!";
            if (property.getValue1())
                LOGGER.warn(log + " Pray for developer's hands' curvature radius!");
            else
                LOGGER.info(log);
        }
    }

    private void gatherPropertiesToMap() {
        propertyMap = new HashMap<>();
        makeAllPropertiesNotPresent();
        fillPropertyMap(properties, "settings file");
        fillPropertyMap(defaultProperties, "default settings file");
        checkIfAllPropertiesPresent();
    }

    private final Properties properties = new Properties();
    private final Properties defaultProperties = new Properties();

    private Map<String, String> propertyMap = null;
    private final Set<Pair<String, Boolean>> notPresentPropertiesSet = new ConcurrentHashSet<>(); // Weird, but non-concurrent HashSet throws exception on second read.

    private static final Logger LOGGER = LogManager.getLogger(PropertyReader.class);

    private static final String DEFAULT_PROPERTIES_FILENAME_DEFAULT = "cfg/default.properties";
    private static final String PROPERTIES_FILENAME_DEFAULT = "cfg/server.properties";
    private static final String[] NECESSARY_FIELDS = new String[]{"ip", "port", "db_type", "db_domain", "db_port", "db_name", "db_user", "db_password", "db_creation_method"};
    private static final String[] UNNECESSARY_FIELDS = new String[]{"db_name_debug", "db_user_debug", "db_password_debug", "db_creation_method_debug", "db_root_password", "ws_timeout", "host"};

}