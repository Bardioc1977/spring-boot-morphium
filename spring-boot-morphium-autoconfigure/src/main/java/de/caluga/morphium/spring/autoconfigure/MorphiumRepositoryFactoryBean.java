package de.caluga.morphium.spring.autoconfigure;

import de.caluga.morphium.Morphium;
import de.caluga.morphium.annotations.Id;
import de.caluga.morphium.data.RepositoryMetadata;
import jakarta.data.repository.CrudRepository;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

/**
 * Spring {@link FactoryBean} that creates a JDK dynamic proxy implementing
 * a Morphium repository interface. One instance per {@code @Repository} interface.
 *
 * @param <T> the repository interface type
 */
public class MorphiumRepositoryFactoryBean<T> implements FactoryBean<T> {

    private final Class<T> repositoryInterface;

    @Autowired
    private Morphium morphium;

    public MorphiumRepositoryFactoryBean(Class<T> repositoryInterface) {
        this.repositoryInterface = repositoryInterface;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        var typeArgs = resolveTypeArguments(repositoryInterface);
        Class<?> entityClass = typeArgs[0];
        Class<?> idClass = typeArgs[1];
        String idFieldName = findIdFieldName(entityClass);

        var metadata = new RepositoryMetadata(entityClass, idClass, idFieldName);
        var handler = new MorphiumRepositoryInvocationHandler(morphium, metadata, repositoryInterface);

        return (T) Proxy.newProxyInstance(
                repositoryInterface.getClassLoader(),
                new Class<?>[]{ repositoryInterface },
                handler);
    }

    @Override
    public Class<?> getObjectType() {
        return repositoryInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * Walks the interface hierarchy to find CrudRepository&lt;T, K&gt; type arguments.
     */
    static Class<?>[] resolveTypeArguments(Class<?> repoInterface) {
        for (Type iface : repoInterface.getGenericInterfaces()) {
            if (iface instanceof ParameterizedType pt) {
                Type raw = pt.getRawType();
                if (raw instanceof Class<?> rawClass && CrudRepository.class.isAssignableFrom(rawClass)) {
                    Type[] args = pt.getActualTypeArguments();
                    if (args.length >= 2 && args[0] instanceof Class<?> entity && args[1] instanceof Class<?> id) {
                        return new Class<?>[]{ entity, id };
                    }
                }
            }
        }
        // Recurse into super-interfaces
        for (Class<?> superIface : repoInterface.getInterfaces()) {
            try {
                return resolveTypeArguments(superIface);
            } catch (IllegalArgumentException ignored) {
            }
        }
        throw new IllegalArgumentException(
                "Cannot resolve entity/id types from " + repoInterface.getName());
    }

    /**
     * Finds the field annotated with {@code @Id} in the entity class hierarchy.
     */
    private static String findIdFieldName(Class<?> entityClass) {
        Class<?> cls = entityClass;
        while (cls != null && cls != Object.class) {
            for (Field f : cls.getDeclaredFields()) {
                if (f.isAnnotationPresent(Id.class)) {
                    return f.getName();
                }
            }
            cls = cls.getSuperclass();
        }
        // Fallback: look for a field named "id" or "morphiumId"
        try {
            entityClass.getDeclaredField("id");
            return "id";
        } catch (NoSuchFieldException ignored) {
        }
        try {
            entityClass.getDeclaredField("morphiumId");
            return "morphiumId";
        } catch (NoSuchFieldException ignored) {
        }
        throw new IllegalArgumentException(
                "No @Id field found in " + entityClass.getName());
    }
}
