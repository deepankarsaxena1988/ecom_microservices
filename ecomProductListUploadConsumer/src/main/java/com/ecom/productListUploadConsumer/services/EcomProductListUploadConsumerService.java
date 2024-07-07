package com.ecom.productListUploadConsumer.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.ecom.productListUploadAVRO.dto.EcomProductListDTOAVRO;
import com.ecom.productListUploadConsumer.entity.Product;
import com.ecom.productListUploadConsumer.repository.ProductRepository;

import jakarta.transaction.Transactional;

@EnableKafka
@Service
public class EcomProductListUploadConsumerService {

	private static String UPLOAD_TRUE_FLAG = "T";
	public final static String TEMP_FILE_LOC = "C:\\Work\\ecom\\temp\\output";

	@Autowired
	private ProductRepository productRepository;
	@Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
	private  String BATCH_SIZE;

	{
		System.out.println("batch size=" + BATCH_SIZE);
	}

	@KafkaListener(topics = "${topic.name}")
	public void processProductListUpload(ConsumerRecord<String, EcomProductListDTOAVRO> productListUploadRecord) {

		String avrokey = productListUploadRecord.key();
		EcomProductListDTOAVRO ecomProdListUploadAVROObj = productListUploadRecord.value();

		System.out.println(ecomProdListUploadAVROObj);

		if (ecomProdListUploadAVROObj != null
				&& StringUtils.isNotBlank((ecomProdListUploadAVROObj.getUploaded()).toString())
				&& ecomProdListUploadAVROObj.getUploaded().toString().equalsIgnoreCase(UPLOAD_TRUE_FLAG)) {
			uploadProductListInDB(ecomProdListUploadAVROObj);
		}

	}

	private void uploadProductListInDB(EcomProductListDTOAVRO ecomProdListUploadAVROObj) {
		List<Product> readUploadedProductList = readUploadedProductList(
				ecomProdListUploadAVROObj.getZipFileName().toString());
		if (readUploadedProductList != null && !readUploadedProductList.isEmpty()) {
			insertProductsDetailInDB(readUploadedProductList);
		}
	}

	private List<Product> readUploadedProductList(String productListExcel) {
		FileInputStream fis;
		String uploadedFileName = productListExcel.substring(0, productListExcel.indexOf("."));
		List<Product> uploadedProdList = new ArrayList<Product>();
		try {
			fis = new FileInputStream(
					new File(TEMP_FILE_LOC + "\\" + uploadedFileName + "\\" + "ProductListFile.xlsx"));
			XSSFWorkbook wb = new XSSFWorkbook(fis);
			XSSFSheet ws = wb.getSheetAt(0);
			int count=0;
			for (Row row : ws) // iteration over row using for each loop
			{
				if(count>0) {
					uploadedProdList.add(getProductFromExcelRow(row));
				}
				count++;
			}
			System.out.println();
		} catch (FileNotFoundException e) { // TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) { // TODO Auto-generated catch block
			e.printStackTrace();
		}
		return uploadedProdList;
	}

	/**
	 * This method will parse an excel sheet row and create a product object
	 * corresponding to it and return that product object
	 * 
	 * @param row
	 * @return Product
	 */

	private Product getProductFromExcelRow(Row row) {
		Product prod = null;
		if (row != null) {
			if (org.apache.poi.util.StringUtil.isNotBlank(row.getCell(0).getStringCellValue())) {
				prod = new Product();
				prod.setProductCode(row.getCell(0).getStringCellValue());
				prod.setProductName(row.getCell(1).getStringCellValue());
				prod.setProductImage(row.getCell(2).getStringCellValue());
				prod.setProductDescHeading(row.getCell(3).getStringCellValue());
				prod.setProductDescDetail(row.getCell(4).getStringCellValue());

			}
		}
		return prod;
	}

	/**
	 * This method will insert Product details into database
	 * 
	 * @param readUploadedProductList
	 */
	@Transactional
	private void insertProductsDetailInDB(List<Product> readUploadedProductList) {
		System.out.println("batch size=" + BATCH_SIZE);
		if (readUploadedProductList != null && !readUploadedProductList.isEmpty()) {
			readUploadedProductList = readUploadedProductList.stream().filter(product -> Objects.nonNull(product))
					.filter(product -> Objects.nonNull(product.getProductCode())).collect(Collectors.toList());
		}

		int totalObjects = readUploadedProductList.size();
		int batchSizeVar=Integer.parseInt(BATCH_SIZE);
		System.out.println("total objects=" + totalObjects);
		for (int i = 0; i < totalObjects; i = i + batchSizeVar) {
			if ((i + batchSizeVar) > totalObjects) {
				List<Product> readUploadedProductSubList = readUploadedProductList.subList(i, totalObjects);
				System.out.println(readUploadedProductSubList);
				productRepository.saveAll(readUploadedProductSubList);
				break;
			}
			List<Product> readUploadedProductSubList = readUploadedProductList.subList(i, i + batchSizeVar);
			productRepository.saveAll(readUploadedProductSubList);
		}
	}

}
