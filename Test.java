import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;


public class Test {
	static XPath xpath=XPathFactory.newInstance().newXPath();
	static DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
	//用于去重，防止重复上传
	static Set<String> repSet=new HashSet<String>();
	static String uploadJarWPomStr="mvn deploy:deploy-file -DgroupId=%s -DartifactId=%s -Dversion=%s -Dpackageing=jar -DpomFile=%s -Dfile=%s -Durl=http://192.168.0.125:8081/nexus/content/repositories/%s -DrepositoryId=%s -s C:\\Users\\xxxx\\.m2\\settings.xml\r\n";
	static String uploadPomStr=    "mvn deploy:deploy-file -DgroupId=%s -DartifactId=%s -Dversion=%s -Dpackageing=pom -Dfile=%s -Durl=http://192.168.0.125:8081/nexus/content/repositories/%s -DrepositoryId=%s -s C:\\Users\\xxxx\\.m2\\settings.xml\r\n";
	static FileWriter fw;
	
	public static void main(String[] args) throws IOException {
		fw=new FileWriter("D:\\Users\\xxxx\\Desktop\\test.txt");
		String path="C:\\Users\\xxxx\\Desktop\\repository";
		
		listFiles(path);
		fw.flush();
		fw.close();
	}

	static void listFiles(String path){
		File f=new File(path);
		if(f.isDirectory()){
			String[] list = f.list();
			for(int i=0;i<list.length;i++){
				String fn=list[i];
				File subf=new File(path+"\\"+fn);
				String subP=subf.getAbsolutePath();
				if(subf.isDirectory()){
					//System.out.println(subP);
					listFiles(subP);
				}else{
					String fname=subf.getName();
					if(fname.lastIndexOf(".")>0){
						String fileExt=subf.getName().substring(subf.getName().lastIndexOf("."));
						if(".pom".equalsIgnoreCase(fileExt)){
							String pPath=subf.getParentFile().getAbsolutePath();
							if(!repSet.contains(pPath)){
								repSet.add(pPath);
								DocumentBuilder newDocumentBuilder;
								try {
									newDocumentBuilder = dbf.newDocumentBuilder();
									Document doc = newDocumentBuilder.parse(new File(subP));
									
									String artifactId = (String)xpath.evaluate("/project/artifactId", doc, XPathConstants.STRING);
									String pGroupId=(String)xpath.evaluate("/project/groupId", doc, XPathConstants.STRING);
									String version=(String)xpath.evaluate("/project/version", doc, XPathConstants.STRING);
									
									if("".equals(pGroupId)||null==pGroupId){
										pGroupId=(String)xpath.evaluate("/project/parent/groupId", doc, XPathConstants.STRING);
									}
									if("".equals(version)||null==version){
										version=(String)xpath.evaluate("/project/parent/version", doc, XPathConstants.STRING);
									}
									String pkg=(String)xpath.evaluate("/project/packaging", doc, XPathConstants.STRING);
									if(!"war".equalsIgnoreCase(pkg)){
										String fPrex=artifactId+"-"+version;
										String jarFName=pPath+"\\"+fPrex+".jar";
										String pomFName=pPath+"\\"+fPrex+".pom";
										
										//System.out.println(jarFName);
										File ft=new File(jarFName);
										String str="";
										//jar的snapshot
										if(ft.exists()&&version.indexOf("SNAPSHOT")>0){
											str=String.format(uploadJarWPomStr, pGroupId,artifactId,version,pomFName,jarFName,"XXX_SNAPSHOT","XXX_SNAPSHOT");
										}
										//pom的snapshot
										else if(!ft.exists()&&version.indexOf("SNAPSHOT")>0){
											str=String.format(uploadPomStr, pGroupId,artifactId,version,pomFName,"XXX_SNAPSHOT","XXX_SNAPSHOT");
										}
										//jar的release
										else if(ft.exists()&&!(version.indexOf("SNAPSHOT")>0)){
											str=String.format(uploadJarWPomStr, pGroupId,artifactId,version,pomFName,jarFName,"XXX_RELEASE","XXX_RELEASE");
										}
										//pom的release
										else if(!ft.exists()&&!(version.indexOf("SNAPSHOT")>0)){
											str=String.format(uploadPomStr, pGroupId,artifactId,version,pomFName,"XXX_RELEASE","XXX_RELEASE");
										}
										fw.append(str);
										System.out.println(str);
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
						}
					}
				}
			}
		}
	}
	
	
}