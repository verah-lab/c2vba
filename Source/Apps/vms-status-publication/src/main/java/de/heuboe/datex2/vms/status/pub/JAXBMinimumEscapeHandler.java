package de.heuboe.datex2.vms.status.pub;


import java.io.IOException;
import java.io.Writer;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

/**
 * This is the original implementation of 
 * com.sun.xml.bind.marshaller.MinimumEscapeHandler
 * 
 * by
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 *     
 * 'Performs no character escaping. Usable only when the output encoding
 *  is UTF, but this handler gives the maximum performance.'
 *     
 *  If some special escaping requirements arise, this may be 
 *  used as pattern. It can be applied to de.heuboe.util.JAXB instances
 *  by invoking 
 *  
 *  setMarshalerProperty( "com.sun.xml.bind.marshaller.CharacterEscapeHandler", 
 *                        MinimumEscapeHandler.theInstance ); 
 *     
 *     
 * @author peters
 *
 */
public class JAXBMinimumEscapeHandler implements CharacterEscapeHandler {
    
    private JAXBMinimumEscapeHandler() {}  // no instanciation please
    
    public static final CharacterEscapeHandler theInstance = new JAXBMinimumEscapeHandler(); 

    public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
        // avoid calling the Writerwrite method too much by assuming
        // that the escaping occurs rarely.
        // profiling revealed that this is faster than the naive code.
        int limit = start+length;
        for (int i = start; i < limit; i++) {
            char c = ch[i];
                if(c == '&' || c == '<' || c == '>' || c == '\r' || (c == '\"' && isAttVal) ) {
                if(i!=start)
                    out.write(ch,start,i-start);
                start = i+1;
                switch (ch[i]) {
                    case '&':
                        out.write("&amp;");
                        break;
                    case '<':
                        out.write("&lt;");
                        break;
                    case '>':
                        out.write("&gt;");
                        break;
                    case '\"':
                        out.write("&quot;");
                        break;
                }
            }
        }
        
        if( start!=limit )
            out.write(ch,start,limit-start);
    }

}
