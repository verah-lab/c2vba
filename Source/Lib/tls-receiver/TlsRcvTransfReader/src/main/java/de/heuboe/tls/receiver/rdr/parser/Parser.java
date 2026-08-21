package de.heuboe.tls.receiver.rdr.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.Interval;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.heuboe.tls.receiver.antlr4.ReceiverBaseListener;
import de.heuboe.tls.receiver.antlr4.ReceiverLexer;
import de.heuboe.tls.receiver.antlr4.ReceiverParser;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.ArgsContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.ArgumentContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.ArrayGetter2Context;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.ArrayGetterContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.BcdGetterContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.BlockGetterContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.ConditionContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.DeblockdefContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.ExpressionContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.FloatGetterContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.FunctionContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.GetterContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.IfGetterContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.IntGetterContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.LocListContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.LocationContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.LocationListContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.OptEndianContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.OptSignedContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.OptionalGetterContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.SetGetterContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.SingleValueGetterContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.StringContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.StringGetterContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.TargetTypeListContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.TimeGetterContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.TypeContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.TypeListContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.WertebereichContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.ZahlContext;
import de.heuboe.tls.receiver.antlr4.ReceiverParser.ZahlenlisteContext;
import de.heuboe.tls.receiver.interfaces.GetterRule;
import de.heuboe.tls.receiver.interfaces.SystemMessageManagement;
import de.heuboe.tls.receiver.interfaces.TransformationRulesContainer;
import de.heuboe.tls.receiver.rdr.core.Condition;
import de.heuboe.tls.receiver.rdr.core.DeBlockDefinition;
import de.heuboe.tls.receiver.rdr.core.Expression;
import de.heuboe.tls.receiver.rdr.core.FunctionAbstract;
import de.heuboe.tls.receiver.rdr.core.FunctionInval;
import de.heuboe.tls.receiver.rdr.core.TransformationRules;
import de.heuboe.tls.receiver.rdr.getter.ArrayGetter;
import de.heuboe.tls.receiver.rdr.getter.BcdGetter;
import de.heuboe.tls.receiver.rdr.getter.BlockGetter;
import de.heuboe.tls.receiver.rdr.getter.ByteGetter;
import de.heuboe.tls.receiver.rdr.getter.FloatGetter;
import de.heuboe.tls.receiver.rdr.getter.IfGetter;
import de.heuboe.tls.receiver.rdr.getter.LongGetter;
import de.heuboe.tls.receiver.rdr.getter.NodeGetter;
import de.heuboe.tls.receiver.rdr.getter.OptionalGetter;
import de.heuboe.tls.receiver.rdr.getter.SetGetter;
import de.heuboe.tls.receiver.rdr.getter.ShortGetter;
import de.heuboe.tls.receiver.rdr.getter.SkipGetter;
import de.heuboe.tls.receiver.rdr.getter.StringGetter;
import de.heuboe.tls.receiver.rdr.getter.TimeGetter;


@SuppressWarnings("deprecation")
public class Parser {

	private static final Logger LOGGER = LogManager.getLogger(Parser.class);
	private boolean ok = true;
	private Map<String, Set<Integer>> locListMap = null;
	private static final String PARSEEXCEPTION = "Parser Exception: ";

    private SystemMessageManagement smm = null;
    
    public void setSystemMessageManagement( SystemMessageManagement smmParam ) {
        this.smm = smmParam;
    }

        public TransformationRulesContainer parse(InputStream istrm) throws IOException {
                BufferedReader reader = new BufferedReader(new InputStreamReader(istrm));

                return parse(reader);
        }

	public TransformationRulesContainer parse(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));

		return parse(reader);
	}

	private TransformationRulesContainer parse(BufferedReader reader) throws IOException {
                TransformationRulesContainer transformationRules = new TransformationRules();
                locListMap = new HashMap<>();

                ReceiverLexer l = new ReceiverLexer(new ANTLRInputStream(reader));
		ReceiverParser p = new ReceiverParser(new CommonTokenStream(l));

		p.addErrorListener(new BaseErrorListener() {
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
			    String msgOut = "parse error at line " + line + ": " + msg;

                if ( null != smm ) {
                    smm.sendMessage( msg );
                }

                throw new IllegalStateException("parse error at line " + line + ": " + msg, e);
			}
		});

		addDeBlocDefListener( (TransformationRules) transformationRules, p );

                addLocationListListener( p );

                addtargetTypeListListener( p );

		try {
			p.receiver();
		} catch(Exception e) {
		    String msg = PARSEEXCEPTION + e;
		    
            LOGGER.error( msg );

            if ( null != smm ) {
                smm.sendMessage( msg );
            }

			return null;
		}
		return ok ? transformationRules : null;
	}

        /**
         * @param p The parser where the listener is added
         */
        private void addLocationListListener( ReceiverParser p ) {
                p.addParseListener(new ReceiverBaseListener() {

                        @Override
                        public void exitLocationList(ReceiverParser.LocationListContext ctx) {
                                try {
                                        createLocListDefinition(ctx, locListMap);
                                } catch(Exception e) {
                                        String msg = PARSEEXCEPTION + e;
                                        
                                        LOGGER.error( msg );
    
                                        if ( null != smm ) {
                                            smm.sendMessage( msg );
                                        }
                                        ok = false;
                                }
                        }

                });
        }

        /**
         * @param p The parser where the listener is added
         */
        private void addtargetTypeListListener( ReceiverParser p ) {
                p.addParseListener(new ReceiverBaseListener() {

                        @Override
                        public void exitTargetTypeList(ReceiverParser.TargetTypeListContext ctx) {
                                try {
                                        createTargetTypeListDefinition(ctx, locListMap);
                                } catch(Exception e) {
                                        String msg = PARSEEXCEPTION + e;
                                        
                                        LOGGER.error( msg );
    
                                        if ( null != smm ) {
                                            smm.sendMessage( msg );
                                        }
                                        ok = false;
                                }
                        }
                });
        }

        Set<String> typeList = null;

        private void createTargetTypeListDefinition( TargetTypeListContext ctx, Map<String, Set<Integer>> locListMap ) {
                typeList = createTypeList( ctx.typeList() );
        }

        private Set<String> createTypeList( TypeListContext typeList ) {
                Set<String> resList = new HashSet<>();
                for ( TypeContext l : typeList.type()) {
                        String type = l.string().getText();
                        resList.add( type );
                }
                return resList;
        }

        /**
         * @param transformationRules The set of transformation rules of a DeBlock
         * @param p The parser where the listener is added
         */
        private void addDeBlocDefListener( TransformationRules transformationRules, ReceiverParser p ) {
                p.addParseListener(new ReceiverBaseListener() {

			@Override
			public void exitDeblockdef(ReceiverParser.DeblockdefContext ctx) {
				try {
					DeBlockDefinition def = createDeBlockDefinition(ctx);
					String locsName = def.getSpecialLocationsName();
					if (null != locsName) {
					        Set<Integer> locs = locListMap.get( locsName );
					        if (null == locs) {
				                        String inputLocation = inputLocation( ctx );
				                        throw new IllegalArgumentException( "Location list " + locsName + " undefined " + inputLocation );
					        }
					        def.setSpecialLocations( locs );
					}
					locsName = def.getSpecialLocationsExcludedName();
					if (null != locsName) {
					        Set<Integer> locs = locListMap.get( locsName );
					        if (null == locs) {
					                String inputLocation = inputLocation( ctx );
					                throw new IllegalArgumentException( "Excluded location list " + locsName + " undefined " + inputLocation );
					        }
					        def.setSpecialLocations( locs );
					}
					transformationRules.addDefinition(def, ctx);
				} catch(Exception e) {
		            String msg = PARSEEXCEPTION + e;
		            
		            LOGGER.error( msg );

		            if ( null != smm ) {
		                smm.sendMessage( msg );
		            }
					e.printStackTrace();
					ok = false;
				}
			}

		});
        }

	private void breakMe() {
	        // intended to be used to set breakpoints
	}

	private DeBlockDefinition createDeBlockDefinition(DeblockdefContext ctx) {
		DeBlockDefinition def = new DeBlockDefinition();
		def.setName(getText(ctx.string()));
		def.getFg().addAll(getValueList(ctx.zahlenliste(0)));
		def.getId().addAll(getValueList(ctx.zahlenliste(1)));
		def.setTyp(getInteger(ctx.zahl()));
		def.setHeader(ctx.header() != null);
		breakMe();
		if (null != ctx.notAtLocations()) {
		        def.setSpecialLocationsExcludedName( ctx.notAtLocations().STRING().getText() );
		}
		if (null != ctx.atLocations()) {
		        def.setSpecialLocationsName( ctx.atLocations().STRING().getText() );
		}
		for(GetterContext g : ctx.getter()) {
			GetterRule rule = createGetterRule(g);
			if (rule == null) {
	            String msg = "Cannot create Getter Rule";
	            
	            LOGGER.error( msg );

	            if ( null != smm ) {
	                smm.sendMessage( msg );
	            }
			} else {
				def.add(rule);
			}
		}
		def.complete();
		return def;
	}

	private void createLocListDefinition( LocationListContext ctx, Map<String, Set<Integer>> locListMap ) {
	        String listName = ctx.STRING().getText();
	        Set<Integer> locList = createLocList( ctx.locList() );
	        if (null != locListMap.get( listName )) {
	                String inputLocation = inputLocation( ctx );
	                throw new IllegalArgumentException( "Duplicate location list defined " + inputLocation );
	        }
	        locListMap.put( listName, locList );
	}

	private Set<Integer> createLocList( LocListContext locList ) {
	        Set<Integer> resList = new HashSet<>();
	        for ( LocationContext l : locList.location() ) {
	                String inputLocation = inputLocation( l );
	                Integer loc = new Integer( Integer.parseInt( l.INT( 0 ).getText() ) );
	                if (0 >= loc.intValue() || 65535 < loc.intValue() ) {
	                        throw new IllegalArgumentException( "Location (<Location>-<Distance>) out of range " + inputLocation );
	                }
	                Integer dist = new Integer( Integer.parseInt( l.INT( 1 ).getText() ) );
	                if (0 >= dist.intValue() || 255 < dist.intValue() ) {
	                        throw new IllegalArgumentException( "Distance (<Location>-<Distance>) out of range " + inputLocation );
	                }
	                Integer iNode = loc * 256 + dist;
	                resList.add( iNode );
	        }
                return resList;
        }

	private GetterRule createSingleValueGetter( SingleValueGetterContext  ctx, String resultType ) {
	        if ( ctx.intGetter() != null ) {
	                return createIntGetter( ctx.intGetter(), resultType );
	        }
	        if ( ctx.bcdGetter() != null ) {
	                return createBcdGetter( ctx.bcdGetter(), resultType );
	        }
	        if ( ctx.timeGetter() != null ) {
	                return createTimeGetter( ctx.timeGetter(), resultType );
	        }
	        if ( ctx.floatGetter() != null ) {
	                return createFloatGetter( ctx.floatGetter(), resultType );
	        }
	        if ( ctx.setGetter() != null ) {
	                return createSetGetter( ctx.setGetter(), resultType );
	        }
	        return null;
	}

        private GetterRule createGetterRule( GetterContext ctx ) {
                String targetType = "";
                if (ctx.optTargetType() != null) {
                        targetType = ctx.optTargetType().getChild( 2 ).getText();
                        if (null != this.typeList) {
                                if (!this.typeList.contains( targetType )) {
                                        String inputLocation = inputLocation( ctx );
                                        throw ( new IllegalArgumentException( "Undefined type ("+targetType+") used and a type list is defined: " + inputLocation ) );
                                }
                        }
                }
                if ( ctx.singleValueGetter() != null ) {
                        return createSingleValueGetter( ctx.singleValueGetter(), targetType );
                }

                if ( ctx.stringGetter() != null ) {
                        return createStringGetter( ctx.stringGetter(), targetType );
                }
                if ( ctx.blockGetter() != null ) {
                        return createBlockGetter( ctx.blockGetter() , targetType );
                }
                if ( ctx.arrayGetter() != null ) {
                        return createArrayGetter( ctx.arrayGetter(), targetType );
                }
                if ( ctx.arrayGetter2() != null ) {
                        return createArrayGetter2( ctx.arrayGetter2(), targetType );
                }
                if ( ctx.skipGetter() != null ) {
                        return createSkipGetter( /* ctx.skipGetter() */ );
                }
                if ( ctx.ifGetter() != null ) {
                        return createIfGetter( ctx.ifGetter() );
                }
                if ( ctx.optionalGetter() != null ) {
                        return createOptionalGetter( ctx.optionalGetter() );
                }
                return null;
        }

	private GetterRule createIntGetter(IntGetterContext ctx, String resultType) {
                String inputLocation = inputLocation( ctx );
		String type = ctx.intGettertype().getText();
		String name = getText(ctx.string());
		boolean bigEndian = isBigEndian(ctx.optEndian());
		boolean signed = isSigned(ctx.optSigned(), type);
		Expression expr = null;
		FunctionAbstract func = null;
                if (ctx.expression() != null) {
                        expr = createExpression( ctx.expression() );
                }
                if (ctx.function() != null) {
                        func = createFunction( ctx.function() );
                }
		switch(type) {
		case "BYTE":
			return new ByteGetter(name, signed, expr, func, resultType);
		case "SHORT":
			return new ShortGetter(name, signed, bigEndian, expr, func, resultType);
		case "NODE":
			return new NodeGetter(name, expr, func, resultType);
		case "LONG":
			return new LongGetter(name, signed, bigEndian, expr, func, resultType);
		default:
		        throw new IllegalArgumentException( "Type not expected here: " + type + inputLocation );
		}
	}

        private GetterRule createBcdGetter( BcdGetterContext ctx, String targetType ) {
                String name = getText( ctx.string() );
                int size = getInteger( ctx.zahl() );
                Expression expr = null;
                FunctionAbstract func = null;
                if ( ctx.expression() != null ) {
                        expr = createExpression( ctx.expression() );
                }
                if ( ctx.function() != null ) {
                        func = createFunction( ctx.function() );
                }
                return new BcdGetter( name, size, expr, func, targetType );
        }

	private GetterRule createFloatGetter(FloatGetterContext ctx, String targetType) {
	        String name = getText(ctx.string());
                Expression expr = null;
                FunctionAbstract func = null;
                if (ctx.expression() != null) {
                        expr = createExpression( ctx.expression() );
                }
                if (ctx.function() != null) {
                        func = createFunction( ctx.function() );
                }
	        return new FloatGetter(name, expr, func, targetType );
	}

	private GetterRule createTimeGetter(TimeGetterContext ctx, String targetType) {
		String spec = getText(ctx.string(0));
		String name = getText(ctx.string(1));
		return new TimeGetter(spec, name, targetType);
	}

	private GetterRule createStringGetter(StringGetterContext ctx, String targetType) {
		String name = getText(ctx.string());
		String sizeCol = null;
 		int size = 0;
 		boolean toEnd = false;
 		if (ctx.stringFixSize() != null) {
 			size = getInteger(ctx.stringFixSize().fixedSize().zahl());
 		}
 		if (ctx.stringToEnd() != null) {
 			toEnd = true;
 		}
                if (ctx.stringVarSizeWithSizeCol() != null) {
                        sizeCol = getText( ctx.stringVarSizeWithSizeCol().withSize2().string() );
                }
                StringGetter res = new StringGetter(name, size, toEnd, targetType);
                res.setSizeCol( sizeCol );
		return res;
	}

	private GetterRule createBlockGetter(BlockGetterContext ctx, String targetType) {
		String name = null;
		String sizeCol = null;
 		int size = 0;
 		boolean toEnd = false;
 		boolean skip = false;
 		if (ctx.blockFixSize() != null) {
 			size = getInteger(ctx.blockFixSize().fixedSize().zahl());
 		}
 		if (ctx.blockToEnd() != null) {
 			toEnd = true;
 		}
                if (ctx.blockToEndSkip() != null) {
                        toEnd = true;
                        skip = true;
                        name = "-SKIP-";
                } else {
                        name = getText(ctx.string());
                }
                if (ctx.blockWithSizeCol() != null) {
                        sizeCol = getText( ctx.blockWithSizeCol().withSize2().string() );
                }
                BlockGetter res = new BlockGetter(name, size, toEnd, targetType);
                res.setSizeCol( sizeCol );
                if (skip) {
                        res.doSkip();
                }
		return res;
	}

	private GetterRule createArrayGetter(ArrayGetterContext ctx, String targetType) {
		String name = getText(ctx.string());
		ArrayGetter arrayGetter = new ArrayGetter(name, targetType);
		for(GetterContext x : ctx.getter()) {
			GetterRule rule = createGetterRule(x);
			arrayGetter.addRule(rule);
		}
		return arrayGetter;
	}

        private GetterRule createArrayGetter2( ArrayGetter2Context ctx, String targetType ) {
                String name = getText( ctx.string( 1 ) );
                ArrayGetter arrayGetter = new ArrayGetter( name, targetType );
                arrayGetter.setSizeCol( getText( ctx.string( 0 ) ) );
                for ( GetterContext x : ctx.getter() ) {
                        GetterRule rule = createGetterRule( x );
                        arrayGetter.addRule( rule );
                }
                return arrayGetter;
        }

	private GetterRule createSkipGetter(/*SkipGetterContext ctx*/) {
		return new SkipGetter();
	}

	private GetterRule createSetGetter(SetGetterContext ctx, String resultType) {
		String name = getText(ctx.string());
                String inputLocation = inputLocation( ctx );
		if (ctx.function() != null) {
		        FunctionAbstract function = createFunction(ctx.function());
                        return new SetGetter(name, function, resultType);
		}
		if (ctx.expression() != null) {
		        Expression expression = createExpression(ctx.expression());
		        return new SetGetter(name, expression, resultType);
		}
		throw ( new RuntimeException( "Should not get here - createSetGetter. Input " + inputLocation ) );
	}

	/**
	 * generate a text that describes the inputlocation of a receive script
	 * @param ctx Parser context carrying the inputlocation
	 * @return Describing text
	 */
	public static String inputLocation( ParserRuleContext ctx  ) {
                int sline = ctx.start.getLine();
                int sindex = ctx.start.getCharPositionInLine();
                int eline = ctx.stop.getLine();
                int eindex = ctx.stop.getCharPositionInLine();

                CharStream cs = ctx.start.getTokenSource().getInputStream();
                int stopIndex = ctx.stop != null ? ctx.stop.getStopIndex() : -1;
                String inpTxt = cs.getText(new Interval(ctx.start.getStartIndex(), stopIndex));

                String res = "at input between " + sline + ":" + sindex + " and " + eline + ":" + eindex + "-> '" + inpTxt + "'";

                return res;
	}

        private FunctionAbstract createFunction(FunctionContext ctx) {
                String funcName = "unknownFunction";
                if ( null != ctx.functionname() ) {
                        funcName = ctx.functionname().getText();
                }
                ArgsContext args = ctx.args();
                List<Expression> exprList = new ArrayList<>();
                if (null != args) {
                        List<ArgumentContext> al = args.arglist().argument();
                        for ( ArgumentContext ac : al) {
                                ExpressionContext expr2 = ac.expression();
                                exprList.add( createExpression( expr2 ) );
                        }
                }
                if (funcName.equals( "inval" )) {
                        String inputLocation = inputLocation( ctx );
                        if ( exprList.isEmpty() || 4 <= exprList.size() ) {
                                throw ( new IllegalArgumentException( "Inappropriate number of arguments for inval: " + inputLocation ) );
                        }
                        return handleInvalVariants( funcName, inputLocation, exprList );
                }
                return null;
        }

        private FunctionInval handleInvalVariants( String funcName, String inputLocation, List<Expression> exprList ) {
                if (3 == exprList.size()) { // string ',' invalidValues ',' expression
                        return handleThreeArgsInval( funcName, inputLocation, exprList );
                }
                if (2 == exprList.size()) { // invalidValues ',' expression
                        return handleTwoArgsInval( funcName, inputLocation, exprList );
                }
                if (1 == exprList.size()) { // invalidValues
                        return handleOneArgInval( funcName, inputLocation, exprList );
                }
                throw ( new IllegalArgumentException( "Inappropriate number of arguments for inval: " + inputLocation ) );
        }

        /**
         * @param funcName
         * @param inputLocation
         * @param exprList
         * @return
         */
        private FunctionInval handleOneArgInval( String funcName, String inputLocation, List<Expression> exprList ) {
                if ( Expression.ExprType.CONSTLIST != exprList.get( 0 ).getExprType() && Expression.ExprType.CONST != exprList.get( 0 ).getExprType() ) {
                        throw ( new IllegalArgumentException( "Argument needs to a number/numberlist in this form " + inputLocation ) );
                }
                List<Expression> exprList2 = new ArrayList<>();
                exprList2.add( new Expression( "$?" ) );
                exprList2.addAll( exprList );
                return new FunctionInval( funcName, exprList2 );
        }

        /**
         * @param funcName
         * @param inputLocation
         * @param exprList
         * @return
         */
        private FunctionInval handleTwoArgsInval( String funcName, String inputLocation, List<Expression> exprList ) {
                if ( Expression.ExprType.CONST != exprList.get( 0 ).getExprType() && Expression.ExprType.CONSTLIST != exprList.get( 0 ).getExprType() ) {
                        throw ( new IllegalArgumentException( "First argument has to be a number/numberlist in this form " + inputLocation ) );
                }
                if ( Expression.ExprType.CONSTLIST == exprList.get( 1 ).getExprType() ) {
                        throw ( new IllegalArgumentException( "Second argument may not be a numberlist in this form " + inputLocation ) );
                }
                List<Expression> exprList2 = new ArrayList<>();
                exprList2.add( new Expression( "$?" ) );
                exprList2.addAll( exprList );
                return new FunctionInval( funcName, exprList2 );
        }

        /**
         * @param funcName
         * @param inputLocation
         * @param exprList
         * @return
         */
        private FunctionInval handleThreeArgsInval( String funcName, String inputLocation, List<Expression> exprList ) {
                if ( Expression.ExprType.VARIABLE != exprList.get( 0 ).getExprType() ) {
                        throw ( new IllegalArgumentException( "First argument has to be a variable in this form: " + inputLocation ) );
                }
                if ( Expression.ExprType.CONST != exprList.get( 1 ).getExprType() && Expression.ExprType.CONSTLIST != exprList.get( 1 ).getExprType() ) {
                        throw ( new IllegalArgumentException( "Second argument has to be a number/numberlist in this form: " + inputLocation ) );
                }
                if ( Expression.ExprType.CONSTLIST == exprList.get( 2 ).getExprType() ) {
                        throw ( new IllegalArgumentException( "Third argument may not be a numberlist in this form: " + inputLocation ) );
                }
                return new FunctionInval( funcName, exprList );
        }

	private Expression createExpression(ExpressionContext ctx) {
		if (ctx.string() != null) {
			return new Expression(getText(ctx.string()));
		}
		if (ctx.zahl() != null) {
			return new Expression(getInteger(ctx.zahl()));
		}
		if (ctx.zahlenlisteExpr() != null) {
		        List<Integer> valueList = getValueList( ctx.zahlenlisteExpr().zahlenliste() );
		        if (1 == valueList.size()) {
		                return new Expression( valueList.get( 0 ) );
		        }
		        return new Expression(valueList);
		}

		if (ctx.operationWithParenthesis() != null) {
			Expression ex1 = createExpression(ctx.operationWithParenthesis().expression(0));
			Expression ex2 = createExpression(ctx.operationWithParenthesis().expression(1));
			String operator = ctx.operationWithParenthesis().operator().getText();
			return new Expression(ex1, ex2, operator);
		}
		Expression ex1 = createExpression(ctx.expression(0));
		Expression ex2 = createExpression(ctx.expression(1));
		String operator = ctx.operator().getText();
		return new Expression(ex1, ex2, operator);
	}

	private GetterRule createIfGetter(IfGetterContext ctx) {
		Condition condition = createCondition(ctx.condition());
		IfGetter ifGetter = new IfGetter(condition);
		for(GetterContext x : ctx.getter()) {
			GetterRule rule = createGetterRule(x);
			ifGetter.addRule(rule, true);
		}
		if (ctx.optElse() != null) {
			for(GetterContext x : ctx.optElse().getter()) {
				GetterRule rule = createGetterRule(x);
				ifGetter.addRule(rule, false);
			}
		}
		return ifGetter;
	}

	private GetterRule createOptionalGetter(OptionalGetterContext ctx) {
		OptionalGetter optGetter = new OptionalGetter();
		for(GetterContext x : ctx.getter()) {
			GetterRule rule = createGetterRule(x);
			optGetter.addRule(rule);
		}
		return optGetter;
	}

	private Condition createCondition(ConditionContext ctx) {
		Expression expression = createExpression(ctx.expression());
		return new Condition(expression);
	}

	private boolean isSigned(OptSignedContext ctx, String type) {
		boolean signed = true;
		if ("BYTE".equals( type ) || "NODE".equals( type )) {
			signed = false;
		}
		if (ctx == null || ctx.getText() == null) {
			return signed;
		}
		return ctx.getText().equals("signed");
	}

	private boolean isBigEndian(OptEndianContext ctx) {
		return ctx != null && ctx.getText() != null && ctx.getText().equals("bigendian");
	}

	private List<Integer> getValueList(ZahlenlisteContext ctx) {
		if (ctx == null) {
			throw new IllegalArgumentException("Context of zahlenliste ist Null");
		}
		List<Integer> valueList = new ArrayList<>();
		for(ZahlenlisteContext zl : ctx.zahlenliste()) {
			valueList.addAll(getValueList(zl));
		}
		ZahlContext z = ctx.zahl();
		if (z != null) {
			valueList.add(getInteger(z));
		}
		WertebereichContext w = ctx.wertebereich();
		if (w != null) {
			int von = getInteger(w.zahl(0));
			int bis = getInteger(w.zahl(1));
			if (von > bis) {
				throw new IllegalStateException("von > bis in wertebereich zeile " + w.getStart().getLine());
			}
			for(int n=von; n<=bis; ++n) {
				valueList.add(n);
			}
		}
		return valueList;
	}

	private int getInteger(ZahlContext ctx) {
		if (ctx == null) {
			throw new IllegalArgumentException("Context of zahl ist Null");
		}
		String text = ctx.getText();
		int radix = 10;
		if (text.startsWith("0x")) {
			text = text.substring(2);
			radix = 16;
		}
		return Integer.parseInt(text, radix);
	}

	private String getText(StringContext stringContext) {
		String t = stringContext.getText();
		if (t.startsWith("\"")) {
			t = t.substring(1, t.length()-1);
		}
		return t;
	}
}
