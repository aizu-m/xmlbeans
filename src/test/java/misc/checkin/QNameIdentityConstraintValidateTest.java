/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package misc.checkin;

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QNameIdentityConstraintValidateTest {

    private static String schema(String constraint) {
        return
            "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' " +
            "xmlns:t='urn:t' targetNamespace='urn:t' elementFormDefault='qualified'>" +
            "  <xs:complexType name='named'><xs:attribute name='n' type='xs:QName' use='required'/></xs:complexType>" +
            "  <xs:element name='root'>" +
            "    <xs:complexType><xs:sequence>" +
            "      <xs:element name='k' type='t:named' minOccurs='0' maxOccurs='unbounded'/>" +
            "      <xs:element name='ref' type='t:named' minOccurs='0' maxOccurs='unbounded'/>" +
            "    </xs:sequence></xs:complexType>" +
            constraint +
            "    <xs:keyref name='kr' refer='t:kk'><xs:selector xpath='t:ref'/><xs:field xpath='@n'/></xs:keyref>" +
            "  </xs:element>" +
            "</xs:schema>";
    }

    private static final String KEY =
        "<xs:key name='kk'><xs:selector xpath='t:k'/><xs:field xpath='@n'/></xs:key>";

    private static final String UNIQUE =
        "<xs:unique name='kk'><xs:selector xpath='t:k'/><xs:field xpath='@n'/></xs:unique>";

    private static List<XmlError> validate(String xsd, String body) throws Exception {
        SchemaTypeLoader loader = XmlBeans.loadXsd(new XmlObject[]{SchemaDocument.Factory.parse(xsd)});
        XmlObject doc = loader.parse("<t:root xmlns:t='urn:t' xmlns:p='urn:p'>" + body + "</t:root>", null, null);
        List<XmlError> errors = new ArrayList<>();
        boolean valid = doc.validate(new XmlOptions().setErrorListener(errors));
        assertEquals(errors.isEmpty(), valid, errors.toString());
        return errors;
    }

    @Test
    void prefixedQNameKeyIsFilled() throws Exception {
        List<XmlError> errors = validate(schema(KEY), "<t:k n='p:a'/><t:ref n='p:a'/>");
        assertTrue(errors.isEmpty(), errors.toString());
    }

    @Test
    void prefixedQNameDuplicateUniqueIsReported() throws Exception {
        List<XmlError> errors = validate(schema(UNIQUE), "<t:k n='p:a'/><t:k n='p:a'/>");
        assertEquals(1, errors.size(), errors.toString());
        assertEquals("cvc-identity-constraint.4.1", errors.get(0).getErrorCode());
    }

    @Test
    void prefixedQNameDanglingKeyrefIsReported() throws Exception {
        List<XmlError> errors = validate(schema(UNIQUE), "<t:k n='p:a'/><t:ref n='p:zzz'/>");
        assertFalse(errors.isEmpty());
        assertEquals("cvc-identity-constraint.4.3", errors.get(0).getErrorCode());
    }
}
