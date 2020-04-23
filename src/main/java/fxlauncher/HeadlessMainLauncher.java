package fxlauncher;

import javafx.application.Application;
import javafx.application.Platform;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class HeadlessMainLauncher extends AbstractLauncher<Object> {
    private static final Logger log = Logger.getLogger("HeadlessMainLauncher");

    private LoaderParameters parameters;

    private Class<?> appClass;

    public HeadlessMainLauncher(String[] parameters) {
        this.parameters = new LoaderParameters(parameters);
        log.info(this.parameters.toString());
    }

    public static void main(String[] args) throws Exception {
        HeadlessMainLauncher headlessMainLauncher = new HeadlessMainLauncher(args);
        headlessMainLauncher.process();
    }

    protected void process() throws Exception {

        setupLogFile();
        syncFiles();
        if (!loadJarManifest(this.parameters.appPath)) {
            log.info("Error while loading manifest jar file, exiting application");
            System.exit(0);
        }
        createApplicationEnvironment();
        launchApp();
    }

    @Override
    protected void updateProgress(double progress) {
        log.info(String.format("Progress: %d%%", (int) (progress * 100)));
    }

    @Override
    protected void createApplication(Class<Object> appClass) {
        this.appClass = appClass;
    }

    private void launchApp() throws Exception {
        setPhase("Application Start");

        Method mainMethod = appClass.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) new String[0]);
    }

    protected void reportError(String title, Throwable error) {
        log.log(Level.SEVERE, title, error);
    }

    @Override
    protected void setupClassLoader(ClassLoader classLoader) {
    }

}
