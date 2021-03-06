package com.github.anno4j.model.impl;

import com.github.anno4j.Anno4j;
import com.github.anno4j.model.Annotation;
import com.github.anno4j.model.Target;
import com.github.anno4j.model.impl.targets.SpecificResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by schlegel on 06/05/15.
 */
public class AnnotationTest {

    private Anno4j anno4j;
    private ObjectConnection connection;

    @Before
    public void setUp() throws Exception {
        this.anno4j = new Anno4j();
        this.connection = this.anno4j.getObjectRepository().getConnection();
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testPersistAnnotation() throws Exception {
        // Create test annotation
        Annotation annotation = anno4j.createObject(Annotation.class);
        annotation.setAnnotatedAt("" + System.currentTimeMillis());
        annotation.setSerializedAt("" + System.currentTimeMillis());

        // persist annotation
        connection.addObject(annotation);

        // query persisted object
        Annotation result = connection.getObject(Annotation.class, annotation.getResource());

        assertEquals(annotation.getResource().toString(), result.getResource().toString());
        assertEquals(annotation.getAnnotatedAt(), result.getAnnotatedAt());
        assertEquals(annotation.getSerializedAt(), result.getSerializedAt());
    }

    @Test
    public void testResourceDefinition() throws Exception {
        // Create annotation
        Annotation annotation = anno4j.createObject(Annotation.class);
        Resource resource = new URIImpl("http://www.somepage.org/resource1/");
        annotation.setResource(resource);

        // Persist annotation
        connection.addObject(annotation);

        // Query persisted object
        Annotation result = (Annotation) connection.getObject(annotation.getResource());

        // Tests
        assertEquals(annotation.getResource(), result.getResource());
    }

    @Test
    public void testSingleTarget() throws RepositoryException, InstantiationException, IllegalAccessException {
        // Create annotation
        Annotation annotation = anno4j.createObject(Annotation.class);

        // Create specific resource
        SpecificResource specificResource = anno4j.createObject(SpecificResource.class);
        ResourceObject resourceObject = anno4j.createObject(ResourceObject.class);
        resourceObject.setResourceAsString("http://www.somepage.org/resource1/");
        specificResource.setSource(resourceObject);
        annotation.addTarget(specificResource);

        // Persist annotation
        connection.addObject(annotation);

        // Query annotation
        Annotation result = (Annotation) connection.getObject(annotation.getResource());

        // Tests
        assertEquals(1, result.getTarget().size());
        assertEquals(("http://www.somepage.org/resource1/"), ((SpecificResource) result.getTarget().toArray()[0]).getSource().getResource().toString());
    }

    @Test
    public void testMultipleTargets() throws RepositoryException, InstantiationException, IllegalAccessException {
        // Create annotation
        Annotation annotation = anno4j.createObject(Annotation.class);

        // Create specific resource1
        SpecificResource specificResource = anno4j.createObject(SpecificResource.class);
        ResourceObject resourceObject = anno4j.createObject(ResourceObject.class);
        resourceObject.setResourceAsString("http://www.somepage.org/resource1/");
        specificResource.setSource(resourceObject);
        annotation.addTarget(specificResource);

        // Create specific resource2
        SpecificResource specificResource2 = anno4j.createObject(SpecificResource.class);
        ResourceObject resourceObject2 = anno4j.createObject(ResourceObject.class);
        resourceObject2.setResourceAsString("http://www.somepage.org/resource2/");
        specificResource2.setSource(resourceObject2);
        annotation.addTarget(specificResource2);

        // Persist annotation
        connection.addObject(annotation);

        // Query annotation
        Annotation result = (Annotation) connection.getObject(annotation.getResource());

        // Tests
        List<String> urls = new ArrayList<>();
        for(Target target : result.getTarget()) {
            urls.add(((SpecificResource) target).getSource().getResource().toString());
        }

        assertTrue(urls.contains("http://www.somepage.org/resource1/"));
        assertTrue(urls.contains("http://www.somepage.org/resource2/"));
        assertEquals(2, result.getTarget().size());
    }

    @Test
    public void testSerializedAtAndAnnotatedAt() throws RepositoryException, IllegalAccessException, InstantiationException {
        int year = 2015;
        int month = 12;
        int day = 16;
        int hours = 12;
        int minutes = 0;
        int seconds = 0;

        int hours2 = 0;
        int minutes2 = 5;
        int seconds2 = 16;

        Annotation annotation = anno4j.createObject(Annotation.class);
        annotation.setSerializedAt(year, month, day, hours, minutes, seconds);
        annotation.setAnnotatedAt(year, month, day, hours2, minutes2, seconds2);

        // Persist annotation
        connection.addObject(annotation);

        // Query annotation
        Annotation result = (Annotation) connection.getObject(annotation.getResource());

        assertEquals("2015-12-16T12:00:00Z", result.getSerializedAt());
        assertEquals("2015-12-16T00:05:16Z", result.getAnnotatedAt());
    }
}