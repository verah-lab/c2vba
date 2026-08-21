package de.heuboe.tls.receiver.interfaces;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface TransformationReader {

    public TransformationRulesContainer createTransformationRules( File file, SystemMessageManagement smm ) throws IOException;
    
    public TransformationRulesContainer createTransformationRules( InputStream file, SystemMessageManagement smm ) throws IOException;
}
