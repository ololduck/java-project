package fr.upem.java_advanced.project.args;

import java.nio.file.Path;
import java.util.List;

@FunctionalInterface
public interface Validator {
	public List<ValidationError> validate(Path p);
}
