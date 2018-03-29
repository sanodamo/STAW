package org.staw.framework;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.staw.datarepository.dao.TestContext.TestContextProvider;
import org.staw.framework.constants.BrowserTargetType;
import org.staw.framework.constants.GlobalConstants;
import org.staw.framework.helpers.TestSetupHelper;
import org.staw.framework.models.KeywordRegistry;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class KeywordDispatcher {

	private static SoftAssertion myAssert;
	private static String sessionId;

	public KeywordDispatcher(SoftAssertion sr) {
		myAssert = sr;
	}

	public static Logger log = Logger.getLogger(KeywordDispatcher.class.getName());
	
	private static String getExecutionEnv(){
		try {
			return TestSetupHelper.getRunEnvironemnt();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static boolean keywordResult(ArrayList<String> argList, Class<?> className, String methodName) {
		int argListSize = 0;
		if (argList != null)
			argListSize = argList.size();
		boolean keywordResult = false;
		Class<?>[] paramType;
		Object[] methodArguments;
		int methodParameters = 0;
		boolean createConstWithNoArg = false;
		Constructor<?> constructor = null;
		Class<?>[] argTypes = { SoftAssertion.class };
		try {
			constructor = className.getDeclaredConstructor(argTypes);
		} catch (NoSuchMethodException | SecurityException e) {
			log.info("Unable to create Constructor with SoftAssertion. Trying Constructor with no Args for Method: " + methodName);
			try{
				constructor = className.getConstructor();
				createConstWithNoArg = true;
			}catch(NoSuchMethodException | SecurityException e2){
				log.error("Unable to create Constructor with no args.");
				e2.printStackTrace();
			}
		}
		Object[] arguments = { myAssert };
		Method[] methods = className.getDeclaredMethods();
		for (Method meth : methods) {
			if (meth.getName().equalsIgnoreCase(methodName.replace("_", ""))) {
				methodParameters = meth.getParameters().length;
				methodArguments = new Object[methodParameters + 1];
				paramType = new Class<?>[methodParameters];
				if (argListSize != methodParameters) {
					return myAssert.Failed("Expected " + methodParameters + " argument(s) found " + argListSize + " argument(s)");
				}
				try {
					for (int i = 0; i < meth.getParameterTypes().length; i++) {
						paramType[i] = meth.getParameterTypes()[i];
					}
					if(createConstWithNoArg){
						methodArguments[0] = constructor.newInstance();
					}else{
						methodArguments[0] = constructor.newInstance(arguments);
					}
					if (methodParameters != 0) {
						for (int i = 1; i <= argListSize; i++) {
							methodArguments[i] = argList.get(i - 1);
						}
					}
					MethodHandles.Lookup lookup = MethodHandles.publicLookup();
					MethodType methodType = MethodType.methodType(boolean.class, paramType);
					MethodHandle handle = lookup.findVirtual(className, meth.getName(), methodType);
					keywordResult = (boolean) handle.invokeWithArguments(methodArguments);
					
					break;
				} catch (Throwable e) {
					log.error("Error with Keyword: " + meth.getName().toUpperCase() + " Test Case  " );
					keywordResult = false;
					break;
				}
			}
		}

		if (keywordResult) {
			return true;
		} else {
			return false;
		}
	}
	

	private static ArrayList<String> getKeywordDataFromXml(int keywordCount, Node singleKeyword) {
		ArrayList<String> argumentList = new ArrayList<>();
		ArrayList<String> returnList = null;
		NodeList nodeArgs;
		String currKeyWord = singleKeyword.getAttributes().getNamedItem("label").getNodeValue();
		
		nodeArgs = singleKeyword.getChildNodes();
		if(nodeArgs.getLength() != 0){
			NodeList arguments = nodeArgs.item(1).getChildNodes();
			for(int i=0; i<arguments.getLength();i++){
				Node node = arguments.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					argumentList.add(node.getAttributes().getNamedItem("value").getNodeValue());
				}
			}
		}
		if (!argumentList.isEmpty())
			returnList = TestSetupHelper.getParametersValue(argumentList);
		else
			returnList = null;
				
		TestContextProvider.SetValue(GlobalConstants.ContextConstant.CURRENT_KEYWORD_STEP, Integer.toString(keywordCount));
		TestContextProvider.SetValue(GlobalConstants.ContextConstant.CURRENT_KEYWORD, currKeyWord);
		TestContextProvider.SetValue(GlobalConstants.ContextConstant.CURRENT_KEYWORD_PARAMETERS,
				returnList != null && returnList.size() > 0 ? returnList.toString().replace("[", "").replace("]", ""): "");
				
		
		return returnList;
	}
	
	
	public boolean executeKeywords(NodeList keyWords) {
		KeywordRegistry[] loginFunctions = { KeywordRegistry.LOGIN };
	
		String strKeyword = "";
		boolean lastKeywordResult = false, currentKeywordResult = false;
		ArrayList<String> argList = null;
		KeywordRegistry kw;
		 
		int keywordCount =1;
		try {
			for (int i = 0; i < keyWords.getLength(); i++) {
				Node singleKeyword = keyWords.item(i);
				if (singleKeyword.getNodeType() == Node.ELEMENT_NODE) {
					if ((sessionId == null || sessionId.isEmpty()) && getExecutionEnv().contains(BrowserTargetType.REMOTE.getTargetType()))
						break;
					argList = getKeywordDataFromXml(keywordCount, singleKeyword);
					strKeyword = singleKeyword.getAttributes().getNamedItem("label").getNodeValue().toUpperCase();
					kw = KeywordRegistry.getKeyword(strKeyword);
					
					if (kw != null) {
						switch (kw) {
												
						case INITIALIZE:
							currentKeywordResult = true;													
							break;
						
						default:
							try {
								currentKeywordResult = keywordResult(argList, kw.getClassName(), kw.name());
							} catch (Throwable e) {
								log.error("Keyword failed to execute: " + kw.getName() + ". Localized Message: " + e.getLocalizedMessage() + " Message: "
										+ e.getMessage());
								e.printStackTrace();
							}
							break;
						}
					}
					
					myAssert.assertTrue(currentKeywordResult);
					if((lastKeywordResult == false && currentKeywordResult == false) && i>0) break;
					if(currentKeywordResult == false && Arrays.asList(loginFunctions).contains(kw)) break;
					lastKeywordResult = currentKeywordResult;
					myAssert.Success("");
					myAssert.Failed("");
					keywordCount++;
				}
			}
		} catch (Exception e) {
			return myAssert.Failed("Error parsing keyword: " + e.getMessage());
		}
		return currentKeywordResult;
	}
}