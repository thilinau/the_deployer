package it.bridge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;


import it.bridge.objects.HostAuthentication;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class AgentXMLUpdator {
	 
	public static int VERSION_FORMAT = 3;
	public static String BUND_DIR;
	
	public static void updateSingleModuleEntry( String agentXmlFile, String jarName ) throws Exception
    {
        try
        {
            DOMParser parser = new DOMParser();
            parser.parse( agentXmlFile );

            Document document = parser.getDocument();

            if( jarName != null && jarName.length() > 0 )
            {
                System.out.println( "Updating agent.xml : " + jarName );
                NodeList nodeList = ((org.w3c.dom.Document) document).getElementsByTagName( "jar" );

                int j = -1;
                for( int i = 0; i < nodeList.getLength(); i++ )
                {
                    Element xmlElement = (Element) nodeList.item( i );
                    if( ((org.w3c.dom.Node) xmlElement).getFirstChild().getNodeValue().equals( jarName ) )
                    {
                        j = i;
                        break;
                    }
                }

                if( j < 0 )
                {
                    throw new Exception( "ERROR : Module '" + jarName + "' Not Found in agent.xml" );
                }
                nodeList = ((org.w3c.dom.Document) document).getElementsByTagName( "version" );
                Element verXmlElement = (Element) nodeList.item( j );
                Node verXmlNode = (Node) ((org.w3c.dom.Node) verXmlElement).getFirstChild();

                verXmlNode.setNodeValue( updateVersion( verXmlNode.getNodeValue() ) );
            }

            XMLSerializer serializer = new XMLSerializer();
            FileWriter fileWriter = new FileWriter( agentXmlFile );
            serializer.setOutputCharStream( fileWriter );
            serializer.serialize( (org.w3c.dom.Element) document );
            fileWriter.flush();
            fileWriter.close();
        }
        catch( Exception ex )
        {
            throw new Exception( ex );
        }
    }
	
	
    public static String updateVersion(String currentVersion)
    {
        StringTokenizer stringTokenizer = new StringTokenizer(currentVersion, ".");

        VERSION_FORMAT = stringTokenizer.countTokens();
        StringBuilder newVersion = new StringBuilder();
        String[] version = new String[VERSION_FORMAT];

        for (int i = 0; i < VERSION_FORMAT; i++)
        {
            version[i] = (String) stringTokenizer.nextElement();
        }
        int newVer = Integer.parseInt(version[VERSION_FORMAT - 1]);
        newVer++;
        version[VERSION_FORMAT - 1] = String.valueOf(newVer);

        for (int i = 0; i < VERSION_FORMAT; i++)
        {
            newVersion.append(version[i]);
            if (i < VERSION_FORMAT - 1)
            {
                newVersion.append(".");
            }
        }
        return newVersion.toString();
    }
    
    
    public static boolean updateXML( String xmlfile, List<String> jarNames ) throws Exception
    {
    	boolean RET_STATUS=true;
        DOMParser parser = new DOMParser();
        parser.parse(xmlfile);

        Document document = parser.getDocument();

        for (String jarName : jarNames)
        {

        	NodeList nodeList = document.getElementsByTagName("jar");

            int j = -1;
            for (int i = 0; i < nodeList.getLength(); i++)
            {
                Element xmlElement = (Element) nodeList.item(i);

              	if (xmlElement.getFirstChild().getNodeValue().equals(jarName))
                {
                    j = i;
                    break;
                }
            }

            if ( j < 0 )
            {
            	loggers.addLogAndPrint("SEVE","DEP : AGENT.XML - Module '" + jarName + "' Not Found",false );
            	RET_STATUS=false;
            	
            }else{
	            nodeList = document.getElementsByTagName("version");
	            Element verXmlElement = (Element) nodeList.item(j);
	            Node verXmlNode = verXmlElement.getFirstChild();
	
	            verXmlNode.setNodeValue( updateVersion(verXmlNode.getNodeValue()));
            }
        }

        XMLSerializer serializer = new XMLSerializer();
        FileWriter fileWriter = new FileWriter(xmlfile);
        serializer.setOutputCharStream(fileWriter);
        serializer.serialize(document);
        fileWriter.flush();
        fileWriter.close();
        
        return RET_STATUS;
    }


    
    private boolean uploadAgentXML(String CATEGORY){
    	
    	boolean RET_STATUS=true;
    	boolean uploadStat=true;
    	
    	try {
    		
    		String MULTI_HOST=GetInfo.getConfig("TBX_AGENT_XML_MULTIPLE_HOSTNAMES");
    		
    		if ( MULTI_HOST.equals("-1") ){
    			
				//Get agent.xml download server authentication details    			
    			HostAuthentication hA_agentxml= GetInfo.getHostAuthentication("", "agentxml", 0);
    				
	    		uploadStat= GetInfo.deleteFile(GetInfo.getPath("TBX_AGENT_XML_PATH",CATEGORY) + File.separator + "agent.xml",hA_agentxml);

	    		if ( uploadStat == true ){
	    			uploadStat= GetInfo.uploadFile("agent.xml", BUND_DIR + File.separator + CATEGORY ,GetInfo.getPath("TBX_AGENT_XML_PATH",CATEGORY) ,hA_agentxml);
	    		}

	    		if ( uploadStat == false ){
	    			RET_STATUS=false;
	    		}
    		}
    		else{
				
    			List<String> MULTI_HOST_LIST = new ArrayList<String>(Arrays.asList(MULTI_HOST.split(",")) );
    			for ( int i=0; i<MULTI_HOST_LIST.size();i++){
    				
    				//Get agent.xml download server authentication details    			
        			HostAuthentication hA_agentxml= GetInfo.getHostAuthentication("TBX_AGENT_XML_MULTIPLE", "deploy", i);
        			       			   				
    	    		uploadStat= GetInfo.deleteFile( GetInfo.getPath("TBX_AGENT_XML_MULTIPLE_PATH_" + (i +1),CATEGORY) + File.separator + "agent.xml" ,hA_agentxml );
    	    		
    	    		if ( uploadStat == true ){
   
    	    			uploadStat= GetInfo.uploadFile("agent.xml",BUND_DIR + File.separator + CATEGORY, GetInfo.getPath("TBX_AGENT_XML_MULTIPLE_PATH_" + (i + 1),CATEGORY),hA_agentxml);
    	    		}
    	    		
    	    		if ( uploadStat == false ){
    	    			RET_STATUS=false;
    	    		}
    			}
    		}
    			
    	}catch(Exception e){
    		
    		RET_STATUS=false;
    		loggers.addLogEx("SEVE",this.getClass().toString() +  " : uploadAgentXML : " + e.getMessage(),e);
    	}
    	
    	return RET_STATUS;
    	
    }
    
    private boolean downloadAgentXML(String CATEGORY) throws FileNotFoundException, IOException{
    	    	
    	boolean RET_STATUS=true;
    	
		//Get agent.xml backup server authentication details    			
		HostAuthentication hA_agentxml= GetInfo.getHostAuthentication("", "agentxml", 0);
	
    	try {
    		
    		boolean downStat = GetInfo.downloadFiles("agent.xml",GetInfo.getPath("TBX_AGENT_XML_PATH",CATEGORY),BUND_DIR + File.separator + CATEGORY,hA_agentxml);
     		
    		if ( downStat == false ){
    			RET_STATUS=false;
    		}
    		
    	}catch(Exception e){
    		e.printStackTrace();
    		RET_STATUS=false;
    		loggers.addLogEx("SEVE",e.getMessage(),e);
    	}
    	
    	return RET_STATUS;
    	
    }
    
    public boolean updateAgentXML (String BUNDLE_DIR, String CATEGORY ){
    	
    	boolean RET_STATUS=true;
    	BUND_DIR = BUNDLE_DIR;
    	
    	String xmlfile=BUND_DIR + File.separator + CATEGORY + File.separator + "agent.xml";
    	ArrayList<String> CLT_LIST=GetInfo.getComponentList(CATEGORY, BUND_DIR, true);
    	
    	try{
    		
    		loggers.addLogAndPrint("INFO","DEP : AGENT.XML - Downloading ...",false );
    		
    		boolean downStat= downloadAgentXML(CATEGORY);
	    	
    		if (downStat ==true){
    			
    			loggers.addLogAndPrint("INFO","DEP : AGENT.XML - Updating Inprogress ...",false );
    			downStat = updateXML(xmlfile, CLT_LIST);
    			
    		}else{
    			RET_STATUS=false;
    			loggers.addLogAndPrint("INFO","DEP : AGENT.XML - Download Failed ...",false );
    		}
    		
    		if (RET_STATUS==true){
    			
    			downStat = uploadAgentXML(CATEGORY);
    			
    			if (downStat == true){
    				loggers.addLogAndPrint("INFO","DEP : AGENT.XML - Upload completed",false );
    			}else{
    				loggers.addLogAndPrint("INFO","DEP : AGENT.XML - Upload | FAILED",false );
    			}    			
    		}
    		
    		if (downStat ==false){
    			RET_STATUS=false;	
    		}
    		
    	}
    	catch(Exception e)
    	{
    		RET_STATUS=false;
			loggers.addLogEx("SEVE",e.getMessage(),e);
    	}
    	
    	return RET_STATUS;
    }
}
