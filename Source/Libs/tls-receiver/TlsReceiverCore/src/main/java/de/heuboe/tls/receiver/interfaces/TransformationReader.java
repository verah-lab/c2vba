package de.heuboe.tls.receiver.interfaces;

import java.io.File;
import java.io.IOException;

import de.heuboe.tls.receiver.core.TransformationRules;


public interface TransformationReader {

	public TransformationRules createTransformationRules(File file) throws IOException;
}
