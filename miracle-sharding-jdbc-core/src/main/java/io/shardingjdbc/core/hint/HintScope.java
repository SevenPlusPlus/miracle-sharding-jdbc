package io.shardingjdbc.core.hint;

public class HintScope {

	 private static final ThreadLocal<Boolean> HINTSCOPE_FLAG = new ThreadLocal<Boolean>() {
	        
	        @Override
	        protected Boolean initialValue() {
	            return false;
	        }
	 };
	 
	 
	 public static boolean isInHintScope()
	 {
		 return HINTSCOPE_FLAG.get();
	 }
	 
	 public static void setHintScope()
	 {
		 HINTSCOPE_FLAG.set(true);
	 }
	 
	 public static void clear()
	 {
		 HINTSCOPE_FLAG.remove();
	 }
}
