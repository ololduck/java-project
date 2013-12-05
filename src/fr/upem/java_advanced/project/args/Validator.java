package fr.upem.java_advanced.project.args;

import java.nio.file.Path;

@FunctionalInterface
public interface Validator {
	public void validate(Path p);
}
