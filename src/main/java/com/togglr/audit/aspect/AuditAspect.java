package com.togglr.audit.aspect;

import com.togglr.audit.enums.DataSource;
import com.togglr.audit.enums.EntityType;
import com.togglr.audit.service.AuditService;
import com.togglr.feature.entity.Environment;
import com.togglr.feature.entity.Feature;
import com.togglr.feature.entity.Namespace;
import com.togglr.feature.repository.EnvironmentRepository;
import com.togglr.feature.repository.FeatureRepository;
import com.togglr.feature.repository.NamespaceRepository;
import com.togglr.rest.dto.FeatureResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {
    private final AuditService auditService;
    private final FeatureRepository featureRepository;
    private final NamespaceRepository namespaceRepository;
    private final EnvironmentRepository environmentRepository;
    private final CacheManager cacheManager;

    @AfterReturning(value = "execution(* com.togglr.feature.service.FeatureService.createFeature(..))", returning = "result")
    public void auditFeatureCreate(Object result) {
        if (result != null) {
            Feature feature = (Feature) result;
            auditService.logCreate(EntityType.FEATURE, feature.getId(), feature.getName(), feature);
        }
    }

    @Around("execution(* com.togglr.rest.controller.FeatureController.getFeature(..))")
    public Object auditFeatureAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String name = (String) args[0];
        String namespaceName = (String) args[1];
        String environmentName = (String) args[2];

        String cacheKey = name + "_" + namespaceName + "_" + environmentName;
        Cache cache = cacheManager.getCache("features");
        boolean existedInCache = cache != null && cache.get(cacheKey) != null;

        Object result = joinPoint.proceed();

        if (result != null) {
            Object responseBody = ((org.springframework.http.ResponseEntity<?>) result).getBody();

            if (responseBody instanceof FeatureResponse featureResponse) {
                DataSource dataSource = existedInCache ? DataSource.CACHE : DataSource.DATABASE;
                auditService.logAccess(EntityType.FEATURE, featureResponse.id(), featureResponse.name(), dataSource);
            }
        }

        return result;
    }

    @Around("execution(* com.togglr.feature.service.FeatureService.updateFeature(..))")
    public Object auditFeatureUpdate(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Long featureId = (Long) args[0];

        Feature originalFeature = featureRepository.findByIdWithRelations(featureId).orElse(null);
        Feature oldFeature = null;

        if (originalFeature != null) {
            oldFeature = Feature.builder()
                    .name(originalFeature.getName())
                    .namespace(originalFeature.getNamespace())
                    .environment(originalFeature.getEnvironment())
                    .enabled(originalFeature.getEnabled())
                    .metadata(originalFeature.getMetadata())
                    .build();
            oldFeature.setId(originalFeature.getId());
            oldFeature.setCreatedAt(originalFeature.getCreatedAt());
            oldFeature.setUpdatedAt(originalFeature.getUpdatedAt());
        }

        Object result = joinPoint.proceed();

        if (result != null && oldFeature != null) {
            Feature newFeature = (Feature) result;
            auditService.logUpdate(EntityType.FEATURE, newFeature.getId(), newFeature.getName(), oldFeature, newFeature);
        }

        return result;
    }

    @Around("execution(* com.togglr.feature.service.FeatureService.deleteFeature(..))")
    public Object auditFeatureDelete(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Long featureId = (Long) args[0];

        Feature oldFeature = featureRepository.findByIdWithRelations(featureId).orElse(null);
        Object result = joinPoint.proceed();

        if (oldFeature != null) {
            auditService.logDelete(EntityType.FEATURE, oldFeature.getId(), oldFeature.getName(), oldFeature);
        }

        return result;
    }

    // Namespace Audit
    @AfterReturning(value = "execution(* com.togglr.feature.service.NamespaceService.create(..))", returning = "result")
    public void auditNamespaceCreate(Object result) {
        if (result != null) {
            Namespace namespace = (Namespace) result;
            auditService.logCreate(EntityType.NAMESPACE, namespace.getId(), namespace.getName(), namespace);
        }
    }

    @Around("execution(* com.togglr.feature.service.NamespaceService.delete(..))")
    public Object auditNamespaceDelete(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Long namespaceId = (Long) args[0];

        Namespace oldNamespace = namespaceRepository.findById(namespaceId).orElse(null);
        Object result = joinPoint.proceed();

        if (oldNamespace != null) {
            auditService.logDelete(EntityType.NAMESPACE, oldNamespace.getId(), oldNamespace.getName(), oldNamespace);
        }

        return result;
    }

    // Environment Audit
    @AfterReturning(value = "execution(* com.togglr.feature.service.EnvironmentService.create(..))", returning = "result")
    public void auditEnvironmentCreate(JoinPoint joinPoint, Object result) {
        if (result != null) {
            Environment environment = (Environment) result;
            auditService.logCreate(EntityType.ENVIRONMENT, environment.getId(), environment.getName(), environment);
        }
    }

    @Around("execution(* com.togglr.feature.service.EnvironmentService.delete(..))")
    public Object auditEnvironmentDelete(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Long environmentId = (Long) args[0];

        Environment oldEnvironment = environmentRepository.findById(environmentId).orElse(null);
        Object result = joinPoint.proceed();

        if (oldEnvironment != null) {
            auditService.logDelete(EntityType.ENVIRONMENT, oldEnvironment.getId(), oldEnvironment.getName(), oldEnvironment);
        }

        return result;
    }
}