package de.caluga.morphium.spring.autoconfigure;

import de.caluga.morphium.data.MorphiumRepository;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Scans for {@code @Repository} interfaces that extend {@link CrudRepository} or
 * {@link MorphiumRepository} and registers a {@link MorphiumRepositoryFactoryBean}
 * for each.
 */
public class MorphiumRepositoryRegistrar implements ImportBeanDefinitionRegistrar {

    private static final Logger log = LoggerFactory.getLogger(MorphiumRepositoryRegistrar.class);

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {
        Set<String> basePackages = getBasePackages(importingClassMetadata);
        if (basePackages.isEmpty()) {
            return;
        }

        var scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                // Allow interfaces (default implementation rejects them)
                return beanDefinition.getMetadata().isInterface()
                        && beanDefinition.getMetadata().isIndependent();
            }
        };
        scanner.addIncludeFilter(new AnnotationTypeFilter(Repository.class));

        for (String basePackage : basePackages) {
            for (var candidate : scanner.findCandidateComponents(basePackage)) {
                String className = candidate.getBeanClassName();
                if (className == null) continue;

                try {
                    Class<?> iface = ClassUtils.forName(className, getClass().getClassLoader());
                    if (!iface.isInterface()) continue;
                    if (!CrudRepository.class.isAssignableFrom(iface)
                            && !MorphiumRepository.class.isAssignableFrom(iface)) {
                        continue;
                    }

                    String beanName = StringUtils.uncapitalize(iface.getSimpleName());

                    var bd = BeanDefinitionBuilder.genericBeanDefinition(MorphiumRepositoryFactoryBean.class)
                            .addConstructorArgValue(iface)
                            .setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE)
                            .getBeanDefinition();

                    registry.registerBeanDefinition(beanName, bd);
                    log.debug("Registered Morphium repository bean '{}' for {}", beanName, className);
                } catch (ClassNotFoundException e) {
                    log.warn("Could not load repository candidate class: {}", className);
                }
            }
        }
    }

    private Set<String> getBasePackages(AnnotationMetadata metadata) {
        Set<String> packages = new HashSet<>();

        var attrs = AnnotationAttributes.fromMap(
                metadata.getAnnotationAttributes(EnableMorphiumRepositories.class.getName()));

        if (attrs != null) {
            packages.addAll(Arrays.asList(attrs.getStringArray("value")));
            packages.addAll(Arrays.asList(attrs.getStringArray("basePackages")));
        }

        if (packages.isEmpty()) {
            packages.add(ClassUtils.getPackageName(metadata.getClassName()));
        }

        return packages;
    }
}
