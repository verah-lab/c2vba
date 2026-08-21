grammar Sequencer;
import TlsGrammarBase;

@header {
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

import de.heuboe.tls.grammar.sequencer.flops.Flop;
import de.heuboe.tls.grammar.sequencer.flops.FlopStatement;
import de.heuboe.tls.grammar.sequencer.flops.FlopParameter;
import de.heuboe.tls.grammar.sequencer.Message;
import de.heuboe.tls.sequencer.utils.SequencerBeanContainer;
import de.heuboe.tls.sequencer.model.SequencerGlobals;
import de.heuboe.tls.sequencer.model.SequencerDataType;
import de.heuboe.tls.grammar.interfaces.*;
import de.heuboe.tls.grammar.sequencer.*;
import de.heuboe.tls.grammar.sequencer.functions.*;
}

@members {
    @Getter
    // placeholder for the final result
    private TransformationRules transformationRules = new TransformationRules(); // the final result

    @Getter
    // placeholder for used topics
    private Set<SequencerDataType> dataTypes = new HashSet();

    @Setter
    private SequencerBeanContainer sequencerBeanContainer;

    @Getter
    private SequencerGlobals sequencerGlobals = new SequencerGlobals();

}

sequencer
    :   globals* fgBlock*
    ;

globals
    :   'Global' lhs { sequencerGlobals.addGlobal($lhs.varName, new BasicVariable($lhs.varName, null)); }
    ;

fgBlock
    :   ( logicBlock { transformationRules.addDefinition( $logicBlock.logicBlockDefinition, _ctx ); } )+
    ;

logicBlock returns [LogicBlockDefinition logicBlockDefinition]
    :   { $logicBlockDefinition = new LogicBlockDefinition(); }
        header { $logicBlockDefinition.setName($header.ro); }
        ( receivedObjectProperties { $logicBlockDefinition.setOptions($receivedObjectProperties.props ); } )?
        '{' statements { $logicBlockDefinition.setFillerRules( $statements.lf ); } '}'
    ;

header returns [String ro]
    :   { ObjectDirection od = ObjectDirection.IN;
          String targetTopic = ""; }
        receiverStatement
        (
         ( (objectDirection { od = $objectDirection.od; })? receivedObject )
         |
         ( receivedObject VIA (objectDirection { od = $objectDirection.od; })? targetTopicStatement { targetTopic = $targetTopicStatement.tt; })
        )
        { if (targetTopic.equals("")) { $ro = $receivedObject.ro; } else { $ro = targetTopic; } dataTypes.add(new SequencerDataType($receivedObject.ro, false, od, targetTopic)); }
    ;

// override
statement returns [Filler b]
    :   assign { $b = $assign.assignStatement; }
    |   compound { $b = $compound.ifStatement; }
    |   switchCase { $b = $switchCase.switchCaseStatement; }
    |   flopBlock { $b = $flopBlock.flopStatement; }
    |   copyBlock { $b = $copyBlock.copyStatement; }
    |   message { $b = $message.msg; }
    ;

// override
assign returns [Filler assignStatement]
    :   { ObjectDirection od = ObjectDirection.IN;
          String targetTopic = ""; }
        lhs ':=' rhs { $assignStatement = new ObjectAssignStatement(null, od, targetTopic, $lhs.varName, $rhs.expr, sequencerBeanContainer ); }
    |   variable=VARIABLE ':=' rhs { $assignStatement = new SequencerAssignStatement($variable.getText().replaceFirst("\\$", ""), $rhs.expr ); }
    |   { ObjectDirection od = ObjectDirection.IN;
          String targetTopic = ""; }
          (
            (
             (objectDirection { od = $objectDirection.od; } )? object=IDENTIFIER '.' lhs ':=' rhs
            )
            |
            (
             object=IDENTIFIER '.' lhs (VIA (objectDirection { od = $objectDirection.od; } )? targetTopicStatement { targetTopic = $targetTopicStatement.tt; })? ':=' rhs
            )
          ) { $assignStatement = new ObjectAssignStatement($object.getText(), od, targetTopic, $lhs.varName, $rhs.expr, sequencerBeanContainer ); }
    |   { ObjectDirection od = ObjectDirection.IN;
          String targetTopic = ""; }
        ea 'in'
        (
          (
           (objectDirection { od = $objectDirection.od; } )? object=IDENTIFIER '.' lhs ':=' rhs
          )
          |
          (
           object=IDENTIFIER '.' lhs (VIA (objectDirection { od = $objectDirection.od; } )? targetTopicStatement { targetTopic = $targetTopicStatement.tt; })? ':=' rhs
          )
        ) { $assignStatement = new ObjectAssignStatement($ea.targetType, $ea.targetId, $ea.fgGroup, $object.getText(), od, targetTopic, $lhs.varName, $rhs.expr, sequencerBeanContainer ); }
    ;

ea returns [String targetType, Variable targetId, int fgGroup]
    :   eaTypes { $targetType = $eaTypes.eaType; } ('(' exprValue ')' { $targetId = $exprValue.expr; })? ('fg(' INT ')' { try {$fgGroup = Integer.parseInt($INT.getText());} catch (NullPointerException e) { $fgGroup = 0; } } )?
    |   'De' { $targetType = _localctx.getText(); }
    ;

eaTypes returns [String eaType]
    :
    |   'Ea' { $eaType = _localctx.getText(); }
    |   'DEs des Cluster' { $eaType = _localctx.getText(); }
    |   'DEs des Knoten' { $eaType = _localctx.getText(); }
    |   'DEs der KRI' { $eaType = _localctx.getText(); }
    |   'Cluster des DEs' { $eaType = _localctx.getText(); }
    |   'Knoten des DEs' { $eaType = _localctx.getText(); }
    |   'Knoten der KRI' { $eaType = _localctx.getText(); }
    ;

// override
lhs returns [String varName]
    :   variable=IDENTIFIER { $varName = $variable.getText(); }
    ;

// override
expression returns [Expression expr]
    :   '(' e=expression ')' { $expr = $e.expr; }
    |   e1=expression op=operator e2=expression { $op.oob.setLhs( $e1.expr ); $op.oob.setRhs( $e2.expr ); $expr = $op.oob; }
    |   exprValue { $expr = $exprValue.expr; }
    |   function { $expr = $function.tmp; }
    ;

// override
exprValue returns [Variable expr]
    :   number { $expr = new ConstantValue(  new ValueCollection.IntValue( $number.intval ) ); }
    |   SPECIFICATION { $expr = new AccessVariable( $SPECIFICATION.text ); }
    |   IDENTIFIER { $expr = new BrokerVariable( $IDENTIFIER.text, null ); }
    |   arrayDefinition { $expr = $arrayDefinition.expr; }
    |   VARIABLE { $expr = new AccessVariable( $VARIABLE.text.replaceFirst("\\$", "") ); }
    |   string { $expr = new BasicVariable(null, new ValueCollection.StringValue($string.value)); }
    |   obj=IDENTIFIER '*' state=IDENTIFIER '.' var=IDENTIFIER
        {
            if (sequencerBeanContainer == null) {
                $expr = new ObjectVariable($obj.getText(), $state.getText() == null ? null : $state.getText(), $var.getText(), null);
            } else {
                $expr = new ObjectVariable($obj.getText(), $state.getText() == null ? null : $state.getText(), $var.getText(), sequencerBeanContainer.getSequencerSendingService().getKafkaOperatorService());
            }
            dataTypes.add(new SequencerDataType($obj.getText(), ObjectStateType.findByKeyWord($state.getText()) == ObjectStateType.OLD ? true : false, ObjectDirection.IN, ""));
        }
    ;

function returns [Function tmp]
    :   functionName '(' functionParameterList ')'
        {
            $tmp = $functionName.rawFunction;
            $tmp.addParameters($functionParameterList.paramList);
        }
    |   'getAtIndex' { $tmp = new GetAtIndex("2", sequencerBeanContainer); }
         '(' getAtIndexParameterList { $tmp.addParameters($getAtIndexParameterList.paramList); } ')'
    ;

functionName returns [Function rawFunction]
    : 'isKri' { $rawFunction = new IsKri("1", sequencerBeanContainer); }
    | 'dateTime' { $rawFunction = new DateTime("0|1", sequencerBeanContainer); }
    ;

functionParameterList returns [List<Variable> paramList]
    :   { $paramList = new ArrayList(); }
        (functionParameter { $paramList.add($functionParameter.param); })? (',' functionParameter { $paramList.add($functionParameter.param); })*
    ;

getAtIndexParameterList returns [List<Variable> paramList]
    :   { $paramList = new ArrayList(); }
        exprValue { $paramList.add( $exprValue.expr ); }
        ','
        exprValue { $paramList.add( $exprValue.expr ); }
    ;

functionParameter returns [Variable param]
    :   exprValue { $param = $exprValue.expr; }
    ;

arrayDefinition returns [Variable expr]
    :   SPECIFICATION arrayIndex { $expr = new ArrayAccessVariable( $SPECIFICATION.text, $arrayIndex.index ); }
    |   IDENTIFIER arrayIndex { $expr = new ArrayAccessVariable( $IDENTIFIER.text, $arrayIndex.index ); }
    ;

arrayIndex returns [String index]
    : '['
         (
             INT { $index = $INT.text; }
           | IDENTIFIER { $index = $IDENTIFIER.text; }
           | VARIABLE { $index = $VARIABLE.text.replaceFirst("\\$", ""); }
         )
       ']'
    ;

/* switch case rules */
switchCase returns [Filler switchCaseStatement ]
    :   switchStmt '(' switchExpr=exprValue ')' '{'
        {
            SwitchCaseStatement sc = new SwitchCaseStatement();
            $switchCaseStatement = sc;
        }
        (
            { Boolean breakStmt = false; }
            caseStmt caseExpr=exprValue ':'
            statements
            (breakStmt { breakStmt = true; })?
            {
                sc.addCaseBlock(new CaseStatement($switchExpr.expr, $caseExpr.expr, $statements.lf, breakStmt));
            }
        )+
        '}'
    ;

/* flop rules */
flopBlock returns [Filler flopStatement]
    :   { Boolean triggerable = false; }
        flopType
        ('retriggerbarer' { triggerable = true; })?
        'Monoflop'
        '(' p1=INT (',' p2=INT)? ')' '{' statements '}'
        {
            FlopParameter parameter;

            if ($p2 != null) {
                parameter = new FlopParameter($p1.getText(), $p2.getText());
            } else {
                parameter = new FlopParameter($p1.getText());
            }

            Flop flop = new Flop($flopType.type, triggerable, parameter, $statements.lf);
            // first script parsing is just necessary for topic recognition therfor the bean container will be null
            if (sequencerBeanContainer == null) {
                $flopStatement = new FlopStatement(flop, null);
            } else {
                $flopStatement = new FlopStatement(flop, sequencerBeanContainer);
            }
        }
    ;

flopType returns [String type]
    :   'eaweiser' { $type = _localctx.getText(); }
    |   'einmaliger' { $type = _localctx.getText(); }
    |   'knotenweiser' { $type = _localctx.getText(); }
    |   'clusterweiser' { $type = _localctx.getText(); }
    ;

/* message rules */
message returns [Message msg]
    :   { List<Variable> paramList = new ArrayList(); }
        messageType '(' string (',' messageParameter { paramList.add($messageParameter.parameter); })* ')'
        {
            $msg = new Message(
                    $messageType.type,
                    $string.value,
                    paramList, sequencerBeanContainer);
        }
    ;

messageType returns [String type]
    :   'ErrorMessage' { $type = _localctx.getText(); }
    |   'SystemMessage' { $type = _localctx.getText(); }
    ;

messageParameter returns [Variable parameter]
    :   VARIABLE { $parameter = new AccessVariable( $VARIABLE.getText().replaceFirst("\\$", "")); }
    |   arrayDefinition { $parameter = $arrayDefinition.expr; }
    ;

copyBlock returns [Filler copyStatement]
    :   { ObjectDirection od = ObjectDirection.IN;
          String targetTopic = ""; }
        'copy to' IDENTIFIER (VIA (objectDirection { od = $objectDirection.od; } )? targetTopicStatement { targetTopic = $targetTopicStatement.tt; })? { $copyStatement = new CopyStatement($IDENTIFIER.text, od, targetTopic); }
    ;

receiverStatement
    : 'Bei Eingang von'
    ;

targetTopicStatement returns [ String tt ]
    : IDENTIFIER { $tt = _localctx.getText(); }
    ;

objectDirection returns [ ObjectDirection od ]
    : direction '.' { $od = $direction.dir; }
    ;

direction returns [ ObjectDirection dir ]
    : 'in' { $dir = ObjectDirection.findByKeyWord(_localctx.getText()); }
    | 'out' { $dir = ObjectDirection.findByKeyWord(_localctx.getText()); }
    ;

receivedObject returns [ String ro ]
    :   IDENTIFIER { $ro = _localctx.getText(); }
    ;

receivedObjectProperties returns [ EnumMap<ObjectProperty, String> props ]
    :   { $props = new EnumMap<>(ObjectProperty.class); }
        '(' (objectOptions { $props.putAll($objectOptions.singleProp); })+ ')'
    ;

objectOptions returns [ EnumMap<ObjectProperty, String> singleProp ]
    :   optionProperty '=' optionValue ','*
        {
            $singleProp = new EnumMap<>(ObjectProperty.class);
            $singleProp.put($optionProperty.prop, ObjectProperty.checkDataType($optionProperty.prop, $optionValue.val));
        }
    ;

optionProperty returns [ ObjectProperty prop ]
    :   IDENTIFIER { $prop = ObjectProperty.findByKeyWord($IDENTIFIER.text); }
    ;

optionValue returns [ String val ]
    :   IDENTIFIER { $val = $IDENTIFIER.text; }
    |   string { $val = $string.value; }
    |   TRUE { $val = _localctx.getText(); }
    |   FALSE { $val = _localctx.getText(); }
    ;

switchStmt
    :   'switch'
    |   'Switch'
    |   'SWITCH'
    ;

caseStmt
    :   'case'
    |   'Case'
    |   'CASE'
    ;

breakStmt
    :   'break'
    |   'Break'
    |   'BREAK'
    ;

SPECIFICATION
    :   ('A'..'Z' | '_' )+
    ;

VIA
    : 'via'
    ;

