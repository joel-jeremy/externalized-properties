// package io.github.jeyjeyemem.externalizedproperties.core.internal;

// import java.lang.reflect.Method;
// import java.util.Objects;

// /**
//  * Key object that can be used as key values to identify a method.
//  */
// public class MethodKey {
//     private final String methodName;
//     private final String parameterTypesDescriptor;
//     private final int hash;

//     /**
//      * Constuctor.
//      * 
//      * @param method The method.
//      */
//     public MethodKey(Method method) {
//         this(method.getName(), method.getParameterTypes());
//     }

//     /**
//      * Constuctor.
//      * 
//      * @param methodName The method name.
//      * @param parameterTypes The method parameter types.
//      */
//     public MethodKey(String methodName, Class<?>[] parameterTypes) {
//         this.methodName = methodName;
//         this.parameterTypesDescriptor = buildParameterTypeDescriptor(parameterTypes);
//         this.hash = Objects.hash(methodName, parameterTypesDescriptor);
//     }

//     /**
//      * {@inheritDoc}
//      */
//     @Override
//     public int hashCode() {
//         return hash;
//     }

//     /**
//      * {@inheritDoc}
//      */
//     @Override
//     public boolean equals(Object other) {
//         if (other instanceof MethodKey) {
//             MethodKey otherKey = (MethodKey)other;
//             return methodName.equals(otherKey.methodName) &&
//                 parameterTypesDescriptor.equals(otherKey.parameterTypesDescriptor);
//         }

//         return false;
//     }

//     /**
//      * {@inheritDoc}
//      */
//     @Override
//     public String toString() {
//         return methodName + parameterTypesDescriptor;
//     }

//     private String buildParameterTypeDescriptor(Class<?>[] parameterTypes) {
//         StringBuilder sb = new StringBuilder();
        
//         sb.append("(");
//         separateWithCommas(parameterTypes, sb);
//         sb.append(")");

//         return sb.toString();
//     }

//     private void separateWithCommas(Class<?>[] parameterTypes, StringBuilder sb) {
//         for (int j = 0; j < parameterTypes.length; j++) {
//             sb.append(parameterTypes[j].getTypeName());
//             if (j < (parameterTypes.length - 1)) {
//                 sb.append(",");
//             }
//         }
//     }
// }