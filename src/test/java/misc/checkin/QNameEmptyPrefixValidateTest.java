/*   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package misc.checkin;

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlQName;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QNameEmptyPrefixValidateTest {

    private static final String XSD =
        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' xmlns:t='urn:t' " +
        "targetNamespace='urn:t' elementFormDefault='qualified'>" +
        "  <xs:element name='root' type='xs:QName'/>" +
        "</xs:schema>";

    private static SchemaTypeLoader loader;

    @BeforeAll
    static void compileSchema() throws Exception {
        loader = XmlBeans.loadXsd(new XmlObject[]{SchemaDocument.Factory.parse(XSD)});
    }

    private static boolean validates(String value) throws Exception {
        XmlObject doc = loader.parse("<t:root xmlns:t='urn:t'>" + value + "</t:root>", null, null);
        return doc.validate();
    }

    @Test
    void leadingColonIsReported() throws Exception {
        // a colon with an empty prefix is outside the xsd:QName lexical space;
        // these used to validate clean, silently parsed as the unprefixed name
        assertFalse(validates(":abc"));
        assertFalse(validates(" :abc "));
    }

    @Test
    void leadingColonIsRejectedOnSet() {
        assertThrows(XmlValueOutOfRangeException.class, () -> XmlQName.Factory.newValue(":abc"));
    }

    @Test
    void validQNamesStillValidate() throws Exception {
        assertTrue(validates("abc"));
        assertTrue(validates("t:abc"));
        assertTrue(validates(" t:abc "));
    }
}
