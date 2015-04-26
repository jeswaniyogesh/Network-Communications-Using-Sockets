import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Stack;

import javax.imageio.ImageIO;


public class NewBrowser {
	

		static int activeTagCount = 0;
		static Stack<String> activeTagStack = new Stack<String>();
		ArrayList<String> printTagList = new ArrayList<String>();
		static String printString = "";
		
		public static void getSourceCode(String website) throws UnknownHostException, IOException{
			int port = 80;
			String[] a = website.split(":");

			if(a.length==3){
				a = a[a.length-1].split("/");
				port = Integer.parseInt(a[0]);
			}
			
			//System.out.println(port);
			
			String[] websiteSplitArray = website.split("/", 4);
			String a1 = ":" + Integer.toString(port);
			
			websiteSplitArray[2] = websiteSplitArray[2].replace(a1, "");
			
			Socket socket = new Socket(websiteSplitArray[2],port); 
			
			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))); 
			out.println("GET /" + websiteSplitArray[3] + " HTTP/1.0"); 
			out.println("Host: " + websiteSplitArray[2]); 
			out.println(); 
			out.flush(); 
			
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
			
			String inputLine=in.readLine();
			String code = "";
			while(inputLine!=null){
				//System.out.println("____________________________________________________________________-"+inputLine);
				inputLine = in.readLine();
				code = code + inputLine;
				//System.out.println(inputLine);
			}
			
			String[] a2 = websiteSplitArray[3].split("/");
			String url1="";
			for(int i=0; i<a2.length-1;i++){
				url1="/" + a2[i];
			}
			
			renderPage(code, websiteSplitArray[2]+url1);
			in.close(); 
			socket.close();
		} 
		
		
		
		public static void renderPage(String code, String host) throws UnknownHostException, IOException{
			String[] splitResult = new String[10];
			String[] splitResult1;
			
			splitResult = code.split("<title>");
			splitResult = splitResult[1].split("</title>");
			System.out.println(splitResult[0].trim());
			
			splitResult = code.split("<body>");
			splitResult[1] = splitResult[1].trim().replaceAll(" +", " ");//replace multiple spaces with 1 space
			//System.out.println(splitResult[1]);
			splitResult = splitResult[1].split("</body>");
			
			splitResult[0] = splitResult[0].replace("<div>", "");
			splitResult[0] = splitResult[0].replace("</div>", "");
			
			splitResult[0] = splitResult[0].replace("<h1>", "\n");
			splitResult[0] = splitResult[0].replace("</h1>", "");
			
			splitResult[0] = splitResult[0].replace("<address>", "\n");
			splitResult[0] = splitResult[0].replace("</address>", "");
			
			splitResult[0] = splitResult[0].replace("<hr>", "\n__________________________________________________________________________________\n");
			splitResult[0] = splitResult[0].replace("</h1>", "");
			
			splitResult[0] = splitResult[0].replace("<p>", "\n");
			splitResult[0] = splitResult[0].replace("</p>", "");
			
			splitResult[0] = splitResult[0].trim();
			printString = splitResult[0];
			//System.out.println(printString);
			
			//render <a> tag
			while(printString.contains("<a ")){
				String href;
				splitResult = splitResult[0].split("<a", 2);
				printString = splitResult[0];
				splitResult = splitResult[1].split("href", 2);
				splitResult = splitResult[1].split("\"",2);
				splitResult = splitResult[1].split("\"",2);
				printString = printString + "(href = " + splitResult[0] + ")";
				splitResult = splitResult[1].split(">",2);
				splitResult = splitResult[1].split("</a>",2);
				printString = printString + splitResult[0] + splitResult[1];
				splitResult[0] = printString;
			}	
			
			
			while(printString.contains("<img ")){
				String src = "";
				String alt = "";
				
				splitResult = printString.split("<img",2);
				printString = splitResult[0];
				splitResult = splitResult[1].split(">",2);
				
				if(splitResult[0].contains("alt")){
					splitResult1 = splitResult[0].split("alt");
					splitResult1 = splitResult1[1].split("=");
					splitResult1 = splitResult1[1].split("\"");
					splitResult1 = splitResult1[1].split("\"");
					alt = splitResult1[0];
					//System.out.println(alt);	
				}
				
				if(splitResult[0].contains("src")){
					splitResult1 = splitResult[0].split("src");
					splitResult1 = splitResult1[1].split("=");
					splitResult1 = splitResult1[1].split("\"");
					splitResult1 = splitResult1[1].split("\"");
					src = splitResult1[0];
					//System.out.println(alt);	
				}
				
				if(!src.contains(".com")){
					src = "http://" + host + "/" + src;
				}
				
				downloadImage(src);
				splitResult1=src.split("/");
				
				printString = printString + "\n(imagePath: " + splitResult1[splitResult1.length-1] + ")" + alt + splitResult[1];
			}
			
			while(printString.contains("<")){	
				splitResult1 = printString.split("<", 2);
				printString = splitResult1[0];
				splitResult = splitResult1[1].split(">",2);
				printString = printString + "\n" + " " + splitResult[1];
			}	
			
			
			System.out.println(printString);
		}
		
		
		
		
		
		
		
		public static void downloadImage(String link) throws UnknownHostException, IOException{
			
			System.out.println(link);
			
			String[] webSplit = link.split("/", 4);
			Socket socket = new Socket(webSplit[2],80); 
			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))); 
			out.println("GET /" + webSplit[3] + " HTTP/1.0"); 
			out.println("Host: " + webSplit[2]); 
			out.println(); 
			out.flush(); 
			DataInputStream in = new DataInputStream(socket.getInputStream());
			InputStream inImage = null;
			inImage = socket.getInputStream();
			
			webSplit = link.split("/");
			BufferedInputStream reader = new BufferedInputStream(socket.getInputStream());
			FileOutputStream foutStream = new FileOutputStream(webSplit[webSplit.length-1]); 
	       InputStream is;

	       int byteCode =0;
	       char ch ;
	           StringBuilder builder = new StringBuilder();

	           while((byteCode=reader.read())!=-1)
	           {
	               builder.append((char)byteCode);
	              
	           }
	       String text = builder.toString();
	       
	       String[] sub = text.split("\r\n\r\n");
	       
	       byte[] byts = sub[1].getBytes();

	       for(int i=0;i<byts.length;i++){
	    	   foutStream.write(byts[i]);
	       }
	       
			
		    InputStream inputStream = null;
			OutputStream outputStream = null;
			URL url = new URL(link);
			inputStream = url.openStream();
			outputStream = new FileOutputStream(webSplit[webSplit.length-1]);
    		byte[] buffer = new byte[2048];
			int length;
            while ((length = inputStream.read(buffer)) != -1) {
            	outputStream.write(buffer, 0, length);
			}
			
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		public static void main(String[] args) throws UnknownHostException, IOException{
			//getSourceCode("http://www.december.com/html/demo/hello.html");
			//getSourceCode("http://www.utdallas.edu/~ozbirn/image.html");
			//getSourceCode("http://htmldog.com/examples/images1.html");
			//getSourceCode("http://portquiz.net:8080/");
			getSourceCode("http://www.utdallas.edu/os.html");
			/*String a = "<a>1</a><a>2</a>";
			String[] b = a.split("<a>", 2);
			System.out.println(b[1]);
			b = b[1].split("<a>", 2);
			System.out.println(b[0]);*/
			//getSourceCode(args[0]);
			
			downloadImage("http://assets.climatecentral.org/images/uploads/news/Earth.jpg");
		}
		
}

