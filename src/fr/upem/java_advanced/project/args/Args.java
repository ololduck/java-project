package fr.upem.java_advanced.project.args;

import java.nio.file.Path;
import java.util.List;

public class Args {
	private final Validator validator;
	private Args next;

	public Args(Validator validator, Args next) {
		this.validator = validator;
		this.next = next;
	}
	
	public List<ValidationError> apply(Path p) {
		List<ValidationError> errors;
		errors = validator.validate(p);
		
		if (next != null) {
			errors.addAll(next.apply(p));
		}
		return errors;
	}
	
	public void setNext(Args next) {
		this.next = next;
	}
}
