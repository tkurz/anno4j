package com.github.anno4j.querying.tests;

import com.github.anno4j.model.Annotation;
import com.github.anno4j.model.Body;
import com.github.anno4j.querying.QuerySetup;
import org.apache.marmotta.ldpath.parser.ParseException;
import org.junit.Test;
import org.openrdf.annotations.Iri;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ReversePathTest extends QuerySetup {

    @Test
    public void testFirstBody() throws RepositoryException, QueryEvaluationException, MalformedQueryException, ParseException {
        List<Annotation> annotations = queryService
                .addCriteria("oa:hasBody[is-a ex:inverseBody]/^oa:hasBody/oa:serializedAt", "07.05.2015")
                .execute();

        assertEquals(1, annotations.size());

        Annotation annotation = annotations.get(0);
        assertEquals("07.05.2015", annotation.getSerializedAt());

        // Testing if the body was persisted correctly
        InverseBody testBody = (InverseBody) annotation.getBody();
        assertEquals("Some Testing Value", testBody.getValue());
    }

    @Test
    public void testSecondBody() throws RepositoryException, QueryEvaluationException, MalformedQueryException, ParseException {
        List<Annotation> annotations = queryService
                .addCriteria("oa:hasBody[is-a ex:inverseBody]/^oa:hasBody/oa:annotatedAt", "01.01.2011")
                .execute();

        assertEquals(1, annotations.size());

        Annotation annotation = annotations.get(0);
        assertEquals("01.01.2011", annotation.getAnnotatedAt());

        // Testing if the body was persisted correctly
        InverseBody testBody = (InverseBody) annotation.getBody();
        assertEquals("Another Testing Value", testBody.getValue());
    }

    @Test
    public void falseTest() throws RepositoryException, QueryEvaluationException, MalformedQueryException, ParseException {
        List<Annotation> annotations = queryService
                .addCriteria("oa:hasBody[is-a ex:inverseBody]/^oa:hasBody/oa:serzializedAt", "01.01.2011")
                .execute();

        assertEquals(0, annotations.size());
    }

    @Override
    public void persistTestData() throws RepositoryException, InstantiationException, IllegalAccessException {
        // Persisting some data
        Annotation annotation = anno4j.createObject(Annotation.class);
        annotation.setSerializedAt("07.05.2015");
        InverseBody inverseBody = anno4j.createObject(InverseBody.class);
        inverseBody.setValue("Some Testing Value");
        annotation.setBody(inverseBody);
        anno4j.persist(annotation);

        Annotation annotation1 = anno4j.createObject(Annotation.class);
        annotation1.setAnnotatedAt("01.01.2011");
        InverseBody inverseBody2 = anno4j.createObject(InverseBody.class);
        inverseBody2.setValue("Another Testing Value");
        annotation1.setBody(inverseBody2);
        anno4j.persist(annotation1);
    }


    @Iri("http://www.example.com/schema#inverseBody")
    public static interface InverseBody extends Body {
        @Iri("http://www.example.com/schema#value")
        String getValue();

        @Iri("http://www.example.com/schema#value")
        void setValue(String value);
    }
}
