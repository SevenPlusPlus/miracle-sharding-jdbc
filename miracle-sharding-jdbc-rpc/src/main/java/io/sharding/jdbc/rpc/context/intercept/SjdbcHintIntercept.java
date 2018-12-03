package io.sharding.jdbc.rpc.context.intercept;

import com.miracle.module.rpc.core.api.RpcContext;
import com.miracle.module.rpc.core.api.wrapper.filter.RpcContextSwapIntercept;
import io.shardingjdbc.core.api.HintManager;
import io.shardingjdbc.core.hint.HintManagerHolder;


public class SjdbcHintIntercept implements RpcContextSwapIntercept {

	private static final String SJDBC_MASTER_ROUTE_HINT_ATTR_NAME = "SJDBC_MASTER_ROUTE_HINT";
	
	@Override
	public String getName() {
		return "sjdbchint";
	}

	@Override
	public void swapInAsConsumer() {
		boolean bRouteMaster = HintManagerHolder.isMasterRouteOnly();
		if(bRouteMaster)
		{
			RpcContext.getContext().setAttachment(SJDBC_MASTER_ROUTE_HINT_ATTR_NAME, String.valueOf(bRouteMaster));
		}
	}

	@Override
	public void swapOutAsProvider() {
		if(RpcContext.getContext().getAttachment(SJDBC_MASTER_ROUTE_HINT_ATTR_NAME) != null)
		{
			boolean bRouteMaster = false;
			try{
				bRouteMaster = Boolean.parseBoolean(RpcContext.getContext().getAttachment(SJDBC_MASTER_ROUTE_HINT_ATTR_NAME));
			}
			catch(Throwable t)
			{}
			
			if(bRouteMaster)
			{
				HintManager.getInstance().setMasterRouteOnly();
			}
		}
	}

	@Override
	public void swapDoneAsConsumer() {
		
	}

	@Override
	public void swapDoneAsProvider() {
		HintManagerHolder.clear();
	}

}
