package io.github.jeyjeyemem.externalizedproperties.core.testentities;

import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.EnvironmentVariablesProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.SystemPropertiesProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testfixtures.ProxyMethodUtils;

public class ProxyMethods {
    private ProxyMethods(){}

    public static ProxyMethod javaVersion() {
        return ProxyMethodUtils.fromMethod(
            SystemPropertiesProxyInterface.class, 
            "javaVersion"
        );
    }

    public static ProxyMethod path() {
        return ProxyMethodUtils.fromMethod(
            EnvironmentVariablesProxyInterface.class, 
            "path"
        );
    }
    
    public static ProxyMethod javaHome() {
        return ProxyMethodUtils.fromMethod(
            EnvironmentVariablesProxyInterface.class, 
            "javaHome"
        );
    }

    public static ProxyMethod property() {
        return ProxyMethodUtils.fromMethod(
            BasicProxyInterface.class, 
            "property"
        );
    }

    public static ProxyMethod property1() {
        return ProxyMethodUtils.fromMethod(
            BasicProxyInterface.class, 
            "property1"
        );
    }

    public static ProxyMethod property2() {
        return ProxyMethodUtils.fromMethod(
            BasicProxyInterface.class, 
            "property2"
        );
    }

    public static ProxyMethod property3() {
        return ProxyMethodUtils.fromMethod(
            BasicProxyInterface.class, 
            "property3"
        );
    }

    public static ProxyMethod intProperty() {
        return ProxyMethodUtils.fromMethod(
            PrimitiveProxyInterface.class, 
            "intPrimitiveProperty"
        );
    }

    public static ProxyMethod longProperty() {
        return ProxyMethodUtils.fromMethod(
            PrimitiveProxyInterface.class, 
            "longPrimitiveProperty"
        );
    }

    public static ProxyMethod floatProperty() {
        return ProxyMethodUtils.fromMethod(
            PrimitiveProxyInterface.class, 
            "floatPrimitiveProperty"
        );
    }

    public static ProxyMethod doubleProperty() {
        return ProxyMethodUtils.fromMethod(
            PrimitiveProxyInterface.class, 
            "doublePrimitiveProperty"
        );
    }

    public static ProxyMethod shortProperty() {
        return ProxyMethodUtils.fromMethod(
            PrimitiveProxyInterface.class, 
            "shortPrimitiveProperty"
        );
    }

    public static ProxyMethod booleanProperty() {
        return ProxyMethodUtils.fromMethod(
            PrimitiveProxyInterface.class, 
            "booleanPrimitiveProperty"
        );
    }
}
