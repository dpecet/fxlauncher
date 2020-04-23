package fxlauncher;

import java.lang.reflect.Field;

public class LoaderParameters {

    private final String SPLIT_REGEX = "=";

    public String appPath;

    public LoaderParameters(String[] args) {
        for (String arg : args) {
            setParameter(arg.split(SPLIT_REGEX));
        }
    }

    private void setParameter(String[] param) {
        try {
            if (param.length == 2) {
                Field field = getClass().getDeclaredField(param[0]);
                field.set(this, field.getType().cast(param[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "LoaderParameters{" +
                "appPath='" + appPath + '\'' +
                '}';
    }
}
