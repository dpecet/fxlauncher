package fxlauncher;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;


@SuppressWarnings("unchecked")
public abstract class AbstractLauncher<APP> {

    private static final Logger log = Logger.getLogger("AbstractLauncher");

    private final String LOGS_DIRECTORY = "logs";
    private final String LOG_FILE_NAME = "updater.log";

    private JarManifest manifest = null;
    private String phase;

    protected void setupLogFile() throws IOException {
        String filename = LOGS_DIRECTORY + File.separator + LOG_FILE_NAME;
        new File(LOGS_DIRECTORY).mkdirs();
        System.out.println("logging to " + filename);
        FileHandler handler = new FileHandler(filename);
        handler.setFormatter(new SimpleFormatter());
        log.addHandler(handler);
    }

    protected ClassLoader createClassLoader() {
        List<URL> libs = manifest.getClassPath().stream().map(it -> getUrl(it)).collect(Collectors.toList());
        libs.add(getUrl(manifest.getAppPath()));

        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        if (systemClassLoader instanceof FxlauncherClassCloader) {
            ((FxlauncherClassCloader) systemClassLoader).addUrls(libs);
            return systemClassLoader;
        } else {
            ClassLoader classLoader = new URLClassLoader(libs.toArray(new URL[libs.size()]));
            Thread.currentThread().setContextClassLoader(classLoader);
            setupClassLoader(classLoader);
            return classLoader;
        }
    }

    protected URL getUrl(String path) {
        try {
            return new File(path).toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean loadJarManifest(String fileName) {
        if (fileName != null) {
            log.info("Loading manifest for file : " + fileName);
            Optional<JarManifest> oJarManifest = JarManifest.loadFromFile(fileName);
            if (oJarManifest.isPresent()) {
                this.manifest = oJarManifest.get();
                if (manifest.getMainClass() == null) {
                    log.severe("No main class in manifest, error");
                    return false;
                }
                manifest = oJarManifest.get();
                return true;
            }
        } else {
            log.info("No application file defined");
        }
        return false;
    }

    /**
     * Check if remote files are newer then local files. Return true if files are updated, triggering the whatsnew option else false.
     * Also return false and do not check for updates if the <code>--offline</code> commandline argument is set.
     *
     * @return true if new files have been downloaded, false otherwise.
     * @throws Exception
     */
    protected boolean syncFiles() throws Exception {

//        Path cacheDir = manifest.resolveCacheDir(getParameters().getNamed());
//        log.info(String.format("Using cache dir %s", cacheDir));
//
//        phase = "File Synchronization";
//
//        if (getParameters().getUnnamed().contains("--offline")) {
//            log.info("not updating files from remote, offline selected");
//            return false; // to signal that nothing has changed.
//        }
//        List<LibraryFile> needsUpdate = manifest.files.stream()
//                .filter(LibraryFile::loadForCurrentPlatform)
//                .filter(it -> it.needsUpdate(cacheDir))
//                .collect(Collectors.toList());
//
//        if (needsUpdate.isEmpty())
//            return false;
//
//        Long totalBytes = needsUpdate.stream().mapToLong(f -> f.size).sum();
//        Long totalWritten = 0L;
//
//        for (LibraryFile lib : needsUpdate) {
//            Path target = cacheDir.resolve(lib.file).toAbsolutePath();
//            Files.createDirectories(target.getParent());
//
//            URI uri;
//
//            // We avoid using uri.resolve() here so as to not break UNC paths. See issue #143
//            String separator = manifest.uri.getPath().endsWith("/") ? "" : "/";
//            uri = URI.create(manifest.uri.toString() + separator + lib.file);
//
//
//            try (InputStream input = openDownloadStream(uri); OutputStream output = Files.newOutputStream(target)) {
//
//                byte[] buf = new byte[65536];
//
//                int read;
//                while ((read = input.read(buf)) > -1) {
//                    output.write(buf, 0, read);
//                    totalWritten += read;
//                    Double progress = totalWritten.doubleValue() / totalBytes.doubleValue();
//                    updateProgress(progress);
//                }
//            }
//        }
        return true;
    }

    protected void createApplicationEnvironment() throws Exception {
        phase = "Create Application";

        ClassLoader classLoader = createClassLoader();
        Class<APP> appclass = (Class<APP>) classLoader.loadClass(manifest.getMainClass());

        createApplication(appclass);
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public JarManifest getManifest() {
        return manifest;
    }

    protected abstract void updateProgress(double progress);

    protected abstract void createApplication(Class<APP> appClass);

    protected abstract void reportError(String title, Throwable error);

    protected abstract void setupClassLoader(ClassLoader classLoader);
}
