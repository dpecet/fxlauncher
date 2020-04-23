package fxlauncher;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class JarManifest {

    private static final Logger log = Logger.getLogger(JarManifest.class.getName());

    private String appPath;
    private String manifestVersion;
    private String implementationVersion;
    private List<String> classPath;
    private String mainClass;

    public String getManifestVersion() {
        return manifestVersion;
    }

    public String getImplementationVersion() {
        return implementationVersion;
    }

    public List<String> getClassPath() {
        return classPath;
    }

    public String getMainClass() {
        return mainClass;
    }

    public String getAppPath() {
        return appPath;
    }

    public JarManifest(String appPath) {
        this.appPath = appPath;
    }

    private void parseString(String rawValue) {
        for (String line : getAllLines(rawValue)) {
            String value = line.substring(line.indexOf(":") + 1).trim();
            if (line.startsWith("Manifest-Version")) {
                this.manifestVersion = value;
            }
            if (line.startsWith("Implementation-Version")) {
                this.implementationVersion = value;
            }
            if (line.startsWith("Main-Class")) {
                this.mainClass = value;
            }
            if (line.startsWith("Class-Path")) {
                this.classPath = Arrays.asList(value.split("\\s")).stream().map(s -> s.trim()).collect(Collectors.toList());
            }
        }
    }

    private List<String> getAllLines(String rawValue) {
        List<String> allValues = new ArrayList<>();
        StringBuilder oneLine = new StringBuilder();
        for (String line : rawValue.split("\r\n")) {
            if (line.indexOf(":") != -1 && oneLine.length() > 0) {
                allValues.add(oneLine.toString());
                oneLine.setLength(0);
            }
            oneLine.append(line.trim());
        }
        allValues.add(oneLine.toString());
        return allValues;
    }

    @Override
    public String toString() {
        return "JarManifest{" +
                "manifestVersion='" + manifestVersion + '\'' +
                ", implementationVersion='" + implementationVersion + '\'' +
                ", classPath=" + classPath +
                ", mainClass='" + mainClass + '\'' +
                '}';
    }

    public static Optional<JarManifest> loadFromFile(String filePath) {
        try {
            if (!filePath.endsWith(".jar")) {
                log.info("File : " + filePath + " not jar file");
                return Optional.empty();
            }
            File file = new File(filePath);
            if (!file.exists()) {
                log.info("File : " + filePath + " doesn't exists");
                return Optional.empty();
            }
            String rawValue = new String(ZipUtil.getEntry("META-INF/MANIFEST.MF", new FileInputStream(file)));
            JarManifest jarManifest = new JarManifest(filePath);
            jarManifest.parseString(rawValue);
            log.info("Parsed : ");
            log.info(jarManifest.toString());
            return Optional.of(jarManifest);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error while loading manifest file", e);
        }
        return Optional.empty();
    }
}
