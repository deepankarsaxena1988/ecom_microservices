package ecom.ecomException.exceptions;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ObjectNotValidException extends RuntimeException{
	
	private   Set<String> errorMessages; 
	
	

}
