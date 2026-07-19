package com.framework.util;

import java.lang.reflect.Method;

public class Util {
    public static boolean haveParameter(Method methode, Class<?> param) {
        for (Class<?> parameterType : methode.getParameterTypes()) {
            if (parameterType.equals(param)) {
                return true;
            }
        }

        return false;
    }
}
