package ecom.ecomException.validator;

import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.stereotype.Component;

import ecom.ecomException.exceptions.ObjectNotValidException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
public class ObjectValidator<T>  {

	public final ValidatorFactory validationFactory=Validation.buildDefaultValidatorFactory();
	
	public final Validator validator=validationFactory.getValidator();
	
	public void validate(T objectToValidate) {
		Set<ConstraintViolation<T>> voilations = validator.validate(objectToValidate);
		if(!voilations.isEmpty()) {
			Set<String> errorMessages = voilations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
			throw new ObjectNotValidException(errorMessages);
		}
	}
	
	
}
