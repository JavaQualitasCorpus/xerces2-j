/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.parsers;

import java.io.IOException;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLDocumentScanner;
import org.apache.xerces.impl.XMLDTDScanner;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLInputSource;
import org.apache.xerces.impl.XMLValidator;
import org.apache.xerces.impl.validation.DatatypeValidatorFactory;
import org.apache.xerces.impl.validation.GrammarPool;
import org.apache.xerces.impl.validation.validators.XMLDTDValidator;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XMLString;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * This is the base class for all XML document parsers. XMLDocumentParser
 * provides a common implementation shared by the various document parsers
 * in the Xerces package. While this class is provided for convenience, it
 * does not prevent other kinds of parsers to be constructed using the XNI
 * interfaces.
 *
 * @author Stubs generated by DesignDoc on Mon Sep 11 11:10:57 PDT 2000
 * @author Arnaud  Le Hors, IBM
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public abstract class XMLDocumentParser
    extends XMLParser
    implements XMLDocumentHandler, XMLDTDHandler, XMLDTDContentModelHandler {

    //
    // Constants
    //

    // debugging

    /** Set to true and recompile to print exception stack trace. */
    private static final boolean PRINT_EXCEPTION_STACK_TRACE = false;

    //
    // Data
    //

    // components (non-configurable)

    /** Grammar pool. */
    protected GrammarPool fGrammarPool;

    /** Datatype validator factory. */
    protected DatatypeValidatorFactory fDatatypeValidatorFactory;

    // components (configurable)

    /** Document scanner. */
    protected XMLDocumentScanner fScanner;

    /** DTD scanner. */
    protected XMLDTDScanner fDTDScanner;

    /** Validator. */
    protected XMLValidator fValidator;

    // state

    /** 
     * True if a parse is in progress. This state is needed because
     * some features/properties cannot be set while parsing (e.g.
     * validation and namespaces).
     */
    protected boolean fParseInProgress = false;

    //
    // Constructors
    //

    /**
     * Default Constructor.
     * Creates an XMLDocumentParser with its own SymbolTable and GrammarPool. 
     */
    protected XMLDocumentParser() {
        this(new SymbolTable(), new GrammarPool());
    } // <init>()

    /**
     * Constructor allowing to specify the SymbolTable and GrammarPool to use
     * 
     * @param symbolTable 
     * @param grammarPool 
     */
    protected XMLDocumentParser(SymbolTable symbolTable,
                                GrammarPool grammarPool) {
        super(symbolTable);

        // set default features
        final String NAMESPACES = Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE;
        fFeatures.put(NAMESPACES, Boolean.TRUE);
        final String VALIDATION = Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
        fFeatures.put(VALIDATION, Boolean.FALSE);
        final String EXTERNAL_GENERAL_ENTITIES = Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE;
        fFeatures.put(EXTERNAL_GENERAL_ENTITIES, Boolean.TRUE);
        final String EXTERNAL_PARAMETER_ENTITIES = Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE;
        fFeatures.put(EXTERNAL_PARAMETER_ENTITIES, Boolean.TRUE);

        // create and register components
        final String SYMBOL_TABLE = Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
        fProperties.put(SYMBOL_TABLE, fSymbolTable);

        fGrammarPool = grammarPool;
        final String GRAMMAR_POOL = Constants.XERCES_PROPERTY_PREFIX + Constants.GRAMMAR_POOL_PROPERTY;
        fProperties.put(GRAMMAR_POOL, fGrammarPool);

        fScanner = new XMLDocumentScanner();
        final String DOCUMENT_SCANNER = Constants.XERCES_PROPERTY_PREFIX + Constants.DOCUMENT_SCANNER_PROPERTY;
        fProperties.put(DOCUMENT_SCANNER, fScanner);

        fDTDScanner = new XMLDTDScanner();
        final String DTD_SCANNER = Constants.XERCES_PROPERTY_PREFIX + Constants.DTD_SCANNER_PROPERTY;
        fProperties.put(DTD_SCANNER, fDTDScanner);

        fValidator = new XMLValidator();
        final String VALIDATOR = Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATOR_PROPERTY;
        fProperties.put(VALIDATOR, fValidator);
        
        /***
        fDatatypeValidatorFactory = new DatatypeValidatorFactory();
        final String DATATYE_VALIDATOR_FACTORY = Constants.XERCES_PROPERTY_PREFIX + Constants.DATATYE_VALIDATOR_FACTORY_PROPERTY;
        fProperties.put(DATATYE_VALIDATOR_FACTORY, fDatatypeValidatorFactory);
        /***/

    } // <init>(SymbolTable,GrammarPool)

    //
    // XMLParser methods
    //

    /** 
     * Reset all components before parsing. 
     *
     * @throws SAXException Thrown if an error occurs during initialization.
     */
    protected void reset() throws SAXException {
        super.reset();


        // setup document pipeline
        /***/
        fScanner.setDocumentHandler(fValidator);
        fValidator.setDocumentHandler(this);
        /***
        fScanner.setDocumentHandler(this);
        /***/

        // setup dtd pipeline
        /***/
        fDTDScanner.setDTDHandler(fValidator);
        fValidator.setDTDHandler(this);
        /***
        fDTDScanner.setDTDHandler(this);
        /***/

        // setup dtd content model pipeline
        /***/
        fDTDScanner.setDTDContentModelHandler(fValidator);
        fValidator.setDTDContentModelHandler(this);
        /***
        fDTDScanner.setDTDContentModelHandler(this);
        /***/

        // reset every component
        fScanner.reset(this);
        fDTDScanner.reset(this);
        fValidator.reset(this);

    } // reset()

    //
    // XMLComponentManager methods
    //

    /**
     * setFeature
     * 
     * @param featureId 
     * @param state 
     */
    public void setFeature(String featureId, boolean state)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        super.setFeature(featureId, state);

        // forward to every component
        fScanner.setFeature(featureId, state);
        fDTDScanner.setFeature(featureId, state);
        fValidator.setFeature(featureId, state);

    } // setFeature

    /**
     * setProperty
     * 
     * @param propertyId 
     * @param value 
     */
    public void setProperty(String propertyId, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        super.setProperty(propertyId, value);

        // forward to every component
        fScanner.setProperty(propertyId, value);
        fDTDScanner.setProperty(propertyId, value);
        fValidator.setProperty(propertyId, value);

    } // setProperty

    /**
     * Parses the specified input source.
     *
     * @param source The input source.
     *
     * @exception org.xml.sax.SAXException Throws exception on SAX error.
     * @exception java.io.IOException Throws exception on i/o error.
     */
    public void parse(InputSource source)
        throws SAXException, IOException {

        if (fParseInProgress) {
            // REVISIT - need to add new error message
            throw new SAXException(
                              "FWK005 parse may not be called while parsing.");
        }

        try {
            reset();
            fEntityManager.setEntityHandler(fScanner);
            fEntityManager.startDocumentEntity(new XMLInputSource(source));
            fScanner.scanDocument(true);
            fParseInProgress = false;
        } catch (SAXException ex) {
            fParseInProgress = false;
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } catch (IOException ex) {
            fParseInProgress = false;
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } catch (Exception ex) {
            fParseInProgress = false;
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw new org.xml.sax.SAXException(ex);
        }

    } // parse(InputSource)

    //
    // XMLParser methods
    //

    /**
     * Check a property. If the property is know and supported, this method
     * simply returns. Otherwise, the appropriate exception is thrown.
     *
     * @param propertyId The unique identifier (URI) of the property
     *                   being set.
     * @exception org.xml.sax.SAXNotRecognizedException If the
     *            requested property is not known.
     * @exception org.xml.sax.SAXNotSupportedException If the
     *            requested property is known, but the requested
     *            value is not supported.
     * @exception org.xml.sax.SAXException If there is any other
     *            problem fulfilling the request.
     */
    protected void checkProperty(String propertyId)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        //
        // SAX2 Properties
        //

        if (propertyId.startsWith(Constants.SAX_PROPERTY_PREFIX)) {
            String property =
                propertyId.substring(Constants.SAX_PROPERTY_PREFIX.length());
        }

        //
        // Xerces Properties
        //

        else if (propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            String property =
                propertyId.substring(Constants.XERCES_PROPERTY_PREFIX.length());
            if (property.equals(Constants.DTD_SCANNER_PROPERTY)) {
                return;
            }
            /***
            if (property.equals(Constants.ENTITY_RESOLVER_PROPERTY)) {
                return;
            }   
            /***/
        }

        super.checkProperty(propertyId);

    } // checkProperty(String)

    //
    // XMLDocumentHandler methods
    //

    /**
     * The start of the document.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void startDocument() throws SAXException {
    } // startDocument()

    /**
     * Notifies of the presence of an XMLDecl line in the document. If
     * present, this method will be called immediately following the
     * startDocument call.
     * 
     * @param version    The XML version.
     * @param encoding   The IANA encoding name of the document, or null if
     *                   not specified.
     * @param standalone The standalone value, or null if not specified.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void xmlDecl(String version, String encoding, String standalone)
        throws SAXException {
    } // xmlDecl(String,String,String)

    /**
     * Notifies of the presence of the DOCTYPE line in the document.
     * 
     * @param rootElement The name of the root element.
     * @param publicId    The public identifier if an external DTD or null
     *                    if the external DTD is specified using SYSTEM.
     * @param systemId    The system identifier if an external DTD, null
     *                    otherwise.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void doctypeDecl(String rootElement, String publicId, String systemId)
        throws SAXException {
    } // doctypeDecl(String,String,String)

    /**
     * The start of a namespace prefix mapping. This method will only be
     * called when namespace processing is enabled.
     * 
     * @param prefix The namespace prefix.
     * @param uri    The URI bound to the prefix.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
    } // startPrefixMapping(String,String)

    /**
     * The start of an element. If the document specifies the start element
     * by using an empty tag, then the startElement method will immediately
     * be followed by the endElement method, with no intervening methods.
     * 
     * @param element    The name of the element.
     * @param attributes The element attributes.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void startElement(QName element, XMLAttributes attributes)
        throws SAXException {
    } // startElement(QName,XMLAttributes)

    /**
     * Character content.
     * 
     * @param text The content.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void characters(XMLString text) throws SAXException {
    } // characters(XMLString)

    /**
     * Ignorable whitespace. For this method to be called, the document
     * source must have some way of determining that the text containing
     * only whitespace characters should be considered ignorable. For
     * example, the validator can determine if a length of whitespace
     * characters in the document are ignorable based on the element
     * content model.
     * 
     * @param text The ignorable whitespace.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void ignorableWhitespace(XMLString text) throws SAXException {
    } // ignorableWhitespace(XMLString)

    /**
     * The end of an element.
     * 
     * @param element The name of the element.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endElement(QName element) throws SAXException {
    } // endElement(QName)

    /**
     * The end of a namespace prefix mapping. This method will only be
     * called when namespace processing is enabled.
     * 
     * @param prefix The namespace prefix.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endPrefixMapping(String prefix) throws SAXException {
    } // endPrefixMapping(String)

    /** 
     * The start of a CDATA section. 
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void startCDATA() throws SAXException {
    } // startCDATA()

    /**
     * The end of a CDATA section. 
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endCDATA() throws SAXException {
    } // endCDATA()

    /**
     * The end of the document.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endDocument() throws SAXException {
    } // endDocument()

    //
    // XMLDocumentHandler and XMLDTDHandler methods
    //

    /**
     * This method notifies of the start of an entity. The document entity
     * has the pseudo-name of "[xml]"; The DTD has the pseudo-name of "[dtd]; 
     * parameter entity names start with '%'; and general entity names are
     * just the entity name.
     * <p>
     * <strong>Note:</strong> Since the document is an entity, the handler
     * will be notified of the start of the document entity by calling the
     * startEntity method with the entity name "[xml]" <em>before</em> calling
     * the startDocument method. When exposing entity boundaries through the
     * SAX API, the document entity is never reported, however.
     * <p>
     * <strong>Note:</strong> Since the DTD is an entity, the handler
     * will be notified of the start of the DTD entity by calling the
     * startEntity method with the entity name "[dtd]" <em>before</em> calling
     * the startDTD method.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     * 
     * @param name     The name of the entity.
     * @param publicId The public identifier of the entity if the entity
     *                 is external, null otherwise.
     * @param systemId The system identifier of the entity if the entity
     *                 is external, null otherwise.
     * @param encoding The auto-detected IANA encoding name of the entity
     *                 stream. This value will be null in those situations
     *                 where the entity encoding is not auto-detected (e.g.
     *                 internal parameter entities).
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void startEntity(String name, String publicId, String systemId,
                            String encoding) throws SAXException {
    } // startEntity(String,String,String,String)

    /**
     * Notifies of the presence of a TextDecl line in an entity. If present,
     * this method will be called immediately following the startEntity call.
     * <p>
     * <strong>Note:</strong> This method will never be called for the
     * document entity; it is only called for external general entities
     * referenced in document content.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     * 
     * @param version  The XML version, or null if not specified.
     * @param encoding The IANA encoding name of the entity.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void textDecl(String version, String encoding) throws SAXException {
    } // textDecl(String,String)

    /**
     * A comment.
     * 
     * @param text The text in the comment.
     *
     * @throws SAXException Thrown by application to signal an error.
     */
    public void comment(XMLString text) throws SAXException {
    } // comment(XMLString)

    /**
     * A processing instruction. Processing instructions consist of a
     * target name and, optionally, text data. The data is only meaningful
     * to the application.
     * <p>
     * Typically, a processing instruction's data will contain a series
     * of pseudo-attributes. These pseudo-attributes follow the form of
     * element attributes but are <strong>not</strong> parsed or presented
     * to the application as anything other than text. The application is
     * responsible for parsing the data.
     * 
     * @param target The target.
     * @param data   The data or null if none specified.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void processingInstruction(String target, XMLString data)
        throws SAXException {
    } // processingInstruction(String,XMLString)

    /**
     * This method notifies the end of an entity. The document entity has
     * the pseudo-name of "[xml]"; the DTD has the pseudo-name of "[dtd]; 
     * parameter entity names start with '%'; and general entity names are
     * just the entity name.
     * <p>
     * <strong>Note:</strong> Since the document is an entity, the handler
     * will be notified of the end of the document entity by calling the
     * endEntity method with the entity name "[xml]" <em>after</em> calling
     * the endDocument method. When exposing entity boundaries through the
     * SAX API, the document entity is never reported, however.
     * <p>
     * <strong>Note:</strong> Since the DTD is an entity, the handler
     * will be notified of the end of the DTD entity by calling the
     * endEntity method with the entity name "[dtd]" <em>after</em> calling
     * the endDTD method.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     * 
     * @param name The name of the entity.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endEntity(String name) throws SAXException {
    } // endEntity(String)

    //
    // XMLDTDHandler methods
    //

    /**
     * The start of the DTD.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void startDTD() throws SAXException {
    } // startDTD()

    /**
     * An element declaration.
     * 
     * @param name         The name of the element.
     * @param contentModel The element content model.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void elementDecl(String name, String contentModel)
        throws SAXException {
    } // elementDecl(String,String)

    /**
     * The start of an attribute list.
     * 
     * @param elementName The name of the element that this attribute
     *                    list is associated with.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void startAttlist(String elementName) throws SAXException {
    } // startAttlist(String)

    /**
     * An attribute declaration.
     * 
     * @param elementName   The name of the element that this attribute
     *                      is associated with.
     * @param attributeName The name of the attribute.
     * @param type          The attribute type. This value will be one of
     *                      the following: "CDATA", "ENTITY", "ENTITIES",
     *                      "ENUMERATION", "ID", "IDREF", "IDREFS", 
     *                      "NMTOKEN", "NMTOKENS", or "NOTATION".
     * @param enumeration   If the type has the value "ENUMERATION", this
     *                      array holds the allowed attribute values;
     *                      otherwise, this array is null.
     * @param defaultType   The attribute default type. This value will be
     *                      one of the following: "#FIXED", "#IMPLIED",
     *                      "#REQUIRED", or null.
     * @param defaultValue  The attribute default value, or null if no
     *                      default value is specified.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void attributeDecl(String elementName, String attributeName, 
                              String type, String[] enumeration, 
                              String defaultType, XMLString defaultValue)
        throws SAXException {
    } // attributeDecl(String,String,String,String[],String,XMLString)

    /**
     * The end of an attribute list.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endAttlist() throws SAXException {
    } // endAttlist()

    /**
     * An internal entity declaration.
     * 
     * @param name The name of the entity. Parameter entity names start with
     *             '%', whereas the name of a general entity is just the 
     *             entity name.
     * @param text The value of the entity.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void internalEntityDecl(String name, XMLString text) 
        throws SAXException {
    } // internalEntityDecl(String,XMLString)

    /**
     * An external entity declaration.
     * 
     * @param name     The name of the entity. Parameter entity names start
     *                 with '%', whereas the name of a general entity is just
     *                 the entity name.
     * @param publicId The public identifier of the entity or null if the
     *                 the entity was specified with SYSTEM.
     * @param systemId The system identifier of the entity.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void externalEntityDecl(String name, 
                                   String publicId, String systemId) 
        throws SAXException {
    } // externalEntityDecl(String,String,String)

    /**
     * An unparsed entity declaration.
     * 
     * @param name     The name of the entity.
     * @param publicId The public identifier of the entity, or null if not
     *                 specified.
     * @param systemId The system identifier of the entity, or null if not
     *                 specified.
     * @param notation The name of the notation.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void unparsedEntityDecl(String name, 
                                   String publicId, String systemId, 
                                   String notation) throws SAXException {
    } // unparsedEntityDecl(String,String,String,String)

    /**
     * A notation declaration
     * 
     * @param name     The name of the notation.
     * @param publicId The public identifier of the notation, or null if not
     *                 specified.
     * @param systemId The system identifier of the notation, or null if not
     *                 specified.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void notationDecl(String name, String publicId, String systemId)
        throws SAXException {
    } // notationDecl(String,String,String)

    /**
     * The start of a conditional section.
     * 
     * @param type The type of the conditional section. This value will
     *             either be CONDITIONAL_INCLUDE or CONDITIONAL_IGNORE.
     *
     * @throws SAXException Thrown by handler to signal an error.
     *
     * @see CONDITIONAL_INCLUDE
     * @see CONDITIONAL_IGNORE
     */
    public void startConditional(short type) throws SAXException {
    } // startConditional(short)

    /**
     * The end of a conditional section.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endConditional() throws SAXException {
    } // endConditional()

    /**
     * The end of the DTD.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endDTD() throws SAXException {
    } // endDTD()

    //
    // XMLDTDContentModelHandler methods
    //

    /**
     * The start of a content model. Depending on the type of the content
     * model, specific methods may be called between the call to the
     * startContentModel method and the call to the endContentModel method.
     * 
     * @param elementName The name of the element.
     * @param type        The content model type.
     *
     * @throws SAXException Thrown by handler to signal an error.
     *
     * @see TYPE_EMPTY
     * @see TYPE_ANY
     * @see TYPE_MIXED
     * @see TYPE_CHILDREN
     */
    public void startContentModel(String elementName, short type)
        throws SAXException {
    } // startContentModel(String,short)

    /**
     * A referenced element in a mixed content model. If the mixed content 
     * model only allows text content, then this method will not be called
     * for that model. However, if this method is called for a mixed
     * content model, then the zero or more occurrence count is implied.
     * <p>
     * <strong>Note:</strong> This method is only called after a call to 
     * the startContentModel method where the type is TYPE_MIXED.
     * 
     * @param elementName The name of the referenced element. 
     *
     * @throws SAXException Thrown by handler to signal an error.
     *
     * @see TYPE_MIXED
     */
    public void mixedElement(String elementName) throws SAXException {
    } // mixedElement(elementName)

    /**
     * The start of a children group.
     * <p>
     * <strong>Note:</strong> This method is only called after a call to
     * the startContentModel method where the type is TYPE_CHILDREN.
     * <p>
     * <strong>Note:</strong> Children groups can be nested and have
     * associated occurrence counts.
     *
     * @throws SAXException Thrown by handler to signal an error.
     *
     * @see TYPE_CHILDREN
     */
    public void childrenStartGroup() throws SAXException {
    } // childrenStartGroup()

    /**
     * A referenced element in a children content model.
     * 
     * @param elementName The name of the referenced element.
     *
     * @throws SAXException Thrown by handler to signal an error.
     *
     * @see TYPE_CHILDREN
     */
    public void childrenElement(String elementName) throws SAXException {
    } // childrenElement(String)

    /**
     * The separator between choices or sequences of a children content
     * model.
     * <p>
     * <strong>Note:</strong> This method is only called after a call to
     * the startContentModel method where the type is TYPE_CHILDREN.
     * 
     * @param separator The type of children separator.
     *
     * @throws SAXException Thrown by handler to signal an error.
     *
     * @see SEPARATOR_CHOICE
     * @see SEPARATOR_SEQUENCE
     * @see TYPE_CHILDREN
     */
    public void childrenSeparator(short separator) throws SAXException {
    } // childrenSeparator(short)

    /**
     * The occurrence count for a child in a children content model.
     * <p>
     * <strong>Note:</strong> This method is only called after a call to
     * the startContentModel method where the type is TYPE_CHILDREN.
     * 
     * @param occurrence The occurrence count for the last children element
     *                   or children group.
     *
     * @throws SAXException Thrown by handler to signal an error.
     *
     * @see OCCURS_ZERO_OR_ONE
     * @see OCCURS_ZERO_OR_MORE
     * @see OCCURS_ONE_OR_MORE
     * @see TYPE_CHILDREN
     */
    public void childrenOccurrence(short occurrence) throws SAXException {
    } // childrenOccurrence(short)

    /**
     * The end of a children group.
     * <p>
     * <strong>Note:</strong> This method is only called after a call to
     * the startContentModel method where the type is TYPE_CHILDREN.
     *
     * @see TYPE_CHILDREN
     */
    public void childrenEndGroup() throws SAXException {
    } // childrenEndGroup()

    /**
     * The end of a content model.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endContentModel() throws SAXException {
    } // endContentModel()

} // class XMLDocumentParser
