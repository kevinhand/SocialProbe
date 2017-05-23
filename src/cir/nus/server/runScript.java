package cir.nus.server;

import java.io.IOException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;

public class runScript {
	    int iExitValue;
	    String sCommandString;

	    public void runTheScript(String command){
	        sCommandString = command;
	        CommandLine oCmdLine = CommandLine.parse(sCommandString);
	        DefaultExecutor oDefaultExecutor = new DefaultExecutor();
	        oDefaultExecutor.setExitValue(0);
	        try {
	            iExitValue = oDefaultExecutor.execute(oCmdLine);
	        } catch (ExecuteException e) {
	            // TODO Auto-generated catch block
	            System.err.println("Execution failed.");
	            e.printStackTrace();
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            System.err.println("permission denied.");
	            e.printStackTrace();
	        }
	    }

}


