package de.heuboe.tls.receiver.impl;

import java.io.File;
import java.io.IOException;

import de.heuboe.tls.receiver.core.TransformationRules;
import de.heuboe.tls.receiver.interfaces.TransformationReader;
import de.heuboe.tls.receiver.parser.Parser;

/**
 * This transformation reader parses an input file and creates the transformation rules using an antlr4 parser.
 * 
 * @author ralfz
 *
 */
public class TransformationReaderImpl implements TransformationReader {

	@Override
	public TransformationRules createTransformationRules(File file) throws IOException {
		Parser parser = new Parser();
		TransformationRules transformationRules = parser.parse(file);
		return transformationRules;
	}

}
