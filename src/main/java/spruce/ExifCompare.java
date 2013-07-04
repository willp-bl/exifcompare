package spruce;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.image.TiffParser;
import org.apache.tika.parser.jpeg.JpegParser;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class uses Apache Tika (1.3) to extract and compare metadata from tiff and jpeg files
 * @author wpalmer
 */
public class ExifCompare {

	private static final DefaultDetector detector = new DefaultDetector();

	private static Map<String, String> extractMetadata(File pFile) {
		Map<String, String> metadata = new ConcurrentHashMap<String, String>();
		try {
			TikaInputStream tis = TikaInputStream.get(pFile);
			MediaType mt = detector.detect(tis, new Metadata());
			String type = mt.getType().toLowerCase();//e.g. "image"
			String subtype = mt.getSubtype().toLowerCase();//e.g. tiff

			if(type.equals("image")) {
				//extract technical metadata
				Metadata md = new Metadata();
				md.set(Metadata.CONTENT_TYPE, mt.getType());
				Parser parser = null;
				if(subtype.equals("tiff")) {
					parser = new TiffParser();
				}
				if(subtype.equals("jpeg")) {
					parser = new JpegParser();
				}
				if(parser==null) return null;
					try {
						FileInputStream fis = new FileInputStream(pFile);
						parser.parse(fis, new DefaultHandler(), md, new ParseContext());
						fis.close();

						for(String key:md.names()) metadata.put(key, md.get(key));

					} catch (SAXException | TikaException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				} 				
			
		} catch(IOException e) {
			System.out.println("ERROR!!!");
			e.printStackTrace();
			return null;
		}
		return metadata;
	}

	private static void compare(String pOne, String pTwo) {
		File fOne = new File(pOne);
		File fTwo = new File(pTwo);
		
		Map<String, String> one = extractMetadata(fOne);
		Map<String, String> two = extractMetadata(fTwo);
		
		
		System.out.println("==============================================");
		System.out.println("Conflicts between "+fOne.getName()+" and "+fTwo.getName()+":");

		//remove any metadata that is the same in tiff and jpeg
		//also print conflits to the screen
		for(String key:one.keySet()) {
			if(two.containsKey(key)) {
				if(one.get(key).equals(two.get(key))) {
					one.remove(key);
					two.remove(key);
				} else {
					System.out.println("Key: "+key);
					System.out.println("     value: "+one.get(key));
					System.out.println("     value: "+two.get(key));
				}
			}
		}

		for(String key:two.keySet()) {
			if(one.containsKey(key)) {
				if(one.get(key).equals(two.get(key))) {
					one.remove(key);
					two.remove(key);
				} else {
					System.out.println("Key: "+key);
					System.out.println("     value: "+one.get(key));
					System.out.println("     value: "+two.get(key));
				}
			}
		}

		//print unique entries in each file
		System.out.println("==============================================");
		System.out.println("Unique metadata entries for "+fOne.getName()+": "+one.keySet().size());
		for(String key:one.keySet()) System.out.println(key+": "+one.get(key));
		System.out.println("==============================================");
		System.out.println("Unique metadata entries for "+fTwo.getName()+": "+two.keySet().size());
		for(String key:two.keySet()) System.out.println(key+": "+two.get(key));
		System.out.println("==============================================");
				
	}

	/**
	 * Test main method to compare input files
	 * @param args
	 */
	public static void main(String[] args) {

		String dir = "C:/Bin/VMSharedFolder/SPRUCE/UoN/";
		
		//assume just two files
		String file = "";
		
		//file = "05-0566m";
		//compare(dir+"Tiffs/"+file+".tif", dir+"jpgs/"+file+".jpg");

		file = "07-3739m";
		//compare(dir+"Tiffs/"+file+".tif", dir+"jpgs/"+file+".jpg");
		compare(file+".tif", file+".jpg");

		//file = "07-4413p";
		//compare(dir+"Tiffs/"+file+".tif", dir+"jpgs/"+file+".jpg");

		//file = "07-4433p";
		//compare(dir+"Tiffs/"+file+".tif", dir+"jpgs/"+file+".jpg");

		//file = "08-5710m";
		//compare(dir+"Tiffs/"+file+".tif", dir+"jpgs/"+file+".jpg");

	}

}
