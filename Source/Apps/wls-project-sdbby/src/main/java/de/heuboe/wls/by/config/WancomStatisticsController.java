package de.heuboe.wls.by.config;

import java.util.LinkedList;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import de.heuboe.log.Logger;
import de.heuboe.system.CallStack;
import de.heuboe.wls.by.cfg.SystemConfiguration;

/**
 * 
 * Spring service readiness controller
 * 
 * @author peters
 *
 */
@Controller
public class WancomStatisticsController {
	
	private static final Logger LOGGER = Logger.getLogger( WancomStatisticsController.class );
	
	@Autowired   // NOSONAR
	SystemConfiguration systemConfiguration;	
	
	
    @GetMapping("uz2MqsAqs")
    public synchronized ResponseEntity< String > uz2MqsAqs() {
		try {
			LinkedList<String> lines = new LinkedList<>( systemConfiguration.uz2MqsAqsUfds() );
			
		    return new ResponseEntity<>( lines.stream().collect( Collectors.joining( "<br>" ) ), HttpStatus.OK );
		} catch( Throwable ex ) {      // NOSONAR
			String error = "Error retrieving MQs, AQs and UFDs";
			LOGGER.error( error + ": " + ex.toString() );
			LOGGER.error( CallStack.getStackTraceAsString( ex ) );
			return new ResponseEntity<>( error, HttpStatus.BAD_REQUEST );
		}
   }

    /**
     * 
     * Calculates Wancom statistics

     * @param wancomKeyFile Wancom key file
     * @param fg   			Funktionsgruppe
     * @param format   		Format: table -> HTML, sonst CSV
     * 
     * @return readiness
     */
    @GetMapping("wancomStatistics")
    public synchronized ResponseEntity< String > wancomStatistics( String wancomKeyFile, String fg, String format ) {
    	
    	try {
    	
	    	if( wancomKeyFile == null ) {
	    		return new ResponseEntity<>( "No 'wancomKeyFile' provided", HttpStatus.BAD_REQUEST );
	    	}
	    	
	    	if( fg == null ) {
	    		return new ResponseEntity<>( "No 'fg' provided", HttpStatus.BAD_REQUEST );
	    	}
	    	
	    	LinkedList<String> lines = new LinkedList<>( systemConfiguration.writeWancomStatistics( wancomKeyFile,  fg ) );
	    	
	    	if( format.equals( "table" ) ) {
	    		
		    	String style = " style='border: 1px solid black; padding: 3px;'";
		    	String styleCell = " style='border: 1px solid black; padding: 3px; text-align: right'";
		    	
		        //embrace <td> and <tr> for lines and columns
		        for (int i = 0; i < lines.size(); i++) {
		            lines.set(i, lines.get(i).replace(";", "</td><td" + styleCell + ">"));
		            lines.set(i, "<tr" + style + "><td" + styleCell + ">" + lines.get(i) + "</td></tr>");
		        }
		        
		        // embrace <table> and </table>
		        lines.set(0, "<table style='border-collapse: collapse;'>" + lines.get(0));
		        lines.set(lines.size() - 1, lines.get(lines.size() - 1) + "</table>"); 
		        
	    		lines.addFirst( "<br>  &nbsp;" );
	    		lines.addFirst( "<b>Wancom Tagesstatistik, FG " + fg + ", Datei " + wancomKeyFile +"</b>" );
	    	}
	    	
	        return new ResponseEntity<>( lines.stream().collect( Collectors.joining( "\r\n" ) ), HttpStatus.OK );
    	} catch( Throwable ex ) {      // NOSONAR
    		String error = "Error creating Wancom statistics";
    		LOGGER.error( error + ": " + ex.toString() );
    		LOGGER.error( CallStack.getStackTraceAsString( ex ) );
    		return new ResponseEntity<>( error, HttpStatus.BAD_REQUEST );
    	}

    }
}
