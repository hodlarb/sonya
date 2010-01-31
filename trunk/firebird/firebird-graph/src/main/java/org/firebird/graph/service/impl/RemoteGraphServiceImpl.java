/*
 * Copyright (c) 2009-2010, Young-Gue Bae
 * All rights reserved.
 */
package org.firebird.graph.service.impl;

import java.util.List;

import org.firebird.analyzer.graph.scoring.ScoringConfig;
import org.firebird.analyzer.service.AnalysisManager;
import org.firebird.analyzer.service.impl.AnalysisManagerImpl;
import org.firebird.collector.CollectorConfig;
import org.firebird.collector.service.CollectManager;
import org.firebird.collector.service.impl.CollectManagerImpl;
import org.firebird.graph.service.RemoteGraphService;
import org.firebird.io.model.Edge;
import org.firebird.io.model.Vertex;
import org.firebird.io.service.EdgeManager;
import org.firebird.io.service.VertexManager;
import org.firebird.io.service.impl.EdgeManagerImpl;
import org.firebird.io.service.impl.VertexManagerImpl;

/**
 * Remote service implementation for graph.
 * 
 * @author Young-Gue Bae
 */
public class RemoteGraphServiceImpl implements RemoteGraphService {

	private VertexManager vertexManager;
	private EdgeManager edgeManager;
	private CollectManager collectManager;
	private AnalysisManager analysisManager;
	
	/**
     * Creates a remote query manager.
     * 
     */
    public RemoteGraphServiceImpl() {
    	this.vertexManager = new VertexManagerImpl();
    	this.edgeManager = new EdgeManagerImpl();
    	this.collectManager = new CollectManagerImpl();
    	this.analysisManager = new AnalysisManagerImpl();
    }
	
	/**
	 * Gets the vertices.
	 * 
	 * @param websiteId the website id
	 * @return List<Vertex> the vertex list
	 */
	public List<Vertex> getVertices(int websiteId) throws Exception {
		return vertexManager.getVertices(websiteId);
	}

	/**
	 * Gets the edges.
	 * 
	 * @param websiteId1 the website id1
	 * @param websiteId2 the website id2
	 * @return List<Edge> the edge list
	 */
	public List<Edge> getEdges(int websiteId1, int websiteId2) throws Exception {
		return edgeManager.getEdges(websiteId1, websiteId2);		
	}

	/**
	 * Collects the twitter data.
	 * 
	 * @param config the collect config
	 * @param screenName the user's screen name
	 */
	public void collectTwitter(CollectorConfig config, String screenName) throws Exception {
		collectManager.collectTwitter(config, screenName);
	}
	
	/**
	 * Evaluates the scores of the graph.
	 * such as HITS, Betweenness Centrality, Closeness Centrality, Degree etc.
	 * 
	 * @param config the scoring config
	 * @param websiteId1 the website id1
	 * @param websiteId2 the website id2
	 */
	public void scoringGraph(ScoringConfig config, int websiteId1, int websiteId2) throws Exception {
		analysisManager.scoringGraph(config, websiteId1, websiteId2);
	}
}
