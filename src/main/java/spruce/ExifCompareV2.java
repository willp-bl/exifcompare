package spruce;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingConstants;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.common.IImageMetadata.IImageMetadataItem;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputField;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

/**
 * This class uses Apache Commons-Imaging (1.0-SNAPSHOT) to extract and compare metadata from tiff and jpeg files
 * It also writes a new copy of the input tiff with an added exif field
 * @author wpalmer
 */
public class ExifCompareV2 {

	private static Map<String, String> extractMetadata(File pFile) {
		Map<String, String> metadata = new ConcurrentHashMap<String, String>();

		IImageMetadata md = null;
		try {
			md = Imaging.getMetadata(pFile);
		} catch (ImageReadException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		//split the metadata results in to key-value pairs
		for(IImageMetadataItem s:md.getItems()) {
			String item = s.toString();
			int offset = item.indexOf(": ");
			String key = item.substring(0, offset);
			String value = item.substring(offset+2);
			metadata.put(key, value);
		}

		return metadata;
	}

	private static void compare(String pTIFF, String pJPEG, Map<String, String> pInject) {
		File fTiff = new File(pTIFF);
		File fJpeg = new File(pJPEG);
		
		Map<String, String> tiff = extractMetadata(fTiff);
		Map<String, String> jpeg = extractMetadata(fJpeg);
		
		System.out.println("==============================================");
		System.out.println("Conflicts between "+fTiff.getName()+" and "+fJpeg.getName()+":");

		//remove any metadata that is the same in tiff and jpeg
		//also print conflits to the screen
		for(String key:tiff.keySet()) {
			if(jpeg.containsKey(key)) {
				if(tiff.get(key).equals(jpeg.get(key))) {
					tiff.remove(key);
					jpeg.remove(key);
				} else {
					System.out.println("Key: "+key);
					System.out.println("     value: "+tiff.get(key));
					System.out.println("     value: "+jpeg.get(key));
				}
			}
		}

		//remove any metadata that is the same in the jpeg and tiff
		//also print conflicts
		for(String key:jpeg.keySet()) {
			if(tiff.containsKey(key)) {
				if(tiff.get(key).equals(jpeg.get(key))) {
					tiff.remove(key);
					jpeg.remove(key);
				} else {
					System.out.println("Key: "+key);
					System.out.println("     value: "+tiff.get(key));
					System.out.println("     value: "+jpeg.get(key));
				}
			} else {
				//save fields here that we want to inject into the TIFF
				//we don't reach this code if there is already a field by this name in the TIFF
				switch(key) {
				//case "By-line": 
				case "Object Name": 
				case "Caption/Abstract": {
					pInject.put(key, jpeg.get(key));
					break;
				}
				default: {
					break;
				}
				}
			}
		}

		//print unique entries in each file
		System.out.println("==============================================");
		System.out.println("Unique metadata entries for "+fTiff.getName()+": "+tiff.keySet().size());
		for(String key:tiff.keySet()) System.out.println(key+": "+tiff.get(key));
		System.out.println("==============================================");
		System.out.println("Unique metadata entries for "+fJpeg.getName()+": "+jpeg.keySet().size());
		for(String key:jpeg.keySet()) System.out.println(key+": "+jpeg.get(key));
		System.out.println("==============================================");

		//finally inject some metadata into the tiff
		try {
			injectMetadata2(pTIFF, pInject);
		} catch (ImageReadException | IOException | ImageWriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}

	//NOTE that this writes files that are larger than they should be!!
	private static void injectMetadata2(String pTIFF, Map<String, String> pInject) throws ImageReadException, IOException, ImageWriteException {

		//load the image
		BufferedImage buf = Imaging.getBufferedImage(new File(pTIFF));
		
		Map<String, Object> params = new HashMap<String, Object>();
		
		//get the current metadata from the tiff
		TiffImageMetadata tim = (TiffImageMetadata)Imaging.getMetadata(new File(pTIFF));
		TiffOutputSet tos = tim.getOutputSet();

		//check that there is at least a root and exif directory
		tos.getOrCreateRootDirectory();
		tos.getOrCreateExifDirectory();

		//TODO: check if field currently exists
		
		//TODO: inject fields from pInject
		
		//inject a test field to the tiff
		//make sure this string is null terminated!! i.e. \0
		byte[] byteString = new String("THIS IS A TEST\0").getBytes("UTF-16LE");
		TiffOutputField field = new TiffOutputField(ExifTagConstants.EXIF_TAG_USER_COMMENT, 
													ExifTagConstants.EXIF_TAG_USER_COMMENT.dataTypes.get(0),
													byteString.length, 
													byteString);
		tos.getExifDirectory().add(field);

		//add the metadata to the output parameters
		params.put(ImagingConstants.PARAM_KEY_EXIF, tos);

		//write the tiff
		Imaging.writeImage(buf, new File(pTIFF+".new.tif"), ImageFormat.IMAGE_FORMAT_TIFF, params);
		
	}

	/**
	 * Test main method to compare input files
	 * @param args
	 */
	public static void main(String[] args) {

		String dir = "C:/Bin/VMSharedFolder/SPRUCE/UoN/";
		
		//assume just two files
		String file = "";
		Map<String, String> inject = new HashMap<String, String>();
		
		file = "05-0566m";
		//compare(dir+"Tiffs/"+file+".tif", dir+"jpgs/"+file+".jpg", inject);
		System.out.println("to inject: "+inject.size());
		for(String key:inject.keySet()) System.out.println(key+": "+inject.get(key));

		inject = new HashMap<String, String>();
		file = "07-3739m";
		compare(dir+"Tiffs/"+file+".tif", dir+"jpgs/"+file+".jpg", inject);
		System.out.println("to inject: "+inject.size());
		for(String key:inject.keySet()) System.out.println(key+": "+inject.get(key));
		//compare(file+".tif", file+".jpg");

		inject = new HashMap<String, String>();
		file = "07-4413p";
		//compare(dir+"Tiffs/"+file+".tif", dir+"jpgs/"+file+".jpg", inject);
		System.out.println("to inject: "+inject.size());
		for(String key:inject.keySet()) System.out.println(key+": "+inject.get(key));

		inject = new HashMap<String, String>();
		file = "07-4433p";
		//compare(dir+"Tiffs/"+file+".tif", dir+"jpgs/"+file+".jpg", inject);
		System.out.println("to inject: "+inject.size());
		for(String key:inject.keySet()) System.out.println(key+": "+inject.get(key));

		inject = new HashMap<String, String>();
		file = "08-5710m";
		//compare(dir+"Tiffs/"+file+".tif", dir+"jpgs/"+file+".jpg", inject);
		System.out.println("to inject: "+inject.size());
		for(String key:inject.keySet()) System.out.println(key+": "+inject.get(key));

	}

}
