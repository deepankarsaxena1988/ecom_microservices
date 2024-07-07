package com.ecom.productListUpload.controller;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.stream.FileImageInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.productListUpload.services.EcomProductListUploadService;
import com.ecom.productListUploadAVRO.dto.EcomProductListDTOAVRO;

@RestController
public class ProductListUploadController {
	public final static int BUF_SIZE = 1024;

	public final static String TEMP_FILE_LOC = "C:\\Work\\ecom\\temp";
	
	@Autowired
	public EcomProductListUploadService service;

	@CrossOrigin(origins = "*")
	@PostMapping(path = "/uploadProductList", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity loadProductList(@RequestPart("file") MultipartFile file) {
		System.out.println("test");
		System.out.println(file);

		try {
			
			File filetemp = new File(TEMP_FILE_LOC + "\\" + file.getOriginalFilename());// byteArrayOutputStream);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filetemp));
			bufferedOutputStream.write(file.getBytes());

			bufferedOutputStream.close(); 
			

			extractProductListZipAndScheduleForInsertInDB(TEMP_FILE_LOC + "\\" + file.getOriginalFilename());
			
			EcomProductListDTOAVRO ecomProductListTO=new EcomProductListDTOAVRO();
			ecomProductListTO.setZipFileName(file.getOriginalFilename());
			ecomProductListTO.setUploaded("T");
			sendUploadMessageToTopic( ecomProductListTO);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HttpHeaders responseHeaders = new HttpHeaders();
		// responseHeaders.set("Access-Control-Allow-Origin","*");
		responseHeaders.set("Access-Control-Allow-Credentials", "true");
		responseHeaders.set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		responseHeaders.set("Access-Control-Max-Age", "3600");
		responseHeaders.set("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me");

		return new ResponseEntity(responseHeaders, HttpStatus.CREATED);
	}

	
	private void extractProductListZipAndScheduleForInsertInDB(String zipPath) {
		
		try {
			FileInputStream tempFile=new FileInputStream(zipPath);
			ZipInputStream tempZipFile=new ZipInputStream(tempFile);
			
			ZipEntry zipEntry=tempZipFile.getNextEntry();
			
			byte[] buffer = new byte[1024];
			  
			while(zipEntry!=null) {
				File outputFile=new File(TEMP_FILE_LOC+"\\"+"output\\"+zipEntry.getName());
				
				new File(outputFile.getParent()).mkdirs();
				
                FileOutputStream fos = new FileOutputStream(outputFile);
                System.out.println("Unzipping to "+outputFile.getAbsolutePath());
                
                int len;
                while ((len = tempZipFile.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                tempZipFile.closeEntry();
                zipEntry = tempZipFile.getNextEntry();
			}
			
			tempFile.close();
			tempZipFile.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void sendUploadMessageToTopic(EcomProductListDTOAVRO ecomProductList) {
		service.sendMessageToTopic(ecomProductList);
		
	}
}
