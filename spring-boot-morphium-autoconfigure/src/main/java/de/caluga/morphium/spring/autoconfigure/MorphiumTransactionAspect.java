package de.caluga.morphium.spring.autoconfigure;

import de.caluga.morphium.Morphium;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

@Aspect
@Component
@ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
@ConditionalOnBean(Morphium.class)
public class MorphiumTransactionAspect {

    private final Morphium morphium;

    public MorphiumTransactionAspect(Morphium morphium) {
        this.morphium = morphium;
    }

    @Around("@annotation(de.caluga.morphium.spring.autoconfigure.MorphiumTransactional) || " +
            "@within(de.caluga.morphium.spring.autoconfigure.MorphiumTransactional)")
    public Object aroundTransactional(ProceedingJoinPoint pjp) throws Throwable {
        morphium.startTransaction();
        try {
            Object result = pjp.proceed();
            morphium.commitTransaction();
            return result;
        } catch (Throwable t) {
            morphium.abortTransaction();
            throw t;
        }
    }
}
