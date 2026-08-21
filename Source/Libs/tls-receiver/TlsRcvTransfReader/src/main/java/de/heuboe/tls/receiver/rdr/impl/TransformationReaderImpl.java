package de.heuboe.tls.receiver.rdr.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import de.heuboe.tls.receiver.interfaces.SystemMessageManagement;
import de.heuboe.tls.receiver.interfaces.TransformationReader;
import de.heuboe.tls.receiver.interfaces.TransformationRulesContainer;
import de.heuboe.tls.receiver.rdr.getter.TimeGetter;
import de.heuboe.tls.receiver.rdr.parser.Parser;

/**
 * This transformation reader parses an input file and creates the transformation rules using an antlr4 parser.
 * 
 * @author ralfz, ronald
 *
 */
public class TransformationReaderImpl implements TransformationReader {

        @Override
        public TransformationRulesContainer createTransformationRules( File file, SystemMessageManagement smm ) throws IOException {
                Parser parser = new Parser();
                parser.setSystemMessageManagement( smm );
                TimeGetter.setSystemMessageManagement( smm );
                TransformationRulesContainer transformationRules = parser.parse( file ); // NOSONAR: better debugging
                return transformationRules;
        }

        @Override
        public TransformationRulesContainer createTransformationRules( InputStream istrm, SystemMessageManagement smm ) throws IOException {
                Parser parser = new Parser();
                parser.setSystemMessageManagement( smm );
                TimeGetter.setSystemMessageManagement( smm );
                TransformationRulesContainer transformationRules = parser.parse( istrm ); // NOSONAR: better debugging
                return transformationRules;
        }

}
