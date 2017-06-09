package br.com.iris_bot.haarcascade;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JOptionPane;

import java.lang.reflect.Method;

public class PlatformDetection {
	
	public static void detectPlatform(){
		
		//System.getProperties().list(System.out);
		
		String osName = System.getProperty("os.name");
		String osArch = System.getProperty("os.arch");
		
		System.out.println("Detected platform: "+osName+" - "+osArch);
		
		if(osName.toLowerCase().contains("windows")){
			if(osArch.toLowerCase().contains("amd64")) loadJar("windows-x86_64");
			else if(osArch.toLowerCase().contains("x86")) loadJar("windows-x86");
			else notSupported();
		}else if(osName.toLowerCase().contains("linux")){
			if(osArch.toLowerCase().contains("amd64")) loadJar("linux-x86_64");
			else if(osArch.toLowerCase().contains("i386")) loadJar("linux-x86");
			else if(osArch.toLowerCase().contains("ppc")) loadJar("linux-ppc64le");
			else if(osArch.toLowerCase().contains("arm")) loadJar("linux-armhf");
			else notSupported();
		}else if(osName.toLowerCase().contains("android")){
			if(osArch.toLowerCase().contains("arm")) loadJar("android-arm");
			else if(osArch.toLowerCase().contains("x86")) loadJar("android-x86");
			else notSupported();
		}else if(osName.toLowerCase().contains("mac")){
			if(osArch.toLowerCase().contains("amd64")) loadJar("macosx-x86_64");
			else if(osArch.toLowerCase().contains("i386")) loadJar("macosx-x86_64");
			else notSupported();
		}else notSupported();
		
	}
	
	private static void notSupported(){
		System.out.println("Not supported platform");
		try{JOptionPane.showMessageDialog(null, "Not supported platform");}catch (Exception e) {}
		System.exit(0);
	}
	
	private static void loadJar(String plat){
		try {
		    File file = new File("platform/opencv-"+plat+".jar");
		    URL url = file.toURI().toURL();
		    URLClassLoader classLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
		    Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		    method.setAccessible(true);
		    method.invoke(classLoader, url);
		} catch (Exception ex) {
		    notSupported();
		}
	}

}
