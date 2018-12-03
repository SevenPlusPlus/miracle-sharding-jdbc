package io.shardingjdbc.aop;

import io.shardingjdbc.aop.anotation.RdbMasterSlave;
import io.shardingjdbc.core.api.HintManager;
import io.shardingjdbc.core.hint.HintManagerHolder;
import io.shardingjdbc.core.hint.HintScope;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;



@Aspect
@Component
public class MasterSlaveAnotationAspect implements Ordered{

	@Pointcut("@annotation(io.shardingjdbc.aop.anotation.RdbMasterSlave)")
    public void rdbMasterSlaveDao() {

    }
	
	private Method getRdbMasterSlaveDaoMethod(ProceedingJoinPoint pjp) {
        Method method = ((MethodSignature) (pjp.getSignature())).getMethod();

        if (method.getAnnotation(RdbMasterSlave.class) == null) {
            try {
                method = pjp.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                return null;
            }
        }
        return method;
    }

    @Around("rdbMasterSlaveDao()")
    public Object interceptRdbMasterSlaveDaoMethod(ProceedingJoinPoint pjp) throws Throwable {
    	Method method = getRdbMasterSlaveDaoMethod(pjp);
        RdbMasterSlave rdbMs = method.getAnnotation(RdbMasterSlave.class);
        boolean forceMaster = rdbMs.toMaster();
        HintManager hintManager = null;
        boolean hasSetMasterRoute = false;
        boolean hasSetHintScoreByMe = false;
        if(!HintScope.isInHintScope())
        {
        	HintScope.setHintScope();
        	hasSetHintScoreByMe = true;
        }
        
        if(forceMaster && !HintManagerHolder.isMasterRouteOnly())
        {
        	hintManager = HintManager.getInstance();
        	hintManager.setMasterRouteOnly();
        	hasSetMasterRoute = true;
        }
        try{
        	 return pjp.proceed();
        }
        finally
        {
        	if(hasSetHintScoreByMe)
        	{
        		HintScope.clear();
        		hasSetHintScoreByMe = false;
        	}
        	if(hintManager != null && hasSetMasterRoute)
        	{
        		HintScope.clear();
        		hintManager.close();
        		MasterSlaveDataSource.resetDMLFlag();
        	}
        }
    }

	    
	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

}
