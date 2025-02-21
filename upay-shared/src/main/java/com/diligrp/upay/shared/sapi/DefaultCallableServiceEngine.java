package com.diligrp.upay.shared.sapi;

import com.diligrp.upay.shared.domain.ServiceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认可调用服务引擎实现类
 */
public class DefaultCallableServiceEngine implements ICallableServiceEngine {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultCallableServiceEngine.class);

    private static final Map<String, CallableServiceEndpoint<?>> endpoints = new ConcurrentHashMap<>();

    private static final String CHAR_COLON = ":";

    /**
     * 注册可调用服务点
     *
     * @param object      - 可调用服务组件对象
     * @param componentId - 服务组件唯一标识
     * @param methods     - 服务组件上的方法名
     */
    @Override
    public void registerCallableServiceEndpoints(Object object, String componentId, String... methods) {
        Class<?> classType = object.getClass();
        if (methods != null && methods.length > 0) {
            for (String name : methods) {
                try {
                    Method method = classType.getMethod(name, ServiceRequest.class);
                    registerCallableServiceEndpoint(object, componentId, method);
                } catch (NoSuchMethodException mex) {
                    LOG.warn("Callable service endpoint({}:{}) not found", componentId, name);
                }
            }
        } else {
            // Only scan public methods
            for (Method method : classType.getMethods()) {
                Type[] types = method.getGenericParameterTypes();
                // Only one parameter ServiceRequest<T> in method
                if (types.length == 1 && types[0] instanceof ParameterizedType type) {
                    if (type.getRawType() == ServiceRequest.class) {
                        registerCallableServiceEndpoint(object, componentId, method);
                    }
                }
            }
        }
    }

    /**
     * 获取可调用服务点
     *
     * @param service - 服务名,格式：componentId:methodName
     */
    @Override
    public CallableServiceEndpoint<?> callableServiceEndpoint(String service) {
        return endpoints.get(service);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // Do not scan AOP proxy object, since we cannot get required type(T in ServiceRequest<T>) from proxy object
        if (AopUtils.isAopProxy(bean)) {
            // Class<?> rawType = AopUtils.getTargetClass(object); You can get proxied object
            return bean;
        }

        CallableComponent annotation = bean.getClass().getAnnotation(CallableComponent.class);
        if (annotation != null) {
            String componentId = annotation.id();
            if (componentId == null) {
                componentId = beanName;
                LOG.warn("Service component id not set, use bean name instead: {}", componentId);
            }
            try {
                registerCallableServiceEndpoints(bean, componentId, annotation.methods());
            } catch (Exception ex) {
                LOG.error("Register callable service endpoint(" + componentId + ") failed", ex);
            }
        }

        return bean;
    }

    protected void registerCallableServiceEndpoint(Object target, String componentId, Method method) {
        Type[] types = method.getGenericParameterTypes();
        // Only one parameter ServiceRequest<T> in method
        if (types.length == 1 && types[0] instanceof ParameterizedType type) {
            // Get class type T in ServiceRequest<T>
            Class<?> requiredType;
            Type dataType = type.getActualTypeArguments()[0];
            if (dataType instanceof ParameterizedType) {
                requiredType = (Class<?>) ((ParameterizedType) dataType).getRawType();
            } else {
                requiredType = (Class<?>) dataType;
            }

            CallableServiceEndpoint<?> endpoint = CallableServiceEndpoint.create(target, method, requiredType);
            this.endpoints.put(componentId + CHAR_COLON + method.getName(), endpoint);
            LOG.info("Callable service endpoint ({}:{}) registered", componentId, method.getName());
        }
    }
}
