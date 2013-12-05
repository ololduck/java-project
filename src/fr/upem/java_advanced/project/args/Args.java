package fr.upem.java_advanced.project.args;

import java.nio.file.Path;

public class Args {
	private final Validator validator;
	private Args next;

	public Args(Validator validator, Args next) {
		this.validator = validator;
		this.next = next;
	}
	
	public void apply(Path p) {
		validator.validate(p);
		
		if (next != null) {
			next.apply(p);
		}
	}
	
	public void setNext(Args next) {
		this.next = next;
	}
}
