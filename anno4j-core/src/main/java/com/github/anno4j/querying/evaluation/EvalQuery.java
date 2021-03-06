package com.github.anno4j.querying.evaluation;

import com.github.anno4j.model.namespaces.OADM;
import com.github.anno4j.querying.Criteria;
import com.github.anno4j.querying.QueryServiceConfiguration;
import com.github.anno4j.querying.evaluation.ldpath.LDPathEvaluator;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.vocabulary.RDF;
import org.apache.marmotta.ldpath.backend.sesame.SesameValueBackend;
import org.apache.marmotta.ldpath.parser.LdPathParser;
import org.apache.marmotta.ldpath.parser.ParseException;

import java.io.StringReader;

public class EvalQuery {

    public static Query evaluate(QueryServiceConfiguration queryServiceDTO) throws ParseException {

        Query query = QueryFactory.make();
        query.setQuerySelectType();

        ElementGroup elementGroup = new ElementGroup();

        Var annotationVar = Var.alloc("annotation");

        // Creating and adding the first triple - "?annotation rdf:type oa:Annotation
        Triple t1 = new Triple(annotationVar, RDF.type.asNode(), NodeFactory.createURI(OADM.ANNOTATION));
        elementGroup.addTriplePattern(t1);

        // Evaluating the criteria
        for (Criteria c : queryServiceDTO.getCriteria()) {
            SesameValueBackend backend = new SesameValueBackend();

            LdPathParser parser = new LdPathParser(backend, queryServiceDTO.getConfiguration(), new StringReader(c.getLdpath()));
            Var var = LDPathEvaluator.evaluate(parser.parseSelector(queryServiceDTO.getPrefixes()), elementGroup, annotationVar, queryServiceDTO.getEvaluatorConfiguration());

            if (c.getConstraint() != null) {
                EvalComparison.evaluate(elementGroup, c, var);
            }
        }

        // Adding all generated patterns to the query object
        query.setQueryPattern(elementGroup);

        // Choose what we want so select - SELECT ?annotation in this case
        query.addResultVar(annotationVar);

        // Setting the default prefixes, like rdf: or dc:
        query.getPrefixMapping().setNsPrefixes(queryServiceDTO.getPrefixes());

        return query;
    }
}